package com.whizzosoftware.wzwave.frame;

import org.junit.Test;
import static org.junit.Assert.*;

public class MemoryGetIdTest {
    @Test
    public void testRequestConstructor() {
        MemoryGetId mgid = new MemoryGetId();
        byte[] b = mgid.getBytes();
        assertEquals(5, b.length);
        assertEquals(0x01, b[0]);
        assertEquals(0x03, b[1]);
        assertEquals(0x00, b[2]);
        assertEquals(0x20, b[3]);
        assertEquals((byte)0xDC, b[4]);
    }

    @Test
    public void testResponseConstructor() {
        MemoryGetId mgid = new MemoryGetId(new byte[] {0x01, 0x08, 0x01, 0x20, 0x01, 0x6a, 0x2d, (byte)0xec, 0x01, 0x7d});
        assertEquals(-20, mgid.getHomeId()); // TODO
        assertEquals(0x01, mgid.getNodeId());
    }
}
