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

public class MemoryGetIdTest {
    @Test
    public void testRequestConstructor() {
        MemoryGetId mgid = new MemoryGetId();
        byte[] b = mgid.getBytes();
        assertEquals(5, b.length);
        assertEquals(0x01, b[0]);
        assertEquals(0x03, b[1]);
        assertEquals(0x00, b[2]);
        assertEquals(0x20, b[3]);
        assertEquals((byte)0xDC, b[4]);
    }

    @Test
    public void testResponseConstructor() {
        byte[] b = new byte[] {0x01, 0x08, 0x01, 0x20, 0x01, 0x6a, 0x2d, (byte)0xec, 0x01, 0x7d};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        MemoryGetId mgid = new MemoryGetId(buffer);
        assertEquals(1, buffer.readableBytes());
        assertEquals(-20, mgid.getHomeId()); // TODO
        assertEquals(0x01, mgid.getNodeId());
    }
}
