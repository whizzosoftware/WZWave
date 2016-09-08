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

import com.whizzosoftware.wzwave.commandclass.*;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.persist.PersistenceContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
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
    private boolean nodeInfoNeeded;
    /**
     * Indicates whether a non-listening node is sleeping.
     */
    private boolean isSleeping;
    /**
     * Indicates that the node is available. This means it is either a listening node and has been contacted recently
     * or is a sleeping node and has checked in before its wakeup interval has expired.
     */
    private Boolean available;
    private final LinkedList<DataFrame> wakeupQueue = new LinkedList<>();
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
     *  @param info information about the new node
     * @param isListeningNode indicates whether the node is a "actively listening" node
     * @param listener the listener for callbacks
     */
    public ZWaveNode(NodeInfo info, boolean isListeningNode, NodeListener listener) {
        super(info.getNodeId(), info.getGenericDeviceClass(), info.getSpecificDeviceClass());

        this.isListeningNode = isListeningNode;
        this.listener = listener;
        this.basicDeviceClass = info.getBasicDeviceClass();
        this.nodeInfoNeeded = true;

        addCommandClass(NoOperationCommandClass.ID, new NoOperationCommandClass());
    }

    public ZWaveNode(PersistenceContext pctx, Byte nodeId, NodeListener listener) {
        super(pctx, nodeId);
        this.listener = listener;
        this.nodeInfoNeeded = false;
    }

    public void startInterview(ZWaveControllerContext context, boolean newlyIncluded) {
        // if the node was newly included, flag that it is actively listening
        isSleeping = !(isListeningNode || newlyIncluded);

        // if the device is listening and we don't already know about it, request its node info
        if (nodeInfoNeeded && shouldRequestNodeInfo() && !isSleeping) {
            setState(context, ZWaveNodeState.NodeInfo);
        // if the device is listening and we do know about it, get its current state
        } else if (!isSleeping && shouldRequestState()) {
            setState(context, ZWaveNodeState.RetrieveStatePending);
        // otherwise, set it to started since we won't be able to get its node info
        } else {
            setState(context, ZWaveNodeState.Started);
        }
    }

    public void setNodeInfoNeeded(boolean nodeInfoNeeded) {
        this.nodeInfoNeeded = nodeInfoNeeded;
    }

    protected void setSleeping(ZWaveControllerContext context, boolean isSleeping) {
        this.isSleeping = isSleeping;

        // if the node is set to listener, flush the wakeup queue
        if (!isSleeping) {
            flushWakeupQueue(context);
        }
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
        return isSleeping;
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

    public int getWakeupQueueCount() {
        return wakeupQueue.size();
    }

    /**
     * Called when an inbound message is received for this specific node.
     *
     * @param context the context for processing the data frame
     * @param dataFrame the data frame received
     */
    public void onDataFrameReceived(ZWaveControllerContext context, DataFrame dataFrame) {
        if (dataFrame instanceof ApplicationCommand) {
            byte commandClassId = ((ApplicationCommand)dataFrame).getCommandClassId();
            CommandClass cc = getCommandClass(commandClassId);
            if (cc != null) {

                // if it's a BasicCommandClass instance, allow the node to perform any
                // required command mapping
                if (cc instanceof BasicCommandClass) {
                    cc = performBasicCommandClassMapping((BasicCommandClass)cc);
                }

                // allow the command class to process the frame
                cc.onApplicationCommand(new WrapperedNodeContext(context, this), ((ApplicationCommand)dataFrame).getCommandClassBytes(), 0);

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
        } else if (dataFrame instanceof ApplicationUpdate) {
            processApplicationUpdate(context, (ApplicationUpdate)dataFrame);
        }
    }

    /**
     * Add a data frame to the write queue.
     *
     * @param context the context for processing the data frame
     * @param data the data frame to write
     */
    public void sendDataFrame(ZWaveControllerContext context, DataFrame data) {
        queueDataFrame(context, data, true);
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

    /**
     * Add a data frame to the write queue.
     *
     * @param context the context for processing the data frame
     * @param frame the data frame to write
     * @param deferIfNotListening indicates if the command should be deferred (added to the wakeup queue) if the node
     *                            is flagged as not listening
     */
    protected void queueDataFrame(ZWaveControllerContext context, DataFrame frame, boolean deferIfNotListening) {
        if (!isSleeping() || !deferIfNotListening) {
            logger.trace("Queueing data frame for write: {}", frame);
            context.sendDataFrame(frame);
            if (nodeState == ZWaveNodeState.RetrieveStateSent) {
                pendingStatusResponses++;
            }
        } else {
            logger.trace("Queueing data frame for next wakeup: {}", frame);
            wakeupQueue.add(frame);
        }
    }

    /**
     * Flushes all commands sitting in the wakeup queue to the write queue.
     *
     * @param context the context for processing the data frame
     */
    public void flushWakeupQueue(ZWaveControllerContext context) {
        while (wakeupQueue.size() > 0) {
            queueDataFrame(context, wakeupQueue.pop(), false);
        }
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

    protected void processApplicationUpdate(ZWaveControllerContext context, ApplicationUpdate update) {
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
                // if we received an unsolicited update, the node is awake so push out any pending commands
                flushWakeupQueue(context);
                refresh(false);
                break;
        }
    }
}
