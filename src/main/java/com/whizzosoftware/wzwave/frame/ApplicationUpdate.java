/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.buffer.ByteBuf;

/**
 * An application update data frame.
 *
 * @author Dan Noguerol
 */
public class ApplicationUpdate extends DataFrame {
    public static final byte ID = 0x49;

    public static final byte UPDATE_STATE_NODE_INFO_REQ_FAILED = (byte)0x81;
    public static final byte UPDATE_STATE_NODE_INFO_RECEIVED = (byte)0x84;

    private Byte nodeId;
    private byte state;
    private NodeInfo nodeInfo;

    public ApplicationUpdate() {
        super(DataFrameType.REQUEST, ID, null);
    }

    public ApplicationUpdate(DataFrameType type, byte state, byte nodeId) {
        this(type, state, nodeId, null);
    }

    public ApplicationUpdate(DataFrameType type, byte state, byte nodeId, NodeInfo nodeInfo) {
        super(type, ID, null);
        this.state = state;
        this.nodeId = nodeId;
        this.nodeInfo = nodeInfo;
    }

    public ApplicationUpdate(ByteBuf buffer) {
        super(buffer);
        state = buffer.readByte();
        this.nodeId = buffer.readByte();
        if (state == UPDATE_STATE_NODE_INFO_RECEIVED) {
            nodeInfo = new NodeInfo(buffer, dataFrameLength - 6);
        } else {
            buffer.readByte(); // read 0 length
        }
    }

    public Byte getNodeId() {
        return nodeId;
    }

    public void setNodeId(Byte nodeId) {
        this.nodeId = nodeId;
    }

    public byte getState() {
        return state;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public boolean didInfoRequestFail() {
        return (state == UPDATE_STATE_NODE_INFO_REQ_FAILED);
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return null;
    }
}
