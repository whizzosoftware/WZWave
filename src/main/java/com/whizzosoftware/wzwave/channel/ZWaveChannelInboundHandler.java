package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ZWaveChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveChannelInboundHandler.class);

    private final Queue<Byte> nodeProtocolInfoQueue = new LinkedList<Byte>();
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
        } else {
            logger.error("Received unknown data frame: " + frame);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("An exception occurred", cause);
    }

    private void processInitData(ChannelHandlerContext ctx, InitData initData) {
        for (Byte nodeId : initData.getNodes()) {
            logger.trace("Node " + nodeId + " found; requesting protocol info");

            // the node protocol info response won't include a node ID so we add this to a queue
            // in order to correlate the response to this node
            nodeProtocolInfoQueue.add(nodeId);

            // send the node protocol info request
            ctx.channel().write(new NodeProtocolInfo(nodeId));
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
