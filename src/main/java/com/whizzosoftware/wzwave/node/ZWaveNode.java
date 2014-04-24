/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.*;
import com.whizzosoftware.wzwave.controller.serial.SerialZWaveController;
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
abstract public class ZWaveNode extends ZWaveEndpoint implements NodeContext {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Byte basicDeviceClass;
    private boolean listening;
    protected final LinkedList<DataFrame> writeQueue = new LinkedList<DataFrame>();
    private final LinkedList<DataFrame> wakeupQueue = new LinkedList<DataFrame>();
    protected DataFrame lastSentData;
    private ZWaveNodeState nodeState;
    private int stateRetries;
    /**
     * Indicates whether the Version command class has sent its startup messages
     */
    private boolean versionStartupMessagesSent;
    private NodeListener listener;

    public ZWaveNode(byte nodeId, NodeProtocolInfo info, NodeListener listener) {
        super(nodeId, info.getGenericDeviceClass(), info.getSpecificDeviceClass());

        this.listener = listener;
        setState(ZWaveNodeState.NodeInfo);

        basicDeviceClass = info.getBasicDeviceClass();
        listening = info.isListening();

        // if the device is listening, request its node info
        if (listening) {
            queueDataFrame(new RequestNodeInfo(nodeId));
        }
    }

    protected void setListening(boolean listening) {
        this.listening = listening;

        // if the node is set to listener, flush the wakeup queue
        if (listening) {
            flushWakeupQueue();
        }
    }

    public Byte getBasicDeviceClass() {
        return basicDeviceClass;
    }

    protected void setState(ZWaveNodeState nodeState) {
        logger.debug("Node {} changing to state: {}", getNodeId(), nodeState);
        this.nodeState = nodeState;
        this.stateRetries = 0;

        if (nodeState == ZWaveNodeState.Started && listener != null) {
            listener.onNodeStarted(this);
        }
    }

    abstract protected void refresh(boolean deferIfNotListening);

    public int getWriteQueueCount() {
        return writeQueue.size();
    }

    public int getWakeupQueueCount() {
        return wakeupQueue.size();
    }

    public boolean hasPendingTransaction() {
        return (lastSentData != null);
    }

    /**
     * Called periodically by the controller to allow nodes to perform processing.
     *
     * Note: This should only be called from the Controller thread.
     *
     * @param controller the Controller instance (used for command sending)
     */
    public void runLoop(SerialZWaveController controller) {
        // if the node is ready to retrieve command class versions, allow the command class to queue its messages
        if (nodeState == ZWaveNodeState.RetrieveVersionPending) {
            CommandClass cc = getCommandClass(VersionCommandClass.ID);
            cc.queueStartupMessages(getNodeId(), this);
            setState(ZWaveNodeState.RetrieveVersionCompleted);
            versionStartupMessagesSent = true;
        }

        // if the node is ready to retrieve its state, queue all command class startup messages
        if (nodeState == ZWaveNodeState.RetrieveStatePending) {
            for (CommandClass cc : getCommandClasses()) {
                // queue all command class startup messages
                // note: if the Version command class hasn't queued its startup messages at this point, allow it to
                //       do so -- otherwise, prevent it from occurring twice
                if (cc.getId() != VersionCommandClass.ID || !versionStartupMessagesSent) {
                    cc.queueStartupMessages(getNodeId(), this);
                }
            }
            setState(ZWaveNodeState.RetrieveStateCompleted);
        }

        // if we aren't waiting on a message response and there's a new request message queued, send it
        if (!hasPendingTransaction() && getWriteQueueCount() > 0) {
            DataFrame d = writeQueue.pop();
            logger.trace("Sending data frame to controller: {}", d);
            controller.sendDataFrame(d);
            lastSentData = d;
        } else if (!hasPendingTransaction() && getWriteQueueCount() == 0) {
            if (nodeState == ZWaveNodeState.RetrieveVersionCompleted) {
                setState(ZWaveNodeState.RetrieveStatePending);
            } else if (nodeState == ZWaveNodeState.RetrieveStateCompleted) {
                setState(ZWaveNodeState.Started);
            }
        }
    }

    /**
     * Called when an inbound message is received for this specific node.
     *
     * Note: This should only be called from the Controller thread.
     *
     * @param dataFrame the data frame received
     */
    public void onDataFrameReceived(SerialZWaveController controller, DataFrame dataFrame, boolean unsolicited) {
        if (dataFrame instanceof ApplicationCommand) {
            byte commandClassId = ((ApplicationCommand)dataFrame).getCommandClassId();
            CommandClass cc = getCommandClass(commandClassId);
            if (cc != null) {
                // if it's a BasicCommandClass instance, allow the node to perform any
                // required command mapping
                if (cc instanceof BasicCommandClass) {
                    cc = performBasicCommandClassMapping((BasicCommandClass)cc);
                }
                cc.onApplicationCommand(((ApplicationCommand)dataFrame).getCommandClassBytes(), 0, this);
            } else {
                logger.error("Received message for unsupported command class ID: {}", ByteUtil.createString(commandClassId));
            }
            lastSentData = null;
        } else if (dataFrame instanceof ApplicationUpdate) {
            processApplicationUpdate((ApplicationUpdate)dataFrame, unsolicited);
            lastSentData = null;
        }
    }

    /**
     * Add a data frame to the write queue.
     *
     * @param data the data frame to write
     */
    public void queueDataFrame(DataFrame data) {
        queueDataFrame(data, true);
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
     * @param data the data frame to write
     * @param deferIfNotListening indicates if the command should be deferred (added to the wakeup queue) if the node
     *                            is flagged as not listening
     */
    protected void queueDataFrame(DataFrame data, boolean deferIfNotListening) {
        if (listening || !deferIfNotListening) {
            logger.debug("Queueing data frame for write: {}", data);
            writeQueue.add(data);
        } else {
            logger.debug("Queueing data frame for next wakeup: {}", data);
            wakeupQueue.add(data);
        }
    }

    /**
     * Flushes all commands sitting in the wakeup queue to the write queue.
     */
    public void flushWakeupQueue() {
        while (wakeupQueue.size() > 0) {
            queueDataFrame(wakeupQueue.pop(), false);
        }
    }

    protected void processApplicationUpdate(ApplicationUpdate update, boolean unsolicited) {
        switch (nodeState) {

            case NodeInfo:
                // if the application update failed to send, re-send it
                if (update.didInfoRequestFail()) {
                    if (stateRetries < 1) {
                        logger.debug("Application update failed for node {}; will retry", getNodeId());
                        queueDataFrame(new RequestNodeInfo(getNodeId()));
                        stateRetries++;
                    } else {
                        logger.debug("No node information provided after {} retries; moving to next state", stateRetries);
                        setState(ZWaveNodeState.RetrieveStatePending);
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
                        setState(ZWaveNodeState.RetrieveVersionPending);
                    // otherwise, we assume all command classes are version 1 and move on
                    } else {
                        setState(ZWaveNodeState.RetrieveStatePending);
                    }
                }
                break;

            default:
                logger.debug("Unsolicited ApplicationUpdate received; refreshing node");
                // if we received an unsolicited update, the node is awake so push out any pending commands
                flushWakeupQueue();
                refresh(false);
                break;
        }
    }
}
