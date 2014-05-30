/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.MockFrameListener;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.controller.serial.SerialZWaveController;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.parser.FrameParser;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;
import com.whizzosoftware.wzwave.frame.ACK;
import org.junit.Test;
import static org.junit.Assert.*;

public class SerialZWaveControllerTest {
    /*
    @Test
    public void testWriteWithNoACK() {
        MockControllerWriteDelegate wd = new MockControllerWriteDelegate();
        Controller c = new Controller(new MockSerialChannel(), wd);

        c.sendCommand((byte)0x02, BinarySwitchCommandClass.createSet((byte) 0xFF));

        long sendTime = System.currentTimeMillis();

        assertEquals(1, wd.getByteStreamCount());
        assertTrue(wd.getFrameList().get(0) instanceof SendData);

        SendData sd = (SendData)wd.getFrameList().get(0);
        sd.setSendTime(sendTime);
        assertFalse(sd.wasACKed());
        assertEquals(0, sd.getSendCount());

        // test process() just before retry
        c.process(sendTime + 999);
        assertEquals(1, wd.getByteStreamCount());
        assertFalse(sd.wasACKed());

        // test process() at retry
        c.process(sendTime + 1000);
        assertEquals(2, wd.getByteStreamCount());
        assertFalse(sd.wasACKed());

        // receive ACK and check that process() didn't re-send
        c.onMessageReceived(new ACK());
        c.process(sendTime + 2000);
        assertEquals(2, wd.getByteStreamCount());
        assertTrue(sd.wasACKed());
    }

    @Test
    public void testWriteWithResponseTimeout() {
        MockControllerWriteDelegate wd = new MockControllerWriteDelegate();
        Controller c = new Controller(new MockSerialChannel(), wd);
        long sendTime = System.currentTimeMillis();

        c.sendCommand((byte)0x02, BinarySwitchCommandClass.createSet((byte) 0xFF));
        c.sendCommand((byte)0x02, BinarySwitchCommandClass.createSet((byte) 0xFF));

        assertEquals(1, wd.getByteStreamCount());
        assertTrue(wd.getFrameList().get(0) instanceof SendData);
        SendData sd = (SendData)wd.getFrameList().get(0);
        sd.setSendTime(sendTime);
        assertEquals(sendTime, sd.getSendTime());

        c.process(sendTime + 50000);

        assertEquals(2, wd.getByteStreamCount());
    }
*/
    @Test
    public void testNoDoubleSend() {
        MockZWaveControllerWriteDelegate wd = new MockZWaveControllerWriteDelegate();
        SerialZWaveController c = new SerialZWaveController(new MockSerialChannel(), wd, null);
        long sendTime = System.currentTimeMillis();

        c.sendDataFrame(BinarySwitchCommandClass.createSetv1((byte) 0x02, true));
        c.process(System.currentTimeMillis());
        assertEquals(1, wd.getFrameCount());
        c.process(System.currentTimeMillis());
        assertEquals(1, wd.getFrameCount());
    }

    @Test
    public void testRequestNodeInfoTransaction() {
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);

        MockZWaveControllerWriteDelegate wd = new MockZWaveControllerWriteDelegate();
        SerialZWaveController c = new SerialZWaveController(new MockSerialChannel(), wd, null);

        // queue up and process a RequestNodeInfo message
        RequestNodeInfo request = new RequestNodeInfo((byte)0x06);
        c.sendDataFrame(request);
        c.process(System.currentTimeMillis());
        // make sure it was sent
        assertEquals(1, wd.getFrameCount());

        // queue up and process another message
        DataFrame request2 = BinarySwitchCommandClass.createGetv1((byte) 0x02);
        c.sendDataFrame(request2);
        c.process(System.currentTimeMillis());
        // make sure it wasn't sent
        assertEquals(1, wd.getFrameCount());

        // receive ACK
        c.onACK();
        c.process(System.currentTimeMillis());
        // make sure no other messages were sent
        assertEquals(1, wd.getFrameCount());

