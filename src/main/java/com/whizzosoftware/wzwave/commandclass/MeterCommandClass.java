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

    private static final byte METER_GET = 0x01;
    private static final byte METER_REPORT = 0x02;

    private Type type;
    private Scale scale;
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

    public Type getType() {
        return type;
    }

    public Scale getScale() {
        return scale;
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
        context.sendDataFrame(createGet(nodeId, scale));
        return 1;
    }

    private void parseMeterReport(byte[] ccb, int startIndex, int version) {
        // read meter type & scale
        int t;
        if (version > 1) {
            t = ccb[startIndex + 2] & 0x1F;
        } else {
            t = ccb[startIndex + 2];
        }
        int s = (ccb[startIndex + 3] >> 3) & 0x03;

        switch (t) {
            case 1:
                type = Type.Electric;
                switch (s) {
                    case 0:
                        scale = Scale.KilowattHours;
                        break;
                    case 1:
                        scale = Scale.KilovoltAmpereHours;
                        break;
                    case 2:
                        scale = Scale.Watts;
                        break;
                    case 3:
                        scale = Scale.PulseCount;
                        break;
                    default:
                        scale = Scale.Reserved;
                        break;
                }
                break;
            case 2:
                type = Type.Gas;
                switch (s) {
                    case 0:
                        scale = Scale.CubicMeters;
                        break;
                    case 1:
                        scale = Scale.CubicFeet;
                        break;
                    case 3:
                        scale = Scale.PulseCount;
                        break;
                    default:
                        scale = Scale.Reserved;
                        break;
                }
                break;
            case 3:
                type = Type.Water;
                switch (s) {
                    case 0:
                        scale = Scale.CubicMeters;
                        break;
                    case 1:
                        scale = Scale.CubicFeet;
                        break;
                    case 2:
                        scale = Scale.USGallons;
                        break;
                    case 3:
                        scale = Scale.PulseCount;
                        break;
                    default:
                        scale = Scale.Reserved;
                }
                break;
            default:
                logger.warn("Found unknown meter type: {}", t);
                type = Type.Unknown;
                break;
        }

        // read precision, scale and size
        int precision = (ccb[startIndex + 3] >> 5) & 0x07;
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

    /**
     * Create a Get data frame.
     *
     * @param nodeId the target node ID
     * @param s the scale (null for version 1)
     *
     * @return a DataFrame instance
     */
    public DataFrame createGet(byte nodeId, Scale s) {
        switch (getVersion()) {
            case 1:
                return createSendDataFrame("METER_GET", nodeId, new byte[]{MeterCommandClass.ID, METER_GET}, true);
            default: {
                byte scale = scaleToByte(s);
                byte b = (byte) ((scale << 3) & 0x18);
                return createSendDataFrame("METER_GET", nodeId, new byte[]{MeterCommandClass.ID, METER_GET, b}, true);
            }

        }
    }

    private byte scaleToByte(Scale s) {
        if (s != null) {
            switch (s) {
                case KilowattHours:
                case CubicMeters:
                    return 0;
                case KilovoltAmpereHours:
                case CubicFeet:
                    return 1;
                case Watts:
                case USGallons:
                case Reserved:
                    return 2;
                case PulseCount:
                    return 3;
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    public enum Type {
        Unknown,
        Electric,
        Gas,
        Water
    }

    public enum Scale {
        KilowattHours,
        KilovoltAmpereHours,
        Watts,
        PulseCount,
        CubicMeters,
        CubicFeet,
        USGallons,
        Reserved
    }

    @Override
    public String toString() {
        return "MeterCommandClass{" +
                "type=" + type +
                ", scale=" + scale +
                ", currentValue=" + currentValue +
                ", previousValue=" + previousValue +
                ", delta=" + delta +
                '}';
    }
}
