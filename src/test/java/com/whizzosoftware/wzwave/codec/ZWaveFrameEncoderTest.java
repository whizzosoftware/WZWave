package com.whizzosoftware.wzwave.codec;

import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.CAN;
import com.whizzosoftware.wzwave.frame.NAK;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZWaveFrameEncoderTest {
    @Test
    public void testACK() throws Exception {
        ZWaveFrameEncoder encoder = new ZWaveFrameEncoder();
        ByteBuf buf = Unpooled.buffer();
        encoder.encode(null, new ACK(), buf);
        assertEquals(1, buf.readableBytes());
        assertEquals(ACK.ID, buf.getByte(0));
    }

    @Test
    public void testNAK() throws Exception {
        ZWaveFrameEncoder encoder = new ZWaveFrameEncoder();
        ByteBuf buf = Unpooled.buffer();
        encoder.encode(null, new NAK(), buf);
        assertEquals(1, buf.readableBytes());
        assertEquals(NAK.ID, buf.getByte(0));
    }

    @Test
    public void testCAN() throws Exception {
        ZWaveFrameEncoder encoder = new ZWaveFrameEncoder();
        ByteBuf buf = Unpooled.buffer();
        encoder.encode(null, new CAN(), buf);
        assertEquals(1, buf.readableBytes());
        assertEquals(CAN.ID, buf.getByte(0));
    }
}
