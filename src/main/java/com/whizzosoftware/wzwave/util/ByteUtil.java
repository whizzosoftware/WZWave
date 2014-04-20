/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.util;

import com.whizzosoftware.wzwave.frame.Frame;

/**
 * A utility class for various byte related functions.
 *
 * @author Dan Noguerol
 */
public class ByteUtil {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    static public String createString(Frame message) {
        byte[] b = message.getBytes();
        return createString(b, b.length);
    }

    static public String createString(byte[] bytes, int length) {
        char[] hexChars = new char[length * 5];
        int v;
        for (int j=0; j < length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 5] = '0';
            hexChars[j * 5 + 1] = 'x';
            hexChars[j * 5 + 2] = hexArray[v >>> 4];
            hexChars[j * 5 + 3] = hexArray[v & 0x0F];
            hexChars[j * 5 + 4] = ' ';
        }
        return new String(hexChars);
    }

    static public String createString(byte b) {
        char[] hexChars = new char[5];
        int v = b & 0xFF;
        hexChars[0] = '0';
        hexChars[1] = 'x';
        hexChars[2] = hexArray[v >>> 4];
        hexChars[3] = hexArray[v & 0x0F];
        hexChars[4] = ' ';
        return new String(hexChars);
    }

    static public int convertTwoBytesToInt(byte b1, byte b2) {
        return (b1 << 8) | (b2);
    }
}
