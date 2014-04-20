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
 * Message for retrieving version information.
 *
 * @author Dan Noguerol
 */
public class Version extends DataFrame {
    public static final byte ID = 0x15;

    private String libraryVersion;
    private byte libraryType;

    public Version() {
        super(Type.REQUEST, ID, null);
    }

    public Version(byte[] bytes) {
        super(bytes);
        libraryVersion = new String(bytes, 4, 12);
        libraryType = bytes[bytes.length - 2];
    }

    public String getLibraryVersion() {
        return libraryVersion;
    }

    public byte getLibraryType() {
        return libraryType;
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return new RequestResponseTransaction(this, startTime);
    }
}
