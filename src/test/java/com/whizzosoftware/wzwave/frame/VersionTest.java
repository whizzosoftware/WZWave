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

public class VersionTest {
    @Test
    public void testRequestConstructor() {
        Version v = new Version();
        byte[] b = v.getBytes();
        assertEquals(5, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x03, b[1]); // length
        assertEquals(0x00, b[2]); // type (request)
        assertEquals(0x15, b[3]); // command ID
        assertEquals((byte)0xE9, b[4]); // checksum
    }

    @Test
    public void testResponseConstructor() {
        byte[] b = new byte[] {0x01, 0x10, 0x01, 0x15, 0x5a, 0x2d, 0x57, 0x61, 0x76, 0x65, 0x20, 0x32, 0x2e, 0x37, 0x38, 0x00, 0x01, (byte)0x9b};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        Version v = new Version(buffer);
        assertEquals(1, buffer.readableBytes());
        assertEquals("Z-Wave 2.78", v.getLibraryVersion());
        assertEquals(0x01, v.getLibraryType());
    }
}
