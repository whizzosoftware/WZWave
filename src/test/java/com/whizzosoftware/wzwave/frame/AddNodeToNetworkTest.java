/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AddNodeToNetworkTest {
    @Test
    public void testRequestConstructor() {
        AddNodeToNetwork f = new AddNodeToNetwork(AddNodeToNetwork.ADD_NODE_ANY);
        byte[] b = f.getBytes();
        assertEquals(6, b.length);
    }

    @Test
    public void testResponseConstructor() {
        byte[] b = new byte[] {0x01, 0x07, 0x00, 0x4A, 0x02, 0x01, 0x00, 0x00, (byte)0xB1};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        AddNodeToNetwork f = new AddNodeToNetwork(buffer);
        assertEquals(1, buffer.readableBytes());
        assertEquals(AddNodeToNetwork.ADD_NODE_STATUS_LEARN_READY, f.getStatus());
        assertEquals(0, f.getSource());
        assertFalse(f.hasNodeInfo());
    }
}
