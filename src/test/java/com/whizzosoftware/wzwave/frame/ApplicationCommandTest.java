/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ApplicationCommandTest {
    @Test
    public void testExplicitConstructor() {
        ApplicationCommand cmd = new ApplicationCommand(DataFrameType.REQUEST, (byte)0, (byte)6, new byte[] {0x25, 0x03, 0x00});
        byte[] b = cmd.getBytes();
        assertEquals(10, b.length);
        assertEquals((byte)0x01, b[0]);
        assertEquals((byte)0x08, b[1]);
        assertEquals((byte)0x00, b[2]);
        assertEquals((byte)0x04, b[3]);
        assertEquals((byte)0x00, b[4]);
        assertEquals((byte)0x06, b[5]);
        assertEquals((byte)0x25, b[6]);
        assertEquals((byte)0x03, b[7]);
        assertEquals((byte)0x00, b[8]);
        assertEquals(-45, b[9]);
    }

    @Test
    public void testByteArrayConstructor() {
        byte[] b1 = {0x01,0x09,0x00,0x04,0x00,0x06,0x03,0x25,0x03,0x00,-45};
        ApplicationCommand cmd = new ApplicationCommand(b1);
        assertEquals(0, cmd.getStatus());
        assertEquals(6, cmd.getNodeId());
        assertEquals(BinarySwitchCommandClass.ID, cmd.getCommandClassId());
    }
}
