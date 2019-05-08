/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ALARM_GET = 0x04;
    public static final byte ALARM_REPORT = 0x05;

    public static final byte ID = (byte)0x71;

    private byte type;
    private byte level;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_ALARM";
    }

    public byte getType() {
        return type;
    }

    public byte getLevel() {
        return level;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == ALARM_REPORT) {
            type = ccb[startIndex+2];
            level = ccb[startIndex+3];
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        context.sendDataFrame(createGet(nodeId));
        return 1;
    }

    public DataFrame createGet(byte nodeId) {
        return createSendDataFrame("ALARM_GET", nodeId, new byte[] {AlarmCommandClass.ID, ALARM_GET}, true);
    }

    @Override
    public String toString() {
        return "AlarmCommandClass{" +
                "version=" + getVersion() +
                ", type=" + type +
                ", level=" + level +
                '}';
    }
}
