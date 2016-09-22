/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.channel.event.*;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.frame.transaction.NodeInclusionTransaction;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handler for all Z-Wave frame transactions. Responsible for tracking the state of the current transaction
 * including successes, failures and timeouts.
 *
 * @author Dan Noguerol
 */
public class TransactionInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TransactionInboundHandler.class);

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
                NettyZWaveChannelContext zctx = new NettyZWaveChannelContext();
                if (currentDataFrameTransaction.addFrame(zctx, frame)) {
                    if (currentDataFrameTransaction.isComplete()) {
                        logger.trace("*** Data frame transaction ({}) completed", tid);
                        logger.trace("");

                        // cancel the timeout callback
                        if (timeoutFuture != null) {
                            timeoutFuture.cancel(true);
                            timeoutFuture = null;
                        }
                    }
                    zctx.process(ctx);
                // if transaction didn't consume frame, then pass it down the pipeline
                } else {
                    logger.trace("Transaction ignored frame so passing it along");
                    ctx.fireChannelRead(msg);
                }
            } else if (msg instanceof AddNodeToNetwork) {
                logger.trace("Received ADD_NODE_STATUS_NODE_FOUND; starting transaction");
                NettyZWaveChannelContext zctx = new NettyZWaveChannelContext();
                currentDataFrameTransaction = new NodeInclusionTransaction(zctx, (DataFrame)msg);
                zctx.process(ctx);
            } else {
                logger.trace("Received frame outside of transaction context so passing it along: {}", frame);
                ctx.fireChannelRead(msg);
            }
        }
    }

    DataFrameTransaction getCurrentTransaction() {
        return currentDataFrameTransaction;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof DataFrameSentEvent) {
            DataFrameSentEvent dfse = (DataFrameSentEvent)evt;
            logger.trace("Detected data frame write event: {}", dfse.getDataFrame());
            if (!hasCurrentTransaction()) {
                NettyZWaveChannelContext zctx = new NettyZWaveChannelContext();
                currentDataFrameTransaction = dfse.getDataFrame().createWrapperTransaction(zctx, dfse.isListeningNode());
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
                    zctx.process(ctx);
                }
            } else {
                logger.trace("Wrote a data frame with a current transaction: {}", dfse.getDataFrame());
            }
        } else if (evt instanceof TransactionTimeoutEvent) {
            TransactionTimeoutEvent tte = (TransactionTimeoutEvent)evt;
            if (tte.getId().equals(currentDataFrameTransaction.getId())) {
                logger.trace("Detected transaction timeout");
                NettyZWaveChannelContext zctx = new NettyZWaveChannelContext();
                currentDataFrameTransaction.timeout(zctx);
                zctx.process(ctx);
            } else {
                logger.error("Received timeout event for unknown transaction: {}", tte.getId());
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * Indicates whether there is an active transaction.
     *
     * @return a boolean
     */
    boolean hasCurrentTransaction() {
        return (currentDataFrameTransaction != null && !currentDataFrameTransaction.isComplete());
    }

    private class NettyZWaveChannelContext implements ZWaveChannelContext {
        private List<Object> events;
        private List<OutboundDataFrame> frames;

        void process(ChannelHandlerContext ctx) {
            if (events != null) {
                for (Object o : events) {
                    ctx.fireUserEventTriggered(o);
                }
            }
            if (frames != null) {
                for (OutboundDataFrame f : frames) {
                    ctx.writeAndFlush(f);
                }
            }
        }

        @Override
        public void fireEvent(Object o) {
            if (events == null) {
                events = new ArrayList<>();
            }
            events.add(o);
        }

        @Override
        public void writeFrame(OutboundDataFrame f) {
            if (frames == null) {
                frames = new ArrayList<>();
            }
            frames.add(f);
        }
    }
}
