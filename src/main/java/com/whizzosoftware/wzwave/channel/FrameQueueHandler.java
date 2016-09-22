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

import com.whizzosoftware.wzwave.ZWaveRuntimeException;
import com.whizzosoftware.wzwave.channel.event.*;
import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.OutboundDataFrame;
import com.whizzosoftware.wzwave.frame.SendData;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.*;

/**
 * Handler responsible for queueing data frames and writing them to the Z-Wave network when appropriate. This includes
 * managing wakeup queues for each node.
 *
 * @author Dan Noguerol
 */
public class FrameQueueHandler extends ChannelHandlerAdapter implements ChannelOutboundHandler, ChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(FrameQueueHandler.class);

    /**
     * Queue of frames waiting to be written to the Z-Wave network.
     */
    private final Deque<FrameWrite> sendQueue = new LinkedList<>();
    /**
     * Queues used to hold outbound data frames for sleeping nodes.
     */
    private final Map<Byte,Deque<FrameWrite>> wakeupQueueMap = new HashMap<>();
    /**
     * A map of whether nodes are currently sleeping.
     */
    private final Map<Byte,Boolean> sleepMap = new HashMap<>();
    /**
     * The ID of the currently active transaction (if any).
     */
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
        if (msg instanceof ACK) {
            logger.trace("Sending ACK");
            ctx.writeAndFlush(msg, promise);
        } else if (msg instanceof OutboundDataFrame) {
            logger.trace("Outbound frame received: {}", msg);
            processOutboundDataFrame(ctx, (OutboundDataFrame)msg, promise);
        } else if (msg instanceof TransactionStartedEvent) {
            currentTransactionId = ((TransactionStartedEvent)msg).getId();
            logger.trace("Data frame transaction start: {}", currentTransactionId);
        } else if (msg instanceof TransactionCompletedEvent) {
            logger.trace("Data frame transaction completion for {}", currentTransactionId);
            currentTransactionId = null;
            sendNextFrame(ctx, false);
        } else if (msg instanceof TransactionFailedEvent) {
            logger.trace("Data frame transaction failure for {}", currentTransactionId);
            currentTransactionId = null;
            if (msg instanceof SendDataTransactionFailedEvent) {
                SendDataTransactionFailedEvent sdtfe = (SendDataTransactionFailedEvent) msg;
                // if the transaction failure was caused by the node going to sleep, process it as such
                if (!sdtfe.isListeningNode() && !sdtfe.isTargetNodeACKReceived()) {
                    processNodeSleepChange(ctx, sdtfe.getNodeId(), true);
                    if (sdtfe.hasStartFrame()) {
                        logger.trace("Adding failed transaction start frame to head of wakeup queue");
                        getWakeupQueue(sdtfe.getNodeId()).addFirst(new FrameWrite(sdtfe.getStartFrame(), false, null));
                    }
                }
            }
            sendNextFrame(ctx, false);
        } else if (msg instanceof NodeSleepChangeEvent) {
            processNodeSleepChange(ctx, ((NodeSleepChangeEvent)msg).getNodeId(), ((NodeSleepChangeEvent)msg).isSleeping());
        } else {
            logger.error("Direct DataFrame write attempt detected");
        }
    }

    boolean hasPendingFrames() {
        return (sendQueue.size() > 0);
    }

    boolean hasWakeupFrames(byte nodeId) {
        return (getWakeupQueue(nodeId).size() > 0);
    }

    int getWakeupQueueSize(byte nodeId) {
        return getWakeupQueue(nodeId).size();
    }

    boolean hasTransaction() {
        return (currentTransactionId != null);
    }

    private void processOutboundDataFrame(ChannelHandlerContext ctx, OutboundDataFrame odf, ChannelPromise promise) {
        if (odf.hasDataFrame()) {
            FrameWrite fw = new FrameWrite(odf.getDataFrame(), odf.isListeningNode(), promise);
            Byte nodeId = fw.getNodeId();
            if (nodeId == null || !isSleeping(nodeId)) {
                boolean forceSend = false;
                if (currentTransactionId != null && odf.matchesTransaction(currentTransactionId)) {
                    logger.trace("Data frame is in context of current transaction; adding to head of send queue");
                    sendQueue.addFirst(fw);
                    forceSend = true;
                } else {
                    logger.trace("Data frame appended to send queue");
                    sendQueue.add(fw);
                }
                sendNextFrame(ctx, forceSend);
            } else {
                queueWakeupFrame(nodeId, fw);
            }
        } else {
            throw new ZWaveRuntimeException("Received empty OutboundDataFrame");
        }
    }

    private void sendNextFrame(ChannelHandlerContext ctx, boolean forceSend) {
        if (sendQueue.size() > 0 && (currentTransactionId == null || forceSend)) {
            FrameWrite fw = sendQueue.pop();
            logger.trace("Sending next queued data frame: {}", fw.frame);
            ctx.writeAndFlush(fw.frame, fw.promise);
            fw.frame.incremenentSendCount();
            ctx.fireUserEventTriggered(new DataFrameSentEvent(fw.frame, fw.isListeningNode));
        } else {
            logger.trace("No pending data frames to send");
        }
    }

    private void queueWakeupFrame(byte nodeId, FrameWrite fw) {
        logger.trace("Queueing wakeup frame for node {}", nodeId);
        Deque<FrameWrite> wakeupQueue = getWakeupQueue(nodeId);
        wakeupQueue.push(fw);
    }

    private boolean isSleeping(byte nodeId) {
        Boolean b = sleepMap.get(nodeId);
        return (b != null && b);
    }

    private Deque<FrameWrite> getWakeupQueue(byte nodeId) {
        Deque<FrameWrite> wakeupQueue = wakeupQueueMap.get(nodeId);
        if (wakeupQueue == null) {
            wakeupQueue = new LinkedList<>();
            wakeupQueueMap.put(nodeId, wakeupQueue);
        }
        return wakeupQueue;
    }

    private void processNodeSleepChange(ChannelHandlerContext ctx, byte nodeId, boolean sleeping) throws Exception {
        logger.debug("Detected sleep change for node {}: {}", nodeId, sleeping);
        sleepMap.put(nodeId, sleeping);
        Deque<FrameWrite> wakeupQueue = getWakeupQueue(nodeId);
        if (!sleeping) {
            logger.trace("Moving wakeup queue for node {} into send queue", nodeId);
            while (!wakeupQueue.isEmpty()) {
                sendQueue.push(wakeupQueue.pop());
            }
            sendNextFrame(ctx, false);
        } else if (sendQueue.size() > 0){
            logger.trace("Moving pending frames for node {} into wakeup queue", nodeId);
            Iterator<FrameWrite> it = sendQueue.iterator();
            while (it.hasNext()) {
                FrameWrite fw = it.next();
                if (fw.hasDestinationNode(nodeId)) {
                    wakeupQueue.push(fw);
                    it.remove();
                }
            }
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
        private DataFrame frame;
        private ChannelPromise promise;
        private boolean isListeningNode;

        FrameWrite(DataFrame frame, boolean isListeningNode, ChannelPromise promise) {
            this.frame = frame;
            this.promise = promise;
            this.isListeningNode = isListeningNode;
        }

        Byte getNodeId() {
            if (frame instanceof SendData) {
                return ((SendData)frame).getNodeId();
            } else {
                return null;
            }
        }

        boolean hasDestinationNode(byte node) {
            return (frame instanceof SendData && ((SendData)frame).getNodeId() == node);
        }
    }
}
