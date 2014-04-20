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

/**
 * An application update data frame.
 *
 * @author Dan Noguerol
 */
public class ApplicationUpdate extends DataFrame {
    public static final byte ID = 0x49;

    public static final byte UPDATE_STATE_NODE_INFO_REQ_FAILED = (byte)0x81;
    public static final byte UPDATE_STATE_NODE_INFO_RECEIVED = (byte)0x84;

    private byte nodeId;
    private byte state;
    private NodeInfo nodeInfo;

    public ApplicationUpdate() {
        super(Type.REQUEST, ID, null);
    }

    public ApplicationUpdate(byte[] data) {
        super(data);
        state = data[4];
        if (state == UPDATE_STATE_NODE_INFO_RECEIVED) {
            this.nodeId = data[5];
            nodeInfo = new NodeInfo(data, 6);
        }
    }

    public byte getNodeId() {
        return nodeId;
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
