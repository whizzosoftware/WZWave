/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.channel.inbound.TransactionInboundHandler;
import com.whizzosoftware.wzwave.frame.*;
import io.netty.buffer.Unpooled;

import org.junit.Test;
import static org.junit.Assert.*;

public class TransactionInboundHandlerTest {
    @Test
    public void testRequestNodeInfoFailure() {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        // initiate new RequestNodeInfo transaction
        RequestNodeInfo requestFrame = new RequestNodeInfo((byte)0x2c);
        h.onDataFrameWrite(requestFrame);
        assertEquals(0, ctx.getWriteQueue().size());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertTrue(h.hasCurrentTransaction());

        // confirm request was re-queued
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(0) instanceof RequestNodeInfo);

        // simulate re-send
        requestFrame.incremenentSendCount();
        h.onDataFrameWrite(requestFrame);
        assertTrue(h.hasCurrentTransaction());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(1, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertTrue(h.hasCurrentTransaction());

        // confirm request was re-queued
        assertEquals(2, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(1) instanceof RequestNodeInfo);

        // simulate re-send
        requestFrame.incremenentSendCount();
        h.onDataFrameWrite(requestFrame);
        assertTrue(h.hasCurrentTransaction());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(2, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertFalse(h.hasCurrentTransaction());

        // confirm request was not requeued
        assertEquals(2, ctx.getWriteQueue().size());
    }

    @Test
    public void testAddNodeToNetworkTransaction() {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();
        h.channelRead(ctx, new AddNodeToNetwork(Unpooled.wrappedBuffer(new byte[] {0x01, 0x07, 0x00, 0x4A, 0x01, 0x02, 0x00, 0x00, (byte)0xB1})));
        assertTrue(h.hasCurrentTransaction());
        h.channelRead(ctx, new AddNodeToNetwork(Unpooled.wrappedBuffer(new byte[] {0x01, 0x12, 0x00, 0x4A, 0x01, 0x03, 0x02, 0x0B, 0x04, 0x20, 0x01, 0x30, (byte)0x80, (byte)0x84, 0x71, 0x70, (byte)0x85, (byte)0x86, 0x72, (byte)0xCD})));
        assertTrue(h.hasCurrentTransaction());
        h.channelRead(ctx, new AddNodeToNetwork(Unpooled.wrappedBuffer(new byte[] {0x01, 0x12, 0x00, 0x4A, 0x01, 0x05, 0x02, 0x0B, 0x04, 0x20, 0x01, 0x30, (byte)0x80, (byte)0x84, 0x71, 0x70, (byte)0x85, (byte)0x86, 0x72, (byte)0xCB, 0x18})));
        assertFalse(h.hasCurrentTransaction());
    }

    @Test
    public void testTransactionTimeout() {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();
        h.onDataFrameWrite(new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x03, 0x02, 0x20, 0x02, 0x05, 0x31, (byte)0xF2})));
    }
}
