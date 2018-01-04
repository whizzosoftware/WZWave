/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.product;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests of {@link ProductRegistry}.
 *
 * @author Linus Brimstedt
 */
public class ProductRegistryTest {

    @Test
    public void testLookupProductUnknown() throws Exception {
        ProductInfo nullInfo = new ProductInfo(null, null, null);

        ProductInfo info = ProductRegistry.lookupProduct(8, 123, 123);
        assertFalse(info.isComplete());
    }

    @Test
    public void testLookupProductKnown() throws Exception {
        ProductInfo info = ProductRegistry.lookupProduct(316, 1, 17);
        assertEquals("Philio Technology Corporation", info.getManufacturer());
        assertEquals("Smart Energy Plug In Switch (PAN11)", info.getName());
        assertTrue(info.isComplete());
    }

    @Test
    public void testLookupPhilioPan16() {
        ProductInfo info = ProductRegistry.lookupProduct(316, 1, 41);
        assertEquals("Philio Technology Corporation", info.getManufacturer());
        assertEquals("Smart Energy Plug In Switch (PAN16)", info.getName());
        assertTrue(info.isComplete());
    }
}
