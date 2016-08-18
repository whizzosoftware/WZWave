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

import com.whizzosoftware.wzwave.controller.MockZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.SendData;
import com.whizzosoftware.wzwave.security.NonceProvider;
import com.whizzosoftware.wzwave.security.StaticNonceProvider;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;

public class SecurityCommandClassTest {
    @Test
    public void testCreateNonceReportv1() {
        DataFrame df = SecurityCommandClass.createNonceReportv1((byte)0x02, new byte[] {(byte)0xbd, 0x6e, 0x79, 0x51, 0x67, 0x30, 0x67, (byte)0x90});
        byte[] b = df.getBytes();
        assertEquals(19, b.length);
        assertEquals((byte)0x01, b[0]);
        assertEquals((byte)0x11, b[1]);
        assertEquals((byte)0x00, b[2]);
        assertEquals((byte)0x13, b[3]);
        assertEquals((byte)0x02, b[4]);
        assertEquals((byte)0x0A, b[5]);
        assertEquals((byte)0x98, b[6]);
        assertEquals((byte)0x80, b[7]);
        assertEquals((byte)0xBD, b[8]);
        assertEquals((byte)0x6E, b[9]);
        assertEquals((byte)0x79, b[10]);
        assertEquals((byte)0x51, b[11]);
        assertEquals((byte)0x67, b[12]);
        assertEquals((byte)0x30, b[13]);
        assertEquals((byte)0x67, b[14]);
        assertEquals((byte)0x90, b[15]);
        assertEquals((byte)0x05, b[16]);
        assertEquals((byte)0x01, b[17]);
        assertEquals((byte)0xB2, b[18]);
    }

    @Test
    public void testCreateNetworkKeySetv1() throws GeneralSecurityException {
        NonceProvider np = new StaticNonceProvider(
          new byte[] {0x5F, 0x4A, 0x66, 0x43, (byte)0x99, (byte)0xB0, (byte)0xAC, (byte)0x42},
          new byte[] {(byte)0xD8, (byte)0x74, (byte)0xDA, (byte)0x8C, (byte)0xD9, (byte)0x7D, (byte)0xCD, (byte)0xC5}
        );
        MockZWaveControllerContext ctx = new MockZWaveControllerContext();
        DataFrame df = SecurityCommandClass.createNetworkKeySetv1(ctx, (byte)0x01, (byte)0x03, np);
        byte[] b = df.getBytes();

        assertEquals(47, b.length);

        assertEquals(DataFrame.START_OF_FRAME, b[0]);
        assertEquals(0x2D, b[1]);
        assertEquals(0x00, b[2]);
        assertEquals(SendData.ID, b[3]);
        assertEquals(0x03, b[4]);
        assertEquals(0x26, b[5]);
        assertEquals(SecurityCommandClass.ID, b[6]);
        assertEquals((byte)0x81, b[7]);
    }
}
