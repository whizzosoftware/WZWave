package com.whizzosoftware.wzwave.frame;

import org.junit.Test;

import static org.junit.Assert.*;

public class NodeProtocolInfoTest {
    @Test
    public void testRequestConstructor() {
        NodeProtocolInfo mgid = new NodeProtocolInfo((byte)0x01);
        byte[] b = mgid.getBytes();
        assertEquals(6, b.length);
        assertEquals(0x01, b[0]);
        assertEquals(0x04, b[1]);
        assertEquals(0x00, b[2]);
        assertEquals(0x41, b[3]);
        assertEquals(0x01, b[4]);
        assertEquals((byte)0xBB, b[5]);
    }

    @Test
    public void testResponseConstructor() {
        NodeProtocolInfo npi = new NodeProtocolInfo(new byte[] {0x01, 0x09, 0x01, 0x41, (byte)0x92, 0x16, 0x00, 0x02, 0x02, 0x01, 0x33});
        assertTrue(npi.isListening());
        assertTrue(npi.isBeaming());
        assertFalse(npi.isRouting());
        assertEquals(40000, npi.getMaxBaudRate());
        assertEquals(3, npi.getVersion());
        assertFalse(npi.hasSecurity());
        assertEquals(0x02, npi.getBasicDeviceClass());
        assertEquals(0x02, npi.getGenericDeviceClass());
        assertEquals(0x01, npi.getSpecificDeviceClass());
    }
}
