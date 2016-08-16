/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.ApplicationUpdate;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
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
public class ZWaveDataFrameTransactionInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveDataFrameTransactionInboundHandler.class);

    private static final int MAX_SEND_COUNT = 2;

    private ChannelHandlerContext handlerContext;
    private DataFrameTransaction currentDataFrameTransaction;
    private boolean processingTransactionCompletion = false;
    private ScheduledFuture timeoutFuture;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.handlerContext = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Frame) {
            Frame frame = (Frame) msg;
            if (hasCurrentRequestTransaction()) {
                logger.trace("Received frame within transaction context: {}", frame);

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
                            logger.trace("*** Data frame transaction completed with final frame: {}", finalFrame);
                            logger.trace("");

                            // if there's an ApplicationUpdate with no node ID (e.g. when there's a app update failure), attempt
                            // to set the node ID based on the request frame that triggered it
                            if (finalFrame instanceof ApplicationUpdate) {
                                ApplicationUpdate update = (ApplicationUpdate) finalFrame;
                                if ((update.getNodeId() == null || update.getNodeId() == 0) && currentDataFrameTransaction.getStartFrame() instanceof RequestNodeInfo) {
                                    update.setNodeId(((RequestNodeInfo) currentDataFrameTransaction.getStartFrame()).getNodeId());
                                }
                            }

                            // pass the final frame down the pipeline
                            if (finalFrame != null) {
                                ctx.fireChannelRead(finalFrame);
                            }

                        } else {
                            attemptResend(ctx);
                        }

                        // clear the current transaction
                        currentDataFrameTransaction = null;

                        // now the frame transaction is truly done
                        processingTransactionCompletion = false;

                        // alert the outbound pipeline that a frame transaction has been completed
                        ChannelPipeline pipeline = ctx.pipeline();
                        if (pipeline != null) {
                            ZWaveQueuedOutboundHandler writeHandler = (ZWaveQueuedOutboundHandler) ctx.pipeline().get("writeQueue");
                            if (writeHandler != null) {
                                writeHandler.onDataFrameTransactionComplete();
                            }
                        }
                    }
                } else {
                    logger.trace("Transaction didn't consume frame so passing it along");
                    ctx.fireChannelRead(msg);
                }
            } else {
                logger.trace("Received frame outside of transaction context: {}", frame);
                ctx.fireChannelRead(msg);
            }
        } else if (msg instanceof TransactionTimeout) {
            // if a timeout is received for the current transaction, attempt to resend; otherwise ignore it
            TransactionTimeout tt = (TransactionTimeout)msg;
            if (tt.getId().equals(currentDataFrameTransaction.getId())) {
                logger.trace("Transaction timed out");
                attemptResend(ctx);
            }
        }
    }

    public void onDataFrameWrite(DataFrame frame) {
        logger.trace("Detected data frame write: {}", frame);
        if (!hasCurrentRequestTransaction()) {
            currentDataFrameTransaction = frame.createTransaction();
            if (currentDataFrameTransaction != null) {
                logger.trace("*** Data frame transaction started for {}", frame);

                // start timeout
                if (currentDataFrameTransaction.getTimeout() > 0 && handlerContext != null && handlerContext.executor() != null) {
                    timeoutFuture = handlerContext.executor().schedule(
                            new TransactionTimeout(
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

    public boolean hasCurrentRequestTransaction() {
        return (processingTransactionCompletion || (currentDataFrameTransaction != null && !currentDataFrameTransaction.isComplete()));
    }

    protected void attemptResend(ChannelHandlerContext ctx) {
        DataFrame startFrame = currentDataFrameTransaction.getStartFrame();
        if (startFrame.getSendCount() < MAX_SEND_COUNT) {
            logger.debug("Transaction has failed - resending initial request");
            ctx.channel().writeAndFlush(startFrame);
        } else {
            logger.debug("Transaction has failed and has exceeded max resends - aborting");
        }
    }
}
