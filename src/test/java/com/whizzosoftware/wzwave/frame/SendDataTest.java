/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import static org.junit.Assert.*;

public class SendDataTest {
    @Test
    public void testMessageArgConstructor() {
        SendData sd = new SendData("", (byte)0x02, new byte[] {(byte)0xFF}, (byte)0x05, (byte)0x01, true);
        byte[] mb = sd.getBytes();
        assertEquals(10, mb.length);
        assertEquals(0x01, mb[0]);
        assertEquals(0x08, mb[1]);
        assertEquals(0x00, mb[2]);
        assertEquals(0x13, mb[3]);
        assertEquals(0x02, mb[4]);
        assertEquals(0x01, mb[5]);
        assertEquals((byte)0xFF, mb[6]);
        assertEquals(0x05, mb[7]);
        assertEquals(0x01, mb[8]);
        assertEquals(28, mb[9]);
    }

    @Test
    public void testMessageByteArrayConstructorWithRequest() {
        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        SendData sd = new SendData(buffer);
        assertEquals(1, buffer.readableBytes());
        assertEquals(DataFrameType.REQUEST, sd.getType());
        assertFalse(sd.hasRetVal());
        assertEquals((byte)0x06, sd.getNodeId());
        assertTrue(sd.hasCallbackId());
        assertEquals((byte)0x08, (byte)sd.getCallbackId());
    }

    @Test
    public void testMessageByteArrayConstructorWithRetval() {
        byte[] b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xE8};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        SendData sd = new SendData(buffer);
        assertEquals(1, buffer.readableBytes());
        assertEquals(DataFrameType.RESPONSE, sd.getType());
        assertTrue(sd.hasRetVal());
        assertEquals((byte)0x01, (byte)sd.getRetVal());
    }
}
