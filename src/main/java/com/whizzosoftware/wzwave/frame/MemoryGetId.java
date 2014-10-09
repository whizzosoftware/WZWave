/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.RequestResponseTransaction;
import io.netty.buffer.ByteBuf;

/**
 * Retrieves the home ID and node ID from a controller.
 *
 * @author Dan Noguerol
 */
public class MemoryGetId extends DataFrame {
    public static final byte ID = 0x20;

    private int homeId;
    private byte nodeId;

    public MemoryGetId() {
        super(DataFrameType.REQUEST, ID, null);
    }

    public MemoryGetId(ByteBuf buffer) {
        super(buffer);

        homeId = (((int)buffer.readByte()) << 24) | (((int)buffer.readByte()) << 16) | (((int)buffer.readByte()) << 8) | ((int)buffer.readByte());
        nodeId = buffer.readByte();
    }

    public int getHomeId() {
        return homeId;
    }

    public byte getNodeId() {
        return nodeId;
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return new RequestResponseTransaction(this, startTime);
    }
}
