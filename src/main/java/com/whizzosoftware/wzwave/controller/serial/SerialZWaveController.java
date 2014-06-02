/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.serial;

import com.whizzosoftware.wzwave.controller.*;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.node.ZWaveNodeFactory;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.frame.parser.FrameListener;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.controller.serial.rxtx.RXTXChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An implementation of a Z-Wave controller that supports serial control.
 *
 * @author Dan Noguerol
 */
public class SerialZWaveController implements Runnable, ZWaveController, FrameListener, NodeListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SerialChannel serialChannel;
    private Deque<DataFrame> pendingQueue = new LinkedList<DataFrame>();
    private Deque<Frame> readQueue = new LinkedList<Frame>();
    private DataFrameTransaction currentDataFrameTransaction;
    private SerialZWaveControllerReadThread readThread;
    private ZWaveControllerWriteDelegate writeDelegate;
    private Thread controllerThread;
    private String libraryVersion;
    private int homeId;
    private byte nodeId;
    private List<ZWaveNode> nodes = new ArrayList<ZWaveNode>();
    private Map<Byte,ZWaveNode> nodeMap = new HashMap<Byte,ZWaveNode>();
    private final Queue<Byte> nodeProtocolInfoQueue = new LinkedList<Byte>();
    private ZWaveControllerListener listener;

    /**
     * Constructor
     *
     * @param serialChannel the Z-Wave serial API channel
     * @param listener a listener for Z-Wave events that occur
     */
    public SerialZWaveController(SerialChannel serialChannel, ZWaveControllerListener listener) {
        this(serialChannel, null, listener);
    }

    /**
     * Constructor
     *
     * @param serialChannel the Z-Wave serial API channel
     * @param writeDelegate a delegate for writing Z-Wave frames (used for testing)
     * @param listener a listener for Z-Wave events that occur
     */
    public SerialZWaveController(SerialChannel serialChannel, ZWaveControllerWriteDelegate writeDelegate, ZWaveControllerListener listener) {
        this.serialChannel = serialChannel;
        this.writeDelegate = writeDelegate;

        setListener(listener);

        if (this.serialChannel != null) {
            // configure serial port
            this.serialChannel.setBaudRate(115200);
            this.serialChannel.setNumStopBits(SerialChannel.ONE_STOP_BIT);
            this.serialChannel.setParity(SerialChannel.PARITY_NONE);

            // open port
            if (!this.serialChannel.openPort()) {
                throw new RuntimeException("Unable to open serial port");
            }
        }
    }

    @Override
    public void setListener(ZWaveControllerListener listener) {
        this.listener = listener;
    }

    /**
     * Starts the Z-Wave controller. This will startup all threads and send the initial batch of discovery frames.
     */
    public void start() {
        // create the controller thread
        controllerThread = new Thread(this, "Controller");
        controllerThread.setName("Z-Wave Controller");
        controllerThread.start();

        // create the read thread
        readThread = new SerialZWaveControllerReadThread(serialChannel, this);
        readThread.setName("Z-Wave Controller Read");
        readThread.start();

        // set the write delegate if not already set by the constructor
        if (writeDelegate == null) {
            SerialZWaveControllerWriteThread writeThread = new SerialZWaveControllerWriteThread(serialChannel);
            writeDelegate = writeThread;
        }
        writeDelegate.start();

        // send initial discovery frames
        queueDataFrame(new Version());
        queueDataFrame(new MemoryGetId());
        queueDataFrame(new InitData());
    }

    /**
     * Stops the Z-Wave controller.
     */
    public void stop() {
        logger.debug("Stopping");

        readThread.interrupt();
        writeDelegate.stop();
        controllerThread.interrupt();
        serialChannel.close();
    }

    /**
     * Returns the controller's Z-Wave library version
     *
     * @return the version String
     */
    public String getLibraryVersion() {
        return libraryVersion;
    }

    /**
     * Returns the home ID of this controller
     *
     * @return the home ID
     */
    public int getHomeId() {
        return homeId;
    }

    /**
     * Returns the node ID of this controller
     *
     * @return the node ID
     */
    public byte getNodeId() {
        return nodeId;
    }

    /**
     * Returns all nodes the controller is currently aware of.
     *
     * @return a Collection of ZWaveNode instances
     */
    public Collection<ZWaveNode> getNodes() {
        return nodes;
    }

    /**
     * The main run loop.
     */
    public void run() {
        logger.debug("Z-Wave controller thread starting");

        while (!Thread.currentThread().isInterrupted()) {
            // perform any new processing
            process(System.currentTimeMillis());

            // sleep for 50 ms so we don't run in too tight a loop
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // NO-OP
            }
        }

        logger.info("Z-Wave controller thread exiting");
    }

    @Override
    public void onACK() {
        readQueue.add(new ACK());
    }

    @Override
    public void onNAK() {
        readQueue.add(new NAK());
    }

    @Override
    public void onCAN() {
        readQueue.add(new CAN());
    }

    @Override
    public void onDataFrame(DataFrame frame) {
        logger.trace("onDataFrame: " + frame);
        readQueue.add(frame);
    }

    @Override
    public void onNodeStarted(ZWaveNode node) {
        // when a node moves to the "started" state, alert listeners that it's ready to be added
        if (listener != null) {
            listener.onZWaveNodeAdded(node);
        }
    }

    synchronized public void process(long now) {
        // process any new data frames that were received
        if (readQueue.size() > 0) {
            Frame frame = readQueue.pop();

            // send acknowledgement immediately if necessary
            if (frame instanceof DataFrame) {
                writeDelegate.writeFrame(new ACK());
            }

            // if we are in the context of a data frame transaction, add the new data frame to it
            if (hasCurrentRequestTransaction()) {
                logger.debug("Received data frame within transaction context");
                currentDataFrameTransaction.addFrame(frame, now);
                if (currentDataFrameTransaction.isComplete()) {
                    processData(currentDataFrameTransaction.getFinalData(), false);
                    logger.debug("*** Data frame transaction complete");
                    logger.debug("");
                    currentDataFrameTransaction = null;
                } else if (currentDataFrameTransaction.shouldRetry(now)) {
                    logger.debug("Retrying previous data frame: {}", currentDataFrameTransaction.getStartFrame());
                    writeDelegate.writeFrame(currentDataFrameTransaction.getStartFrame());
                    currentDataFrameTransaction.incrementRetryCount(now);
                }
            // otherwise, process the data frame immediately
            } else if (frame instanceof DataFrame) {
                logger.debug("Received date frame outside of transaction context");
                processData((DataFrame)frame, true);
            } else {
                logger.warn("Got unexpected data: " + frame);
            }
        }

        if (hasCurrentRequestTransactionError(now)) {
            logger.debug("*** Data frame transaction error - aborting");
            logger.debug("");

            // clear out the aborted transaction
            currentDataFrameTransaction = null;
        }

        // allow nodes to do processing
        if (!hasCurrentRequestTransaction()) {
            for (ZWaveNode node : nodes) {
                node.runLoop(this);
            }
            transmitNextPendingData(now);
        }
    }

    public boolean createNode(ZWaveNode node) {
        if (node != null) {
            logger.debug("Created node [" + node.getNodeId() + "]: " + node);
            nodes.add(node);
            nodeMap.put(node.getNodeId(), node);
            return true;
        }
        return false;
    }

    synchronized public void sendDataFrame(DataFrame dataFrame) {
        queueDataFrame(dataFrame);
    }

    private void queueDataFrame(DataFrame data) {
        logger.debug("Queueing data frame: " + data + "; current queue size: " + pendingQueue.size());
        pendingQueue.add(data);
    }

    private void transmitNextPendingData(long now) {
        if (pendingQueue.size() > 0) {
            transmitData(pendingQueue.pop(), now);
        }
    }

    private void transmitData(DataFrame data, long now) {
        if (hasCurrentRequestTransaction()) {
            logger.error("State error - writing new data frame while a current data frame transaction is in progress");
        } else {
            currentDataFrameTransaction = data.createTransaction(now);
            logger.debug("");
            logger.debug("*** Starting new data frame transaction");
            writeDelegate.writeFrame(data);
        }
    }

    private void processData(DataFrame data, boolean unsolicited) {
        if (data != null) {
            if (data instanceof Version) {
                libraryVersion = ((Version)data).getLibraryVersion();
                logger.info("Got Z-Wave Version: " + libraryVersion);
            } else if (data instanceof MemoryGetId) {
                MemoryGetId mgid = (MemoryGetId)data;
                homeId = mgid.getHomeId();
                nodeId = mgid.getNodeId();
                logger.info("Home ID: " + homeId + "; node ID: " + nodeId);
            } else if (data instanceof InitData) {
                processInitData((InitData)data);
            } else if (data instanceof NodeProtocolInfo) {
                processNodeProtocolInfo((NodeProtocolInfo)data);
            } else if (data instanceof SendData) {
                processSendData((SendData)data, unsolicited);
            } else if (data instanceof ApplicationCommand) {
                processApplicationCommandHandler((ApplicationCommand)data, unsolicited);
            } else if (data instanceof ApplicationUpdate) {
                processApplicationUpdate((ApplicationUpdate)data, unsolicited);
            } else {
                logger.error("Received unknown data frame: " + data);
            }
        }
    }

    private void processInitData(InitData initData) {
        synchronized (nodeProtocolInfoQueue) {
            for (Byte nodeId : initData.getNodes()) {
                logger.info("Node " + nodeId + " found; requesting protocol info");

                // the node protocol info response won't include a node ID so we add this to a queue
                // in order to correlate the response to this node
                nodeProtocolInfoQueue.add(nodeId);

                // send the node protocol info request
                queueDataFrame(new NodeProtocolInfo(nodeId));
            }
        }
    }

    private void processNodeProtocolInfo(NodeProtocolInfo nodeProtocolInfo) {
        Byte nodeId;
        synchronized (nodeProtocolInfoQueue) {
            nodeId = nodeProtocolInfoQueue.remove();
        }
        if (nodeId != null) {
            logger.debug("Received protocol info for node " + nodeId);
            if (!createNode(ZWaveNodeFactory.createNode(nodeId, nodeProtocolInfo, this))) {
                logger.error("Unable to create node: " + nodeProtocolInfo);
            }
        } else {
            logger.error("Got unexpected protocol info response");
        }
    }

    private void processSendData(SendData sendData, boolean unsolicited) {
        byte nodeId = sendData.getNodeId();
        ZWaveNode node = nodeMap.get(nodeId);
        if (node != null) {
            node.onDataFrameReceived(sendData, unsolicited);
        } else {
            logger.error("Unable to find node " + nodeId);
        }
    }

    private void processApplicationCommandHandler(ApplicationCommand applicationCommand, boolean unsolicited) {
        ZWaveNode node = nodeMap.get(applicationCommand.getNodeId());
        if (node != null) {
            node.onDataFrameReceived(applicationCommand, unsolicited);
            if (listener != null) {
                listener.onZWaveNodeUpdated(node);
            }
        } else {
            logger.error("Unable to find node " + applicationCommand.getNodeId());
        }
    }

    private void processApplicationUpdate(ApplicationUpdate applicationUpdate, boolean unsolicited) {
        Byte nodeId = null;

        // if we received an application update failure, then try to get the node ID from the
        // current data frame transaction request
        if (applicationUpdate.didInfoRequestFail()) {
            logger.warn("UPDATE_STATE_NODE_INFO_REQ_FAILED received");
            if (currentDataFrameTransaction != null && currentDataFrameTransaction.getStartFrame() instanceof RequestNodeInfo) {
                nodeId = ((RequestNodeInfo)currentDataFrameTransaction.getStartFrame()).getNodeId();
            }
        } else {
            nodeId = applicationUpdate.getNodeId();
        }

        if (nodeId != null) {
            ZWaveNode node = nodeMap.get(nodeId);
            if (node != null) {
                node.onDataFrameReceived(applicationUpdate, unsolicited);
                if (listener != null) {
                    listener.onZWaveNodeUpdated(node);
                }
            } else {
                logger.error("Unable to find node " + applicationUpdate.getNodeId());
            }
        } else {
            logger.error("Unable to determine node to route ApplicationUpdate to");
        }
    }

    private boolean hasCurrentRequestTransaction() {
        return (currentDataFrameTransaction != null && !currentDataFrameTransaction.isComplete());
    }

    private boolean hasCurrentRequestTransactionError(long now) {
        return (currentDataFrameTransaction != null && currentDataFrameTransaction.hasError(now));
    }

    public static void main(String[] args) throws Exception {
        SerialZWaveController c = new SerialZWaveController(new RXTXChannel("/dev/cu.SLAB_USBtoUART"), null);
        c.start();
    }
}
