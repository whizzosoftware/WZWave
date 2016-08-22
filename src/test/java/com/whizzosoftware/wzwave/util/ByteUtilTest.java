/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteUtilTest {
    @Test
    public void testParseValue() {
        assertEquals(1, ByteUtil.parseValue(new byte[] {(byte)0x01}, 0, 1, 0), 0);
        assertEquals(127, ByteUtil.parseValue(new byte[] {(byte)0x7F}, 0, 1, 0), 0);
        assertEquals(65535, ByteUtil.parseValue(new byte[] {(byte)0xFF, (byte)0xFF}, 0, 2, 0), 0);
        assertEquals(16777215, ByteUtil.parseValue(new byte[] {(byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF}, 0, 4, 0), 0);
        assertEquals(2147483647, ByteUtil.parseValue(new byte[] {(byte)0x7F, (byte)0xFF, (byte)0xFF, (byte)0xFF}, 0, 4, 0), 0);
    }
}
