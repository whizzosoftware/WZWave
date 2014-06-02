/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.ApplicationCommand;
import com.whizzosoftware.wzwave.frame.DataFrameType;
import com.whizzosoftware.wzwave.frame.SendData;
import org.junit.Test;
import static org.junit.Assert.*;

public class SendDataTransactionTest {
    @Test
    public void transactionWithExpectedResponse() {
        long now = System.currentTimeMillis();
        SendData startFrame = new SendData(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45});
        SendDataTransaction t = new SendDataTransaction(startFrame, now, true);
        assertFalse(t.isComplete());

        t.addFrame(new ACK(), now);
        assertFalse(t.isComplete());

        t.addFrame(new SendData(new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45}), now);
        assertFalse(t.isComplete());

        t.addFrame(new SendData(new byte[] {0x01, 0x04, 0x00, 0x13, 0x00, -45}), now);
        assertFalse(t.isComplete());

        t.addFrame(new ApplicationCommand(DataFrameType.RESPONSE, (byte)0x00, (byte)0x01, new byte[] {0x00}), now);
        assertTrue(t.isComplete());
    }

    @Test
    public void transactionWithoutExpectedResponse() {
        long now = System.currentTimeMillis();
        SendData startFrame = new SendData(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45});
        SendDataTransaction t = new SendDataTransaction(startFrame, now, false);
        assertFalse(t.isComplete());

        t.addFrame(new ACK(), now);
        assertFalse(t.isComplete());

        t.addFrame(new SendData(new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45}), now);
        assertFalse(t.isComplete());

        t.addFrame(new SendData(new byte[] {0x01, 0x04, 0x00, 0x13, 0x00, -45}), now);
        assertTrue(t.isComplete());
    }

    @Test
    public void transactionWithExpectedResponseTimeout() {
        long now = System.currentTimeMillis();
        SendData startFrame = new SendData(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45});
        SendDataTransaction t = new SendDataTransaction(startFrame, now, true);
        assertFalse(t.isComplete());

        t.addFrame(new ACK(), now);
        assertFalse(t.isComplete());

        t.addFrame(new SendData(new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45}), now);
        assertFalse(t.isComplete());

        t.addFrame(new SendData(new byte[] {0x01, 0x04, 0x00, 0x13, 0x00, -45}), now);
        assertFalse(t.isComplete());

        assertFalse(t.hasError(now + 1000));
        assertTrue(t.hasError(now + 2001));
    }
}
