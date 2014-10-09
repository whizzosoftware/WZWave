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

public class NodeProtocolInfoTest {
    @Test
    public void testRequestConstructor() {
        NodeProtocolInfo mgid = new NodeProtocolInfo((byte)0x01);
        byte[] b = mgid.getBytes();
        assertEquals(6, b.length);
        assertEquals(0x01, b[0]);
        assertEquals(0x04, b[1]);
        assertEquals(0x00, b[2]);
        assertEquals(0x41, b[3]);
        assertEquals(0x01, b[4]);
        assertEquals((byte)0xBB, b[5]);
    }

    @Test
    public void testResponseConstructor() {
        byte[] b = new byte[] {0x01, 0x09, 0x01, 0x41, (byte)0x92, 0x16, 0x00, 0x02, 0x02, 0x01, 0x33};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        NodeProtocolInfo npi = new NodeProtocolInfo(buffer);
        assertEquals(1, buffer.readableBytes());
        assertTrue(npi.isListening());
        assertTrue(npi.isBeaming());
        assertFalse(npi.isRouting());
        assertEquals(40000, npi.getMaxBaudRate());
        assertEquals(3, npi.getVersion());
        assertFalse(npi.hasSecurity());
        assertEquals(0x02, npi.getBasicDeviceClass());
        assertEquals(0x02, npi.getGenericDeviceClass());
        assertEquals(0x01, npi.getSpecificDeviceClass());
    }
}
