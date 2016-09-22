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

import java.util.ArrayList;
import java.util.List;

/**
 * Multilevel Sensor Command Class
 *
 * @author Dan Noguerol
 */
public class MultilevelSensorCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte SENSOR_MULTILEVEL_GET = 0x04;
    private static final byte SENSOR_MULTILEVEL_REPORT = 0x05;

    public static final byte ID = 0x31;

    private Type type;
    private Scale scale;
    private List<Double> values = new ArrayList<>();

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_SENSOR_MULTILEVEL";
    }

    public Type getType() {
        return type;
    }

    public Scale getScale() {
        return scale;
    }

    public List<Double> getValues() {
        return values;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        switch (getVersion()) {
            case 1:
            case 2:
            case 3:
            case 4:
                if (ccb[startIndex+1] == SENSOR_MULTILEVEL_REPORT) {
                    logger.trace("Received meter report: {}", ByteUtil.createString(ccb, ccb.length));
                    setTypeAndScale(ccb[startIndex+2], (ccb[startIndex + 3] >> 3) & 0x03);
                    int precision = (ccb[startIndex + 3] >> 5) & 0x07;
                    int size = ccb[startIndex + 3] & 0x07;
                    logger.trace("{} meter precision: {}, size: {}, scale: {}", type, precision, size, scale);
                    values.clear();
                    for (int i=0; i < (ccb.length - (startIndex + 4)) / size; i++) {
                        values.add(i, ByteUtil.parseValue(ccb, (startIndex + 4) * (i+1), size, precision));
                    }
                    logger.trace("Current values are {}", values);
                } else {
                    logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
                }
                break;
            default:
                logger.error("Multilevel Sensor Command Class > 4 not currently supported");
                break;
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        context.sendDataFrame(createGet(nodeId));
        return 1;
    }

    public DataFrame createGet(byte nodeId) {
        return createSendDataFrame("SENSOR_MULTILEVEL_GET", nodeId, new byte[] {MultilevelSensorCommandClass.ID, SENSOR_MULTILEVEL_GET}, true);
    }

    private void setTypeAndScale(byte t, int s) {
        switch (t) {
            case 0x00:
                type = Type.Reserved;
                scale = Scale.Reserved;
                break;
            case 0x01:
                type = Type.AirTemperature;
                switch (s) {
                    case 0x00:
                        scale = Scale.Celsius;
                        break;
                    case 0x01:
                        scale = Scale.Fahrenheit;
                        break;
                    default:
                        scale = Scale.Reserved;
                        break;
                }
                break;
            case 0x02:
                type = Type.GeneralPurpose;
                switch (s) {
                    case 0x00:
                        scale = Scale.PercentageValue;
                        break;
                    case 0x01:
                        scale = Scale.DimensionlessValue;
                        break;
                    default:
                        scale = Scale.Reserved;
                        break;
                }
                break;
            case 0x03:
                type = Type.Luminance;
                switch (s) {
                    case 0x00:
                        scale = Scale.PercentageValue;
                        break;
                    case 0x01:
                        scale = Scale.Lux;
                        break;
                    default:
                        scale = Scale.Reserved;
                        break;
                }
                break;
            case 0x04:
                type = Type.Power;
                switch (s) {
                    case 0x00:
                        scale = Scale.Watt;
                        break;
                    case 0x01:
                        scale = Scale.BtuPerHour;
                        break;
                    default:
                        scale = Scale.Reserved;
                        break;
                }
                break;
            case 0x05:
                type = Type.Humidity;
                switch (s) {
                    case 0x00:
                        scale = Scale.PercentageValue;
                        break;
                    case 0x01:
                        scale = Scale.AbsoluteHumidity;
                        break;
                    default:
                        scale = Scale.Reserved;
                        break;
                }
                break;
        }
    }

    public enum Type {
        AirTemperature,
        GeneralPurpose,
        Humidity,
        Luminance,
        Power,
        Reserved
    }

    public enum Scale {
        AbsoluteHumidity,
        BtuPerHour,
        Celsius,
        DimensionlessValue,
        Fahrenheit,
        Lux,
        PercentageValue,
        Reserved,
        Watt
    }

    @Override
    public String toString() {
        return "MultilevelSensorCommandClass{" +
                "type=" + type +
                ", scale=" + scale +
                ", values=" + values +
                '}';
    }
}
