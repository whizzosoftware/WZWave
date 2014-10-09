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

public class RequestNodeInfoTest {
    @Test
    public void testRequestConstructor() {
        RequestNodeInfo mgid = new RequestNodeInfo((byte)0x01);
        byte[] b = mgid.getBytes();
        assertEquals(6, b.length);
        assertEquals(0x01, b[0]);
        assertEquals(0x04, b[1]);
        assertEquals(0x00, b[2]);
        assertEquals(0x60, b[3]);
        assertEquals(0x01, b[4]);
        assertEquals((byte)0x9A, b[5]);
    }

    @Test
    public void testResponseConstructor() {
        byte[] b = new byte[] {0x01, 0x04, 0x01, 0x60, 0x01, (byte)0x9B};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        RequestNodeInfo rni = new RequestNodeInfo(buffer);
        assertEquals(1, buffer.readableBytes());
        assertNotNull(rni.getRetVal());
        assertEquals((byte)0x01, (byte)rni.getRetVal());
        assertTrue(rni.wasSuccessfullySent());
    }
}
