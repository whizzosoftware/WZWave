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
        MeterCommandClass mcc = new MeterCommandClass();
        DataFrame frame = mcc.createGet((byte) 3, null);
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
        MeterCommandClass mcc = new MeterCommandClass();
        mcc.setVersion(2);
        DataFrame frame = mcc.createGet((byte) 4, MeterCommandClass.Scale.Watts);
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
        byte[] ccb = {0x32, 0x02, 0x21, 0x64, 0x00, 0x00, 0x00, 0x0c, 0x00, (byte) 0x82, 0x00, 0x00, 0x00, 0x02, (byte) 0xe4};
        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccb, 0);

        MeterCommandClass.MeterReadingValue val = cc.getLastValue(MeterCommandClass.Scale.Watts);
        assertNull(val);

        val = cc.getLastValue(MeterCommandClass.Scale.KilowattHours);
        assertNotNull(val);
        assertEquals((Integer) 130, val.getDelta());
        assertEquals((Double) 0.012, val.getCurrentValue());
        assertEquals((Double) 0.002, val.getPreviousValue());
    }

    @Test
    public void testMeterReportv2Electric2() {
        byte[] ccb = {0x32, 0x02, 0x21, 0x74, 0x00, (byte) 0xB8, 0x1A, (byte) 0x12, 0x02, 0x58, 0x00, (byte) 0xB8, 0x08, 0x06};
        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccb, 0);

        MeterCommandClass.MeterReadingValue val = cc.getLastValue(MeterCommandClass.Scale.Watts);
        assertNotNull(val);
        assertEquals(600, (int) val.getDelta());
        assertEquals(12065.298, val.getCurrentValue(), 3);
        assertEquals(12060.678, val.getPreviousValue(), 3);
    }

    @Test
    public void testMeterReportv2Electric_FibaroWallPlug() {
        byte[] ccb = {0x32, 0x02, 0x01, 0x44, 0x00, 0x00, 0x00, 0x03};
        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccb, 0);

        MeterCommandClass.MeterReadingValue val = cc.getLastValue(MeterCommandClass.Scale.KilowattHours);
        assertNotNull(val);
        assertEquals((Double) 0.03, val.getCurrentValue());
        assertNull(val.getPreviousValue());
        assertNull(val.getDelta());
    }


    @Test
    public void testMeterReportElectric_Pan11() {

        byte[] ccbWattsFirstReading = {0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x00, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE5}; //3.4
        byte[] ccbWattsSecondReading = {0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE5}; //3.1
        byte[] ccbKiloWattsFirstReading = {0x32, 0x02, 0x21, 0x44, 0x00, 0x00, 0x0A, (byte) 0xA6, 0x0E, 0x10, 0x00, 0x00, 0x0A, (byte) 0xA5, (byte) 0xA9}; // 27.26

        MeterCommandClass cc = new MeterCommandClass();
        cc.setVersion(2);
        cc.onApplicationCommand(null, ccbWattsFirstReading, 0);

        MeterCommandClass.MeterReadingValue val = cc.getLastValue(MeterCommandClass.Scale.KilowattHours);
        assertNull(val); // We did not yet get a Kilowatt value

        cc.onApplicationCommand(null, ccbKiloWattsFirstReading, 0);

        val = cc.getLastValue(MeterCommandClass.Scale.Watts);
        assertNotNull(val);
        assertEquals((Double) 3.4, val.getCurrentValue());
        assertEquals((Double) 0.0, val.getPreviousValue());
        assertEquals((int) 0, (int) val.getDelta());

        cc.onApplicationCommand(null, ccbWattsSecondReading, 0);
        val = cc.getLastValue(MeterCommandClass.Scale.Watts);
        assertNotNull(val);
        assertEquals((Double) 3.1, val.getCurrentValue());
        assertEquals((Double) 0.0, val.getPreviousValue());
        assertEquals((int) 0, (int) val.getDelta());

        val = cc.getLastValue(MeterCommandClass.Scale.KilowattHours);
        assertNotNull(val);
        assertEquals((Double) 27.26, val.getCurrentValue());
        assertEquals((Double) 27.25, val.getPreviousValue());
        assertEquals((int) 3600, (int) val.getDelta());

    }

}
