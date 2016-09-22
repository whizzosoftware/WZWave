/*
 ********************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.channel.ZWaveChannelContext;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.RequestNodeInfoTransaction;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.buffer.ByteBuf;

/**
 * Requests information about a particular node.
 *
 * @author Dan Noguerol
 */
public class RequestNodeInfo extends DataFrame {
    public static final byte ID = 0x60;

    public static final byte UPDATE_STATE_NODE_INFO_REQ_FAILED = 0x00;
    public static final byte UPDATE_STATE_NODE_INFO_RECEIVED = 0x01;

    private Byte nodeId;
    private Byte retVal;

    public RequestNodeInfo(byte nodeId) {
        super(DataFrameType.REQUEST, ID, new byte[]{nodeId});
        this.nodeId = nodeId;
    }

    public RequestNodeInfo(ByteBuf buffer) {
        super(buffer);
        retVal = buffer.readByte();
    }

    public Byte getNodeId() {
        return nodeId;
    }

    public Byte getRetVal() {
        return retVal;
    }

    public Boolean wasSuccessfullySent() {
        return (retVal != null && retVal == (byte)0x01);
    }

    @Override
    public DataFrameTransaction createTransaction(ZWaveChannelContext ctx, boolean listeningNode) {
        return new RequestNodeInfoTransaction(ctx, this, listeningNode);
    }

    public String toString() {
        if (nodeId != null) {
            return "REQUEST_NODE_INFO(" + ByteUtil.createString(nodeId) + ")";
        } else {
            switch (retVal) {
                case UPDATE_STATE_NODE_INFO_REQ_FAILED:
                    return "REQUEST_NODE_INFO[failed]";
                case UPDATE_STATE_NODE_INFO_RECEIVED:
                    return "REQUEST_NODE_INFO[success]";
                default:
                    return "REQUEST_NODE_INFO: " + retVal;
            }
        }
    }
}
