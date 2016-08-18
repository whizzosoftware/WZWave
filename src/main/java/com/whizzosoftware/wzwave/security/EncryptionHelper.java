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

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class EncryptionHelper {
    private static final byte[] frameEncryptionPassword = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA};
    private static final byte[] dataOriginAuthenticationPassword = {0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55};

    /**
     * Creates the frame encryption key used to encrypt data frame payloads.
     *
     * @param networkKey the network key
     *
     * @return a byte array
     * @throws GeneralSecurityException on failure
     */
    static public byte[] createEncryptionKey(byte[] networkKey) throws GeneralSecurityException {
        return encryptECB(frameEncryptionPassword, networkKey);
    }

    /**
     * Creates the data origin authentication key used to generate message authentication codes (MACs) that
     * are used by the Security Command Class.
     *
     * @param networkKey the network key
     *
     * @return a byte array
     * @throws GeneralSecurityException on failure
     */
    static public byte[] createAuthenticationKey(byte[] networkKey) throws GeneralSecurityException {
        return encryptECB(dataOriginAuthenticationPassword, networkKey);
    }

    /**
     * Creates an initialization vector used by AES-OFB and AES-CBC encryption.
     *
     * @param randomNonce a randomly generated 8-byte nonce
     * @param deviceNonce the 8-byte nonce that was provided by the destination device
     *
     * @return a byte array
     */
    static public byte[] createInitializationVector(byte[] randomNonce, byte[] deviceNonce) {
        // initialization vector is our random nonce and the device-provided nonce concatenated together
        byte[] iv = new byte[16];
        System.arraycopy(randomNonce, 0, iv, 0, 8);
        System.arraycopy(deviceNonce, 0, iv, 8, 8);
        return iv;
    }

    /**
     * Creates a message authentication code (MAC) that is suffixed to the security command class data to insure
     * it hasn't been tampered with.
     *
     * @param securityCommandClassCommand the security command class command byte
     * @param srcNode the source node
     * @param dstNode the destination node
     * @param deviceNonce the nonce provided by the destination node
     * @param encPayload the encrypted payload
     * @param authKey the data origin authentication key to use for encryption
     *
     * @return a byte array
     * @throws GeneralSecurityException on failure
     */
    static public byte[] createMAC(byte securityCommandClassCommand, byte srcNode, byte dstNode, byte[] deviceNonce, byte[] encPayload, byte[] authKey) throws GeneralSecurityException {
        byte[] mac = new byte[8];

        // create the IV and then encrypt with the auth key
        byte[] tmpauth = EncryptionHelper.encryptECB(createInitializationVector(
            new byte[] {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA},
            deviceNonce
        ), authKey);

        // build the buffer
        int bufSize = encPayload.length + 4;
        byte[] buffer = new byte[bufSize];
        buffer[0] = securityCommandClassCommand;
        buffer[1] = srcNode;
        buffer[2] = dstNode;
        buffer[3] = (byte)encPayload.length;
        System.arraycopy(encPayload, 0, buffer, 4, encPayload.length);

        byte[] encpck = new byte[16];
        int block = 0;
        for (int i=0; i < bufSize; i++) {
            encpck[block] = buffer[i];
            block++;
            if (block == 16) {
                for (int j=0; j < 16; j++) {
                    tmpauth[j] = (byte)(encpck[j] ^ tmpauth[j]);
                    encpck[j] = 0;
                }
                block = 0;
                tmpauth = encryptECB(tmpauth, authKey);
            }
        }

        if (block > 0) {
            for (int i=0; i < 16; i++) {
                tmpauth[i] = (byte)(encpck[i] ^ tmpauth[i]);
            }
            tmpauth = encryptECB(tmpauth, authKey);
        }

        System.arraycopy(tmpauth, 0, mac, 0, 8);

        return mac;
    }

    static public byte[] encryptOFB(byte[] msg, byte[] iv, byte[] key) throws GeneralSecurityException {
        return encrypt("AES/OFB/NoPadding", msg, iv, key);
    }

    static byte[] encryptECB(byte[] msg, byte[] key) throws GeneralSecurityException {
        return encrypt("AES/ECB/NoPadding", msg, null, key);
    }

    static private byte[] encrypt(String cipherType, byte[] msg, byte[] iv, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(cipherType);
        SecretKeySpec skeyspec = new SecretKeySpec(key, "AES");
        if (iv != null) {
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
        }
        return cipher.doFinal(msg);
    }
}
