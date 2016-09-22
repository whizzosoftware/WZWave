/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.channel.event.NodeSleepChangeEvent;
import com.whizzosoftware.wzwave.commandclass.*;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.persist.PersistenceContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract base class for all Z-Wave nodes.
 *
 * @author Dan Noguerol
 */
abstract public class ZWaveNode extends ZWaveEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Byte basicDeviceClass;
    private boolean isListeningNode;
    private ZWaveNodeState nodeState;
    /**
     * Indicates whether a non-listening node is sleeping.
     */
    private boolean sleeping;
    /**
     * Indicates that the node is available. This means it is either a listening node and has been contacted recently
     * or is a sleeping node and has checked in before its wakeup interval has expired.
     */
    private Boolean available;
    private int stateRetries;
    /**
     * Indicates whether the Version command class has sent its startup messages
     */
    private boolean versionStartupMessagesSent;
    private int pendingVersionResponses;
    private int pendingStatusResponses;
    private NodeListener listener;

    /**
     * Constructor.
     * @param info information about the new node
     * @param isListeningNode indicates whether the node is a "actively listening" node
     * @param listener the listener for callbacks
     */
    public ZWaveNode(NodeInfo info, boolean isListeningNode, NodeListener listener) {
        super(info.getNodeId(), info.getGenericDeviceClass(), info.getSpecificDeviceClass());

        this.isListeningNode = isListeningNode;
        this.listener = listener;
        this.basicDeviceClass = info.getBasicDeviceClass();

        addCommandClass(NoOperationCommandClass.ID, new NoOperationCommandClass());
    }

    public ZWaveNode(PersistenceContext pctx, Byte nodeId, NodeListener listener) {
        super(pctx, nodeId);
        this.listener = listener;
    }

    public void startInterview(ZWaveControllerContext ctx) {
        if (shouldRequestNodeInfo()) {
            setState(ctx, ZWaveNodeState.Ping);
        } else {
            setState(ctx, ZWaveNodeState.Started);
        }
    }

    protected void setSleeping(ZWaveControllerContext context, boolean sleeping) {
        logger.trace("Setting node {} as sleeping={}", getNodeId(), sleeping);
        this.sleeping = sleeping;
        context.sendEvent(new NodeSleepChangeEvent(getNodeId(), sleeping));
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isStarted() {
        return (nodeState == ZWaveNodeState.Started);
    }

    public boolean isListeningNode() {
        return isListeningNode;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public Byte getBasicDeviceClass() {
        return basicDeviceClass;
    }

    public ZWaveNodeState getState() {
        return nodeState;
    }

    protected void setState(ZWaveControllerContext context, ZWaveNodeState nodeState) {
        logger.trace("Node {} changing to state: {}", getNodeId(), nodeState);
        this.nodeState = nodeState;
        this.stateRetries = 0;

        switch (nodeState) {
            case Ping:
                sendDataFrame(context, ((NoOperationCommandClass)getCommandClass(NoOperationCommandClass.ID)).createGet(getNodeId()));
                break;
            case NodeInfo:
                sendDataFrame(context, new RequestNodeInfo(getNodeId()));
                break;
            case RetrieveVersionPending:
                CommandClass cc = getCommandClass(VersionCommandClass.ID);
                pendingVersionResponses = cc.queueStartupMessages(new WrapperedNodeContext(context, this), getNodeId());
                setState(context, ZWaveNodeState.RetrieveVersionSent);
                versionStartupMessagesSent = true;
                break;
            case RetrieveStatePending:
                pendingStatusResponses = 0;
                for (CommandClass ccc : getCommandClasses()) {
                    // queue all command class startup messages
                    // note: if the Version command class hasn't queued its startup messages at this point, allow it to
                    //       do so -- otherwise, prevent it from occurring twice
                    if (ccc.getId() != VersionCommandClass.ID || !versionStartupMessagesSent) {
                        pendingStatusResponses += ccc.queueStartupMessages(new WrapperedNodeContext(context, this), getNodeId());
                    }
                }
                setState(context, ZWaveNodeState.RetrieveStateSent);
                break;
            case Started:
                if (listener != null) {
                    listener.onNodeStarted(this);
                }
                break;
        }
    }

    abstract protected void refresh(boolean deferIfNotListening);

    /**
     * Called when an application command message is received for this specific node.
     *
     * @param context the context for processing the command
     * @param ac the application command received
     */
    public void onApplicationCommand(ZWaveControllerContext context, ApplicationCommand ac) {
        byte commandClassId = ac.getCommandClassId();
        CommandClass cc = getCommandClass(commandClassId);
        if (cc != null) {
            // if it's a BasicCommandClass instance, allow the node to perform any
            // required command mapping
            if (cc instanceof BasicCommandClass) {
                cc = performBasicCommandClassMapping((BasicCommandClass)cc);
            }

            // allow the command class to process the frame
            cc.onApplicationCommand(new WrapperedNodeContext(context, this), ac.getCommandClassBytes(), 0);

            // if we received a VersionCommandClass and we've got pending responses, decrement the pending count
            // and change to next state if we've received all pending responses
            if (cc instanceof VersionCommandClass && nodeState == ZWaveNodeState.RetrieveVersionSent) {
                pendingVersionResponses--;
                if (pendingVersionResponses <= 0) {
                    setState(context, ZWaveNodeState.RetrieveStatePending);
                }
                // if we're waiting on state responses, decrement the pending count and change to next state if
                // we've received all pending responses
            } else if (nodeState == ZWaveNodeState.RetrieveStateSent) {
                pendingStatusResponses--;
                if (pendingStatusResponses <= 0) {
                    setState(context, ZWaveNodeState.Started);
                }
            }
        } else {
            logger.error("Received message for unsupported command class ID: {}", ByteUtil.createString(commandClassId));
        }
    }

    protected void sendDataFrame(ZWaveControllerContext context, DataFrame frame) {
        context.sendDataFrame(frame, isListeningNode());
        if (nodeState == ZWaveNodeState.RetrieveStateSent) {
            pendingStatusResponses++;
        }
    }

    /**
     * Called when an application update message is received for this node.
     *
     * @param context the context for processing the update
     * @param update the application update received
     */
    public void onApplicationUpdate(ZWaveControllerContext context, ApplicationUpdate update) {
        switch (nodeState) {

            case NodeInfo:
                // if the application update failed to send, re-send it
                if (update.didInfoRequestFail()) {
                    if (stateRetries < 1) {
                        logger.trace("Application update failed for node {}; will retry", getNodeId());
                        sendDataFrame(context, new RequestNodeInfo(getNodeId()));
                        stateRetries++;
                    } else {
                        if (isListeningNode()) {
                            logger.trace("Node {} provided no node info after {} retries; should be listening so flagging as unavailable and started", getNodeId(), stateRetries);
                            available = false;
                            setState(context, ZWaveNodeState.Started);
                        } else {
                            logger.trace("Node {} provided no node info after {} retries; not flagged as listening so assuming it's asleep", getNodeId(), stateRetries);
                            available = true;
                            setState(context, ZWaveNodeState.Started);
                        }
                    }
                } else {
                    // check if there are optional command classes
                    byte[] commandClasses = update.getNodeInfo().getCommandClasses();
                    for (byte commandClassId : commandClasses) {
                        if (!hasCommandClass(commandClassId)) {
                            CommandClass cc = CommandClassFactory.createCommandClass(commandClassId);
                            if (cc != null) {
                                addCommandClass(commandClassId, cc);
                            } else {
                                logger.trace("Ignoring optional command class: {}", ByteUtil.createString(commandClassId));
                            }
                        }
                    }

                    // if this node has the Version command class, then we should retrieve version for information
                    // for all command classes it supports
                    if (getCommandClass(VersionCommandClass.ID) != null) {
                        setState(context, ZWaveNodeState.RetrieveVersionPending);
                        // otherwise, we assume all command classes are version 1 and move on
                    } else {
                        setState(context, ZWaveNodeState.RetrieveStatePending);
                    }
                }
                break;

            default:
                logger.trace("Unsolicited ApplicationUpdate received; refreshing node");
                refresh(false);
                break;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (CommandClass cc : getCommandClasses()) {
            sb.append(cc.toString());
        }
        return "ZWaveNode{" +
                "nodeId=" + getNodeId() +
                ", isListeningNode=" + isListeningNode +
                ", nodeState=" + nodeState +
                ", isSleeping=" + sleeping +
                ", available=" + available +
                ", commandClasses=" + sb.toString() +
                '}';
    }

    public void onSendDataCallback(ZWaveControllerContext context, boolean wasACKReceived) {
        switch (nodeState) {
            case Ping:
                if (wasACKReceived) {
                    logger.debug("Successfully pinged node; proceeding to get node info");
                    setAvailable(true);
                    setSleeping(context, false);
                    setState(context, ZWaveNodeState.NodeInfo);
                } else {
                    if (isListeningNode) {
                        logger.debug("Failed to ping listening node; flagging as unavailable");
                        setAvailable(false);
                        setSleeping(context, false);
                        setState(context, ZWaveNodeState.Started);
                    } else {
                        logger.debug("Failed to ping non-listening node; flagging as asleep");
                        setAvailable(true);
                        setSleeping(context, true);
                        setState(context, ZWaveNodeState.Started);
                    }
                }
                break;
            default:
                logger.trace("Received SendData callback: {}", wasACKReceived);
                break;
        }
    }

    /**
     * Returns the appropriate command class for a BasicCommandClass if a mapping needs
     * to be performed. The default is to perform no mapping.
     *
     * @param cc the BasicCommandClass to map
     * @return the mapped CommandClass
     */
    protected CommandClass performBasicCommandClassMapping(BasicCommandClass cc) {
        return cc;
    }

    public Map<String,Object> restore(PersistenceContext ctx, byte nodeId) {
        Map<String,Object> map = super.restore(ctx, nodeId);
        if (map.containsKey("basicDeviceClass")) {
            basicDeviceClass = (byte)map.get("basicDeviceClass");
        }
        this.isListeningNode = (boolean)map.get("listening");
        return map;
    }

    public Map<String,Object> save(PersistenceContext ctx) {
        Map<String,Object> map = super.save(ctx);
        if (basicDeviceClass != null) {
            map.put("basicDeviceClass", basicDeviceClass);
        }
        map.put("listening", isListeningNode());
        return map;
    }

    public boolean matchesNodeProtocolInfo(NodeProtocolInfo info) {
        return (getBasicDeviceClass() == info.getBasicDeviceClass() && getGenericDeviceClass() == info.getGenericDeviceClass() && getSpecificDeviceClass() == info.getSpecificDeviceClass());
    }

    /**
     * Indicates whether this node should request node information on startup.
     *
     * @return a boolean
     */
    protected boolean shouldRequestNodeInfo() {
        return true;
    }

    /**
     * Indicates whether this node should send request state on startup.
     *
     * @return a boolean
     */
    protected boolean shouldRequestState() {
        return true;
    }
}
