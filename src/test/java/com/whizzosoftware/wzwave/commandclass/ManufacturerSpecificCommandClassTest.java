/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.product.ProductInfo;
import com.whizzosoftware.wzwave.product.ProductRegistry;
import org.junit.Test;
import static org.junit.Assert.*;

public class ManufacturerSpecificCommandClassTest {
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
}
