/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.SendDataTransaction;
import com.whizzosoftware.wzwave.util.ByteUtil;

/**
 * A data frame used to send arbitrary data between nodes.
 *
 * @author Dan Noguerol
 */
public class SendData extends DataFrame {
    public static final byte ID = 0x13;

    private static byte nextCallbackId;

    private String name;
    private byte nodeId;
    private byte[] sendData;
    private Byte retVal;
    private Byte callbackId;
    private Byte tx;
    private boolean isResponseExpected;

    public SendData(String name, byte nodeId, byte[] data, byte txOptions, boolean isResponseExpected) {
        this(name, nodeId, data, txOptions, ++nextCallbackId, isResponseExpected);
    }

    public SendData(String name, byte nodeId, byte[] data, byte txOptions, byte callbackId, boolean isResponseExpected) {
        super(Type.REQUEST, ID, null);

        this.name = name;
        this.sendData = data;
        this.nodeId = nodeId;
        this.isResponseExpected = isResponseExpected;

        byte b[] = new byte[data.length + 4];
        b[0] = nodeId;
        b[1] = (byte)data.length;
        System.arraycopy(data, 0, b, 2, data.length);
        b[data.length + 2] = txOptions;
        b[data.length + 3] = callbackId;
        setData(b);
    }

    public SendData(byte[] data) {
        super(data);
        if (data.length == 6) {
            this.retVal = data[4];
        } else if (data.length == 7) {
            this.callbackId = data[4];
            this.tx = data[5];
        } else if (data.length > 7) {
            this.nodeId = data[4];
            byte dataLength = data[5];
            this.tx = data[dataLength + 6];
            this.callbackId = data[dataLength + 7];
        }
    }

    public byte getNodeId() {
        return nodeId;
    }

    public boolean hasRetVal() {
        return (retVal != null);
    }

    public Byte getRetVal() {
        return retVal;
    }

    public boolean hasCallbackId() {
        return (callbackId != null);
    }

    public Byte getCallbackId() {
        return callbackId;
    }

    public byte[] getSendData() {
        return sendData;
    }

    public String toString() {
        if (name != null) {
            return "SendData(" + nodeId + ")[" + name + "]," + callbackId;
        } else if (sendData != null) {
            return "SendData(" + nodeId + ")[" + ByteUtil.createString(sendData, sendData.length) + "]," + callbackId;
        } else {
            return "SendData(" + nodeId + ")";
        }
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return new SendDataTransaction(this, startTime, isResponseExpected);
    }
}
