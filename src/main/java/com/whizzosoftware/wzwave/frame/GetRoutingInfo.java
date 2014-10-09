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
 * A get routing info data frame.
 *
 * @author Dan Noguerol
 */
public class GetRoutingInfo extends DataFrame {
    public static final byte ID = (byte)0x80;

    private byte[] nodeMask;

    public GetRoutingInfo() {
        super(DataFrameType.REQUEST, ID, null);
    }

    public GetRoutingInfo(ByteBuf buffer) {
        super(buffer);
        nodeMask = buffer.readBytes(29).array();
    }

    public byte[] getNodeMask() {
        return nodeMask;
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return null;
    }
}
