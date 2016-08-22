/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meter command class
 *
 * @author Dan Noguerol
 */
public class MeterCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = 0x32;

    public static final byte SCALE_ELECTRIC_KWH = 0x00;
    public static final byte SCALE_ELECTRIC_KVAH = 0x01;
    public static final byte SCALE_ELECTRIC_W = 0x02;
    public static final byte SCALE_ELECTRIC_PULSES = 0x03;
    public static final byte SCALE_GAS_CUBIC_M = 0x00;
    public static final byte SCALE_GAS_CUBIC_FT = 0x01;
    public static final byte SCALE_GAS_PULSES = 0x03;
    public static final byte SCALE_WATER_CUBIC_M = 0x00;
    public static final byte SCALE_WATER_CUBIC_FT = 0x01;
    public static final byte SCALE_WATER_US_GAL = 0x02;
    public static final byte SCALE_WATER_PULSES = 0x03;

    private static final byte METER_GET = 0x01;
    private static final byte METER_REPORT = 0x02;
    private static final byte METER_SUPPORTED_GET = 0x03;
    private static final byte METER_SUPPORTED_REPORT = 0x04;
    private static final byte METER_RESET = 0x05;

    private MeterType type;
    private Double currentValue;
    private Double previousValue;
    private Integer delta;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_METER";
    }

    @Override
    public int getMaxSupportedVersion() {
        return 2;
    }

    public MeterType getMeterType() {
        return type;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public Double getPreviousValue() {
        return previousValue;
    }

    public Integer getDelta() {
        return delta;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex + 1] == METER_REPORT) {
            logger.trace("Received meter report: {}", ByteUtil.createString(ccb, ccb.length));
            parseMeterReport(ccb, startIndex, getVersion());
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        if (getVersion() == 1) {
            context.sendDataFrame(createGetv1(nodeId));
        } else {
            context.sendDataFrame(createGetv2(nodeId, (byte) 0x00));
        }
        return 1;
    }

    private void parseMeterReport(byte[] ccb, int startIndex, int version) {
        // read meter type
        int meterType;
        if (version > 1) {
            meterType = ccb[startIndex + 2] & 0x1F;
        } else {
            meterType = ccb[startIndex + 2];
        }
        switch (meterType) {
            case 1:
                type = MeterType.Electric;
                break;
            case 2:
                type = MeterType.Gas;
                break;
            case 3:
                type = MeterType.Water;
                break;
            default:
                logger.warn("Found unknown meter type: {}", meterType);
                type = MeterType.Unknown;
                break;
        }

        // read precision, scale and size
        int precision = (ccb[startIndex + 3] >> 5) & 0x07;
        int scale = (ccb[startIndex + 3] >> 3) & 0x03;
        int size = ccb[startIndex + 3] & 0x07;
        logger.trace("{} meter precision: {}, size: {}, scale: {}", type, precision, size, scale);

        // determine current value
        currentValue = ByteUtil.parseValue(ccb, startIndex + 4, size, precision);
        logger.trace("Current value is {}", currentValue);

        if (version == 2 && ccb.length >= (startIndex + 6 + size + size)) {
            // read previous value
            previousValue = ByteUtil.parseValue(ccb, startIndex + 6 + size, size, precision);
            // read delta
            delta = ((ccb[startIndex + size + 4] << 8) & 0xFF00) | (ccb[startIndex + size + 5] & 0xFF);
            logger.trace("Previous value was {} received {} seconds ago", previousValue, delta);
        }
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("METER_GET", nodeId, new byte[]{MeterCommandClass.ID, METER_GET}, true);
    }

    static public DataFrame createGetv2(byte nodeId, byte scale) {
        byte b = (byte) ((scale << 3) & 0x18);
        return createSendDataFrame("METER_GET", nodeId, new byte[]{MeterCommandClass.ID, METER_GET, b}, true);
    }

    public enum MeterType {
        Unknown,
        Electric,
        Gas,
        Water
    }
}
