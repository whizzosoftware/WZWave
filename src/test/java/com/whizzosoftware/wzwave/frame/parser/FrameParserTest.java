package com.whizzosoftware.wzwave.frame.parser;

import com.whizzosoftware.wzwave.MockFrameListener;
import com.whizzosoftware.wzwave.commandclass.VersionCommandClass;
import com.whizzosoftware.wzwave.frame.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FrameParserTest {
    @Test
    public void testACKPlusMessage() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x06,0x01,0x10,0x01,0x15,0x5A,0x2D,0x57,0x61,0x76,0x65,0x20,0x32,0x2E,0x37,0x38,0x00,0x01,(byte)0x9B}, 19);
        assertEquals(2, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ACK);
        assertTrue(cl.messages.get(1) instanceof Version);
    }

    @Test
    public void testPartialMessage() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[]{0x06, 0x01, 0x10, 0x01}, 4);
        assertEquals(1, cl.messages.size());
        p.addBytes(new byte[] {0x15,0x5A,0x2D,0x57,0x61,0x76,0x65,0x20,0x32,0x2E,0x37,0x38,0x00,0x01,(byte)0x9B}, 15);
        assertEquals(2, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ACK);
        assertTrue(cl.messages.get(1) instanceof Version);
    }

    @Test
    public void testGetVersionResponse() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x10, 0x01, 0x15, 0x5a, 0x2d, 0x57, 0x61, 0x76, 0x65, 0x20, 0x32, 0x2e, 0x37, 0x38, 0x00, 0x01, (byte)0x9b}, 18);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof Version);
        assertEquals("Z-Wave 2.78\u0000", ((Version) cl.messages.get(0)).getLibraryVersion());
        assertEquals((byte) 0x01, ((Version) cl.messages.get(0)).getLibraryType());
    }

    @Test
    public void testGetMemoryId() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x08, 0x01, 0x20, 0x01, 0x6a, 0x2d, (byte)0xec, 0x01, 0x7d}, 10);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof MemoryGetId);
        MemoryGetId mgid = (MemoryGetId)cl.messages.get(0);
        assertEquals(-20, mgid.getHomeId());
        assertEquals((byte)1, mgid.getNodeId());
        // TODO
    }

    @Test
    public void testGetControllerCapabilities() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x04, 0x01, 0x05, 0x08, (byte)0xf7}, 6);
        assertEquals(1, cl.messages.size());
        // TODO
    }

    @Test
    public void testSerialAPIGetCapabilities() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        byte[] b = new byte[] {0x01, 0x2b, 0x01, 0x07, 0x03, 0x07, 0x00, (byte)0x86, 0x00, 0x02, 0x00, 0x01, (byte)0xfe, (byte)0x80, (byte)0xfe, (byte)0x88, 0x0f, 0x00, 0x00, 0x00, (byte)0xfb, (byte)0x97, 0x7f, (byte)0x82, 0x07, 0x00, 0x00, (byte)0x80, 0x00, (byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xc2};
        p.addBytes(b, b.length);
        assertEquals(1, cl.messages.size());
        // TODO
    }

    @Test
    public void testGetSUCNodeId() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x04, 0x01, 0x56, 0x00, (byte)0xac}, 6);
        assertEquals(1, cl.messages.size());
        // TODO
    }

    @Test
    public void testGetRandom() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        byte[] b = new byte[] {0x01, 0x25, 0x01, 0x1c, 0x01, 0x20, 0x72, 0x61, 0x4a, (byte)0xd4, (byte)0x82, 0x2e, 0x6f, 0x26, (byte)0x8c, 0x11, (byte)0x86, 0x22, 0x76, (byte)0xfb, 0x41, 0x43, 0x24, 0x3d, (byte)0xa1, 0x73, (byte)0xd5, (byte)0xcb, (byte)0xbc, (byte)0xcb, (byte)0x85, 0x22, 0x36, (byte)0xbd, 0x09, 0x01, (byte)0xc7, (byte)0x83, (byte)0xfa};
        p.addBytes(b, b.length);
        assertEquals(1, cl.messages.size());
        // TODO
    }

    @Test
    public void testGetInitData() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1d, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x01, (byte)0xc0}, 39);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof InitData);
        InitData id = (InitData)cl.messages.get(0);
        assertEquals(2, id.getNodes().size());
        assertEquals(1, cl.messages.size());
        // TODO
    }

    @Test
    public void testSetTimeouts() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x05, 0x01, 0x06, 0x64, 0x0f, (byte)0x96}, 7);
        assertEquals(1, cl.messages.size());
        // TODO
    }

    @Test
    public void testNodeInformation() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x09, 0x01, 0x41, (byte)0x92, 0x16, 0x00, 0x02, 0x02, 0x01, 0x33}, 11);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof NodeProtocolInfo);
        NodeProtocolInfo pi = (NodeProtocolInfo)cl.messages.get(0);
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
    public void testSendData1() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8}, 6);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof SendData);
        SendData sd = (SendData)cl.messages.get(0);
        assertFalse(sd.hasCallbackId());
        assertNull(sd.getCallbackId());
        assertTrue(sd.hasRetVal());
        assertEquals((byte)0x01, (byte)sd.getRetVal());
    }

    @Test
    public void testRequestNodeInfo() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[]{0x01, 0x04, 0x01, 0x60, 0x01, (byte) 0x9b}, 6);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof RequestNodeInfo);
        assertTrue(((RequestNodeInfo)cl.messages.get(0)).wasSuccessfullySent());
    }

    @Test
    public void testApplicationUpdate() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x06, 0x00, 0x49, (byte)0x81, 0x00, 0x00, 0x31}, 8);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationUpdate);
    }

    @Test
    public void testGetRoutingInfo() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01,0x20,0x01,(byte)0x80,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x5c}, 34);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof GetRoutingInfo);
        assertEquals(0x02, ((GetRoutingInfo)cl.messages.get(0)).getNodeMask()[0]);
        for (int i=1; i < 29; i++) {
            assertEquals(0x00, ((GetRoutingInfo)cl.messages.get(0)).getNodeMask()[i]);
        }
    }

    @Test
    public void testSendCommandResponse() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] { 0x01, 0x04, 0x01, 0x13, 0x01, (byte)0xe8 }, 6);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof SendData);
        SendData sd = (SendData)cl.messages.get(0);
        assertTrue(sd.hasRetVal());
        assertEquals(Byte.valueOf((byte)0x01), sd.getRetVal());
        assertEquals(DataFrameType.RESPONSE, sd.getType());
    }

    @Test
    public void testSendCommandRequestCallback() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] { 0x01, 0x05, 0x00, 0x13, 0x02, 0x00, (byte)0xeb }, 7);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof SendData);
        SendData sd = (SendData)cl.messages.get(0);
        assertTrue(sd.hasCallbackId());
        assertEquals(Byte.valueOf((byte)0x02), sd.getCallbackId());
        assertEquals(DataFrameType.REQUEST, sd.getType());
    }

    @Test
    public void testExtraneousPrefixBytes() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] { 0x02, 0x03, 0x04, 0x01, 0x05, 0x00, 0x13, 0x02, 0x00, (byte)0xeb }, 10);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof SendData);
        SendData sd = (SendData)cl.messages.get(0);
        assertTrue(sd.hasCallbackId());
        assertEquals(Byte.valueOf((byte)0x02), sd.getCallbackId());
        assertEquals(DataFrameType.REQUEST, sd.getType());
    }

    @Test
    public void testGetNodeVersion() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x0d, 0x00, 0x04, 0x00, 0x0e, 0x07, (byte)0x86, 0x12, 0x06, 0x03, 0x28, 0x03, 0x19, 0x5c}, 15);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)14, ach.getNodeId());
        assertEquals(VersionCommandClass.ID, ach.getCommandClassId());
