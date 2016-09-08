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

import com.whizzosoftware.wzwave.channel.event.*;
import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.OutboundDataFrame;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Handler responsible for queueing data frames and writing them to the Z-Wave network when appropriate.
 *
 * @author Dan Noguerol
 */
public class FrameQueueHandler extends ChannelHandlerAdapter implements ChannelOutboundHandler, ChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(FrameQueueHandler.class);

    private Deque<FrameWrite> pendingQueue = new LinkedList<>();
    private String currentTransactionId;

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        logger.trace("write: " + msg);
        if (msg instanceof OutboundDataFrame) {
            OutboundDataFrame odf = (OutboundDataFrame)msg;
            if (currentTransactionId == null || odf.matchesTransaction(currentTransactionId)) {
                ctx.writeAndFlush(odf.getDataFrame(), promise);
                odf.getDataFrame().incremenentSendCount();
                ctx.fireUserEventTriggered(new DataFrameSentEvent(odf.getDataFrame(), odf.isListeningNode()));
            } else {
                logger.trace("Queueing data frame: {}", msg);
                pendingQueue.add(new FrameWrite(odf.getDataFrame(), odf.isListeningNode(), promise));
            }
        } else if (msg instanceof TransactionStartedEvent) {
            currentTransactionId = ((TransactionStartedEvent)msg).getId();
            logger.trace("Detected data frame transaction start: {}", currentTransactionId);
        } else if (msg instanceof TransactionCompletedEvent) {
            logger.trace("Detected data frame transaction completion: {}", currentTransactionId);
            currentTransactionId = null;
            sendNextFrame(ctx);
        } else if (msg instanceof TransactionFailedEvent) {
            logger.trace("Detected data frame transaction failure: {}", currentTransactionId);
            currentTransactionId = null;
            sendNextFrame(ctx);
        } else if (msg instanceof ACK){
            ctx.writeAndFlush(msg, promise);
        } else {
            logger.error("Direct DataFrame write attempt detected");
        }
    }

    private void sendNextFrame(ChannelHandlerContext ctx) {
        if (pendingQueue.size() > 0) {
            FrameWrite fw = pendingQueue.pop();
            logger.trace("Sending next queued data frame: {}", fw.frame);
            ctx.writeAndFlush(fw.frame, fw.promise);
            fw.frame.incremenentSendCount();
            ctx.fireUserEventTriggered(new DataFrameSentEvent(fw.frame, fw.isListeningNode));
        } else {
            logger.trace("No pending data frames to send");
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelWritabilityChanged();
    }

    private class FrameWrite {
        DataFrame frame;
        ChannelPromise promise;
        boolean isListeningNode;

        FrameWrite(DataFrame frame, boolean isListeningNode, ChannelPromise promise) {
            this.frame = frame;
            this.promise = promise;
            this.isListeningNode = isListeningNode;
        }
    }
}
