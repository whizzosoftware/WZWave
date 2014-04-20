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
        super(Type.REQUEST, ID, null);
    }

    public MemoryGetId(byte[] bytes) {
        super(bytes);

        homeId = (((int)bytes[4]) << 24) | (((int)bytes[5]) << 16) | (((int)bytes[6]) << 8) | ((int)bytes[7]);
        nodeId = bytes[8];
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
