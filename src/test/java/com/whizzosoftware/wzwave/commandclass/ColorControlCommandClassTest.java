/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class ColorControlCommandClassTest {
    @Test
    public void testGetAndSetv1() {
        // get warm white
        DataFrame df = ColorControlCommandClass.createGetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_WARM_WHITE);
        byte[] b = df.getBytes();
        assertEquals(12, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0A 0x00 0x13 0x03 0x03 0x33 0x03 0x00 0x05 "));

        // get cold white
        df = ColorControlCommandClass.createGetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_COLD_WHITE);
        b = df.getBytes();
        assertEquals(12, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0A 0x00 0x13 0x03 0x03 0x33 0x03 0x01 0x05 "));

        // get red
        df = ColorControlCommandClass.createGetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_RED);
        b = df.getBytes();
        assertEquals(12, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0A 0x00 0x13 0x03 0x03 0x33 0x03 0x02 0x05 "));

        // get green
        df = ColorControlCommandClass.createGetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_GREEN);
        b = df.getBytes();
        assertEquals(12, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0A 0x00 0x13 0x03 0x03 0x33 0x03 0x03 0x05 "));

        // get blue
        df = ColorControlCommandClass.createGetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_BLUE);
        b = df.getBytes();
        assertEquals(12, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0A 0x00 0x13 0x03 0x03 0x33 0x03 0x04 0x05 "));

        // set warm white 195
        df = ColorControlCommandClass.createSetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_WARM_WHITE, (byte) 195);
        b = df.getBytes();
        assertEquals(14, b.length);
        assertEquals(14, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0C 0x00 0x13 0x03 0x05 0x33 0x05 0x01 0x00 0xC3 0x05 "));

        // set cold white 150
        df = ColorControlCommandClass.createSetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_COLD_WHITE, (byte) 50);
        b = df.getBytes();
        assertEquals(14, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0C 0x00 0x13 0x03 0x05 0x33 0x05 0x01 0x01 0x32 0x05 "));

        // set red 10
        df = ColorControlCommandClass.createSetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_RED, (byte) 10);
        b = df.getBytes();
        assertEquals(14, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0C 0x00 0x13 0x03 0x05 0x33 0x05 0x01 0x02 0x0A 0x05 "));

        // set green 25
        df = ColorControlCommandClass.createSetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_GREEN, (byte) 25);
        b = df.getBytes();
        assertEquals(14, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0C 0x00 0x13 0x03 0x05 0x33 0x05 0x01 0x03 0x19 0x05 "));

        // set blue 195
        df = ColorControlCommandClass.createSetv1((byte) 0x03, ColorControlCommandClass.CAPABILITY_ID_BLUE, (byte) 195);
        b = df.getBytes();
        assertEquals(14, b.length);
        assertTrue(ByteUtil.createString(b, b.length).startsWith("0x01 0x0C 0x00 0x13 0x03 0x05 0x33 0x05 0x01 0x04 0xC3 0x05 "));
    }

    @Test
    public void testOnApplicationCommand() {
        // warm white report 0xFF
        ColorControlCommandClass cc = new ColorControlCommandClass();
        byte[] b = { 0x33, 0x04, 0x00, (byte)0xFF };
        cc.onApplicationCommand(null, b, 0);
        assertEquals(ColorControlCommandClass.CAPABILITY_ID_WARM_WHITE, (byte)cc.getCapabilityId());
        assertEquals((byte)0xFF, (byte)cc.getValue());

        // warm white report 0xFF
        cc = new ColorControlCommandClass();
        b = new byte[] { 0x33, 0x04, 0x01, (byte)0xFF };
        cc.onApplicationCommand(null, b, 0);
        assertEquals(ColorControlCommandClass.CAPABILITY_ID_COLD_WHITE, (byte)cc.getCapabilityId());
        assertEquals((byte)0xFF, (byte)cc.getValue());

        // red report 0xC1
        cc = new ColorControlCommandClass();
        b = new byte[] { 0x33, 0x04, 0x02, (byte)0xC1 };
        cc.onApplicationCommand(null, b, 0);
        assertEquals(ColorControlCommandClass.CAPABILITY_ID_RED, (byte)cc.getCapabilityId());
        assertEquals((byte)0xC1, (byte)cc.getValue());

        // green report 0x02
        cc = new ColorControlCommandClass();
        b = new byte[] { 0x33, 0x04, 0x03, (byte)0x02 };
        cc.onApplicationCommand(null, b, 0);
        assertEquals(ColorControlCommandClass.CAPABILITY_ID_GREEN, (byte)cc.getCapabilityId());
        assertEquals((byte)0x02, (byte)cc.getValue());

        // blue report 0xC1
        cc = new ColorControlCommandClass();
        b = new byte[] { 0x33, 0x04, 0x04, (byte)0xC1 };
        cc.onApplicationCommand(null, b, 0);
        assertEquals(ColorControlCommandClass.CAPABILITY_ID_BLUE, (byte)cc.getCapabilityId());
        assertEquals((byte)0xC1, (byte)cc.getValue());
    }
}
