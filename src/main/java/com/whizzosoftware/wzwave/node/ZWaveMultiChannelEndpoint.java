/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

/**
 * Class that represents a multi-channel node endpoint. Multi-instance/Multi-channel Z-Wave devices can have multiple
 * endpoints which act like "child nodes" with their own identify and command classes.
 *
 * @author Dan Noguerol
 */
public class ZWaveMultiChannelEndpoint extends ZWaveEndpoint {
    public static final byte ALARM_SENSOR = (byte)0xA1;
    public static final byte AV_CONTROL_POINT = 0x03;
    public static final byte BINARY_SENSOR = 0x20;
    public static final byte BINARY_SWITCH = 0x10;
    public static final byte DISPLAY = 0x04;
    public static final byte ENERGY_CONTROL = 0x40;
    public static final byte METER = 0x31;
    public static final byte MULTI_LEVEL_SENSOR = 0x21;
    public static final byte MULTI_LEVEL_SWITCH = 0x11;
    public static final byte PULSE_METER = 0x30;
    public static final byte REMOTE_SWITCH = 0x12;
    public static final byte THERMOSTAT = 0x08;
    public static final byte TOGGLE_SWITCH = 0x13;
    public static final byte VENTILATION = 0x16;

    private byte number;

    public ZWaveMultiChannelEndpoint(byte nodeId, byte number, byte genericDeviceClass, byte specificDeviceClass) {
        super(nodeId, genericDeviceClass, specificDeviceClass);
        this.number = number;
    }

    public byte getNumber() {
        return number;
    }
}
