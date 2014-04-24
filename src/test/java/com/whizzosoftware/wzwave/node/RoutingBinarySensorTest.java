package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.MockFrameListener;
import com.whizzosoftware.wzwave.controller.MockSerialChannel;
import com.whizzosoftware.wzwave.controller.MockZWaveControllerWriteDelegate;
import com.whizzosoftware.wzwave.controller.serial.SerialZWaveController;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.frame.parser.FrameParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class RoutingBinarySensorTest {
    @Test
    public void testSleepingNodeStartupCommands() {
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);
        parser.addBytes(new byte[] {0x01, 0x09, 0x01, 0x41, 0x52, (byte)0x9c, 0x00, 0x04, 0x20, 0x01, 0x5d}, 11);
        assertEquals(1, listener.messages.size());
        assertTrue(listener.messages.get(0) instanceof NodeProtocolInfo);

        MockZWaveControllerWriteDelegate delegate = new MockZWaveControllerWriteDelegate();
        SerialZWaveController controller = new SerialZWaveController(new MockSerialChannel(), delegate, null);

        // discover a routing binary sensor
        RoutingBinarySensor sensor = new RoutingBinarySensor((byte)0x09, (NodeProtocolInfo)listener.messages.get(0), null);
        controller.createNode(sensor);
        controller.process(System.currentTimeMillis());

        // make sure no further message was written out
        assertEquals(0, delegate.getFrameCount());
    }

    @Test
    public void testUpdateMessage() {
        MockZWaveControllerWriteDelegate delegate = new MockZWaveControllerWriteDelegate();
        SerialZWaveController controller = new SerialZWaveController(new MockSerialChannel(), delegate, null);

        // discover a routing binary sensor
        RoutingBinarySensor sensor = new RoutingBinarySensor((byte)0x09, new NodeProtocolInfo((byte)0x04, (byte)0x20, (byte)0x01, false), null);
        controller.createNode(sensor);
        controller.process(System.currentTimeMillis());

        // receive ACK
        controller.onACK();
        controller.process(System.currentTimeMillis());

        // receive RequestNodeInfo response
        controller.onDataFrame(new RequestNodeInfo(new byte[]{0x01, 0x04, 0x01, 0x60, 0x01, -45}));
        controller.process(System.currentTimeMillis());

        // receive failed ApplicationUpdate
        controller.onDataFrame(new ApplicationUpdate(new byte[]{0x01, 0x06, 0x00, 0x49, (byte) 0x81, 0x00, 0x00, 0x31}));
        controller.process(System.currentTimeMillis());

        // make sure binary sensor idle is undefined
        assertNull(sensor.isSensorIdle());

        // receive application update
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);
        parser.addBytes(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x09, 0x03, 0x20, 0x01, 0x00, (byte)0xd9}, 11);
        controller.onDataFrame((DataFrame)listener.messages.get(0));
        controller.process(System.currentTimeMillis());

        // make sure idle is updated
        assertTrue(sensor.isSensorIdle());
    }

    @Test
    public void testWakeupQueue() {
        // discover a routing binary sensor
        RoutingBinarySensor sensor = new RoutingBinarySensor((byte)0x09, new NodeProtocolInfo((byte)0x04, (byte)0x20, (byte)0x01, false), null);

        // make sure we have no messages waiting in the wakeup queue
        sensor.runLoop(null);
        assertEquals(0, sensor.getWakeupQueueCount());

        // put into started state & verify messages in wakeup queue
        sensor.setState(ZWaveNodeState.RetrieveStatePending);
        sensor.runLoop(null);
        assertEquals(2, sensor.getWakeupQueueCount());

        // send wakeup notification
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);
        parser.addBytes(new byte[] {0x01, 0x08, 0x00, 0x04, 0x00, 0x09, 0x02, (byte)0x84, 0x07, 0x7B}, 10);
        sensor.onDataFrameReceived(null, (DataFrame)listener.messages.get(0), true);

        // check that wakeup queue has been purged & there are 2 messages in the write queue
        assertEquals(0, sensor.getWakeupQueueCount());
        assertEquals(2, sensor.getWriteQueueCount());
    }

    @Test
    public void testWithMultilevelSensorCommandClass() {
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);

        MockZWaveControllerWriteDelegate delegate = new MockZWaveControllerWriteDelegate();
        SerialZWaveController controller = new SerialZWaveController(new MockSerialChannel(), delegate, null);

        assertEquals(0, delegate.getFrameCount());

        // discover a routing binary sensor
        controller.createNode(new RoutingBinarySensor((byte) 0x0D, new NodeProtocolInfo((byte) 0x04, (byte) 0x20, (byte) 0x01, false), null));
        controller.process(System.currentTimeMillis());

        /** RequestNodeInfo transaction start */

        // receive ACK
        controller.onACK();
        controller.process(System.currentTimeMillis());

        // receive successful request
        listener.messages.clear();
        parser.addBytes(new byte[] {0x01, 0x04, 0x01, 0x60, 0x01, (byte)0x9b}, 6);
        controller.onDataFrame((DataFrame)listener.messages.get(0));
        controller.process(System.currentTimeMillis());

        // receive node info req failed
        listener.messages.clear();
        parser.addBytes(new byte[] {0x01, 0x06, 0x00, 0x49, (byte)0x81, 0x00, 0x00, 0x31}, 8);
        controller.onDataFrame((DataFrame)listener.messages.get(0));
        controller.process(System.currentTimeMillis());

        // receive SensorMultilevelCmd_Get response
        listener.messages.clear();
        parser.addBytes(new byte[]{0x01, 0x0c, 0x00, 0x04, 0x00, 0x0d, 0x06, 0x31, 0x05, 0x01, 0x22, (byte) 0xff, 0x3c, 0x28}, 14);
        controller.onDataFrame((DataFrame)listener.messages.get(0));
        controller.process(System.currentTimeMillis());

        /** RequestNodeInfo transaction end */
    }
}
