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

import com.whizzosoftware.wzwave.channel.event.*;
import com.whizzosoftware.wzwave.frame.*;
import io.netty.buffer.Unpooled;

import org.junit.Test;
import static org.junit.Assert.*;

public class TransactionInboundHandlerTest {
    @Test
    public void testRequestNodeInfoApplicationUpdate() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        // initiate new RequestNodeInfo transaction
        RequestNodeInfo requestFrame = new RequestNodeInfo((byte)0x02);
        h.userEventTriggered(ctx, new DataFrameSentEvent(requestFrame, true));
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        assertFalse(h.getTransactionContext().isComplete());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(1, ctx.getUserEvents().size());
        assertFalse(h.getTransactionContext().isComplete());

        // receive successful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, RequestNodeInfo.UPDATE_STATE_NODE_INFO_RECEIVED, (byte)0x9a})));
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(h.hasCurrentTransaction());
        assertFalse(h.getTransactionContext().isComplete());

        // receive application update callback
        h.channelRead(ctx, new ApplicationUpdate(Unpooled.wrappedBuffer(new byte[] {0x01, 16, 0x00, 0x49, (byte)0x84, 0x02, 0x0a, 0x04, 0x10, 0x01, 0x25, 0x27, 0x75, 0x73, (byte)0x86, 0x72, 0x77, (byte)0xb8})));
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(2, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
        assertFalse(h.hasCurrentTransaction());

        TransactionCompletedEvent tce = (TransactionCompletedEvent)ctx.getUserEvents().get(1);
        assertTrue(tce.hasFrame());
        assertTrue(tce.getFrame() instanceof ApplicationUpdate);
        assertEquals(0x02, (byte)((ApplicationUpdate)tce.getFrame()).getNodeId());
    }

    @Test
    public void testRequestNodeInfoApplicationUpdateWithNoNodeId() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        // initiate new RequestNodeInfo transaction
        RequestNodeInfo requestFrame = new RequestNodeInfo((byte)0x02);
        h.userEventTriggered(ctx, new DataFrameSentEvent(requestFrame, true));
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        assertFalse(h.getTransactionContext().isComplete());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(1, ctx.getUserEvents().size());
        assertFalse(h.getTransactionContext().isComplete());

        // receive successful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, RequestNodeInfo.UPDATE_STATE_NODE_INFO_RECEIVED, (byte)0x9a})));
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(h.hasCurrentTransaction());
        assertFalse(h.getTransactionContext().isComplete());

        // receive application update callback
        h.channelRead(ctx, new ApplicationUpdate(Unpooled.wrappedBuffer(new byte[] {0x01, 16, 0x00, 0x49, (byte)0x84, 0x00, 0x0a, 0x04, 0x10, 0x01, 0x25, 0x27, 0x75, 0x73, (byte)0x86, 0x72, 0x77, (byte)0xb8})));
        assertEquals(0, ctx.getWriteQueue().size());
        assertEquals(2, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
        assertFalse(h.hasCurrentTransaction());

        TransactionCompletedEvent tce = (TransactionCompletedEvent)ctx.getUserEvents().get(1);
        assertTrue(tce.hasFrame());
        assertTrue(tce.getFrame() instanceof ApplicationUpdate);
        assertEquals(0x02, (byte)((ApplicationUpdate)tce.getFrame()).getNodeId());
    }

