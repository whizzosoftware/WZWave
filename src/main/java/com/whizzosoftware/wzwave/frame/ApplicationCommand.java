/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.buffer.ByteBuf;

/**
 * An application command data frame.
 *
 * @author Dan Noguerol
 */
public class ApplicationCommand extends DataFrame {
    public static final byte ID = 0x04;

    private byte nodeId;
    private byte status;
    private byte[] commandClassBytes;

    public ApplicationCommand(DataFrameType type, byte rxStatus, byte sourceNode, byte[] commandClassBytes) {
        super(type, ID, null);

        byte[] b = new byte[commandClassBytes.length + 2];
        b[0] = rxStatus;
        b[1] = sourceNode;
        System.arraycopy(commandClassBytes, 0, b, 2, commandClassBytes.length);
        setData(b);

        this.nodeId = sourceNode;
        this.status = rxStatus;
        this.commandClassBytes = commandClassBytes;
    }

    public ApplicationCommand(ByteBuf buffer) {
        super(buffer);

        this.status = buffer.readByte();
        this.nodeId = buffer.readByte();

        byte cmdLength = buffer.readByte();
        commandClassBytes = buffer.readBytes(cmdLength).array();
    }

    public byte getNodeId() {
        return nodeId;
    }

    public byte getStatus() {
        return status;
    }

    public byte getCommandClassId() {
        return commandClassBytes[0];
    }

    public byte[] getCommandClassBytes() {
        return commandClassBytes;
    }

    public String toString() {
        return "ApplicationCommand[" + getNodeId() + "]: " + ByteUtil.createString(commandClassBytes, commandClassBytes.length - 1);
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return null;
    }
}