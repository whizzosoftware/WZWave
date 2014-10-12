package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;
import io.netty.buffer.Unpooled;

import org.junit.Test;
import static org.junit.Assert.*;

public class ZWaveDataFrameTransactionInbountHandlerTest {
    @Test
    public void testRequestNodeInfoFailure() {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ZWaveDataFrameTransactionInboundHandler h = new ZWaveDataFrameTransactionInboundHandler();

        // initiate new RequestNodeInfo transaction
        RequestNodeInfo requestFrame = new RequestNodeInfo((byte)0x2c);
        h.onDataFrameWrite(requestFrame);
        assertEquals(0, ctx.getWriteQueue().size());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(0, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertFalse(h.hasCurrentRequestTransaction());

        // confirm request was re-queued
        assertEquals(1, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(0) instanceof RequestNodeInfo);

        // simulate re-send
        requestFrame.incremenentSendCount();
        h.onDataFrameWrite(requestFrame);
        assertTrue(h.hasCurrentRequestTransaction());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(1, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertFalse(h.hasCurrentRequestTransaction());

        // confirm request was re-queued
        assertEquals(2, ctx.getWriteQueue().size());
        assertTrue(ctx.getWriteQueue().get(1) instanceof RequestNodeInfo);

        // simulate re-send
        requestFrame.incremenentSendCount();
        h.onDataFrameWrite(requestFrame);
        assertTrue(h.hasCurrentRequestTransaction());

        // receive ACK
        h.channelRead(ctx, new ACK());
        assertEquals(2, ctx.getWriteQueue().size());

        // receive unsuccessful send
        h.channelRead(ctx, new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x00, (byte)0x9a})));
        assertFalse(h.hasCurrentRequestTransaction());

        // confirm request was not requeued
        assertEquals(2, ctx.getWriteQueue().size());
    }
}
