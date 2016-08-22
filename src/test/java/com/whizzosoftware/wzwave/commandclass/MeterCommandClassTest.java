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
import static org.junit.Assert.*;

public class MeterCommandClassTest {
    @Test
    public void testMeterGetv1() {
        DataFrame frame = MeterCommandClass.createGetv1((byte)3);
        byte[] b = frame.getBytes();
        assertEquals(11, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x09, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x03, b[4]); // node ID
        assertEquals(0x02, b[5]); // command length
        assertEquals(0x32, b[6]); // command ID (Meter)
        assertEquals(0x01, b[7]); // METER_GET
        assertEquals(0x05, b[8]); // TX options
    }

    @Test
    public void testMeterGetv2() {
        DataFrame frame = MeterCommandClass.createGetv2((byte)4, MeterCommandClass.SCALE_ELECTRIC_W);
        byte[] b = frame.getBytes();
        assertEquals(12, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x0A, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x04, b[4]); // node ID
        assertEquals(0x03, b[5]); // command length
        assertEquals(0x32, b[6]); // command ID (Meter)
        assertEquals(0x01, b[7]); // METER_GET
        assertEquals(0x10, b[8]); // Scale (Watts)
        assertEquals(0x05, b[9]); // TX options
    }

    @Test
    public void testMeterReportv2Electric() {
        byte[] ccb = {0x32, 0x02, 0x21, 0x64, 0x00, 0x00, 0x00, 0x0c, 0x00, (byte)0x82, 0x00, 0x00, 0x00, 0x02, (byte)0xe4};
        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccb, 0);

        assertEquals(MeterCommandClass.MeterType.Electric, cc.getMeterType());
        assertEquals((Integer)130, cc.getDelta());
        assertEquals((Double)0.012, cc.getCurrentValue());
        assertEquals((Double)0.002, cc.getPreviousValue());
    }

    @Test
    public void testMeterReportv2Electric2() {
        byte[] ccb = {0x32, 0x02, 0x21, 0x74, 0x00, (byte)0xB8, 0x1A, (byte)0x12, 0x02, 0x58, 0x00, (byte)0xB8, 0x08, 0x06};
        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccb, 0);

        assertEquals(MeterCommandClass.MeterType.Electric, cc.getMeterType());
        assertEquals(600, (int)cc.getDelta());
        assertEquals(12065.298, cc.getCurrentValue(), 3);
        assertEquals(12060.678, cc.getPreviousValue(), 3);
    }

    @Test
    public void testMeterReportv2Electric_FibaroWallPlug() {
        byte[] ccb = {0x32, 0x02, 0x01, 0x44, 0x00, 0x00, 0x00, 0x03};
        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccb, 0);

        assertEquals(MeterCommandClass.MeterType.Electric, cc.getMeterType());
        assertEquals((Double)0.03, cc.getCurrentValue());
        assertNull(cc.getPreviousValue());
        assertNull(cc.getDelta());
    }

}
