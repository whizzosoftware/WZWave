/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;

public class NoOperationCommandClass extends CommandClass {
    public static final byte ID = 0x00;

    public DataFrame createGet(byte nodeId) {
        return createSendDataFrame("COMMAND_CLASS_NO_OPERATION", nodeId, new byte[] {NoOperationCommandClass.ID}, false);
    }

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_NO_OPERATION";
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        return 0;
    }
}
