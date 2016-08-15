/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.channel;

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
public class ZWaveQueuedOutboundHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveQueuedOutboundHandler.class);

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
            ZWaveDataFrameTransactionInboundHandler transactionHandler = (ZWaveDataFrameTransactionInboundHandler)ctx.pipeline().get("transaction");
            if (transactionHandler != null) {
                DataFrame frame = (DataFrame) msg;
                if (transactionHandler.hasCurrentRequestTransaction()) {
                    logger.trace("Queueing data frame: " + frame + "; current queue size: " + pendingQueue.size());
                    pendingQueue.add(new FrameWrite(frame, promise));
                } else {
                    logger.trace("No transaction detected, sending data frame: {}", frame);
                    ctx.writeAndFlush(msg, promise);
                    frame.incremenentSendCount();
                    transactionHandler.onDataFrameWrite(frame);
                }
            } else {
                logger.error("No transaction handler found; dropping data frame");
            }
        } else {
            ctx.writeAndFlush(msg, promise);
        }
    }

    public void onDataFrameTransactionComplete() {
        logger.trace("Detected data frame transaction completion");
        ZWaveDataFrameTransactionInboundHandler transactionHandler = (ZWaveDataFrameTransactionInboundHandler)context.pipeline().get("transaction");
        if (pendingQueue.size() > 0) {
            FrameWrite fw = pendingQueue.pop();
            logger.trace("Sending next queued data frame: {}", fw.frame);
            context.writeAndFlush(fw.frame, fw.promise);
            fw.frame.incremenentSendCount();
            transactionHandler.onDataFrameWrite(fw.frame);
        } else {
            logger.trace("No pending data frames to send");
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
