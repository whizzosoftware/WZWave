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

/**
 * Alarm Sensor Command Class.
 *
 * @author Dan Noguerol
 */
public class AlarmSensorCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte SENSOR_ALARM_GET = 0x01;
    public static final byte SENSOR_ALARM_REPORT = 0x02;

    public static final byte ID = (byte)0x9C;

    private Type type;
    private byte level;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_SENSOR_ALARM";
    }

    public Type getType() {
        return type;
    }

    public byte getLevel() {
        return level;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == SENSOR_ALARM_REPORT) {
            type = Type.convert(ccb[startIndex+2]);
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
        return createSendDataFrame("SENSOR_ALARM_GET", nodeId, new byte[] {AlarmSensorCommandClass.ID, SENSOR_ALARM_GET}, true);
    }

    public enum Type {
        GENERAL,
        SMOKE,
        CARBON_MONOXIDE,
        CARBON_DIOXIDE,
        HEAT,
        FLOOD;

        public static Type convert(byte b) {
            return Type.values()[b];
        }
    }

    @Override
    public String toString() {
        return "AlarmSensorCommandClass{" +
                "version=" + getVersion() +
                ", type=" + type +
                ", level=" + level +
                '}';
    }
}
