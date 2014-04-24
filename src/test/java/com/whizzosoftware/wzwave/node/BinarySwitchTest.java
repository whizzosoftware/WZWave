package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.MockFrameListener;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.controller.MockSerialChannel;
import com.whizzosoftware.wzwave.controller.MockZWaveControllerWriteDelegate;
import com.whizzosoftware.wzwave.controller.serial.SerialZWaveController;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.frame.parser.FrameParser;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class BinarySwitchTest {
    @Test
    public void testStart() {
        // create new binary switch
        // make sure it's set to listening to startup commands don't go to wakeup queue
        BinarySwitch bs = new BinarySwitch((byte)0x01, new NodeProtocolInfo((byte)0x04, (byte)0x10, (byte)0x01, true), null);
        assertEquals(0, bs.getWakeupQueueCount());
        assertEquals(1, bs.getWriteQueueCount());
        assertTrue(bs.writeQueue.get(0) instanceof RequestNodeInfo);
    }

    @Test
    public void testListeningNodeReceivesRequestNodeInfo() {
        MockZWaveControllerWriteDelegate delegate = new MockZWaveControllerWriteDelegate();
        SerialZWaveController controller = new SerialZWaveController(new MockSerialChannel(), delegate, null);

        // discover a routing binary sensor
        BinarySwitch bswitch = new BinarySwitch((byte)0x01, new NodeProtocolInfo((byte)0x04, (byte)0x10, (byte)0x01, true), null);
        controller.createNode(bswitch);
        controller.process(System.currentTimeMillis());

        assertEquals(1, delegate.getFrameCount());
        assertTrue(delegate.getFrameList().get(0) instanceof RequestNodeInfo);
    }

    @Test
    public void testBasicReportMapping() {
        // create new binary switch
        BinarySwitch bs = new BinarySwitch((byte)0x01, new NodeProtocolInfo((byte)0x04, (byte)0x10, (byte)0x01, false), null);

        // assert that the new switch has the appropriate command classes and its initial values are null (undefined)
        assertTrue(bs.hasCommandClass(BinarySwitchCommandClass.ID));

        // since BASIC_REPORT is supposed to get mapped to SWITCH_BINARY_REPORT, send a new SWITCH_BINARY_REPORT (value=0xFF)
        // message to node and verify COMMAND_CLASS_SWITCH_BINARY is updated properly
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);
        parser.addBytes(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x02, 0x03, 0x20, 0x03, (byte)0xFF, (byte)0xD5}, 11);
        assertEquals(1, listener.messages.size());
        assertTrue(listener.messages.get(0) instanceof ApplicationCommand);
        DataFrame m = (DataFrame)listener.messages.get(0);
        bs.onDataFrameReceived(null, m, false);
        assertTrue(BinarySwitch.isOn(bs));
    }

    @Test
    public void testBinarySwitchReport() {
        // create new binary switch
        BinarySwitch bs = new BinarySwitch((byte)0x01, new NodeProtocolInfo((byte)0x04, (byte)0x10, (byte)0x01, false), null);

        // assert that the new switch has the appropriate command class and its initial value is null (undefined)
        assertTrue(bs.hasCommandClass(BinarySwitchCommandClass.ID));
        assertNull(BinarySwitch.isOn(bs));

        // send a new SWITCH_BINARY_REPORT (value=0xFF) to node and verify COMMAND_CLASS_SWITCH_BINARY is updated properly
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);
        parser.addBytes(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x02, 0x03, 0x25, 0x03, (byte)0xFF, (byte)0xD5}, 11);
        assertEquals(1, listener.messages.size());
        assertTrue(listener.messages.get(0) instanceof ApplicationCommand);
        DataFrame m = (DataFrame)listener.messages.get(0);
        bs.onDataFrameReceived(null, m, false);
        assertTrue(BinarySwitch.isOn(bs));

        // send a new SWITCH_BINARY_REPORT (off) to node and verify COMMAND_CLASS_SWITCH_BINARY is updated properly
        listener.clear();
        parser.addBytes(new byte[]{0x01, 0x09, 0x00, 0x04, 0x00, 0x02, 0x03, 0x25, 0x03, (byte) 0x00, (byte) 0xD5}, 11);
        assertEquals(1, listener.messages.size());
        assertTrue(listener.messages.get(0) instanceof ApplicationCommand);
        m = (DataFrame)listener.messages.get(0);
        bs.onDataFrameReceived(null, m, false);
        assertFalse(BinarySwitch.isOn(bs));
    }
}
