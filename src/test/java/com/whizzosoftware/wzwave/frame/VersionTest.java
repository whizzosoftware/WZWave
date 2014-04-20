package com.whizzosoftware.wzwave.frame;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionTest {
    @Test
    public void testRequestConstructor() {
        Version v = new Version();
        byte[] b = v.getBytes();
        assertEquals(5, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x03, b[1]); // length
        assertEquals(0x00, b[2]); // type (request)
        assertEquals(0x15, b[3]); // command ID
        assertEquals((byte)0xE9, b[4]); // checksum
    }

    @Test
    public void testResponseConstructor() {
        Version v = new Version(new byte[] {0x01, 0x10, 0x01, 0x15, 0x5a, 0x2d, 0x57, 0x61, 0x76, 0x65, 0x20, 0x32, 0x2e, 0x37, 0x38, 0x00, 0x01, (byte)0x9b});
        assertEquals("Z-Wave 2.78\u0000", v.getLibraryVersion());
        assertEquals(0x01, v.getLibraryType());
    }
}
