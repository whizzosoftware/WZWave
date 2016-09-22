/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.SendData;
import com.whizzosoftware.wzwave.node.MockNodeContext;
import com.whizzosoftware.wzwave.persist.MockPersistenceContext;
import com.whizzosoftware.wzwave.persist.mapdb.MapDbPersistentStore;
import com.whizzosoftware.wzwave.product.ProductInfo;
import com.whizzosoftware.wzwave.product.ProductRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.*;

public class ManufacturerSpecificCommandClassTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testParseManufacturerSpecificData() throws CommandClassParseException {
        ManufacturerSpecificCommandClass mscc = new ManufacturerSpecificCommandClass();
        ProductInfo info = mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, (byte)0x86, 0x00, 0x03, 0x00, 0x0B}, 0);
        assertEquals(ProductRegistry.M_AEON_LABS, info.getManufacturer());
        assertEquals(ProductRegistry.P_SMART_ENERGY_STRIP, info.getName());

        info = mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, (byte)0x86, 0x00, 0x03, 0x00, 0x06}, 0);
        assertEquals(ProductRegistry.M_AEON_LABS, info.getManufacturer());
        assertEquals(ProductRegistry.P_SMART_ENERGY_SWITCH, info.getName());

        info = mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, (byte)0x63, 0x52, 0x50, 0x31, 0x30}, 0);
        assertEquals(ProductRegistry.M_GE_JASCO, info.getManufacturer());
        assertEquals(ProductRegistry.P_45604_OUTDOOR_MODULE, info.getName());

        info = mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, (byte)0x63, 0x52, 0x57, 0x35, 0x33}, 0);
        assertEquals(ProductRegistry.M_GE_JASCO, info.getManufacturer());
        assertEquals(ProductRegistry.P_45609_RELAY_SWITCH, info.getName());

        info = mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, 0x63, 0x44, 0x57, 0x32, 0x30}, 0);
        assertEquals(ProductRegistry.M_GE_JASCO, info.getManufacturer());
        assertEquals(ProductRegistry.P_45612_DIMMER_SWITCH, info.getName());
    }

    @Test
    public void testParseManufacturerSpecificDataWithMissingData() {
        try {
            ManufacturerSpecificCommandClass mscc = new ManufacturerSpecificCommandClass();
            mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00}, 0);
            fail("Should have thrown exception");
        } catch (CommandClassParseException ignored) {
        }
        try {
            ManufacturerSpecificCommandClass mscc = new ManufacturerSpecificCommandClass();
            mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, 0x05}, 0);
            fail("Should have thrown exception");
        } catch (CommandClassParseException ignored) {
        }
        try {
            ManufacturerSpecificCommandClass mscc = new ManufacturerSpecificCommandClass();
            mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, 0x05, 0x00}, 0);
            fail("Should have thrown exception");
        } catch (CommandClassParseException ignored) {
        }
        try {
            ManufacturerSpecificCommandClass mscc = new ManufacturerSpecificCommandClass();
            mscc.parseManufacturerSpecificData(new byte[] {0x72, 0x05, 0x00, 0x05, 0x00, 0x05}, 0);
            fail("Should have thrown exception");
        } catch (CommandClassParseException ignored) {
        }
    }

    @Test
    public void testQueuedStartupMessagesWithNoProductInfo() {
        MockNodeContext ctx = new MockNodeContext((byte)0x02, null);
        ManufacturerSpecificCommandClass c = new ManufacturerSpecificCommandClass();
        c.queueStartupMessages(ctx, (byte)0x02);
        assertEquals(1, ctx.getSentDataFrames().size());
        assertEquals("MANUFACTURER_SPECIFIC_GET", ((SendData)ctx.getSentDataFrames().get(0)).getName());
    }

    @Test
    public void testQueuedStartupMessagesWithProductInfo() {
        MockNodeContext ctx = new MockNodeContext((byte)0x02, null);
        ManufacturerSpecificCommandClass c = new ManufacturerSpecificCommandClass();
        c.setProductInfo(ProductRegistry.lookupProduct(-122, 3, 6));
        c.queueStartupMessages(ctx, (byte)0x02);
        assertEquals(0, ctx.getSentDataFrames().size());
        assertEquals(ProductRegistry.P_SMART_ENERGY_SWITCH, c.getProductInfo().getName());
        assertEquals(ProductRegistry.M_AEON_LABS, c.getProductInfo().getManufacturer());
    }

    @Test
    public void testQueuedStartupMessagesWithNullManufacturer() {
        MockNodeContext ctx = new MockNodeContext((byte)0x02, null);
        ManufacturerSpecificCommandClass c = new ManufacturerSpecificCommandClass();
        c.setProductInfo(ProductRegistry.lookupProduct(null, null, null));
        c.queueStartupMessages(ctx, (byte)0x02);
        assertEquals(0, ctx.getSentDataFrames().size());
        assertEquals(ProductInfo.UNKNOWN, c.getProductInfo().getName());
        assertEquals(ProductInfo.UNKNOWN, c.getProductInfo().getManufacturer());
    }

    @Test
    public void testQueuedStartupMessagesWithNullProductType() {
        MockNodeContext ctx = new MockNodeContext((byte)0x02, null);
        ManufacturerSpecificCommandClass c = new ManufacturerSpecificCommandClass();
        c.setProductInfo(ProductRegistry.lookupProduct(-122, null, null));
        c.queueStartupMessages(ctx, (byte)0x02);
        assertEquals(0, ctx.getSentDataFrames().size());
        assertEquals(ProductInfo.UNKNOWN, c.getProductInfo().getName());
        assertEquals(ProductRegistry.M_AEON_LABS, c.getProductInfo().getManufacturer());
    }

    @Test
    public void testQueuedStartupMessagesWithNullProduct() {
        MockNodeContext ctx = new MockNodeContext((byte)0x02, null);
        ManufacturerSpecificCommandClass c = new ManufacturerSpecificCommandClass();
        c.setProductInfo(ProductRegistry.lookupProduct(-122, 3, null));
        c.queueStartupMessages(ctx, (byte)0x02);
        assertEquals(0, ctx.getSentDataFrames().size());
        assertEquals(ProductInfo.UNKNOWN, c.getProductInfo().getName());
        assertEquals(ProductRegistry.M_AEON_LABS, c.getProductInfo().getManufacturer());
    }

    @Test
    public void testSaveAndRestoreWithProductInfo() throws IOException {
        MapDbPersistentStore s = new MapDbPersistentStore(folder.newFolder());
        ManufacturerSpecificCommandClass c = new ManufacturerSpecificCommandClass();
        c.setProductInfo(ProductRegistry.lookupProduct(-122, 3, 6));
        c.save(s.getContext(), (byte)0x02);
        c = new ManufacturerSpecificCommandClass();
        c.restore(s.getContext(), (byte)0x02);
        assertNotNull(c.getProductInfo());
        assertEquals(ProductRegistry.P_SMART_ENERGY_SWITCH, c.getProductInfo().getName());
        assertEquals(ProductRegistry.M_AEON_LABS, c.getProductInfo().getManufacturer());
    }

    @Test
    public void testSaveAndRestoreWithNullProductInfo() throws IOException {
        MapDbPersistentStore s = new MapDbPersistentStore(folder.newFolder());
        ManufacturerSpecificCommandClass c = new ManufacturerSpecificCommandClass();
        c.setProductInfo(ProductRegistry.lookupProduct(null, null, null));
        c.save(s.getContext(), (byte)0x02);
        c = new ManufacturerSpecificCommandClass();
        c.restore(s.getContext(), (byte)0x02);
        assertNotNull(c.getProductInfo());
        assertEquals(ProductInfo.UNKNOWN, c.getProductInfo().getName());
        assertEquals(ProductInfo.UNKNOWN, c.getProductInfo().getManufacturer());
    }
}
