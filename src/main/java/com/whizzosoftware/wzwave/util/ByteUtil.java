/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.util;

import com.whizzosoftware.wzwave.frame.Frame;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;

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
        return createString(bytes, 0, length);
    }

    static public String createString(byte[] bytes, int startIndex, int length) {
        char[] hexChars = new char[length * 5];
        int v;
        for (int j=startIndex; j < length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 5] = '0';
            hexChars[j * 5 + 1] = 'x';
            hexChars[j * 5 + 2] = hexArray[v >>> 4];
            hexChars[j * 5 + 3] = hexArray[v & 0x0F];
            hexChars[j * 5 + 4] = ' ';
        }
        return new String(hexChars);
    }

    static public String createString(ByteBuf buf) {
        int length = buf.readableBytes();
        char[] hexChars = new char[length * 5];
        int v;
        int i = 0;
        for (int j=buf.readerIndex(); j < buf.readerIndex() + length; j++) {
            v = buf.getByte(j) & 0xFF;
            hexChars[i * 5] = '0';
            hexChars[i * 5 + 1] = 'x';
            hexChars[i * 5 + 2] = hexArray[v >>> 4];
            hexChars[i * 5 + 3] = hexArray[v & 0x0F];
            hexChars[i * 5 + 4] = ' ';
            i++;
        }
        return new String(hexChars);
    }

    static public String createString(byte b) {
        char[] hexChars = new char[4];
        int v = b & 0xFF;
        hexChars[0] = '0';
        hexChars[1] = 'x';
        hexChars[2] = hexArray[v >>> 4];
        hexChars[3] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    static public int convertTwoBytesToInt(byte msb, byte lsb) {
        return (msb << 8) | (lsb);
    }

    static public double parseValue(byte[] b, int start, int length, int precision) {
        long value = 0;
        for (int i=start; i < start+length; i++) {
            int shift = 8 * ((length - (i - start)) - 1);
            value += (b[i] & 0xFF) << shift;
        }
        return new BigDecimal(value).movePointLeft(precision).doubleValue();
    }
}
