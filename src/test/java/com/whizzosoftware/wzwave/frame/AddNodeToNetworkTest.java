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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AddNodeToNetworkTest {
    @Test
    public void testRequestConstructor() {
        AddNodeToNetwork f = new AddNodeToNetwork(AddNodeToNetwork.ADD_NODE_ANY);
        byte[] b = f.getBytes();
        assertEquals(7, b.length);
    }

    @Test
    public void testResponseConstructorWithNoNodeInfo() {
        byte[] b = new byte[] {0x01, 0x07, 0x00, 0x4A, 0x02, 0x01, 0x00, 0x00, (byte)0xB1};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        AddNodeToNetwork f = new AddNodeToNetwork(buffer);
        assertEquals(1, buffer.readableBytes());
        assertEquals(AddNodeToNetwork.ADD_NODE_STATUS_LEARN_READY, f.getStatus());
        assertEquals(0, f.getSource());
        assertFalse(f.hasNodeInfo());
    }

    @Test
    public void testResponseConstructorWithNodeInfo() {
        byte[] b = new byte[] {0x01, 0x14, 0x00, 0x4A, 0x01, 0x03, 0x02, 0x0D, 0x04, 0x10, 0x01, 0x25, 0x31, 0x32, 0x27, 0x70, (byte)0x85, 0x72, (byte)0x86, (byte)0xEF, (byte)0x82, (byte)0xD4};
        ByteBuf buffer = Unpooled.wrappedBuffer(b);
        AddNodeToNetwork f = new AddNodeToNetwork(buffer);
        assertEquals(1, buffer.readableBytes());
        assertEquals(AddNodeToNetwork.ADD_NODE_STATUS_ADDING_SLAVE, f.getStatus());
        assertEquals(2, f.getSource());
        assertTrue(f.hasNodeInfo());
        assertEquals(0x04, f.getNodeInfo().getBasicDeviceClass());
        assertEquals(0x10, f.getNodeInfo().getGenericDeviceClass());
        assertEquals(0x01, f.getNodeInfo().getSpecificDeviceClass());
        assertNotNull(f.getNodeInfo().getCommandClasses());
        assertEquals(10, f.getNodeInfo().getCommandClasses().length);
        assertEquals(0x25, f.getNodeInfo().getCommandClasses()[0]);
        assertEquals(0x31, f.getNodeInfo().getCommandClasses()[1]);
        assertEquals(0x32, f.getNodeInfo().getCommandClasses()[2]);
        assertEquals(0x27, f.getNodeInfo().getCommandClasses()[3]);
        assertEquals(0x70, f.getNodeInfo().getCommandClasses()[4]);
        assertEquals((byte)0x85, f.getNodeInfo().getCommandClasses()[5]);
        assertEquals(0x72, f.getNodeInfo().getCommandClasses()[6]);
        assertEquals((byte)0x86, f.getNodeInfo().getCommandClasses()[7]);
        assertEquals((byte)0xEF, f.getNodeInfo().getCommandClasses()[8]);
        assertEquals((byte)0x82, f.getNodeInfo().getCommandClasses()[9]);
    }
}
