/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.ApplicationCommand;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Battery Command Class
 *
 * @author Dan Noguerol
 */
public class BatteryCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte BATTERY_GET = 0x02;
    private static final byte BATTERY_REPORT = 0x03;

    public static final byte ID = (byte)0x80;

    private Byte level;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_BATTERY";
    }

    public Byte getLevel() {
        return level;
    }

    @Override
    public void onDataFrame(DataFrame m, DataQueue queue) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();
            if (ccb[1] == BATTERY_REPORT) {
                if (ccb[2] >= 0x00 && ccb[2] <= 0x64) {
                    level = ccb[2];
                    logger.debug("Received updated level: " + ByteUtil.createString(level));
                } else if (ccb[2] == 0xFF) {
                    logger.debug("Received battery low warning");
                } else {
                    logger.warn("Ignoring invalid report value: " + ByteUtil.createString(ccb[2]));
                }
            } else {
                logger.warn("Ignoring unsupported message: " + m);
            }
        } else {
            logger.error("Received unexpected message: " + m);
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, DataQueue queue) {
        queue.queueDataFrame(createGet(nodeId));
    }

    static public DataFrame createGet(byte nodeId) {
        return createSendDataFrame("BATTERY_GET", nodeId, new byte[]{BatteryCommandClass.ID, BATTERY_GET}, true);
    }
}
