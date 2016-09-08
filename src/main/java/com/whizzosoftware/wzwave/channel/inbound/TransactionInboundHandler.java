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
import com.whizzosoftware.wzwave.channel.event.*;
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

                // give new frame to current transaction
                if (currentDataFrameTransaction.addFrame(frame)) {
                    if (currentDataFrameTransaction.isComplete()) {
                        // cancel the timeout callback
                        if (timeoutFuture != null) {
                            timeoutFuture.cancel(true);
                            timeoutFuture = null;
                        }

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

                            clearTransaction();
                            ctx.fireUserEventTriggered(new TransactionCompletedEvent(tid, finalFrame));
                        } else if (currentDataFrameTransaction.shouldRetry()) {
                            attemptResend(ctx);
                        } else {
                            logger.trace("*** Data frame transaction ({}) failed", tid);
                            logger.trace("");
                            clearTransaction();
                            ctx.fireUserEventTriggered(new TransactionFailedEvent(tid));
                        }
                    }
                // if transaction didn't consume frame, then pass it down the pipeline
                } else {
                    logger.trace("Transaction ignored frame so passing it along");
                    ctx.fireChannelRead(msg);
                }
            } else if (msg instanceof AddNodeToNetwork) {
                logger.trace("Received ADD_NODE_STATUS_NODE_FOUND; starting transaction");
                currentDataFrameTransaction = new NodeInclusionTransaction((DataFrame)msg);
            } else {
                logger.trace("Received frame outside of transaction context so passing it along: {}", frame);
                ctx.fireChannelRead(msg);
            }
        }
    }

    public DataFrameTransaction getCurrentTransaction() {
        return currentDataFrameTransaction;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.trace("User event received: {}", evt);
        if (evt instanceof DataFrameSentEvent) {
            DataFrameSentEvent dfse = (DataFrameSentEvent)evt;
            logger.trace("Detected data frame write event: {}", dfse.getDataFrame());
            if (!hasCurrentTransaction()) {
                currentDataFrameTransaction = dfse.getDataFrame().createWrapperTransaction(dfse.isListeningNode());
                if (currentDataFrameTransaction != null) {
                    logger.trace("*** Data frame transaction started for {} with ID {}", dfse.getDataFrame(), currentDataFrameTransaction.getId());
                    // start timeout timer
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
                    ctx.fireUserEventTriggered(new TransactionStartedEvent(currentDataFrameTransaction.getId()));
                }
            } else {
                logger.trace("Wrote a data frame with a current transaction: {}", dfse.getDataFrame());
            }
        } else if (evt instanceof TransactionTimeoutEvent) {
            TransactionTimeoutEvent tte = (TransactionTimeoutEvent)evt;
            if (tte.getId().equals(currentDataFrameTransaction.getId())) {
                logger.trace("Detected transaction timeout");
                if (currentDataFrameTransaction.shouldRetry()) {
                    attemptResend(ctx);
                } else {
                    clearTransaction();
                    ctx.fireUserEventTriggered(new TransactionFailedEvent(tte.getId()));
                }
            } else {
                logger.error("Received timeout event for unknown transaction: {}", tte.getId());
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * Indicated whether there is an active transaction.
     *
     * @return a boolean
     */
    public boolean hasCurrentTransaction() {
        return (currentDataFrameTransaction != null && !currentDataFrameTransaction.isComplete());
    }

    private void clearTransaction() {
        currentDataFrameTransaction = null;
    }

    private void attemptResend(ChannelHandlerContext ctx) {
        DataFrame startFrame = currentDataFrameTransaction.getStartFrame();
        if (startFrame.getSendCount() < MAX_SEND_COUNT) {
            logger.debug("Transaction has failed - will reset and resend initial request");
            currentDataFrameTransaction.reset();
            // if a CAN was received, then we decrement the send count by one so this attempt doesn't count
            // towards the maximum resend count
            ctx.channel().writeAndFlush(new OutboundDataFrame(startFrame, currentDataFrameTransaction.isListeningNode()));
        } else {
            logger.debug("Transaction has failed and has exceeded max resends");
            String id = currentDataFrameTransaction.getId();
            clearTransaction();
            ctx.fireUserEventTriggered(new TransactionFailedEvent(id));
        }
    }
}
