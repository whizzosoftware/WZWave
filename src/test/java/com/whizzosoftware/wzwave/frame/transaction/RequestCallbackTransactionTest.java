/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.channel.MockZWaveChannelContext;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionStartedEvent;
import com.whizzosoftware.wzwave.frame.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequestCallbackTransactionTest {
    @Test
    public void testValidTransaction() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();
        DataFrame startFrame = new SetDefault();

        RequestCallbackTransaction t = new RequestCallbackTransaction(ctx, startFrame, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        assertEquals(0, ctx.getWriteQueue().size());

        t.addFrame(ctx, new ACK());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        t.addFrame(ctx, new SetDefault());
        assertEquals(2, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
    }

    @Test
    public void testCANAfterSend() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();
        DataFrame startFrame = new SetDefault();

        RequestCallbackTransaction t = new RequestCallbackTransaction(ctx, startFrame, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        assertEquals(0, ctx.getWriteQueue().size());

        t.addFrame(ctx, new CAN());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame() == startFrame);
        assertEquals(-1, startFrame.getSendCount());
    }

    @Test
    public void testCANAfterACK() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();
        DataFrame startFrame = new SetDefault();

        RequestCallbackTransaction t = new RequestCallbackTransaction(ctx, startFrame, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        assertEquals(0, ctx.getWriteQueue().size());

        t.addFrame(ctx, new ACK());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        t.addFrame(ctx, new CAN());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame() == startFrame);
        assertEquals(-1, startFrame.getSendCount());
    }
}
