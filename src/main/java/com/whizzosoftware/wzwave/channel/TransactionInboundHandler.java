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
    private TransactionContext transactionContext;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.handlerContext = ctx;
    }

    /**
     * Called by Netty when data is read from the Z-Wave network.
     *
     * @param ctx the handler context
     * @param msg the message that was read
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        processData(ctx, msg, System.currentTimeMillis());
    }

    /**
     * Process data from the Z-Wave network.
     *
     * @param ctx the handler context
     * @param msg the message to process
     * @param time the time the data was read
     */
    void processData(ChannelHandlerContext ctx, Object msg, long time) {
        if (msg instanceof Frame) {
            Frame frame = (Frame) msg;
            if (hasCurrentTransaction()) {
                String tid = transactionContext.getId();
                logger.trace("Received frame within transaction ({}) context: {}", tid, frame);

                // give new frame to current transaction
                NettyZWaveChannelContext zctx = new NettyZWaveChannelContext();
                if (transactionContext.addFrame(zctx, frame)) {
                    if (transactionContext.isComplete()) {
                        logger.trace("*** Data frame transaction ({}) completed", tid);
                        logger.trace("");
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
                transactionContext = new TransactionContext(new NodeInclusionTransaction(zctx, (DataFrame)msg), time);
                zctx.process(ctx);
            } else {
                logger.trace("Received frame outside of transaction context so passing it along: {}", frame);
                ctx.fireChannelRead(msg);
            }
        }
    }

    /**
     * Called by Netty when an event is fired.
     *
     * @param ctx the handler context
     * @param evt the event that was fired
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        processEvent(ctx, evt, System.currentTimeMillis());
    }

    /**
     * Process an event propagated by Netty.
     *
     * @param ctx the handler context
     * @param evt the event
     * @param time the time the event was fired
     */
    void processEvent(ChannelHandlerContext ctx, Object evt, long time) {
        if (evt instanceof DataFrameSentEvent) {
            DataFrameSentEvent dfse = (DataFrameSentEvent)evt;
            logger.trace("Detected data frame write event: {}", dfse.getDataFrame());
            if (!hasCurrentTransaction()) {
                NettyZWaveChannelContext zctx = new NettyZWaveChannelContext();
                DataFrameTransaction t = dfse.getDataFrame().createWrapperTransaction(zctx, dfse.isListeningNode());
                if (t != null) {
                    transactionContext = new TransactionContext(t, time);
                    logger.trace("*** Data frame transaction started for {} with ID {}", dfse.getDataFrame(), t.getId());
                    zctx.process(ctx);
                }
            } else if (transactionContext != null && transactionContext.getStartFrame() == dfse.getDataFrame()) {
                logger.trace("Detected re-send of transaction start frame; starting timeout");
                transactionContext.resetTimeout(time);
            } else {
                logger.trace("Wrote a data frame with a current transaction: {}", dfse.getDataFrame());
            }
        } else if (evt instanceof TransactionTimeoutEvent) {
            TransactionTimeoutEvent tte = (TransactionTimeoutEvent)evt;
            if (tte.getId().equals(transactionContext.getId())) {
                logger.trace("Detected transaction timeout");
                NettyZWaveChannelContext zctx = new NettyZWaveChannelContext();
                transactionContext.processTimeoutEvent(zctx);
                zctx.process(ctx);
            } else {
                logger.error("Received timeout event for unknown transaction: {}", tte.getId());
            }
        } else if (evt instanceof TransactionFailedEvent) {
            TransactionFailedEvent tfe = (TransactionFailedEvent)evt;
            if (tfe.getId().equals(transactionContext.getId())) {
                logger.trace("Aborting failed transaction: {}", tfe.getId());
                transactionContext.cleanup();
                transactionContext = null;
            } else {
                logger.error("Received transaction failure for unknown transaction: {}", tfe.getId());
            }
        } else if (evt instanceof IncompleteDataFrameEvent) {
            if (transactionContext != null) {
                logger.trace("Incomplete data received from network; extending transaction timeout for {}", transactionContext.getId());
                transactionContext.resetTimeout(System.currentTimeMillis());
            }
            ctx.fireUserEventTriggered(evt);
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
        return (transactionContext != null && !transactionContext.isComplete());
    }

    /**
     * Returns the current transaction context.
     *
     * @return a TransactionContext instance or null if there is none
     */
    TransactionContext getTransactionContext() {
        return transactionContext;
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

    /**
     * A class that wrappers a DataFrameTransaction and manages timeout logic.
     */
    public class TransactionContext {
        DataFrameTransaction transaction;
        ScheduledFuture future;
        long timeoutStartTime;
        long timeoutDuration;

        TransactionContext(DataFrameTransaction transaction, long startTime) {
            this.transaction = transaction;
            scheduleTimeout(startTime);
        }

        public DataFrameTransaction getTransaction() {
            return transaction;
        }

        String getId() {
            return transaction.getId();
        }

        DataFrame getStartFrame() {
            return transaction.getStartFrame();
        }

        boolean addFrame(ZWaveChannelContext ctx, Frame frame) {
            boolean b = transaction.addFrame(ctx, frame);
            if (transaction.isComplete()) {
                cancelTimeout();
            }
            return b;
        }

        boolean isComplete() {
            return transaction.isComplete();
        }

        void processTimeoutEvent(ZWaveChannelContext ctx) {
            transaction.timeout(ctx);
        }

        public long getTimeoutStartTime() {
            return timeoutStartTime;
        }

        void resetTimeout(long currentTime) {
            cancelTimeout();
            scheduleTimeout(currentTime);
        }

        void cleanup() {
            cancelTimeout();
        }

        private void scheduleTimeout(long currentTime) {
            if (transaction.getTimeout() > 0 && handlerContext != null && handlerContext.executor() != null) {
                this.timeoutStartTime = currentTime;
                this.timeoutDuration = transaction.getTimeout();
                future = handlerContext.executor().schedule(
                        new TransactionTimeoutHandler(
                                transaction.getId(),
                                handlerContext,
                                TransactionInboundHandler.this
                        ),
                        timeoutDuration,
                        TimeUnit.MILLISECONDS
                );
            } else {
                logger.warn("Unable to schedule transaction timeout callback");
            }
        }

        private void cancelTimeout() {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
        }
    }
}
