package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.channel.MockZWaveChannelContext;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionStartedEvent;
import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.CAN;
import com.whizzosoftware.wzwave.frame.OutboundDataFrame;
import com.whizzosoftware.wzwave.frame.Version;
import org.junit.Test;
import static org.junit.Assert.*;

public class RequestResponseTransactionTest {
    @Test
    public void testSuccess() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        RequestResponseTransaction t = new RequestResponseTransaction(ctx, new Version(), true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        t.addFrame(ctx, new ACK());
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        t.addFrame(ctx, new Version("version", (byte)0x01));
        assertTrue(t.isComplete());
        assertEquals(2, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(1) instanceof TransactionCompletedEvent);
        assertTrue(((TransactionCompletedEvent)ctx.getUserEvents().get(1)).getFrame() instanceof Version);
    }

    @Test
    public void testCANBeforeACK() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        Version startFrame = new Version();
        RequestResponseTransaction t = new RequestResponseTransaction(ctx, startFrame, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        assertTrue(t.addFrame(ctx, new CAN()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        Version sd = (Version)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);
        assertEquals(-1, sd.getSendCount());
    }

    @Test
    public void testCANAfterACK() {
        MockZWaveChannelContext ctx = new MockZWaveChannelContext();

        Version startFrame = new Version();
        RequestResponseTransaction t = new RequestResponseTransaction(ctx, startFrame, true);
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());
        assertTrue(ctx.getUserEvents().get(0) instanceof TransactionStartedEvent);

        assertTrue(t.addFrame(ctx, new ACK()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(0, ctx.getWriteQueue().size());

        assertTrue(t.addFrame(ctx, new CAN()));
        assertFalse(t.isComplete());
        assertEquals(1, ctx.getUserEvents().size());
        assertEquals(1, ctx.getWriteQueue().size());

        assertTrue(ctx.getWriteQueue().get(0) instanceof OutboundDataFrame);
        Version sd = (Version)((OutboundDataFrame)ctx.getWriteQueue().get(0)).getDataFrame();
        assertTrue(sd == startFrame);
        assertEquals(-1, sd.getSendCount());
    }
}
