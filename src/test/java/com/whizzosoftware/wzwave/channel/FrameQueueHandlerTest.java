/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.channel.FrameQueueHandler;
import com.whizzosoftware.wzwave.channel.MockChannelHandlerContext;
import com.whizzosoftware.wzwave.channel.event.*;
import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.commandclass.MeterCommandClass;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.OutboundDataFrame;
import com.whizzosoftware.wzwave.frame.Version;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FrameQueueHandlerTest {
    @Test
    public void testWrite() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        FrameQueueHandler h = new FrameQueueHandler();
        h.write(ctx, new OutboundDataFrame(new Version(), true), null);
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof DataFrameSentEvent);
    }

    @Test
    public void testWakeupQueueWithNonListeningNode() throws Exception {
        DataFrame sbg = new BinarySwitchCommandClass().createGet((byte)0x02);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        FrameQueueHandler h = new FrameQueueHandler();
        h.write(ctx, new NodeSleepChangeEvent((byte)0x02, true), null);
        assertEquals(0, ctx.getWriteQueue().size());
        assertFalse(h.hasPendingFrames());
        assertFalse(h.hasWakeupFrames((byte)0x02));
        assertFalse(h.hasTransaction());

        h.write(ctx, new OutboundDataFrame(new BasicCommandClass().createGet((byte)0x02), false), null);
        assertEquals(0, ctx.getWriteQueue().size());
        assertFalse(h.hasPendingFrames());
        assertTrue(h.hasWakeupFrames((byte)0x02));
        assertFalse(h.hasTransaction());

        h.write(ctx, new OutboundDataFrame(sbg, false), null);
        assertEquals(0, ctx.getWriteQueue().size());
        assertFalse(h.hasPendingFrames());
        assertTrue(h.hasWakeupFrames((byte)0x02));
        assertFalse(h.hasTransaction());

        h.write(ctx, new OutboundDataFrame(new MeterCommandClass().createGet((byte)0x02, MeterCommandClass.Scale.Watts), false), null);
        assertEquals(0, ctx.getWriteQueue().size());
        assertFalse(h.hasPendingFrames());
        assertTrue(h.hasWakeupFrames((byte)0x02));
        assertFalse(h.hasTransaction());

        h.write(ctx, new NodeSleepChangeEvent((byte)0x02, false), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(h.hasPendingFrames());
        assertFalse(h.hasWakeupFrames((byte)0x02));
        assertFalse(h.hasTransaction());

        h.write(ctx, new TransactionStartedEvent(""), null);
        assertTrue(h.hasTransaction());

        h.write(ctx, new OutboundDataFrame(new Version(), false), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(h.hasPendingFrames());
        assertFalse(h.hasWakeupFrames((byte)0x02));
        assertTrue(h.hasTransaction());

        h.write(ctx, new TransactionCompletedEvent("", null), null);
        assertEquals(2, ctx.getWriteQueue().size());
        assertTrue(h.hasPendingFrames());
        assertFalse(h.hasWakeupFrames((byte)0x02));
        assertFalse(h.hasTransaction());

        h.write(ctx, new TransactionStartedEvent(""), null);
        assertTrue(h.hasTransaction());

        h.write(ctx, new SendDataTransactionFailedEvent("", sbg, (byte)0x02, false, false), null);
        assertEquals(3, ctx.getWriteQueue().size());
        assertFalse(h.hasPendingFrames());
        assertTrue(h.hasWakeupFrames((byte)0x02));
        assertEquals(2, h.getWakeupQueueSize((byte)0x02));
        assertFalse(h.hasTransaction());
    }

    @Test
    public void testFailedTransactionQueueNonSleepingNode() throws Exception {
        DataFrame df = new BasicCommandClass().createGet((byte)0x02);
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        FrameQueueHandler h = new FrameQueueHandler();
        h.write(ctx, new OutboundDataFrame(df, true), null);
        h.write(ctx, new TransactionStartedEvent(""), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertEquals(0, h.getWakeupQueueSize((byte)0x02));
        h.write(ctx, new TransactionFailedEvent("", df, (byte)0x02), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertEquals(0, h.getWakeupQueueSize((byte)0x02));
    }

    @Test
    public void testFailedTransactionQueueSleepingNode() throws Exception {
        DataFrame df = new BasicCommandClass().createGet((byte)0x02);
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        FrameQueueHandler h = new FrameQueueHandler();
        h.write(ctx, new OutboundDataFrame(df, false), null);
        h.write(ctx, new TransactionStartedEvent(""), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertEquals(0, h.getWakeupQueueSize((byte)0x02));
        h.write(ctx, new SendDataTransactionFailedEvent("", df, (byte)0x02, false, false), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertEquals(1, h.getWakeupQueueSize((byte)0x02));
    }

    @Test
    public void testFailedTransactionResend() throws Exception {
        DataFrame df = new BasicCommandClass().createGet((byte)0x02);
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        FrameQueueHandler h = new FrameQueueHandler();
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(0, h.getWakeupQueueSize((byte)0x02));

        // write the new data frame
        h.write(ctx, new OutboundDataFrame(df, true), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertEquals(0, h.getWakeupQueueSize((byte)0x02));

        // initiate a new transaction
        df.setTransactionId("aaa");
        h.write(ctx, new TransactionStartedEvent("aaa"), null);
        assertEquals(1, ctx.getWriteQueue().size());
        assertEquals(0, h.getWakeupQueueSize((byte)0x02));

        // assume transaction has failed and the same data frame is being re-sent
        // (this should be allowed since it's in the context of the same message transaction)
        h.write(ctx, new OutboundDataFrame(df, true), null);
        assertEquals(2, ctx.getWriteQueue().size());
    }
}
