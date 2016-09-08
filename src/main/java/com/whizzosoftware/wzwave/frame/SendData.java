/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.SendDataTransaction;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.buffer.ByteBuf;

/**
 * A data frame used to send arbitrary data between nodes.
 *
 * @author Dan Noguerol
 */
public class SendData extends DataFrame {
    public static final byte ID = 0x13;

    public static final byte TRANSMIT_OPTION_ACK = 0x01;
    public static final byte TRANSMIT_OPTION_LOW_POWER = 0x02;
    public static final byte TRANSMIT_OPTION_AUTO_ROUTE = 0x04;
    public static final byte TRANSMIT_OPTION_NO_ROUTE = 0x10;

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
        super(DataFrameType.REQUEST, ID, null);

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

    public SendData(ByteBuf buffer) {
        super(buffer);
        if (dataFrameLength == 4) {
            this.retVal = buffer.readByte();
        } else if (dataFrameLength == 5) {
            this.callbackId = buffer.readByte();
            this.tx = buffer.readByte();
        } else if (dataFrameLength > 5) {
            this.nodeId = buffer.readByte();
            byte dataLength = buffer.readByte();
            this.sendData = buffer.readBytes(dataLength).array();
            this.tx = buffer.readByte();
            this.callbackId = buffer.readByte();
        }
    }

    public String getName() {
        return name;
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

    public boolean hasTx() {
        return (tx != null);
    }

    public Byte getTx() {
        return tx;
    }

    public byte[] getSendData() {
        return sendData;
    }

    public String toString() {
        if (name != null) {
            return "SendData(" + ByteUtil.createString(getNodeId()) + ")[" + name + "]," + callbackId;
        } else if (sendData != null) {
            return "SendData(" + ByteUtil.createString(getNodeId()) + ")[" + ByteUtil.createString(sendData, sendData.length) + "]," + callbackId;
        } else {
            return "SendData(" + ByteUtil.createString(getNodeId()) + ")";
        }
    }

    @Override
    public DataFrameTransaction createTransaction(boolean listeningNode) {
        return new SendDataTransaction(this, listeningNode, isResponseExpected);
    }
}