//        Version v = new Version(ach.getCommandClassBytes());
//        assertEquals("6", v.getLibrary());
//        assertEquals("3.25", v.getApplication());
//        assertEquals("3.40", v.getProtocol());
    }
/*
    @Test
    public void testGetManufacturerSpecificReport() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01,0x0e,0x00,0x04,0x00,0x02,0x08,0x72,0x05,0x00,0x63,0x52,0x50,0x31,0x30,(byte)0xe8}, 16);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)0x02, (byte)ach.getNodeId());
        assertNotNull(ach.getCommand());
        assertTrue(ach.getCommand() instanceof ManufacturerSpecificCommandClass.Report);
        ManufacturerSpecificCommandClass.Report ms = (ManufacturerSpecificCommandClass.Report)ach.getCommand();
        assertEquals(0x63, ms.getManufacturerId());
        assertEquals(0x5250, ms.getProductType());
        assertEquals(0x3130, ms.getProductId());
    }

    @Test
    public void testSwitchAllReport() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x02, 0x03, 0x27, 0x03, (byte)0xff, 0x28}, 11);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)0x02, (byte)ach.getNodeId());
        assertNotNull(ach.getCommand());
        assertTrue(ach.getCommand() instanceof AllSwitchCommandClass.Report);
        AllSwitchCommandClass.Report sa = (AllSwitchCommandClass.Report)ach.getCommand();
        assertEquals(AllSwitchCommandClass.Mode.INCLUDED_ALL, sa.getMode());
    }

    @Test
    public void testPowerLevelReport() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x0a, 0x00, 0x04, 0x00, 0x02, 0x04, 0x73, 0x03, 0x00, (byte)0xff, 0x78}, 12);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)0x02, (byte)ach.getNodeId());
        assertNotNull(ach.getCommand());
        assertTrue(ach.getCommand() instanceof PowerlevelCommandClass.Report);
        PowerlevelCommandClass.Report plr = (PowerlevelCommandClass.Report)ach.getCommand();
        assertEquals(PowerlevelCommandClass.Level.NORMAL, plr.getPowerLevel());
        assertEquals((byte)0xFF, plr.getTimeout());
    }

    @Test
    public void testProtectionReport() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x02, 0x03, 0x75, 0x03, 0x00, (byte)0x85}, 11);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)0x02, (byte)ach.getNodeId());
        assertNotNull(ach.getCommand());
        assertTrue(ach.getCommand() instanceof ProtectionCommandClass.Report);
        ProtectionCommandClass.Report plr = (ProtectionCommandClass.Report)ach.getCommand();
        assertEquals(ProtectionCommandClass.State.UNPROTECTED, plr.getState());
    }

    @Test
    public void testNodeNamingAndLocationReport() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x17, 0x00, 0x04, 0x00, 0x02, 0x11, 0x77, 0x03, 0x00, 0x4f, 0x75, 0x74, 0x64, 0x6f, 0x6f, 0x72, 0x20, 0x53, 0x77, 0x69, 0x74, 0x63, 0x68, (byte)0xc1}, 25);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)0x02, (byte)ach.getNodeId());
        assertNotNull(ach.getCommand());
        assertTrue(ach.getCommand() instanceof NodeNamingAndLocationCommandClass.Report);
        NodeNamingAndLocationCommandClass.Report plr = (NodeNamingAndLocationCommandClass.Report)ach.getCommand();
        assertEquals("Outdoor Switch", plr.getName());
    }

    @Test
    public void testNodeNamingAndLocationReport2() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x02, 0x03, 0x77, 0x06, 0x00, (byte)0x82}, 11);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)0x02, (byte)ach.getNodeId());
        assertNotNull(ach.getCommand());
        assertTrue(ach.getCommand() instanceof NodeNamingAndLocationCommandClass.Report);
        NodeNamingAndLocationCommandClass.Report plr = (NodeNamingAndLocationCommandClass.Report)ach.getCommand();
        assertEquals("", plr.getLocation());
    }

    @Test
    public void testBasicGetReport() {
        MockFrameListener cl = new MockFrameListener();
        FrameParser p = new FrameParser(cl);
        p.addBytes(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x02, 0x03, 0x20, 0x03, 0x00, (byte)0xd0}, 11);
        assertEquals(1, cl.messages.size());
        assertTrue(cl.messages.get(0) instanceof ApplicationCommand);
        ApplicationCommand ach = (ApplicationCommand)cl.messages.get(0);
        assertEquals(0x00, ach.getStatus());
        assertEquals((byte)0x02, (byte)ach.getNodeId());
        assertNotNull(ach.getCommand());
        assertTrue(ach.getCommand() instanceof BasicCommandClass.Report);
        BasicCommandClass.Report plr = (BasicCommandClass.Report)ach.getCommand();
        assertEquals(0x00, plr.getValue());
    }
*/
}
