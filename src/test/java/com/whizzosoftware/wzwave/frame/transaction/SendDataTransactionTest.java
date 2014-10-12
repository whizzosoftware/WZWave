/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.*;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import static org.junit.Assert.*;

public class SendDataTransactionTest {
    @Test
    public void testTransactionWithExpectedResponse() {
        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(startFrame, true);
        assertFalse(t.isComplete());

        assertTrue(t.addFrame(new ACK()));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        b = new byte[] {0x01, 0x04, 0x00, 0x13, 0x00, -45};
        assertTrue(t.addFrame(new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        assertTrue(t.addFrame(new ApplicationCommand(DataFrameType.RESPONSE, (byte) 0x00, (byte) 0x01, new byte[]{0x00})));
        assertTrue(t.isComplete());
        assertFalse(t.hasError());
    }

    @Test
    public void testTransactionWithoutExpectedResponse() {
        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(startFrame, false);
        assertFalse(t.isComplete());

        assertTrue(t.addFrame(new ACK()));
        assertFalse(t.isComplete());

        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());

        b = new byte[] {0x01, 0x04, 0x00, 0x13, 0x00, -45};
        assertTrue(t.addFrame(new SendData(Unpooled.wrappedBuffer(b))));
        assertTrue(t.isComplete());
    }

    @Test
    public void testTransactionWithCANBeforeACK() {
        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(startFrame, true);

        // receive CAN
        assertTrue(t.addFrame(new CAN()));
        assertTrue(t.isComplete());
        assertTrue(t.hasError());
    }

    @Test
    public void testTransactionWithCANAfterACK() {
        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(startFrame, true);

        // receive ACK
        assertTrue(t.addFrame(new ACK()));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        // receive CAN
        assertTrue(t.addFrame(new CAN()));
        assertTrue(t.isComplete());
        assertTrue(t.hasError());
    }

    @Test
    public void testTransactionWithCANAfterResponse() {
        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(startFrame, true);

        // receive ACK
        assertTrue(t.addFrame(new ACK()));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        // receive response
        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());

        // receive CAN
        assertTrue(t.addFrame(new CAN()));
        assertTrue(t.isComplete());
        assertTrue(t.hasError());
    }

    @Test
    public void testTransactionWithCANAfterRequest() {
        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(startFrame, true);

        // receive ACK
        assertTrue(t.addFrame(new ACK()));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        // receive response
        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());

        b = new byte[] {0x01, 0x04, 0x00, 0x13, 0x00, -45};
        assertTrue(t.addFrame(new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        // receive CAN
        assertTrue(t.addFrame(new CAN()));
        assertTrue(t.isComplete());
        assertTrue(t.hasError());
    }
}
