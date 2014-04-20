package com.whizzosoftware.wzwave.frame;

import org.junit.Test;

import static org.junit.Assert.*;

public class RequestNodeInfoTest {
    @Test
    public void testRequestConstructor() {
        RequestNodeInfo mgid = new RequestNodeInfo((byte)0x01);
        byte[] b = mgid.getBytes();
        assertEquals(6, b.length);
        assertEquals(0x01, b[0]);
        assertEquals(0x04, b[1]);
        assertEquals(0x00, b[2]);
        assertEquals(0x60, b[3]);
        assertEquals(0x01, b[4]);
        assertEquals((byte)0x9A, b[5]);
    }

    @Test
    public void testResponseConstructor() {
        RequestNodeInfo rni = new RequestNodeInfo(new byte[] {0x01, 0x05, 0x01, 0x01, 0x60, 0x01, (byte)0x9B});
        assertNotNull(rni.getRetVal());
//        assertEquals((byte)0x01, (byte)rni.getRetVal());
//        assertTrue(rni.wasSuccessfullySent());
    }
}