    @Test
    public void testRequestNodeInfoFailure() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        // initiate new RequestNodeInfo transaction
        RequestNodeInfo requestFrame = new RequestNodeInfo((byte)0x2c);
        h.userEventTriggered(ctx, new DataFrameSentEvent(requestFrame, true));
        assertEquals(0, ctx.getWriteQueue().size());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, RequestNodeInfo.UPDATE_STATE_NODE_INFO_REQ_FAILED, (byte)0x9a})));
        assertTrue(h.hasCurrentTransaction());

        // confirm request was re-queued
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        assertTrue(((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame() instanceof RequestNodeInfo);

        // simulate re-send
        requestFrame.incremenentSendCount();
        h.userEventTriggered(ctx, new DataFrameSentEvent(requestFrame, true));
        assertTrue(h.hasCurrentTransaction());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(1, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertTrue(h.hasCurrentTransaction());

        // confirm request was re-queued
        assertEquals(2, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(1) instanceof OutboundDataFrame);
        assertTrue(((OutboundDataFrame)ctx.getWriteQueue().get(1)).getDataFrame() instanceof RequestNodeInfo);

        // simulate re-send
        requestFrame.incremenentSendCount();
        h.userEventTriggered(ctx, new DataFrameSentEvent(requestFrame, true));
        assertTrue(h.hasCurrentTransaction());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(2, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertFalse(h.hasCurrentTransaction());

        // confirm request was not re-queued
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
    public void testSendDataTransactionTimeout() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();
        assertFalse(h.hasCurrentTransaction());
        h.userEventTriggered(ctx, new DataFrameSentEvent(new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x03, 0x02, 0x20, 0x02, 0x05, 0x31, (byte)0xF2})), true));
        assertTrue(h.hasCurrentTransaction());
        h.userEventTriggered(ctx, new TransactionTimeoutEvent(h.getTransactionContext().getId()));
        assertFalse(h.hasCurrentTransaction());
    }

    @Test
    public void testSendDataSleepingNodeTransactionNoACKFailure() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        h.userEventTriggered(ctx, new DataFrameSentEvent(new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x00, 0x00, 0x25, 0x0a, (byte)0xce})), false));
        assertEquals(1, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        h.channelRead(ctx, new ACK());
        assertEquals(1, ctx.getUserEvents().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8})));
        assertEquals(1, ctx.getUserEvents().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x0a, 0x01, (byte)0xe2})));
        assertEquals(2, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionFailedEvent);
    }

    @Test
    public void testSendDataSleepingNodeTransactionNetworkCongestionFailure() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        h.userEventTriggered(ctx, new DataFrameSentEvent(new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x00, 0x00, 0x25, 0x0a, (byte)0xce})), false));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        h.channelRead(ctx, new ACK());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8})));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x0a, 0x02, (byte)0xe2})));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
    }

    @Test
    public void testSendDataResponseFrameTimeout() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        h.userEventTriggered(ctx, new DataFrameSentEvent(new SendData("", (byte)0x01, new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x00, 0x00, 0x25, 0x0a, (byte)0xce}, (byte)0x05, true), true));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        h.channelRead(ctx, new ACK());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8})));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x0a, 0x00, (byte)0xe2})));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        h.userEventTriggered(ctx, new TransactionTimeoutEvent(h.getTransactionContext().getId()));
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        assertTrue(((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame() instanceof SendData);
    }

    @Test
    public void testSendDataSuccessfulTransaction() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();

        h.userEventTriggered(ctx, new DataFrameSentEvent(new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x00, 0x00, 0x25, 0x0a, (byte)0xce})), false));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);
        h.channelRead(ctx, new ACK());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8})));
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        h.channelRead(ctx, new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x0a, 0x00, (byte)0xe2})));
        assertEquals(2, ctx.getUserEvents().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
    }

    @Test
    public void testTransactionTimeoutExtension() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        TransactionInboundHandler h = new TransactionInboundHandler();
        h.handlerAdded(ctx);
        assertFalse(h.hasCurrentTransaction());

        long now = System.currentTimeMillis();
        h.processEvent(ctx, new DataFrameSentEvent(new SendData(Unpooled.wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x03, 0x02, 0x20, 0x02, 0x05, 0x31, (byte)0xF2})), true), now);
        assertTrue(h.hasCurrentTransaction());
        assertEquals(now, h.getTransactionContext().getTimeoutStartTime());

        now = System.currentTimeMillis();
        h.userEventTriggered(ctx, new IncompleteDataFrameEvent());
        assertTrue(h.hasCurrentTransaction());
        assertEquals(now, h.getTransactionContext().getTimeoutStartTime());
    }
}