        // receive the RequestNodeInfo response
        parser.addBytes(new byte[]{0x01, 0x04, 0x01, 0x60, 0x01, (byte) 0x9b}, 6);
        assertEquals(1, listener.messages.size());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());
        // at this point, we should have sent both the original request and an ACK for the RequestNodeInfo response
        assertEquals(2, wd.getFrameCount());
        assertTrue(wd.getFrameList().get(0) instanceof RequestNodeInfo);
        assertTrue(wd.getFrameList().get(1) instanceof ACK);

        // receive ApplicationUpdate completing the transaction
        listener.clear(); 

        parser.addBytes(new byte[]{0x01, 0x10, 0x00, 0x49, (byte)0x84, 0x0e, 0x0a, 0x04, 0x10, 0x01, 0x25, 0x27, 0x73, 0x70, (byte)0x86, 0x72, 0x77, (byte)0xb1}, 18);
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());
        // at this point, we should see a second ACK and the BinarySwitchCommandClass.Get sent
        assertEquals(4, wd.getFrameCount());
        assertTrue(wd.getFrameList().get(2) instanceof ACK);
        assertTrue(wd.getFrameList().get(3) == request2);
    }

    @Test
    public void testNewNodeInitialization() {
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);

        MockZWaveControllerWriteDelegate wd = new MockZWaveControllerWriteDelegate();
        SerialZWaveController c = new SerialZWaveController(new MockSerialChannel(), wd, null);
        c.start();
        c.process(System.currentTimeMillis());

        // receive Version response
        parser.addBytes(new byte[] {0x01, 0x10, 0x01, 0x15, 0x5A, 0x2D, 0x57, 0x61, 0x76, 0x65, 0x20, 0x32, 0x2E, 0x37, 0x38, 0x00, 0x01, (byte)0x9B }, 18);
        c.onACK();
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());

        // receive MemoryGetId response
        listener.clear();
        parser.addBytes(new byte[]{0x01, 0x08, 0x01, 0x20, 0x01, 0x6A, 0x2D, (byte) 0xEC, 0x01, 0x7D}, 10);
        c.onACK();
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());

        // receive InitData response
        listener.clear();
        byte[] b = new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1D, 0x00, (byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x01, 0x42};
        parser.addBytes(b, b.length);
        c.onACK();
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());

        // receive NodeProtocolInfo response
        listener.clear();
        b = new byte[] {0x01, 0x09, 0x01, 0x41, (byte)0xD3, (byte)0x9C, 0x00, 0x04, 0x11, 0x01, (byte)0xED};
        parser.addBytes(b, b.length);
        c.onACK();
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());

        // receive RequestNodeInfo response
        listener.clear();
        b = new byte[] {0x01, 0x04, 0x01, 0x60, 0x01, (byte)0x9B};
        parser.addBytes(b, b.length);
        c.onACK();
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());

        // receive failed ApplicationUpdate
        listener.clear();
        b = new byte[] {0x01, 0x06, 0x00, 0x49, (byte)0x81, 0x00, 0x00, 0x31};
        parser.addBytes(b, b.length);
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());

        // receive RequestNodeInfo response
        listener.clear();
        b = new byte[] {0x01, 0x04, 0x01, 0x60, 0x01, (byte)0x9B};
        parser.addBytes(b, b.length);
        c.onACK();
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());

        // receive successful ApplicationUpdate
        listener.clear();
        b = new byte[] {0x01, 0x10, 0x00, 0x49, (byte)0x84, 0x10, 0x0a, 0x04, 0x10, 0x01, 0x25, 0x27, 0x73, 0x70, (byte)0x86, 0x72, 0x77, (byte)0xb1};
        parser.addBytes(b, b.length);
        c.process(System.currentTimeMillis());
        c.onDataFrame((DataFrame)listener.messages.get(0));
        c.process(System.currentTimeMillis());
    }
}
