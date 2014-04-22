/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.ApplicationCommand;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

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
    public void onDataFrame(DataFrame m, NodeContext context) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();

            if (ccb[1] == METER_REPORT) {
                logger.debug("Received meter report: {}", ByteUtil.createString(ccb, ccb.length));
                parseMeterReport(ccb, getVersion());
            } else {
                logger.warn("Ignoring unsupported message: {}", m);
            }
        } else {
            logger.error("Received unexpected message: {}", m);
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, NodeContext context) {
        if (getVersion() == 1) {
            context.queueDataFrame(createGetv1(nodeId));
        } else {
            context.queueDataFrame(createGetv2(nodeId, (byte)0x00));
        }
    }

    private void parseMeterReport(byte[] ccb, int version) {
        // read meter type
        int meterType = ccb[2];
        if (version == 2) {
            meterType = ccb[2] & 0x1F;
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
        }

        // read precision, scale and size
        int precision = (ccb[3] >> 5) & 0x07;
        int scale = (ccb[3] >> 3) & 0x03;
        int size = ccb[3] & 0x07;
        logger.debug("{} meter precision: {}, size: {}, scale: {}", type, precision, size, scale);

        // determine current value
        currentValue = ByteUtil.parseValue(ccb, 4, size, precision);
        logger.debug("Current value is {}", currentValue);

        if (version == 2) {
            // read previous value
            previousValue = ByteUtil.parseValue(ccb, 6 + size, size, precision);
            // read delta
            delta = ((ccb[size + 4] << 8) & 0xFF00) | (ccb[size + 5] & 0xFF);
            logger.debug("Previous value was {} received {} seconds ago", previousValue, delta);
        }
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("METER_GET", nodeId, new byte[] {MeterCommandClass.ID, METER_GET}, true);
    }

    static public DataFrame createGetv2(byte nodeId, byte scale) {
        byte b = (byte)((scale << 3) & 0x18);
        return createSendDataFrame("METER_GET", nodeId, new byte[] {MeterCommandClass.ID, METER_GET, b}, true);
    }

    public enum MeterType {
        Electric,
        Gas,
        Water
    }
}
