/*******************************************************************************
 * Copyright (c) 2019 Whizzo Software, LLC.
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
 * IndicatorCommand Class
 *
 * @author Per Otterström
 */
public class IndicatorCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte INDICATOR_SET = 0x01;
    public static final byte INDICATOR_GET = 0x02;
    public static final byte INDICATOR_REPORT = 0x03;

    public static final byte ID = (byte)0x87;

    private Boolean isOn;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_INDICATOR";
    }

    public Boolean isOn() {
        return isOn;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == INDICATOR_REPORT) {
            if (ccb[startIndex+2] == 0x00) {
                isOn = false;
                logger.debug("Received updated isOn (false)");
            } else if ((ccb[startIndex+2] >= 0x01 && ccb[startIndex+2] <= 0x63) || ccb[startIndex+2] == (byte)0xFF) {
                isOn = true;
                logger.debug("Received updated isOn (true)");
            } else {
                logger.error("Ignoring invalid report value: {}", ByteUtil.createString(ccb[startIndex+2]));
            }
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
        return createSendDataFrame("INDICATOR_GET", nodeId, new byte[]{IndicatorCommandClass.ID, INDICATOR_GET}, true);
    }

    public DataFrame createSet(byte nodeId, boolean isOn) {
        return createSendDataFrame("INDICATOR_SET", nodeId, new byte[]{IndicatorCommandClass.ID, INDICATOR_SET, isOn ? (byte) 0xFF : (byte) 0x00}, false);
    }

    @Override
    public String toString() {
        return "IndicatorCommandClass{" +
                "version=" + getVersion() +
                ", isOn=" + isOn +
                '}';
    }
}
