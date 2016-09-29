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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConfigurationCommandClassTest {


    @Test
    public void testConfigurationSetParamToValueByte() {
        ConfigurationCommandClass mcc = new ConfigurationCommandClass();
        DataFrame frame = mcc.createSetParamToValue((byte)3, (byte)0x11, (byte)0xC);
        byte[] b = frame.getBytes();
        assertEquals(14, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x0C, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x03, b[4]); // node ID
        assertEquals(0x05, b[5]); // command length
        assertEquals(0x70, b[6]); // command ID (Configuration)
        assertEquals(0x04, b[7]); // CONFIGURATION_SET
        assertEquals(0x11, b[8]); // Parameter
        assertEquals(0x01, b[9]); // Size
        assertEquals(0x0C, b[10]); // Value
        assertEquals(0x05, b[11]); // TX options
    }

    @Test
    public void testConfigurationSetParamToValueShort() {
        ConfigurationCommandClass mcc = new ConfigurationCommandClass();
        DataFrame frame = mcc.createSetParamToValue((byte)3, (byte)0x11, (short)0x1234);
        byte[] b = frame.getBytes();
        assertEquals(15, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x0D, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x03, b[4]); // node ID
        assertEquals(0x06, b[5]); // command length
        assertEquals(0x70, b[6]); // command ID (Configuration)
        assertEquals(0x04, b[7]); // CONFIGURATION_SET
        assertEquals(0x11, b[8]); // Parameter
        assertEquals(0x02, b[9]); // Size
        assertEquals(0x12, b[10]); // Value
        assertEquals(0x34, b[11]); // Value
        assertEquals(0x05, b[12]); // TX options
    }

    @Test
    public void testConfigurationSetParamToValueint() {
        ConfigurationCommandClass mcc = new ConfigurationCommandClass();
        DataFrame frame = mcc.createSetParamToValue((byte)3, (byte)0x11, (int)0xDEADBEEF);
        byte[] b = frame.getBytes();
        assertEquals(17, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x0F, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x03, b[4]); // node ID
        assertEquals(0x08, b[5]); // command length
        assertEquals(0x70, b[6]); // command ID (Configuration)
        assertEquals(0x04, b[7]); // CONFIGURATION_SET
        assertEquals(0x11, b[8]); // Parameter
        assertEquals(0x03, b[9]); // Size
        assertEquals((byte)0xDE, b[10]); // Value
        assertEquals((byte)0xAD, b[11]); // Value
        assertEquals((byte)0xBE, b[12]); // Value
        assertEquals((byte)0xEF, b[13]); // Value
        assertEquals(0x05, b[14]); // TX options
    }


    @Test
    public void testConfigurationGet() {
        ConfigurationCommandClass mcc = new ConfigurationCommandClass();
        DataFrame frame = mcc.createGet((byte)3, (byte)0x13);
        byte[] b = frame.getBytes();
        assertEquals(12, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x0A, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x03, b[4]); // node ID
        assertEquals(0x03, b[5]); // command length
        assertEquals(0x70, b[6]); // command ID (Configuration)
        assertEquals(0x05, b[7]); // CONFIGURATION_GET
        assertEquals(0x13, b[8]); // Parameter
        assertEquals(0x05, b[9]); // TX options
    }



    @Test
    public void testConfigurationReportParam0x12() {
        byte[] ccb = {0x70, 0x06, 0x12, 0x01, 0x44};
        ConfigurationCommandClass cc = new ConfigurationCommandClass();
        cc.onApplicationCommand(null, ccb, 0);

        assertEquals((Long)0x44L, cc.getLastParameterValue((byte)0x12));
    }

    @Test
    public void testConfigurationGetPreviousValue() {
        ConfigurationCommandClass mcc = new ConfigurationCommandClass();
        DataFrame frame = mcc.createGet((byte) 3, (byte) 0x16);

        assertEquals((Long)null, mcc.getLastParameterValue((byte)0x16));

        byte[] ccb = {0x70, 0x06, 0x16, 0x01, (byte)0xEA};
        mcc.onApplicationCommand(null, ccb, 0);

        assertEquals((Long)null, mcc.getLastParameterValue((byte)0xEA));

    }
}
