package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.channel.MockZWaveChannelContext;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionStartedEvent;
import com.whizzosoftware.wzwave.frame.*;
import io.netty.buffer.Unpooled;

import org.junit.Test;
import static org.junit.Assert.*;

public class RequestNodeInfoTransactionTest {
    @Test
    public void testSendSuccess() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();
        RequestNodeInfo startFrame = new RequestNodeInfo((byte)0x2c);

        // start transaction
        RequestNodeInfoTransaction t = new RequestNodeInfoTransaction(ctx, startFrame, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive RequestNodeInfo
        assertTrue(t.addFrame(null, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x01, (byte)0x9b}))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive ApplicationUpdate
        t.addFrame(ctx, new ApplicationUpdate(DataFrameType.REQUEST, ApplicationUpdate.UPDATE_STATE_NODE_INFO_RECEIVED, (byte)0x2c));
        assertTrue(t.isComplete());
        assertEquals(2, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
    }

    @Test
    public void testSendFailure() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        RequestNodeInfo startFrame = new RequestNodeInfo((byte)0x2c);

        // start transaction
        RequestNodeInfoTransaction t = new RequestNodeInfoTransaction(ctx, startFrame, true);
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        // receive ACK
        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive RequestNodeInfo
        assertTrue(t.addFrame(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[]{0x01, 0x04, 0x01, 0x60, 0x00, (byte) 0x9a}))));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        assertTrue(((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame() == startFrame);
    }
}
