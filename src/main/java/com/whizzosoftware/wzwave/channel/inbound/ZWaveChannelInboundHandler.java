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

import com.whizzosoftware.wzwave.channel.ZWaveChannelListener;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionFailedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionStartedEvent;
import com.whizzosoftware.wzwave.frame.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Inbound handler for Z-Wave data frames.
 *
 * @author Dan Noguerol
 */
public class ZWaveChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveChannelInboundHandler.class);

    private final Queue<Byte> nodeProtocolInfoQueue = new LinkedList<>();
    private ZWaveChannelListener listener;

    public ZWaveChannelInboundHandler(ZWaveChannelListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Frame frame = (Frame)msg;
        if (frame instanceof Version) {
            if (listener != null) {
                listener.onLibraryInfo(((Version) frame).getLibraryVersion());
            }
        } else if (frame instanceof MemoryGetId) {
            MemoryGetId mgid = (MemoryGetId)frame;
            if (listener != null) {
                listener.onControllerInfo(mgid.getHomeId(), mgid.getNodeId());
            }
        } else if (frame instanceof InitData) {
            processInitData(ctx, (InitData)frame);
        } else if (frame instanceof NodeProtocolInfo) {
            processNodeProtocolInfo((NodeProtocolInfo)frame);
        } else if (frame instanceof SendData) {
            if (listener != null) {
                listener.onSendData((SendData) frame);
            }
        } else if (frame instanceof ApplicationCommand) {
            if (listener != null) {
                listener.onApplicationCommand((ApplicationCommand)frame);
            }
        } else if (frame instanceof ApplicationUpdate) {
            if (listener != null) {
                listener.onApplicationUpdate((ApplicationUpdate)frame);
            }
        } else if (frame instanceof AddNodeToNetwork) {
            if (listener != null) {
                listener.onAddNodeToNetwork((AddNodeToNetwork)frame);
            }
        } else if (frame instanceof RemoveNodeFromNetwork) {
            if (listener != null) {
                listener.onRemoveNodeFromNetwork((RemoveNodeFromNetwork)frame);
            }
        } else if (frame instanceof SetDefault) {
            if (listener != null) {
                listener.onSetDefault();
            }
        } else {
            logger.error("Received unknown data frame: " + frame);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("An exception occurred", cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.debug("User event received: {}", evt);
        if (evt instanceof TransactionStartedEvent) {
            TransactionStartedEvent tse = (TransactionStartedEvent)evt;
            listener.onTransactionStarted(tse);
        } else if (evt instanceof TransactionCompletedEvent) {
            TransactionCompletedEvent tce = (TransactionCompletedEvent)evt;
            listener.onTransactionComplete(tce);
            if (tce.hasFrame()) {
                channelRead(ctx, tce.getFrame());
            }
        } else if (evt instanceof TransactionFailedEvent) {
            listener.onTransactionFailed((TransactionFailedEvent)evt);
        }
    }

    private void processInitData(ChannelHandlerContext ctx, InitData initData) {
        for (Byte nodeId : initData.getNodes()) {
            logger.trace("Node " + nodeId + " found; requesting protocol info");

            // the node protocol info response won't include a node ID so we add this to a queue
            // in order to correlate the response to this node
            nodeProtocolInfoQueue.add(nodeId);

            // send the node protocol info request
            ctx.writeAndFlush(new OutboundDataFrame(new NodeProtocolInfo(nodeId), true));
        }
    }

    private void processNodeProtocolInfo(NodeProtocolInfo nodeProtocolInfo) {
        Byte nodeId = nodeProtocolInfoQueue.remove();
        if (nodeId != null) {
            listener.onNodeProtocolInfo(nodeId, nodeProtocolInfo);
        } else {
            logger.error("Got unexpected protocol info response");
        }
    }
}
