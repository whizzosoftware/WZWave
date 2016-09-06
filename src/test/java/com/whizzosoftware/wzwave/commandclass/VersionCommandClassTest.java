/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.SendData;
import com.whizzosoftware.wzwave.node.MockNodeContext;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class VersionCommandClassTest {
    @Test
    public void testCommandClassVersionGet() {
        MockNodeContext ctx = new MockNodeContext((byte)0x02, new CommandClass[] {
            CommandClassFactory.createCommandClass(BasicCommandClass.ID),
            CommandClassFactory.createCommandClass(MeterCommandClass.ID)
        });
        VersionCommandClass vcc = new VersionCommandClass();
        vcc.queueStartupMessages(ctx, (byte)0x02);
        assertEquals(2, ctx.getSentDataFrames().size());
        assertTrue(ctx.getSentDataFrames().get(0) instanceof SendData);
        assertEquals(2, (((SendData)ctx.getSentDataFrames().get(0)).getNodeId()));
        assertEquals("VERSION_GET", (((SendData)ctx.getSentDataFrames().get(0)).getName()));
        assertEquals(2, (((SendData)ctx.getSentDataFrames().get(1)).getNodeId()));
        assertEquals("VERSION_COMMAND_CLASS_GET", (((SendData)ctx.getSentDataFrames().get(1)).getName()));
    }

    @Test
    public void testCommandClassVersionGetWithKnownCommandClassVersions() {
        CommandClass cc1 = CommandClassFactory.createCommandClass(BasicCommandClass.ID);
        CommandClass cc2 = CommandClassFactory.createCommandClass(MeterCommandClass.ID);
        cc2.setVersion(2);
        MockNodeContext ctx = new MockNodeContext((byte)0x02, new CommandClass[] {
            cc1,
            cc2
        });
        VersionCommandClass vcc = new VersionCommandClass();
        vcc.queueStartupMessages(ctx, (byte)0x02);
        assertEquals(1, ctx.getSentDataFrames().size());
        assertTrue(ctx.getSentDataFrames().get(0) instanceof SendData);
        assertEquals(2, (((SendData)ctx.getSentDataFrames().get(0)).getNodeId()));
        assertEquals("VERSION_GET", (((SendData)ctx.getSentDataFrames().get(0)).getName()));
    }
}
