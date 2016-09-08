package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.ApplicationUpdate;
import com.whizzosoftware.wzwave.frame.DataFrameType;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;
import io.netty.buffer.Unpooled;

import org.junit.Test;
import static org.junit.Assert.*;

public class RequestNodeInfoTransactionTest {
    @Test
    public void testSendSuccess() {
        RequestNodeInfo startFrame = new RequestNodeInfo((byte)0x2c);

        // start transaction
        RequestNodeInfoTransaction t = new RequestNodeInfoTransaction(startFrame, true);

        // receive ACK
        assertTrue(t.addFrame(new ACK()));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        // receive RequestNodeInfo
        assertTrue(t.addFrame(new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x01, (byte)0x9b}))));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        // receive ApplicationUpdate
        t.addFrame(new ApplicationUpdate(DataFrameType.REQUEST, ApplicationUpdate.UPDATE_STATE_NODE_INFO_RECEIVED, (byte)0x2c));
        assertTrue(t.isComplete());
        assertFalse(t.hasError());
    }

    @Test
    public void testSendFailure() {
        RequestNodeInfo startFrame = new RequestNodeInfo((byte)0x2c);

        // start transaction
        RequestNodeInfoTransaction t = new RequestNodeInfoTransaction(startFrame, true);

        // receive ACK
        assertTrue(t.addFrame(new ACK()));

        // receive RequestNodeInfo
        assertTrue(t.addFrame(new RequestNodeInfo(Unpooled.wrappedBuffer(new byte[]{0x01, 0x04, 0x01, 0x60, 0x00, (byte) 0x9a}))));

        assertTrue(t.isComplete());
        assertTrue(t.hasError());
    }
}
