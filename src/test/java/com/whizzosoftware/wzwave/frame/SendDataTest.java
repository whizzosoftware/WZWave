package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.MockFrameListener;
import com.whizzosoftware.wzwave.frame.parser.FrameParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class SendDataTest {
    @Test
    public void testMessageArgConstructor() {
        SendData sd = new SendData("", (byte)0x02, new byte[] {(byte)0xFF}, (byte)0x05, (byte)0x01, true);
        byte[] mb = sd.getBytes();
        assertEquals(10, mb.length);
        assertEquals(0x01, mb[0]);
        assertEquals(0x08, mb[1]);
        assertEquals(0x00, mb[2]);
        assertEquals(0x13, mb[3]);
        assertEquals(0x02, mb[4]);
        assertEquals(0x01, mb[5]);
        assertEquals((byte)0xFF, mb[6]);
        assertEquals(0x05, mb[7]);
        assertEquals(0x01, mb[8]);
        assertEquals(28, mb[9]);
    }

    @Test
    public void testMessageByteArrayConstructorWithRequest() {
        SendData sd = new SendData(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x08, -45});
        assertEquals(DataFrame.Type.REQUEST, sd.getType());
        assertFalse(sd.hasRetVal());
        assertEquals((byte)0x06, sd.getNodeId());
        assertTrue(sd.hasCallbackId());
        assertEquals((byte)0x08, (byte)sd.getCallbackId());
    }

    @Test
    public void testMessageByteArrayConstructorWithRetval() {
        SendData sd = new SendData(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xE8});
        assertEquals(DataFrame.Type.RESPONSE, sd.getType());
        assertTrue(sd.hasRetVal());
        assertEquals((byte)0x01, (byte)sd.getRetVal());
    }

    @Test
    public void testFuncCallback() {
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);
        parser.addBytes(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x01, (byte)0xC2}, 11);
        parser.addBytes(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xE8}, 6);
        parser.addBytes(new byte[] {0x01, 0x05, 0x00, 0x13, 0x01, 0x00, (byte)0xE8}, 7);
        assertEquals(3, listener.messages.size());
    }
}
