/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultilevelCommandClassTest {
    @Test
    public void testReport() {
        MultilevelSensorCommandClass cc = new MultilevelSensorCommandClass();
        cc.onApplicationCommand(null, new byte[] {0x31, 0x05, 0x04, 0x64, 0x00, 0x06, (byte)0xFE, 0x14}, 0);
        assertEquals(MultilevelSensorCommandClass.Type.Power, cc.getType());
        assertEquals(MultilevelSensorCommandClass.Scale.Watt, cc.getScale());
        assertEquals(1, cc.getValues().size());
        assertEquals(459.26, cc.getValues().get(0), 2);

        cc.onApplicationCommand(null, new byte[] {0x31, 0x05, 0x04, 0x64, 0x00, 0x06, (byte)0xFE, 0x14, 0x00, 0x06, (byte)0xFE, 0x14}, 0);
        assertEquals(2, cc.getValues().size());
        assertEquals(459.26, cc.getValues().get(0), 2);
        assertEquals(459.26, cc.getValues().get(1), 2);
    }
}
