/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;

/**
 * Abstract base class for all data frames.
 *
 * @author Dan Noguerol
 */
abstract public class DataFrame extends Frame {
    public static final byte START_OF_FRAME = 0x01;

    private Type type;
    private byte commandId;
    private byte[] data;
    private int sendCount;
    private long sendTime;

    /**
     * Constructor
     *
     * @param frame full data frame starting with 0x01 (SOF) and ending with data frame checksum
     */
    public DataFrame(byte[] frame) {
        if (frame[0] != START_OF_FRAME) {
            throw new RuntimeException("Data frame parsing error: no SOF");
        }
        if (frame[1] != frame.length - 2) {
            throw new RuntimeException("Data framing length error");
        }
        this.type = (frame[2] == 0 ? Type.REQUEST : Type.RESPONSE);
        this.commandId = frame[3];
        this.data = new byte[frame.length - 2];
        System.arraycopy(frame, 4, this.data, 0, frame.length - 4);
    }

    /**
     * Constructor
     *
     * @param type the data frame type (TYPE_REQUEST or TYPE_RESPONSE)
     * @param commandId the command ID
     * @param data the data (excluding the SOF, frame type, and commandId)
     */
    public DataFrame(Type type, byte commandId, byte[] data) {
        this.type = type;
        this.commandId = commandId;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public int getSendCount() {
        return sendCount;
    }

    public boolean isSendCountMaxExceeded() {
        return (sendCount > 5);
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
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

    abstract public DataFrameTransaction createTransaction(long startTime);

    public enum Type {
        REQUEST,
        RESPONSE
    }
}
