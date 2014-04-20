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

import java.util.ArrayList;
import java.util.List;

/**
 * An init data data frame. Retrieves a list of known node IDs from the controller.
 *
 * @author Dan Noguerol
 */
public class InitData extends DataFrame {
    public static final byte ID = 0x02;

    private static final int NODE_BITMASK_SIZE = 29;

    private List<Byte> nodes = new ArrayList<Byte>();

    public InitData() {
        super(Type.REQUEST, ID, null);
    }

    public InitData(byte[] bytes) {
        super(bytes);

        if (bytes[6] == NODE_BITMASK_SIZE) {
            // 29 bytes * 8 bits == 232 == the number of possible nodes in the network
            for (int i=0; i < NODE_BITMASK_SIZE; i++) {
                for (int j=0; j < 8; j++) {
                    byte nodeId = (byte)((i*8)+j+1);
                    if ((bytes[i+7] & (0x01 << j)) > 0) {
                        nodes.add(nodeId);
                    }
                }
            }
        }
    }

    public List<Byte> getNodes() {
        return nodes;
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return new RequestResponseTransaction(this, startTime);
    }
}
