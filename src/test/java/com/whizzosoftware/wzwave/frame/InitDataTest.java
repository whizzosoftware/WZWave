package com.whizzosoftware.wzwave.frame;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class InitDataTest {
    @Test
    public void testRequestConstructor() {
        InitData v = new InitData();
        byte[] b = v.getBytes();
        assertEquals(5, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x03, b[1]); // length
        assertEquals(0x00, b[2]); // type (request)
        assertEquals(0x02, b[3]); // command ID
        assertEquals((byte)0xFE, b[4]); // checksum
    }

    @Test
    public void testResponseConstructor() {
        InitData id = new InitData(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1d, 0x01, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x01, (byte)0xe2});
        List<Byte> nodes = id.getNodes();
        assertEquals(2, nodes.size());
        assertEquals(1, (int)nodes.get(0));
        assertEquals(14, (int)nodes.get(1));
    }
}
