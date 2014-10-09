/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
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

/**
 * Multilevel Switch Command Class
 *
 * @author Dan Noguerol
 */
public class MultilevelSwitchCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte SWITCH_MULTILEVEL_SET = 0x01;
    private static final byte SWITCH_MULTILEVEL_GET = 0x02;
    private static final byte SWITCH_MULTILEVEL_REPORT = 0x03;

    public static final byte ID = 0x26;

    private Byte level;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_SWITCH_MULTILEVEL";
    }

    public Byte getLevel() {
        return level;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == SWITCH_MULTILEVEL_REPORT) {
            if ((ccb[startIndex+2] >= 0x00 && ccb[startIndex+2] <= 0x63) || ccb[startIndex+2] == 0xFF) {
                level = ccb[startIndex+2];
                logger.debug("Received updated level: {}", ByteUtil.createString(level));
            } else {
                logger.warn("Ignoring invalid report value: {}", ByteUtil.createString(ccb[startIndex+2]));
            }
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        context.sendDataFrame(createGetv1(nodeId));
        return 1;
    }

    static public DataFrame createSetv1(byte nodeId, byte level) {
        return createSendDataFrame("SWITCH_MULTILEVEL_SET", nodeId, new byte[]{MultilevelSwitchCommandClass.ID, SWITCH_MULTILEVEL_SET, level}, false);
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("SWITCH_MULTILEVEL_GET", nodeId, new byte[]{MultilevelSwitchCommandClass.ID, SWITCH_MULTILEVEL_GET}, true);
    }
}
