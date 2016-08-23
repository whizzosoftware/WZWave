/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel.outbound;

import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.inbound.TransactionInboundHandler;
import com.whizzosoftware.wzwave.frame.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Handler responsible for queueing data frames and writing them to the Z-Wave network when appropriate.
 *
 * @author Dan Noguerol
 */
public class QueuedOutboundHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(QueuedOutboundHandler.class);

    private ChannelHandlerContext context;
    private Deque<FrameWrite> pendingQueue = new LinkedList<>();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        logger.trace("write: " + msg);
        if (msg instanceof DataFrame) {
            TransactionInboundHandler transactionHandler = (TransactionInboundHandler)ctx.pipeline().get("transaction");
            if (transactionHandler != null) {
                DataFrame frame = (DataFrame)msg;
                if (transactionHandler.hasCurrentTransaction()) {
                    String tid = transactionHandler.getCurrentTransactionId();
                    if (frame.hasTransactionId() && frame.getTransactionId().equals(tid)) {
                        logger.trace("Frame is associated with current transaction; sending immediately");
                        ctx.writeAndFlush(msg, promise);
                        frame.incremenentSendCount();
                        transactionHandler.onDataFrameWrite(frame);
                    } else {
                        logger.trace("Queueing data frame: " + frame + "; queue size before adding frame: " + pendingQueue.size());
                        pendingQueue.add(new FrameWrite(frame, promise));
                    }
                } else {
                    logger.trace("No transaction detected, sending data frame: {}", frame);
                    ctx.writeAndFlush(msg, promise);
                    frame.incremenentSendCount();
                    transactionHandler.onDataFrameWrite(frame);
                }
            } else {
                logger.error("No transaction handler found; dropping data frame");
            }
        } else if (msg instanceof TransactionCompletedEvent) {
            logger.trace("Detected data frame transaction completion");
            if (pendingQueue.size() > 0) {
                TransactionInboundHandler transactionHandler = (TransactionInboundHandler)context.pipeline().get("transaction");
                FrameWrite fw = pendingQueue.pop();
                logger.trace("Sending next queued data frame: {}", fw.frame);
                context.writeAndFlush(fw.frame, fw.promise);
                fw.frame.incremenentSendCount();
                transactionHandler.onDataFrameWrite(fw.frame);
            } else {
                logger.trace("No pending data frames to send");
            }
        } else {
            ctx.writeAndFlush(msg, promise);
        }
    }

    private class FrameWrite {
        public DataFrame frame;
        public ChannelPromise promise;

        public FrameWrite(DataFrame frame, ChannelPromise promise) {
            this.frame = frame;
            this.promise = promise;
        }
    }
}
