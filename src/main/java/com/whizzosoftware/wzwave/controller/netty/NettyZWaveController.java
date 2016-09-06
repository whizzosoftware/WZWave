/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.controller.netty;

import com.whizzosoftware.wzwave.channel.*;
import com.whizzosoftware.wzwave.channel.inbound.ACKInboundHandler;
import com.whizzosoftware.wzwave.channel.inbound.ZWaveChannelInboundHandler;
import com.whizzosoftware.wzwave.channel.inbound.TransactionInboundHandler;
import com.whizzosoftware.wzwave.channel.outbound.QueuedOutboundHandler;
import com.whizzosoftware.wzwave.codec.ZWaveFrameDecoder;
import com.whizzosoftware.wzwave.codec.ZWaveFrameEncoder;
import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.node.*;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A Netty implementation of a ZWaveController.
 *
 * The pipelines look like this:
 *
 *                                                      I/O Request via Channel or
 *                                                        ChannelHandlerContext
 *                                                                  |
 * +----------------------------------------------------------------+-------------------+
 * |                           ChannelPipeline                      |                   |
 * |                                                               \|/                  |
 * |    +--------------------------------+            +-------------+--------------+    |
 * |    |    ZWaveChannelInboundHandler  |            |      ZWaveFrameEncoder     |    |
 * |    +---------------+----------------+            +-------------+--------------+    |
 * |                   /|\                                          |                   |
 * |                    |                                          \|/                  |
 * |    +---------------+----------------+            +-------------+--------------+    |
 * |    |    TransactionInboundHandler   |            |    QueuedOutboundHandler   |    |
 * |    +---------------+----------------+            +-------------+--------------+    |
 * |                   /|\                                          |                   |
 * |                    |                                           |                   |
 * |    +---------------+----------------+                          |                   |
 * |    |       ACKInboundHandler        |                          |                   |
 * |    +---------------+----------------+                          |                   |
 * |                   /|\                                          |                   |
 * |                    |                                           |                   |
 * |    +---------------+----------------+                          |                   |
 * |    |        ZWaveFrameDecoder       |                          |                   |
 * |    +---------------+----------------+                          |                   |
 * |                   /|\                                          |                   |
 * +--------------------+-------------------------------------------+-------------------+
 * |                    |                                          \|/                  |
 * +--------------------+-------------------------------------------+-------------------+
 * |                    |                                           |                   |
 * |            [ Socket.read() ]                           [ Socket.write() ]          |
 * +------------------------------------------------------------------------------------+
 *
 * @author Dan Noguerol
 */
public class NettyZWaveController implements ZWaveController, ZWaveControllerContext, ZWaveControllerListener, ZWaveChannelListener, NodeListener {
    private static final Logger logger = LoggerFactory.getLogger(NettyZWaveController.class);

    private String serialPort;
    private Bootstrap bootstrap;
    private Channel channel;
    private String libraryVersion;
    private Integer homeId;
    private Byte nodeId;
    private ZWaveChannelInboundHandler inboundHandler;
    private ZWaveControllerListener listener;
    private final List<ZWaveNode> nodes = new ArrayList<>();
    private final Map<Byte,ZWaveNode> nodeMap = new HashMap<>();

    public NettyZWaveController(String serialPort) {
        this.serialPort = serialPort;
        this.inboundHandler = new ZWaveChannelInboundHandler(this);

        bootstrap = new Bootstrap();
        bootstrap.group(new OioEventLoopGroup());
        bootstrap.channel(RxtxChannel.class);
        bootstrap.handler(new ChannelInitializer<RxtxChannel>() {
            @Override
            protected void initChannel(RxtxChannel channel) throws Exception {
                NettyZWaveController.this.channel = channel;
                channel.config().setBaudrate(115200);
                channel.config().setDatabits(RxtxChannelConfig.Databits.DATABITS_8);
                channel.config().setParitybit(RxtxChannelConfig.Paritybit.NONE);
                channel.config().setStopbits(RxtxChannelConfig.Stopbits.STOPBITS_1);
                channel.pipeline().addLast("decoder", new ZWaveFrameDecoder());
                channel.pipeline().addLast("ack", new ACKInboundHandler());
                channel.pipeline().addLast("transaction", new TransactionInboundHandler());
                channel.pipeline().addLast("handler", inboundHandler);
                channel.pipeline().addLast("encoder", new ZWaveFrameEncoder());
                channel.pipeline().addLast("writeQueue", new QueuedOutboundHandler());
            }
        });
    }

