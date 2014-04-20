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

    public static final byte SCALE_KWH = 0x00;
    public static final byte SCALE_KVAH = 0x01;
    public static final byte SCALE_W = 0x02;
    public static final byte SCALE_PULSES = 0x03;
    public static final byte SCALE_V = 0x04;
    public static final byte SCALE_A = 0x05;
    public static final byte SCALE_POWER_FACTOR = 0x06;

    private static final byte METER_GET = 0x01;
    private static final byte METER_REPORT = 0x02;

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
    public void onDataFrame(DataFrame m, DataQueue queue) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();

            if (ccb[1] == METER_REPORT) {
                // meter type
                switch (ccb[2] & 0x1f) {
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

                logger.debug("Received {} meter report: {}", type, ByteUtil.createString(ccb, ccb.length));

                // determine precision, scale and size
                int precision = (ccb[3] >> 5) & 0x03;
                int scale = (ccb[3] >> 3) & 0x03; // accumulated (0x00) or instant measured (0x02)
                int size = ccb[3] & 0x07;
                logger.trace("precision: {}, size: {}, scale: {}", precision, size, scale);

                // determine current value
                currentValue = parseValue(ccb, 4, size, precision);
                logger.debug("Current value is {}kWh", currentValue);

                // determine previous value
                previousValue = parseValue(ccb, 6 + size, size, precision);
                delta = ((ccb[size+4] << 8) & 0xFF00) | (ccb[size+5] & 0xFF);
                logger.debug("Previous value was {}kWh received {} seconds ago", previousValue, delta);
            } else {
                logger.warn("Ignoring unsupported message: {}", m);
            }
        } else {
            logger.error("Received unexpected message: {}", m);
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, DataQueue queue) {
        queue.queueDataFrame(createGet(nodeId));
    }

    private double parseValue(byte[] b, int start, int length, int precision) {
        double value = 0.0;
        for (int i=start; i < start+length; i++) {
            int shift = 8 * ((length - (i - start)) - 1);
            value += b[i] << shift;
        }
        return new BigDecimal(value).movePointLeft(precision).doubleValue();
    }

    static public DataFrame createGet(byte nodeId) {
        return createSendDataFrame("METER_GET", nodeId, new byte[] {MeterCommandClass.ID, METER_GET, 0x00}, true);
    }

    public enum MeterType {
        Electric,
        Gas,
        Water
    }
}
