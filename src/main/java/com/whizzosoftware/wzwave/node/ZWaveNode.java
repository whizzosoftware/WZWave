/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.*;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Abstract base class for all Z-Wave nodes.
 *
 * @author Dan Noguerol
 */
abstract public class ZWaveNode extends ZWaveEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Byte basicDeviceClass;
    private boolean listening;
    private Boolean available;
    private final LinkedList<DataFrame> wakeupQueue = new LinkedList<DataFrame>();
    protected DataFrame lastSentData;
    protected ZWaveNodeState nodeState;
    private int stateRetries;
    /**
     * Indicates whether the Version command class has sent its startup messages
     */
    private boolean versionStartupMessagesSent;
    private int pendingVersionResponses;
    private int pendingStatusResponses;
    private NodeListener listener;

    public ZWaveNode(ZWaveControllerContext context, byte nodeId, NodeProtocolInfo info, NodeListener listener) {
        super(nodeId, info.getGenericDeviceClass(), info.getSpecificDeviceClass());

        this.listener = listener;
        setState(context, ZWaveNodeState.NodeInfo);

        basicDeviceClass = info.getBasicDeviceClass();
        listening = info.isListening();

        // if the device is listening, request its node info
        if (listening) {
            sendDataFrame(context, new RequestNodeInfo(nodeId));
        }
    }

    protected void setListening(ZWaveControllerContext context, boolean listening) {
        this.listening = listening;

        // if the node is set to listener, flush the wakeup queue
        if (listening) {
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

    public Byte getBasicDeviceClass() {
        return basicDeviceClass;
    }

    protected ZWaveNodeState getState() {
        return nodeState;
    }

    protected void setState(ZWaveControllerContext context, ZWaveNodeState nodeState) {
        logger.debug("Node {} changing to state: {}", getNodeId(), nodeState);
        this.nodeState = nodeState;
        this.stateRetries = 0;

        switch (nodeState) {
            case RetrieveVersionPending:
                CommandClass cc = getCommandClass(VersionCommandClass.ID);
                pendingVersionResponses = cc.queueStartupMessages(new WrapperedNodeContext(context, this), getNodeId());
                setState(context, ZWaveNodeState.RetrieveVersionCompleted);
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
                setState(context, ZWaveNodeState.RetrieveStateCompleted);
                break;
            case Started:
                if (listener != null) {
                    listener.onNodeStarted(this);
                }
        }
    }

    abstract protected void refresh(boolean deferIfNotListening);

    public int getWakeupQueueCount() {
        return wakeupQueue.size();
    }

    /**
     * Called when an inbound message is received for this specific node.
     *
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
                if (cc instanceof VersionCommandClass && nodeState == ZWaveNodeState.RetrieveVersionCompleted) {
                    pendingVersionResponses--;
                    if (pendingVersionResponses <= 0) {
                        setState(context, ZWaveNodeState.RetrieveStatePending);
                    }
                // if we're waiting on state responses, decrement the pending count and change to next state if
                // we've received all pending responses
                } else if (nodeState == ZWaveNodeState.RetrieveStateCompleted) {
                    pendingStatusResponses--;
                    if (pendingStatusResponses <= 0) {
                        setState(context, ZWaveNodeState.Started);
                    }
                }
            } else {
                logger.error("Received message for unsupported command class ID: {}", ByteUtil.createString(commandClassId));
            }
            lastSentData = null;
        } else if (dataFrame instanceof ApplicationUpdate) {
            processApplicationUpdate(context, (ApplicationUpdate)dataFrame);
            lastSentData = null;
        }
    }

    /**
     * Add a data frame to the write queue.
     *
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
     * @param frame the data frame to write
     * @param deferIfNotListening indicates if the command should be deferred (added to the wakeup queue) if the node
     *                            is flagged as not listening
     */
    protected void queueDataFrame(ZWaveControllerContext context, DataFrame frame, boolean deferIfNotListening) {
        if (listening || !deferIfNotListening) {
            logger.debug("Queueing data frame for write: {}", frame);
            context.sendDataFrame(frame);
        } else {
            logger.debug("Queueing data frame for next wakeup: {}", frame);
            wakeupQueue.add(frame);
        }
    }

    /**
     * Flushes all commands sitting in the wakeup queue to the write queue.
     * @param context
     */
    public void flushWakeupQueue(ZWaveControllerContext context) {
        while (wakeupQueue.size() > 0) {
            queueDataFrame(context, wakeupQueue.pop(), false);
        }
    }

    protected void processApplicationUpdate(ZWaveControllerContext context, ApplicationUpdate update) {
        switch (nodeState) {

            case NodeInfo:
                // if the application update failed to send, re-send it
                if (update.didInfoRequestFail()) {
                    if (stateRetries < 1) {
                        logger.debug("Application update failed for node {}; will retry", getNodeId());
                        sendDataFrame(context, new RequestNodeInfo(getNodeId()));
                        stateRetries++;
                    } else {
                        if (listening) {
                            logger.debug("Node {} provided no node info after {} retries; should be listening so flagging as unavailable and started", getNodeId(), stateRetries);
                            available = false;
                            setState(context, ZWaveNodeState.Started);
                        } else {
                            logger.debug("Node {} provided no node info after {} retries; not flagged as listening so assuming it's asleep", getNodeId(), stateRetries);
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
                                logger.debug("Ignoring optional command class: {}", ByteUtil.createString(commandClassId));
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
                logger.debug("Unsolicited ApplicationUpdate received; refreshing node");
                // if we received an unsolicited update, the node is awake so push out any pending commands
                flushWakeupQueue(context);
                refresh(false);
                break;
        }
    }
}
