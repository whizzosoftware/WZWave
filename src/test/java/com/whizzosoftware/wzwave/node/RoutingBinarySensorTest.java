/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
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
            new NodeInfo((byte)0x09, BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID),
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
            new NodeInfo((byte)0x09, BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID),
            false,
            null
        );
        sensor.startInterview(context);

        // receive failed ApplicationUpdate
        sensor.onApplicationUpdate(context, new ApplicationUpdate(
            DataFrameType.REQUEST,
            ApplicationUpdate.UPDATE_STATE_NODE_INFO_REQ_FAILED,
            (byte)0x00
        ));

        // make sure binary sensor idle is undefined
        assertNull(sensor.isSensorIdle());

        // receive application update
        sensor.onApplicationCommand(context, new ApplicationCommand(
                DataFrameType.REQUEST,
                (byte) 0x00,
                (byte) 0x09,
                new byte[]{BasicCommandClass.ID, BasicCommandClass.BASIC_SET, 0x00}
        ));

        // make sure idle is updated
        assertTrue(sensor.isSensorIdle());
    }

    @Test
    public void testWithMultilevelSensorCommandClass() {
        MockZWaveControllerContext context = new MockZWaveControllerContext();
        RoutingBinarySensor sensor = new RoutingBinarySensor(
            new NodeInfo((byte)0x0D, BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID),
            false,
            null
        );

        // receive SensorMultilevelCmd_Get response
        sensor.onApplicationCommand(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            (byte) 0x00,
            (byte) 0x0d,
            new byte[]{MultilevelSensorCommandClass.ID, 0x05, 0x01, 0x22, (byte)0xff, 0x3c}
        ));
    }
}
