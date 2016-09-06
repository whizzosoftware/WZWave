/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel.inbound;

import com.whizzosoftware.wzwave.channel.TransactionTimeoutHandler;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.frame.transaction.NodeInclusionTransaction;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Handler for all Z-Wave frame transactions. Responsible for tracking the state of the current transaction
 * including successes, failures and timeouts.
 *
 * @author Dan Noguerol
 */
public class TransactionInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TransactionInboundHandler.class);

    private static final int MAX_SEND_COUNT = 2;

    private ChannelHandlerContext handlerContext;
    private DataFrameTransaction currentDataFrameTransaction;
    private boolean processingTransactionCompletion = false;
    private ScheduledFuture timeoutFuture;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.handlerContext = ctx;
    }

    /**
     * Called when data is read from the Z-Wave network.
     *
     * @param ctx the handler context
     * @param msg the message that was read
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Frame) {
            Frame frame = (Frame) msg;
            if (hasCurrentTransaction()) {
                String tid = currentDataFrameTransaction.getId();
                logger.trace("Received frame within transaction ({}) context: {}", tid, frame);

                if (currentDataFrameTransaction.addFrame(frame)) {
                    if (currentDataFrameTransaction.isComplete()) {

                        // cancel the timeout callback
                        if (timeoutFuture != null) {
                            timeoutFuture.cancel(true);
                            timeoutFuture = null;
                        }

                        // flag that we're in the process of completing a frame transaction so that any code that checks
                        // will know that the transaction isn't quite done yet
                        processingTransactionCompletion = true;

                        if (!currentDataFrameTransaction.hasError()) {
                            DataFrame finalFrame = currentDataFrameTransaction.getFinalFrame();
                            logger.trace("*** Data frame transaction ({}) completed with final frame: {}", tid, finalFrame);
                            logger.trace("");

                            // if there's an ApplicationUpdate with no node ID (e.g. when there's a app update failure), attempt
                            // to set the node ID based on the request frame that triggered it
                            if (finalFrame instanceof ApplicationUpdate) {
                                ApplicationUpdate update = (ApplicationUpdate)finalFrame;
                                if ((update.getNodeId() == null || update.getNodeId() == 0) && currentDataFrameTransaction.getStartFrame() instanceof RequestNodeInfo) {
                                    update.setNodeId(((RequestNodeInfo)currentDataFrameTransaction.getStartFrame()).getNodeId());
                                }
                            }

                            // pass the final frame down the pipeline
                            if (finalFrame != null) {
                                ctx.fireChannelRead(finalFrame);
                            }

                            // clear the current transaction
                            clearTransaction();

                            // now the frame transaction is truly done
                            processingTransactionCompletion = false;

                            // alert the pipeline that a frame transaction has been completed
                            ctx.fireUserEventTriggered(new TransactionCompletedEvent(tid, false));
                        } else {
                            logger.trace("Detected transaction error for {}", tid);
                            processingTransactionCompletion = false;
                            attemptResend(ctx, currentDataFrameTransaction.hasCAN());
                        }
                    }
                } else {
                    logger.trace("Transaction ignored frame so passing it along");
                    ctx.fireChannelRead(msg);
                }
            } else if (msg instanceof AddNodeToNetwork) {
                logger.trace("Received ADD_NODE_STATUS_NODE_FOUND; starting transaction");
                currentDataFrameTransaction = new NodeInclusionTransaction((DataFrame)msg);
            } else {
                logger.trace("Received frame outside of transaction context: {}", frame);
                ctx.fireChannelRead(msg);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof TransactionCompletedEvent) {
            TransactionCompletedEvent tce = (TransactionCompletedEvent)evt;
            if (tce.isTimeout()) {
                if (tce.getId().equals(currentDataFrameTransaction.getId())) {
                    logger.trace("Transaction timed out");
                    attemptResend(ctx, false);
                }
            } else {
                ctx.fireUserEventTriggered(evt);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * Called when a DataFrame is written to the Z-Wave network.
     *
     * @param frame the frame that was written
     */
    public void onDataFrameWrite(DataFrame frame) {
        logger.trace("Detected data frame write: {}", frame);
        if (!hasCurrentTransaction()) {
            currentDataFrameTransaction = frame.createWrapperTransaction();
            if (currentDataFrameTransaction != null) {
                logger.trace("*** Data frame transaction started for {} with ID {}", frame, currentDataFrameTransaction.getId());

                // start timeout
                if (currentDataFrameTransaction.getTimeout() > 0 && handlerContext != null && handlerContext.executor() != null) {
                    timeoutFuture = handlerContext.executor().schedule(
                        new TransactionTimeoutHandler(
                            currentDataFrameTransaction.getId(),
                            handlerContext,
                            this
                        ),
                        currentDataFrameTransaction.getTimeout(),
                        TimeUnit.MILLISECONDS
                    );
                } else {
                    logger.warn("Unable to schedule transaction timeout callback");
                }
            }
        } else {
            logger.trace("Wrote a data frame with a current transaction: {}", frame);
        }
    }

    /**
     * Indicated whether there is an active transaction.
     *
     * @return a boolean
     */
    public boolean hasCurrentTransaction() {
        return (processingTransactionCompletion || (currentDataFrameTransaction != null && !currentDataFrameTransaction.isComplete()));
    }

    public String getCurrentTransactionId() {
        return currentDataFrameTransaction != null ? currentDataFrameTransaction.getId() : null;
    }

    private void clearTransaction() {
        currentDataFrameTransaction = null;
    }

    private void attemptResend(ChannelHandlerContext ctx, boolean wasCANReceived) {
        DataFrame startFrame = currentDataFrameTransaction.getStartFrame();
        if (startFrame.getSendCount() < MAX_SEND_COUNT) {
            logger.debug("Transaction has failed - will reset transaction and resend initial request");
            currentDataFrameTransaction.reset();
            // if a CAN was received, then we decrement the send count by one so this attempt doesn't count
            // towards the maximum resend count
            if (wasCANReceived) {
                startFrame.decrementSendCount();
            }
            ctx.channel().writeAndFlush(startFrame);
        } else {
            logger.debug("Transaction has failed and has exceeded max resends - aborting");
            String id = currentDataFrameTransaction.getId();
            clearTransaction();
            ctx.fireUserEventTriggered(new TransactionCompletedEvent(id, true));
        }
    }
}
