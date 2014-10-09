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
 * Binary Switch Command Class
 *
 * @author Dan Noguerol
 */
public class BinarySensorCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte SENSOR_BINARY_SET = 0x01;
    private static final byte SENSOR_BINARY_GET = 0x02;
    private static final byte SENSOR_BINARY_REPORT = 0x03;

    public static final byte ID = 0x30;

    public Boolean isIdle;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_SENSOR_BINARY";
    }

    public Boolean isIdle() {
        return isIdle;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        // some devices (e.g. Everspring SM103) seem to use SENSOR_BINARY_SET rather than SENSOR_BINARY_REPORT
        // when sending unsolicited updates
        if (ccb[startIndex+1] == SENSOR_BINARY_REPORT || ccb[startIndex+1] == SENSOR_BINARY_SET) {
            if (ccb[startIndex+2] == 0x00) {
                isIdle = true;
                logger.debug("Received updated isIdle (true)");
            } else if (ccb[startIndex+2] == 0xFF || ccb[startIndex+2] == 0x63) {
                isIdle = false;
                logger.debug("Received updated isIdle (false)");
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

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("SENSOR_BINARY_GET", nodeId, new byte[]{BinarySensorCommandClass.ID, SENSOR_BINARY_GET}, true);
    }
}
