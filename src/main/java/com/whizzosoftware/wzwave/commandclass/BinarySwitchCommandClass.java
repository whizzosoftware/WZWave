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
 * Binary Switch Command Class
 *
 * @author Dan Noguerol
 */
public class BinarySwitchCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte SWITCH_BINARY_SET = 0x01;
    private static final byte SWITCH_BINARY_GET = 0x02;
    private static final byte SWITCH_BINARY_REPORT = 0x03;

    public static final byte ID = 0x25;

    private Boolean isOn;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_SWITCH_BINARY";
    }

    public Boolean isOn() {
        return isOn;
    }

    @Override
    public void onDataFrame(DataFrame m, DataQueue queue) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();
            if (ccb[1] == SWITCH_BINARY_REPORT) {
                if (ccb[2] == 0x00) {
                    isOn = false;
                    logger.debug("Received updated isOn (false)");
                } else if ((ccb[2] >= 0x01 && ccb[2] <= 0x63) || ccb[2] == (byte)0xFF) {
                    isOn = true;
                    logger.debug("Received updated isOn (true)");
                } else {
                    logger.error("Ignoring invalid report value: " + ByteUtil.createString(ccb[2]));
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
        return createSendDataFrame("SWITCH_BINARY_GET", nodeId, new byte[]{BinarySwitchCommandClass.ID, SWITCH_BINARY_GET}, true);
    }

    static public DataFrame createSet(byte nodeId, boolean isOn) {
        return createSendDataFrame("SWITCH_BINARY_SET", nodeId, new byte[]{BinarySwitchCommandClass.ID, SWITCH_BINARY_SET, isOn ? (byte) 0xFF : (byte) 0x00}, false);
    }
}
