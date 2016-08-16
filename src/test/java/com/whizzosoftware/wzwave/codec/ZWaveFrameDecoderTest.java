/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.codec;

import java.util.ArrayList;
import java.util.List;

import com.whizzosoftware.wzwave.commandclass.VersionCommandClass;
import com.whizzosoftware.wzwave.frame.*;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for the ZWaveFrameDecoder.
 *
 * @author Dan Noguerol
 */
public class ZWaveFrameDecoderTest {
    @Test
    public void testACK() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x06});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof ACK);
    }

    @Test
    public void testNAK() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x15});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof NAK);
    }

    @Test
    public void testCAN() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x18});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof CAN);
    }

    @Test
    public void testACKPlusMessage() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x06,0x01,0x10,0x01,0x15,0x5A,0x2D,0x57,0x61,0x76,0x65,0x20,0x32,0x2E,0x37,0x38,0x00,0x01,(byte)0x9B});
        decoder.decode(null, in, out);
        assertEquals(2, out.size());
        assertTrue(out.get(0) instanceof ACK);
        assertTrue(out.get(1) instanceof Version);
    }

    @Test
    public void testPartialMessage() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<>();
        ByteBuf in = wrappedBuffer(new byte[] {0x06, 0x01, 0x10, 0x01});
        decoder.decode(null, in, out);
        assertEquals(0, in.readableBytes());
        assertEquals(0, out.size());
        in = wrappedBuffer(new byte[] {0x15,0x5A,0x2D,0x57,0x61,0x76,0x65,0x20,0x32,0x2E,0x37,0x38,0x00,0x01,(byte)0x9B});
        assertEquals(15, in.readableBytes());
        decoder.decode(null, in, out);
        assertEquals(0, in.readableBytes());
        assertEquals(2, out.size());
        assertTrue(out.get(0) instanceof ACK);
        assertTrue(out.get(1) instanceof Version);
    }

    @Test
    public void testGetVersionResponse() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x10, 0x01, 0x15, 0x5a, 0x2d, 0x57, 0x61, 0x76, 0x65, 0x20, 0x32, 0x2e, 0x37, 0x38, 0x00, 0x01, (byte)0x9b});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof Version);
        assertEquals("Z-Wave 2.78", ((Version)out.get(0)).getLibraryVersion());
        assertEquals((byte) 0x01, ((Version) out.get(0)).getLibraryType());
    }

    @Test
    public void testGetMemoryId() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x08, 0x01, 0x20, 0x01, 0x6a, 0x2d, (byte)0xec, 0x01, 0x7d});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof MemoryGetId);
        MemoryGetId mgid = (MemoryGetId)out.get(0);
        assertEquals(-20, (int)mgid.getHomeId());
        assertEquals((byte)1, (byte)mgid.getNodeId());
        // TODO
    }

    @Test
    public void testGetSUCNodeId() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x56, 0x00, (byte)0xac});
        decoder.decode(null, in, out);
        assertEquals(0, buffer().readableBytes());
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof GetSUCNodeId);
        assertEquals(0, ((GetSUCNodeId)out.get(0)).getSucNodeId());
    }

    @Test
    public void testGetInitData() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1d, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x01, (byte)0xc0});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof InitData);
        InitData id = (InitData)out.get(0);
        assertEquals(2, id.getNodes().size());
        assertEquals(1, out.size());
        // TODO
    }

    @Test
    public void testNodeInformation() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x09, 0x01, 0x41, (byte)0x92, 0x16, 0x00, 0x02, 0x02, 0x01, 0x33});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof NodeProtocolInfo);
        NodeProtocolInfo pi = (NodeProtocolInfo)out.get(0);
        assertTrue(pi.isListening());
        assertTrue(pi.isBeaming());
        assertFalse(pi.isRouting());
        assertEquals(40000, pi.getMaxBaudRate());
        assertEquals(3, pi.getVersion());
        assertFalse(pi.hasSecurity());
        assertEquals(0x02, pi.getBasicDeviceClass());
        assertEquals(0x02, pi.getGenericDeviceClass());
        assertEquals(0x01, pi.getSpecificDeviceClass());
    }

    @Test
    public void testSendData1() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof SendData);
        SendData sd = (SendData)out.get(0);
        assertFalse(sd.hasCallbackId());
        assertNull(sd.getCallbackId());
        assertTrue(sd.hasRetVal());
        assertEquals((byte)0x01, (byte)sd.getRetVal());
    }

    @Test
    public void testRequestNodeInfo() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x60, 0x01, (byte) 0x9b});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof RequestNodeInfo);
        assertTrue(((RequestNodeInfo)out.get(0)).wasSuccessfullySent());
    }

    @Test
    public void testApplicationUpdate() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x06, 0x00, 0x49, (byte)0x81, 0x00, 0x00, 0x31});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof ApplicationUpdate);
    }

    @Test
    public void testGetRoutingInfo() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01,0x20,0x01,(byte)0x80,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x5c});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof GetRoutingInfo);
        assertEquals(0x02, ((GetRoutingInfo)out.get(0)).getNodeMask()[0]);
        for (int i=1; i < 29; i++) {
            assertEquals(0x00, ((GetRoutingInfo)out.get(0)).getNodeMask()[i]);
        }
    }

    @Test
    public void testSendCommandResponse() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof SendData);
        SendData sd = (SendData)out.get(0);
        assertTrue(sd.hasRetVal());
        assertEquals(Byte.valueOf((byte)0x01), sd.getRetVal());
        assertEquals(DataFrameType.RESPONSE, sd.getType());
    }

    @Test
    public void testSendCommandRequestCallback() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x02, 0x00, (byte)0xeb});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof SendData);
        SendData sd = (SendData)out.get(0);
        assertTrue(sd.hasCallbackId());
        assertEquals(Byte.valueOf((byte)0x02), sd.getCallbackId());
        assertEquals(DataFrameType.REQUEST, sd.getType());
    }

    @Test
    public void testExtraneousPrefixBytes() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<>();
        ByteBuf in = wrappedBuffer(new byte[] {0x02, 0x03, 0x04, 0x01, 0x05, 0x00, 0x13, 0x02, 0x00, (byte)0xeb});
        assertEquals(10, in.readableBytes());
        decoder.decode(null, in, out);
        assertEquals(0, in.readableBytes());
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof SendData);
        SendData sd = (SendData)out.get(0);
        assertTrue(sd.hasCallbackId());
        assertEquals(Byte.valueOf((byte)0x02), sd.getCallbackId());
        assertEquals(DataFrameType.REQUEST, sd.getType());
    }

    @Test
    public void testGetNodeVersion() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x0d, 0x00, 0x04, 0x00, 0x0e, 0x07, (byte)0x86, 0x12, 0x06, 0x03, 0x28, 0x03, 0x19, 0x5c});
        decoder.decode(null, in, out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)out.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)14, ach.getNodeId());
        assertEquals(VersionCommandClass.ID, ach.getCommandClassId());
    }

    @Test
    public void testOneFrameAcrossTwoReads() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<>();
        ByteBuf buf = wrappedBuffer(new byte[] {0x01, 0x10, 0x01, 0x15, 0x5a, 0x2d, 0x57, 0x61, 0x76, 0x65});
        assertEquals(10, buf.readableBytes());
        decoder.decode(null, buf, out);
        assertEquals(0, buf.readableBytes());
        assertEquals(0, out.size());
        buf = wrappedBuffer(new byte[] {0x20, 0x32, 0x2e, 0x37, 0x38, 0x00, 0x01, (byte)0x9b});
        assertEquals(8, buf.readableBytes());
        decoder.decode(null, buf, out);
        assertEquals(0, buf.readableBytes());
        assertEquals(1, out.size());
        assertTrue (out.get(0) instanceof Version);
    }

    @Test
    public void testTwoFramesAcrossTwoReads() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x0d, 0x00, 0x04, 0x00, 0x0e, 0x07, (byte)0x86, 0x12, 0x06, 0x03, 0x28, 0x03, 0x19, 0x5c, 0x01, 0x0d});
        assertEquals(17, in.readableBytes());
        decoder.decode(null, in, out);
        assertEquals(0, in.readableBytes());
        assertEquals(1, out.size());
        in = wrappedBuffer(new byte[] {0x00, 0x04, 0x00, 0x0e, 0x07, (byte)0x86, 0x12, 0x06, 0x03, 0x28, 0x03, 0x19, 0x5c});
        assertEquals(13, in.readableBytes());
        decoder.decode(null, in, out);
        assertEquals(0, in.readableBytes());
        assertEquals(2, out.size());
        assertTrue(out.get(0) instanceof ApplicationCommand);
        assertTrue(out.get(1) instanceof ApplicationCommand);
    }

    @Test
    public void testRandom2() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        ByteBuf in = wrappedBuffer(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1d, 0x01, 0x00, 0x00, 0x00, (byte)0xc0, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x01, 0x02});
        assertEquals(39, in.readableBytes());
        decoder.decode(null, in, out);
        assertEquals(0, in.readableBytes());
        assertEquals(1, in.refCnt());
    }

    @Test
    public void testFuncCallback() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<Object>();
        decoder.decode(null, wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x01, (byte)0xC2}), out);
        decoder.decode(null, wrappedBuffer(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xE8}), out);
        decoder.decode(null, wrappedBuffer(new byte[] {0x01, 0x05, 0x00, 0x13, 0x01, 0x00, (byte)0xE8}), out);
        assertEquals(3, out.size());
    }

    @Test
    public void testIncompleteWithMultipleAttempts() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<>();
        decoder.decode(null, wrappedBuffer(new byte[] {0x06, 0x01, 0x08, 0x01, 0x20, (byte)0xD3, 0x3C, (byte)0xB4, 0x11, 0x01}), out);
        assertEquals(0, out.size());
        decoder.decode(null, wrappedBuffer(new byte[] {0x06, 0x01, 0x08, 0x01, 0x20, (byte)0xD3, 0x3C, (byte)0xB4, 0x11}), out);
        assertEquals(0, out.size());
        decoder.decode(null, wrappedBuffer(new byte[] {0x06, 0x01, 0x08, 0x01, 0x20, (byte)0xD3, 0x3C, (byte)0xB4, 0x11, 0x01, (byte)0x9D, 0x01}), out);
        assertEquals(2, out.size());
        assertTrue(out.get(0) instanceof ACK);
        assertTrue(out.get(1) instanceof MemoryGetId);
        out.clear();
        decoder.decode(null, wrappedBuffer(new byte[] {0x05, 0x00, 0x13, 0x01, 0x00, (byte)0xE8}), out);
        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof SendData);
    }

    @Test
    public void testIncompleteWithMultipleAttempts2() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<>();
        ByteBuf buf = wrappedBuffer(new byte[] {0x06});
        assertEquals(1, buf.readableBytes());
        decoder.decode(null, buf, out);
        assertEquals(1, out.size());
        assertEquals(0, buf.readableBytes());

        buf = wrappedBuffer(new byte[] {0x01, 0x25, 0x01});
        assertEquals(3, buf.readableBytes());
        decoder.decode(null, buf, out);
        assertEquals(1, out.size());
        assertEquals(0, buf.readableBytes());
        assertEquals(1, buf.refCnt());

        buf = wrappedBuffer(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1D, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertEquals(19, buf.readableBytes());
        decoder.decode(null, buf, out);
        assertEquals(1, out.size());
        assertEquals(0, buf.readableBytes());
        assertEquals(1, buf.refCnt());

        buf = wrappedBuffer(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1D, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, (byte)0xC3});
        assertEquals(39, buf.readableBytes());
        decoder.decode(null, buf, out);
        assertEquals(2, out.size());
        assertEquals(0, buf.readableBytes());
        assertTrue(out.get(0) instanceof ACK);
        assertTrue(out.get(1) instanceof InitData);

        out = new ArrayList<>();
        decoder.decode(null, wrappedBuffer(new byte[] {0x06, 0x01, 0x09, 0x01, 0x41, (byte)0x93, 0x16, 0x01, 0x02, 0x02, 0x01, 0x33}), out);
        assertEquals(2, out.size());
        assertTrue(out.get(0) instanceof ACK);
        assertTrue(out.get(1) instanceof NodeProtocolInfo);
    }

    @Test
    public void testTwoCompleteFramesAtOnce() throws Exception {
        ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();
        List<Object> out = new ArrayList<>();
        ByteBuf buf = wrappedBuffer(new byte[] {0x01, 0x09, 0x00, 0x13, 0x06, 0x02, 0x25, 0x02, 0x05, 0x01, (byte)0xC2, 0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xE8});
        assertEquals(17, buf.readableBytes());
        decoder.decode(null, buf, out);
        assertEquals(0, buf.readableBytes());
        assertEquals(2, out.size());
    }
}
