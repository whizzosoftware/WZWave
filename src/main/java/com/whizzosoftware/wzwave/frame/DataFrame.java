/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.buffer.ByteBuf;

/**
 * Abstract base class for all data frames.
 *
 * @author Dan Noguerol
 */
abstract public class DataFrame extends Frame {
    public static final byte START_OF_FRAME = 0x01;

    protected int dataFrameLength;
    private DataFrameType type;
    private byte commandId;
    private byte[] data;
    private int sendCount;
    private String transactionId;

    /**
     * Constructor
     *
     * @param buffer the readable byte buffer
     */
    public DataFrame(ByteBuf buffer) {
        if (buffer.readByte() != START_OF_FRAME) {
            throw new RuntimeException("Data frame parsing error: no SOF");
        }
        this.dataFrameLength = buffer.readByte();
        if (buffer.readableBytes() < dataFrameLength) {
            throw new RuntimeException("Data framing length error");
        }
        this.type = (buffer.readByte() == 0 ? DataFrameType.REQUEST : DataFrameType.RESPONSE);
        this.commandId = buffer.readByte();
    }

    /**
     * Constructor
     *
     * @param type the data frame type (TYPE_REQUEST or TYPE_RESPONSE)
     * @param commandId the command ID
     * @param data the data (excluding the SOF, frame type, and commandId)
     */
    public DataFrame(DataFrameType type, byte commandId, byte[] data) {
        this.type = type;
        this.commandId = commandId;
        this.data = data;
    }

    public DataFrameType getType() {
        return type;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void incremenentSendCount() {
        sendCount++;
    }

    public void decrementSendCount() {
        sendCount--;
    }

    public boolean hasTransactionId() {
        return (transactionId != null);
    }

    public String getTransactionId() {
        return transactionId;
    }

    private void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    protected void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getBytes() {
        int dataLen = 0;
        if (data != null) {
            dataLen += data.length;
        }

        byte[] bytes = new byte[dataLen + 5];

        bytes[0] = 0x01;
        bytes[1] = (byte)(dataLen + 3);
        bytes[2] = (byte)type.ordinal();
        bytes[3] = commandId;

        if (data != null) {
            System.arraycopy(data, 0, bytes, 4, data.length);
        }

        byte checksum = (byte)0xff;
        for (int i=1; i < bytes.length - 1; i++) {
            checksum ^= bytes[i];
        }
        bytes[dataLen+4] = checksum;

        return bytes;
    }

    public DataFrameTransaction createWrapperTransaction() {
        DataFrameTransaction t = createTransaction();
        setTransactionId(t.getId());
        return t;
    }

    abstract public DataFrameTransaction createTransaction();
}
