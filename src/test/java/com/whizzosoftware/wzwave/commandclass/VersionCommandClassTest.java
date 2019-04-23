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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class VersionCommandClassTest {
    private CommandClass cc1;
    private CommandClass cc2;
    private MockNodeContext ctx;
    private VersionCommandClass vcc;

    @Before
    public void before() {
        cc1 = CommandClassFactory.createCommandClass(BasicCommandClass.ID);
        cc2 = CommandClassFactory.createCommandClass(MeterCommandClass.ID);
        ctx = new MockNodeContext((byte) 0x02, new CommandClass[]{
                cc1,
                cc2
        });
        vcc = new VersionCommandClass();
    }

    @Test
    public void testCommandClassVersionGet() {
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
        cc2.setVersion(2);
        vcc.queueStartupMessages(ctx, (byte)0x02);

        assertEquals(1, ctx.getSentDataFrames().size());
        assertTrue(ctx.getSentDataFrames().get(0) instanceof SendData);
        assertEquals(2, (((SendData)ctx.getSentDataFrames().get(0)).getNodeId()));
        assertEquals("VERSION_GET", (((SendData)ctx.getSentDataFrames().get(0)).getName()));
    }

    @Test
    public void testVersionReport() {
        byte[] ccb = new byte [] { VersionCommandClass.ID, 0x12, 0x03, 0x04, 0x05, 0x01, 0x00};
        vcc.onApplicationCommand(ctx, ccb, 0);

        assertEquals("3", vcc.getLibrary());
        assertEquals("4.05", vcc.getProtocol());
        assertEquals("1.00", vcc.getApplication());
    }

    @Test
    public void testVersionReportWithOffset() {
        byte[] ccb = new byte [] { 0x00, 0x00, VersionCommandClass.ID, 0x12, 0x03, 0x04, 0x05, 0x01, 0x00};
        vcc.onApplicationCommand(ctx, ccb, 2);

        assertEquals("3", vcc.getLibrary());
        assertEquals("4.05", vcc.getProtocol());
        assertEquals("1.00", vcc.getApplication());
    }

    @Test
    public void testVersionCommandClassReportWithHigherVersion() {
        byte[] ccb = new byte [] { VersionCommandClass.ID, 0x14, 0x32, 0x02};
        vcc.onApplicationCommand(ctx, ccb, 0);

        assertFalse(cc1.hasExplicitVersion());
        assertTrue(cc2.hasExplicitVersion());
        assertEquals(2, cc2.getVersion());
    }

    @Test
    public void testVersionCommandClassReportWithHigherVersionAndOffset() {
        byte[] ccb = new byte [] { 0x00, 0x00, VersionCommandClass.ID, 0x14, 0x32, 0x02};
        vcc.onApplicationCommand(ctx, ccb, 2);

        assertFalse(cc1.hasExplicitVersion());
        assertTrue(cc2.hasExplicitVersion());
        assertEquals(2, cc2.getVersion());
    }

    @Test
    public void testVersionCommandClassReportWithInvalidVersion() {
        byte[] ccb = new byte [] { VersionCommandClass.ID, 0x14, 0x32, 0x00};
        vcc.onApplicationCommand(ctx, ccb, 0);

        assertFalse(cc1.hasExplicitVersion());
        assertFalse(cc2.hasExplicitVersion());
    }

    @Test
    public void testInvalidVersionReport() {
        byte[] ccb = new byte [] { VersionCommandClass.ID, 0x66, 0x32, 0x00};
        vcc.onApplicationCommand(ctx, ccb, 0);

        assertFalse(cc1.hasExplicitVersion());
        assertFalse(cc2.hasExplicitVersion());
    }
}
