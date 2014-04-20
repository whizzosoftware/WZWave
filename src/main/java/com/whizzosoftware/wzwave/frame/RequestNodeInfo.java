/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.RequestNodeInfoTransaction;

/**
 * Requests information about a particular node.
 *
 * @author Dan Noguerol
 */
public class RequestNodeInfo extends DataFrame {
    public static final byte ID = 0x60;

    private Byte nodeId;
    private Byte retVal;

    public RequestNodeInfo(byte nodeId) {
        super(Type.REQUEST, ID, new byte[] {nodeId});

        this.nodeId = nodeId;
    }

    public RequestNodeInfo(byte[] data) {
        super(data);
        retVal = data[4];
    }

    public Byte getNodeId() {
        return nodeId;
    }

    public Byte getRetVal() {
        return retVal;
    }

    public Boolean wasSuccessfullySent() {
        return (retVal != null && retVal == (byte)0x01);
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return new RequestNodeInfoTransaction(this, startTime);
    }
}
