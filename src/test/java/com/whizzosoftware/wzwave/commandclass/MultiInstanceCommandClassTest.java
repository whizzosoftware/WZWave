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

import com.whizzosoftware.wzwave.frame.SendData;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultiInstanceCommandClassTest {
    @Test
    public void testMultiInstanceGet() {
        // make sure v1 creates a proper data frame
        MultiInstanceCommandClass micc = new MultiInstanceCommandClass();
        DataFrame df = micc.createMultiInstanceGet((byte)0x01, BinarySwitchCommandClass.ID);
        byte[] b = df.getBytes();
        assertEquals(12, b.length);
        assertEquals(DataFrame.START_OF_FRAME, b[0]);
        assertEquals(0xA, b[1]); // length
        assertEquals(0x0, b[2]); // request
        assertEquals(SendData.ID, b[3]);
        assertEquals(0x1, b[4]); // node ID
        assertEquals(0x3, b[5]); // command length
        assertEquals(MultiInstanceCommandClass.ID, b[6]);
        assertEquals(MultiInstanceCommandClass.MULTI_INSTANCE_GET, b[7]);
        assertEquals(BinarySwitchCommandClass.ID, b[8]);
        assertEquals(0x5, b[9]);

        // make sure v2 throws an exception
        micc.setVersion(2);
        try {
            micc.createMultiInstanceGet((byte)0x01, BinarySwitchCommandClass.ID);
            fail("Should have thrown exception");
        } catch (RuntimeException ignored) {}
    }

    @Test
    public void testMultiInstanceReport() {
        MultiInstanceCommandClass micc = new MultiInstanceCommandClass();
        byte[] b = new byte[] {MultiInstanceCommandClass.ID, MultiInstanceCommandClass.MULTI_INSTANCE_REPORT, BinarySwitchCommandClass.ID, 2};
        micc.onApplicationCommand(null, b, 0);
        assertEquals(2, micc.getInstanceCount());
    }

    @Test
    public void testMultiInstanceCommandEncapsulation() {
        MultiInstanceCommandClass micc = new MultiInstanceCommandClass();
        try {
            micc.createMultiInstanceCommandEncapsulation((byte)0x02, (byte)0x01, BinarySwitchCommandClass.ID, BinarySwitchCommandClass.SWITCH_BINARY_SET, new byte[] {(byte)0xFF});
        } catch (RuntimeException ignored) {}

        micc.setVersion(2);
        DataFrame df = micc.createMultiInstanceCommandEncapsulation((byte)0x02, (byte)0x01, BinarySwitchCommandClass.ID, BinarySwitchCommandClass.SWITCH_BINARY_SET, new byte[] {(byte)0xFF});
        byte[] b = df.getBytes();
        assertEquals(15, b.length);
        assertEquals(DataFrame.START_OF_FRAME, b[0]);
        assertEquals(0xD, b[1]); // length
        assertEquals(0x0, b[2]); // request
        assertEquals(SendData.ID, b[3]);
        assertEquals(0x2, b[4]); // node ID
        assertEquals(0x6, b[5]); // command length
        assertEquals(MultiInstanceCommandClass.ID, b[6]);
        assertEquals(MultiInstanceCommandClass.MULTI_INSTANCE_CMD_ENCAP, b[7]);
        assertEquals(0x01, b[8]);
        assertEquals(BinarySwitchCommandClass.ID, b[9]);
        assertEquals(BinarySwitchCommandClass.SWITCH_BINARY_SET, b[10]);
        assertEquals("0xFF", ByteUtil.createString(b[11]));
        assertEquals(0x05, b[12]);
    }

    @Test
    public void testMultiChannelCapabilityGet() {
        MultiInstanceCommandClass micc = new MultiInstanceCommandClass();
        try {
            micc.createMultiChannelCapabilityGet((byte)0x02, (byte)0x01);
            fail("Should have thrown an exception");
        } catch (RuntimeException ignored) {}

        micc.setVersion(2);
        DataFrame df = micc.createMultiChannelCapabilityGet((byte)0x02, (byte)0x01);
        byte[] b = df.getBytes();
        assertEquals(12, b.length);
        assertEquals(DataFrame.START_OF_FRAME, b[0]);
        assertEquals(0xA, b[1]); // length
        assertEquals(0x0, b[2]); // request
        assertEquals(SendData.ID, b[3]);
        assertEquals(0x2, b[4]); // node ID
        assertEquals(0x3, b[5]); // command length
        assertEquals(MultiInstanceCommandClass.ID, b[6]);
        assertEquals(MultiInstanceCommandClass.MULTI_CHANNEL_CAPABILITY_GET, b[7]);
        assertEquals(0x01, b[8]);
        assertEquals(0x5, b[9]);
    }

    @Test
    public void testMultiChannelCommandEncapsulation() {
        BinarySwitchCommandClass bscc = new BinarySwitchCommandClass();
        DataFrame cmd = bscc.createSet((byte)22, true);
        MultiInstanceCommandClass micc = new MultiInstanceCommandClass();

        try {
            micc.createMultiChannelCommandEncapsulation((byte)1, (byte)3, cmd, true);
            fail("Should have thrown exception");
        } catch (RuntimeException ignored) {}

        micc.setVersion(2);
        DataFrame ecmd = micc.createMultiChannelCommandEncapsulation((byte)1, (byte)3, cmd, true);
        byte[] data = ecmd.getBytes();
        assertEquals(16, data.length);
        assertEquals(0x01, data[0]); // SOF
        assertEquals(14, data[1]); // frame length
        assertEquals(0x00, data[2]); // request
        assertEquals(SendData.ID, data[3]); // SendData
        assertEquals(22, data[4]); // node ID
        assertEquals(7, data[5]); // command length
        assertEquals(MultiInstanceCommandClass.ID, data[6]); // command class ID
        assertEquals(0x0D, data[7]); // MULTI_CHANNEL_CMD_ENCAP
        assertEquals(0x01, data[8]); // source endpoint
        assertEquals(0x03, data[9]); // destination endpoint
        assertEquals(BinarySwitchCommandClass.ID, data[10]); // command class ID
        assertEquals(0x01, data[11]); // SWITCH_BINARY_SET
        assertEquals((byte)0xFF, data[12]); // true
        assertEquals(0x05, data[13]); // TX options
    }
}
