/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.RequestRequestTransaction;
import io.netty.buffer.ByteBuf;

/**
 * Requests that the controller reset to factory defaults.
 *
 * @author Dan Noguerol
 */
public class SetDefault extends DataFrame {
    public static final byte ID = 0x42;

    public SetDefault() {
        super(DataFrameType.REQUEST, ID, null);
    }

    public SetDefault(ByteBuf buffer) {
        super(buffer);
        buffer.readByte(); // funcId
    }

    @Override
    public DataFrameTransaction createTransaction() {
        return new RequestRequestTransaction(this);
    }
}
