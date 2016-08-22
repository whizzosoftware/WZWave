/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.security;

import org.junit.Test;
import static org.junit.Assert.*;

public class EncryptionHelperTest {
    @Test
    public void testCreateInitializationVector() {
        byte[] randomNonce = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
        byte[] deviceNonce = {0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
        byte[] iv = EncryptionHelper.createInitializationVector(randomNonce, deviceNonce);
        assertEquals(16, iv.length);
        for (int i=0; i < 16; i++) {
            assertEquals(i, iv[i]);
        }
    }

    @Test
    public void testEncryptOFB() throws Exception {
        // Note: these values were taken from a real Z-Wave interaction
        byte[] networkKey = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};
        byte[] iv = EncryptionHelper.createInitializationVector(
            new byte[] {(byte)0xe7, (byte)0x80, 0x45, 0x18, 0x34, 0x7f, (byte)0x8f, (byte)0x88}, // random nonce
            new byte[] {(byte)0xbd, 0x6e, 0x79, 0x51, 0x67, 0x30, 0x67, (byte)0x90} // device nonce
        );
        byte[] frameEncKey = EncryptionHelper.createEncryptionKey(networkKey);
        byte[] plainText = new byte[] {0x00, (byte)0x98, 0x03, 0x00, 0x62, 0x63, (byte)0x80, 0x71, 0x70, (byte)0x86, 0x20, (byte)0xef};
        byte[] cipherText = EncryptionHelper.encryptOFB(plainText, iv, frameEncKey);
        assertEquals(12, cipherText.length);
        assertEquals((byte)0x93, cipherText[0]);
        assertEquals((byte)0x02, cipherText[1]);
        assertEquals((byte)0x46, cipherText[2]);
        assertEquals((byte)0x42, cipherText[3]);
        assertEquals((byte)0x12, cipherText[4]);
        assertEquals((byte)0xAD, cipherText[5]);
        assertEquals((byte)0xBA, cipherText[6]);
        assertEquals((byte)0xE2, cipherText[7]);
        assertEquals((byte)0xD4, cipherText[8]);
        assertEquals((byte)0xB3, cipherText[9]);
        assertEquals((byte)0xE2, cipherText[10]);
        assertEquals((byte)0x2F, cipherText[11]);
    }

    @Test
    public void testEncryptECB() throws Exception {
        byte[] key = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] plainText = new byte[] {0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF};
        byte[] cipherText = EncryptionHelper.encryptECB(plainText, key);
        assertEquals(16, cipherText.length);
        assertEquals(-75, cipherText[0]);
        assertEquals(15, cipherText[1]);
        assertEquals(-40, cipherText[2]);
        assertEquals(92, cipherText[3]);
        assertEquals(12, cipherText[4]);
        assertEquals(10, cipherText[5]);
        assertEquals(-78, cipherText[6]);
        assertEquals(105, cipherText[7]);
        assertEquals(32, cipherText[8]);
        assertEquals(-106, cipherText[9]);
        assertEquals(-111, cipherText[10]);
        assertEquals(-77, cipherText[11]);
        assertEquals(43, cipherText[12]);
        assertEquals(-62, cipherText[13]);
        assertEquals(-8, cipherText[14]);
        assertEquals(99, cipherText[15]);
    }

    @Test
    public void testGenerateMAC() throws Exception {
        byte[] networkKey = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};
        byte[] deviceNonce = new byte[] {(byte)0xbd, 0x6e, 0x79, 0x51, 0x67, 0x30, 0x67, (byte)0x90};
        byte[] doaKey = EncryptionHelper.createAuthenticationKey(networkKey);
        byte[] mac = EncryptionHelper.createMAC((byte)0x03, (byte)0x02, (byte)0x01, deviceNonce, new byte[] {(byte)0xe7, (byte)0x80, (byte)0x45, (byte)0x18, (byte)0x34, (byte)0x7f, (byte)0x8f, (byte)0x88, (byte)0x93, (byte)0x02, (byte)0x46, (byte)0x42}, doaKey);
        assertEquals(8, mac.length);
        assertEquals((byte)0xED, mac[0]);
        assertEquals((byte)0xFA, mac[1]);
        assertEquals((byte)0x63, mac[2]);
        assertEquals((byte)0x5C, mac[3]);
        assertEquals((byte)0x25, mac[4]);
        assertEquals((byte)0xF6, mac[5]);
        assertEquals((byte)0xDB, mac[6]);
        assertEquals((byte)0x0B, mac[7]);
    }
}
