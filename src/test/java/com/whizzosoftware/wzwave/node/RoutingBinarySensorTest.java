package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.MultilevelSensorCommandClass;
import com.whizzosoftware.wzwave.commandclass.WakeUpCommandClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.whizzosoftware.wzwave.controller.MockZWaveControllerContext;
import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;
import com.whizzosoftware.wzwave.frame.*;

public class RoutingBinarySensorTest {
    @Test
    public void testSleepingNodeStartupCommands() {
        // discover a routing binary sensor
        MockZWaveControllerContext context = new MockZWaveControllerContext();
        RoutingBinarySensor sensor = new RoutingBinarySensor(
            context,
            new NodeInfo((byte)0x09, BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID),
            false,
            false,
            null
        );

        // make sure no further message was written out
        assertEquals(0, context.getSentFrameCount());
    }

    @Test
    public void testUpdateMessage() {
        MockZWaveControllerContext context = new MockZWaveControllerContext();

        RoutingBinarySensor sensor = new RoutingBinarySensor(
            context,
            new NodeInfo((byte)0x09, BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID),
            false,
            false,
            null
        );

        // receive failed ApplicationUpdate
        sensor.onDataFrameReceived(context, new ApplicationUpdate(
            DataFrameType.REQUEST,
            ApplicationUpdate.UPDATE_STATE_NODE_INFO_REQ_FAILED,
            (byte)0x00
        ));

        // make sure binary sensor idle is undefined
        assertNull(sensor.isSensorIdle());

        // receive application update
        sensor.onDataFrameReceived(context, new ApplicationCommand(
                DataFrameType.REQUEST,
                (byte) 0x00,
                (byte) 0x09,
                new byte[]{BasicCommandClass.ID, BasicCommandClass.BASIC_SET, 0x00}
        ));

        // make sure idle is updated
        assertTrue(sensor.isSensorIdle());
    }

    @Test
    public void testWakeupQueue() {
        MockZWaveControllerContext context = new MockZWaveControllerContext();

        RoutingBinarySensor sensor = new RoutingBinarySensor(
            context,
            new NodeInfo((byte)0x09, BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID),
            false,
            false,
            null
        );
        assertEquals(ZWaveNodeState.Started, sensor.getState());

        // make sure we have no messages waiting in the wakeup queue
        assertEquals(0, sensor.getWakeupQueueCount());
        assertEquals(0, context.getSentFrameCount());

        // put into started state & verify messages in wakeup queue
        sensor.setState(context, ZWaveNodeState.RetrieveStatePending);
        assertEquals(2, sensor.getWakeupQueueCount());
        assertEquals(0, context.getSentFrameCount());

        // send wakeup notification
        sensor.onDataFrameReceived(context, new ApplicationCommand(
                DataFrameType.REQUEST,
                (byte) 0x00,
                (byte) 0x09,
                new byte[]{WakeUpCommandClass.ID, 0x07}
        ));

        // check that wakeup queue has been purged & there are 2 messages in the write queue
        assertEquals(0, sensor.getWakeupQueueCount());
        assertEquals(2, context.getSentFrameCount());
    }

    @Test
    public void testWithMultilevelSensorCommandClass() {
        MockZWaveControllerContext context = new MockZWaveControllerContext();
        RoutingBinarySensor sensor = new RoutingBinarySensor(
            context,
            new NodeInfo((byte)0x0D, BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID),
            false,
            false,
            null
        );

        // receive SensorMultilevelCmd_Get response
        sensor.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            (byte) 0x00,
            (byte) 0x0d,
            new byte[]{MultilevelSensorCommandClass.ID, 0x05, 0x01, 0x22, (byte)0xff, 0x3c}
        ));
    }
}
