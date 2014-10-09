/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
        assertEquals(0x01, b[9]); // callback ID
        assertEquals(-45, b[10]); // checksum
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
        byte[] ccb = {0x01, 0x14, 0x00, 0x04, 0x00, 0x11, 0x0E, 0x32, 0x02, 0x21, 0x64, 0x00, 0x00, 0x00, 0x0c, 0x00, (byte)0x82, 0x00, 0x00, 0x00, 0x02, (byte)0xe4};
        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccb, 7);

        assertEquals(MeterCommandClass.MeterType.Electric, cc.getMeterType());
        assertEquals((Integer)130, cc.getDelta());
        assertEquals((Double)0.012, cc.getCurrentValue());
        assertEquals((Double)0.002, cc.getPreviousValue());
    }

}
