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
import com.whizzosoftware.wzwave.security.EncryptionHelper;
import com.whizzosoftware.wzwave.security.NonceProvider;
import com.whizzosoftware.wzwave.security.StaticNonceProvider;
import com.whizzosoftware.wzwave.util.ByteUtil;
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
        assertEquals((byte)17, b[17]);
        assertEquals((byte)-94, b[18]);
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

    @Test
    public void testCreateMessageEncapsulationv1() throws GeneralSecurityException {
        NonceProvider np = new StaticNonceProvider(
          new byte[] {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA},
          new byte[] {(byte)0xd8, 0x74, (byte)0xda, (byte)0x8c, (byte)0xd9, 0x7d, (byte)0xcd, (byte)0xc5}
        );
        byte[] encKey = EncryptionHelper.createEncryptionKey(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10});
        byte[] authKey = EncryptionHelper.createAuthenticationKey(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10});
        DataFrame df = SecurityCommandClass.createMessageEncapsulationv1("", (byte)0x05, np, (byte)0x62, (byte)0x01, new byte[] {(byte)0xFF}, encKey, authKey);
        byte[] b = df.getBytes();

        assertEquals(32, b.length);

        // check header
        assertEquals((byte)0x01, b[0]);
        assertEquals((byte)0x1E, b[1]);
        assertEquals((byte)0x00, b[2]);
        assertEquals((byte)0x13, b[3]);
        assertEquals((byte)0x05, b[4]);
        assertEquals((byte)0x17, b[5]);
        assertEquals((byte)0x98, b[6]);
        assertEquals((byte)0x81, b[7]);

        // check random nonce
        assertEquals((byte)0xAA, b[8]);
        assertEquals((byte)0xAA, b[9]);
        assertEquals((byte)0xAA, b[10]);
        assertEquals((byte)0xAA, b[11]);
        assertEquals((byte)0xAA, b[12]);
        assertEquals((byte)0xAA, b[13]);
        assertEquals((byte)0xAA, b[14]);
        assertEquals((byte)0xAA, b[15]);

        // check encrypted payload
        assertEquals((byte)0x43, b[16]);
        assertEquals((byte)0x71, b[17]);
        assertEquals((byte)0xB6, b[18]);
        assertEquals((byte)0xF7, b[19]);

        // check nonce identifier
        assertEquals((byte)0xD8, b[20]);

        // check MAC
        assertEquals((byte)0x1D, b[21]);
        assertEquals((byte)0x1C, b[22]);
        assertEquals((byte)0xD1, b[23]);
        assertEquals((byte)0x25, b[24]);
        assertEquals((byte)0xB8, b[25]);
        assertEquals((byte)0xF2, b[26]);
        assertEquals((byte)0x6F, b[27]);
        assertEquals((byte)0xDC, b[28]);

        assertEquals((byte)0x05, b[29]); // tx options
        assertEquals((byte)0x01, b[30]); // callback ID
        assertEquals((byte)0x5A, b[31]); // checksum
    }
}
