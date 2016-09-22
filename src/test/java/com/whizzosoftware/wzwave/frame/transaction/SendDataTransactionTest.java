/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.channel.MockChannelHandlerContext;
import com.whizzosoftware.wzwave.channel.MockZWaveChannelContext;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionFailedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionStartedEvent;
import com.whizzosoftware.wzwave.frame.*;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import static org.junit.Assert.*;

public class SendDataTransactionTest {
    @Test
    public void testTransactionWithExpectedResponse() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        b = new byte[] {0x01, 0x05, 0x00, 0x13, 0x00, 0x00, -45};
        assertTrue(t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        assertTrue(t.addFrame(ctx, new ApplicationCommand(DataFrameType.RESPONSE, (byte) 0x00, (byte) 0x01, new byte[]{0x00})));
        assertTrue(t.isComplete());
        assertEquals(2, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
        TransactionCompletedEvent tcd = (TransactionCompletedEvent)ctx.getUserEvents().get(1);
        assertTrue(tcd.hasFrame());
        assertTrue(tcd.getFrame() instanceof ApplicationCommand);
    }

    @Test
    public void testTransactionWithoutExpectedResponse() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        assertTrue(t.addFrame(null, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(null, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        b = new byte[] {0x01, 0x05, 0x00, 0x13, 0x00, 0x00, -45};
        assertTrue(t.addFrame(null, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());

        // timeout waiting for expected response
        t.timeout(ctx);
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        SendData sd = (SendData)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);
        assertEquals(0, sd.getSendCount());
    }

    @Test
    public void testTransactionWithCANBeforeACK() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive CAN
        assertTrue(t.addFrame(ctx, new CAN()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        SendData sd = (SendData)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);

        // confirm that send count was decremented so the retry doesn't count towards the send count
        assertEquals(-1, sd.getSendCount());
    }

    @Test
    public void testTransactionWithCANAfterACK() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive CAN
        assertTrue(t.addFrame(ctx, new CAN()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        SendData sd = (SendData)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);

        // confirm that send count was decremented so the retry doesn't count towards the send count
        assertEquals(-1, sd.getSendCount());
    }

    @Test
    public void testTransactionWithCANAfterResponse() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);

        // receive ACK
        assertTrue(t.addFrame(null, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive response
        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive CAN
        assertTrue(t.addFrame(ctx, new CAN()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        SendData sd = (SendData)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);

        // confirm that send count was decremented so the retry doesn't count towards the send count
        assertEquals(-1, sd.getSendCount());
    }

    @Test
    public void testTransactionWithCANAfterRequest() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        byte[] b = new byte[] {0x01, 0x09, 0x00, 0x13, 0x2E, 0x02, 0x25, 0x02, 0x05, 0x4A, (byte)0xA1};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive response
        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x00, -45};
        assertTrue(t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        b = new byte[] {0x01, 0x05, 0x00, 0x13, 0x00, 0x00, -45};
        assertTrue(t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive CAN
        assertTrue(t.addFrame(ctx, new CAN()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        SendData sd = (SendData)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);

        // confirm that send count was decremented so the retry doesn't count towards the send count
        assertEquals(-1, sd.getSendCount());
    }

    @Test
    public void testApplicationCommandReceivedBeforeSendDataCallback() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        byte[] b = new byte[] {0x01, 0x0D, 0x00, 0x13, 0x03, 0x06, 0x60, 0x0D, 0x00, 0x01, 0x20, 0x02, 0x05, 0x1B, (byte)0xB4};
        SendData startFrame = new SendData(Unpooled.wrappedBuffer(b));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive response
        b = new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xE8};
        assertTrue(t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(b))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive application command
        assertFalse(t.addFrame(ctx, new ApplicationCommand(Unpooled.wrappedBuffer(new byte[] {0x01, 0x0D, 0x00, 0x04, 0x00, 0x03, 0x07, 0x60, 0x0D, 0x01, 0x00, 0x20, 0x03, (byte)0xFF, 0x42}))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive callback
        b = new byte[] {0x01, 0x05, 0x00, 0x13, 0x1B, 0x00, (byte)0xF3};
        assertTrue(t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(b))));
        assertTrue(t.isComplete());
        assertEquals(2, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
    }

    @Test
    public void testTransactionWithListeningNodeNoACKCallback() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        SendData startFrame = new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x00, 0x00, 0x25, 0x0a, (byte)0xce}));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive response
        t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8})));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive callback
        t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x0a, 0x01, (byte)0xe2})));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        SendData sd = (SendData)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);
        assertEquals(0, sd.getSendCount());
    }

    @Test
    public void testTransactionWithNonListeningNodeNoACKCallback() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        SendData startFrame = new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x00, 0x00, 0x25, 0x0a, (byte)0xce}));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, false, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive response
        t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8})));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive callback with no ACK
        t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x0a, 0x01, (byte)0xe2})));
        assertTrue(t.isComplete());
        assertEquals(2, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionFailedEvent);
    }

    @Test
    public void testTransactionWithFailureCallback() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        SendData startFrame = new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x00, 0x00, 0x25, 0x0a, (byte)0xce}));
        SendDataTransaction t = new SendDataTransaction(ctx, startFrame, true, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(null, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive response
        t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8})));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive callback
        t.addFrame(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x0a, 0x02, (byte)0xe2})));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        SendData sd = (SendData)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);
        assertEquals(0, sd.getSendCount());
    }
}