    /*
     * ZWaveController methods
     */

    @Override
    public void setListener(ZWaveControllerListener listener) {
        this.listener = listener;
    }

    public void start() {
        bootstrap.connect(new RxtxDeviceAddress(serialPort)).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channel.write(new Version());
                    channel.write(new MemoryGetId());
                    channel.write(new InitData());
                } else {
                    onZWaveConnectionFailure(future.cause());
                }
            }
        });
    }

    @Override
    public void stop() {

    }

    @Override
    public int getHomeId() {
        return homeId;
    }

    @Override
    public byte getNodeId() {
        return nodeId;
    }

    @Override
    public String getLibraryVersion() {
        return libraryVersion;
    }

    @Override
    public Collection<ZWaveNode> getNodes() {
        return nodes;
    }

    @Override
    public ZWaveNode getNode(byte nodeId) {
        return nodeMap.get(nodeId);
    }

    @Override
    public void sendDataFrame(DataFrame dataFrame) {
        channel.write(dataFrame);
    }

    /*
     * ZWaveControllerListener methods
     */

    @Override
    public void onZWaveNodeAdded(ZWaveEndpoint node) {
        if (listener != null) {
            listener.onZWaveNodeAdded(node);
        }
    }

    @Override
    public void onZWaveNodeUpdated(ZWaveEndpoint node) {
        if (listener != null) {
            listener.onZWaveNodeUpdated(node);
        }
    }

    @Override
    public void onZWaveConnectionFailure(Throwable t) {
        if (listener != null) {
            listener.onZWaveConnectionFailure(t);
        } else {
            logger.error("Connection failure and no listener was set", t);
        }
    }

    @Override
    public void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId) {
        if (listener != null && libraryVersion != null && homeId != null && nodeId != null) {
            listener.onZWaveControllerInfo(libraryVersion, homeId, nodeId);
        }
    }

    @Override
    public void onZWaveInclusionStarted() {
        if (listener != null) {
            listener.onZWaveInclusionStarted();
        }
    }

    @Override
    public void onZWaveInclusion(NodeInfo nodeInfo, boolean success) {
        try {
            logger.trace("Inclusion of new node {}", ByteUtil.createString(nodeInfo.getNodeId()));
            ZWaveNode node = ZWaveNodeFactory.createNode(this, nodeInfo, true, true, this);
            logger.trace("Created node [" + node.getNodeId() + "]: " + node);
            nodes.add(node);
            nodeMap.put(node.getNodeId(), node);
            if (listener != null) {
                listener.onZWaveInclusion(nodeInfo, success);
            }
        } catch (NodeCreationException e) {
            logger.error("Unable to create node", e);
        }
    }

    @Override
    public void onZWaveInclusionStopped() {
        if (listener != null) {
            listener.onZWaveInclusionStopped();
        }
    }

    @Override
    public void onZWaveExclusionStarted() {
        if (listener != null) {
            listener.onZWaveExclusionStarted();
        }
    }

    @Override
    public void onZWaveExclusion(NodeInfo nodeInfo, boolean success) {
        if (listener != null) {
            listener.onZWaveExclusion(nodeInfo, success);
        }
    }

    @Override
    public void onZWaveExclusionStopped() {
        if (listener != null) {
            listener.onZWaveExclusionStopped();
        }
    }

    /*
     * ZWaveChannelListener methods
     */

    @Override
    public void onLibraryInfo(String libraryVersion) {
        this.libraryVersion = libraryVersion;
        onZWaveControllerInfo(libraryVersion, homeId, nodeId);
    }

    @Override
    public void onControllerInfo(int homeId, byte nodeId) {
        this.homeId = homeId;
        this.nodeId = nodeId;
        onZWaveControllerInfo(libraryVersion, homeId, nodeId);
    }

    @Override
    public void onNodeProtocolInfo(byte nodeId, NodeProtocolInfo npi) {
        try {
            logger.trace("Received protocol info for node " + nodeId);
            ZWaveNode node = ZWaveNodeFactory.createNode(
                this,
                new NodeInfo(nodeId, npi.getBasicDeviceClass(), npi.getGenericDeviceClass(), npi.getSpecificDeviceClass()),
                false,
                npi.isListening(),
                this
            );
            logger.trace("Created node [" + node.getNodeId() + "]: " + node);
            nodes.add(node);
            nodeMap.put(node.getNodeId(), node);
        } catch (NodeCreationException e) {
            logger.error("Unable to create node", e);
        }
    }

    @Override
    public void onSendData(SendData sendData) {
        byte nodeId = sendData.getNodeId();
        ZWaveNode node = nodeMap.get(nodeId);
        if (node != null) {
            node.onDataFrameReceived(this, sendData);
        } else {
            logger.error("Unable to find node " + nodeId);
        }
    }

    @Override
    public void onApplicationCommand(ApplicationCommand applicationCommand) {
        ZWaveNode node = nodeMap.get(applicationCommand.getNodeId());
        if (node != null) {
            onNodeUpdate(node, applicationCommand);
        } else {
            logger.error("Unable to find node: {}", nodeId);
        }
    }

    @Override
    public void onApplicationUpdate(ApplicationUpdate applicationUpdate) {
        Byte nodeId = applicationUpdate.getNodeId();

        if (applicationUpdate.didInfoRequestFail()) {
            logger.trace("UPDATE_STATE_NODE_INFO_REQ_FAILED received");
        }

        if (nodeId != null) {
            ZWaveNode node = nodeMap.get(nodeId);
            if (node != null) {
                onNodeUpdate(node, applicationUpdate);
            } else {
                logger.error("Unable to find node: {}", nodeId);
            }
        } else {
            logger.error("Unable to determine node to route ApplicationUpdate to");
        }
    }

    private void onNodeUpdate(ZWaveNode node, DataFrame df) {
        node.onDataFrameReceived(this, df);
        if (node.isStarted()) {
            onZWaveNodeUpdated(node);
        }
    }

    @Override
    public void onAddNodeToNetwork(AddNodeToNetwork update) {
        if (listener != null) {
            switch (update.getStatus()) {
                case AddNodeToNetwork.ADD_NODE_STATUS_LEARN_READY:
                    onZWaveInclusionStarted();
                    break;
                case AddNodeToNetwork.ADD_NODE_STATUS_DONE:
                    onZWaveInclusionStopped();
                    break;
                case AddNodeToNetwork.ADD_NODE_STATUS_ADDING_CONTROLLER:
                case AddNodeToNetwork.ADD_NODE_STATUS_ADDING_SLAVE:
                    onZWaveInclusion(update.getNodeInfo(), true);
                    break;
                case AddNodeToNetwork.ADD_NODE_STATUS_FAILED:
                    onZWaveInclusion(update.getNodeInfo(), false);
                    break;
                default:
                    logger.debug("Received unexpected status from AddNodeToNetwork frame: {}", update.getStatus());
            }
        }
    }

    @Override
    public void onRemoveNodeFromNetwork(RemoveNodeFromNetwork update) {
        if (listener != null) {
            switch (update.getStatus()) {
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_LEARN_READY:
                    onZWaveExclusionStarted();
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_DONE:
                    onZWaveExclusionStopped();
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_NODE_FOUND:
                    logger.debug("A node has been found that wants to be excluded: {}", ByteUtil.createString(update.getSource()));
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_REMOVING_CONTROLLER:
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_REMOVING_SLAVE:
                    onZWaveExclusion(update.getNodeInfo(), true);
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_FAILED:
                    onZWaveExclusion(update.getNodeInfo(), false);
                    break;
                default:
                    logger.debug("Received unexpected status from RemoveNodeFromNetwork frame: {}", update.getStatus());
            }
        }
    }

    @Override
    public void onSetDefault() {
        logger.info("Z-Wave controller has been reset to factory default");
    }

    /*
     * NodeListener methods
     */

    @Override
    public void onNodeStarted(ZWaveNode node) {
        // when a node moves to the "started" state, alert listeners that it's ready to be added
        onZWaveNodeAdded(node);
    }
}
