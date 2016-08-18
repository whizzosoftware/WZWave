package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.node.generic.MultilevelSwitch;
import com.whizzosoftware.wzwave.node.generic.StaticController;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;
import com.whizzosoftware.wzwave.node.specific.MultilevelPowerSwitch;
import com.whizzosoftware.wzwave.node.specific.PCController;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZWaveNodeFactoryTest {
    @Test
    public void testCreateNode() throws Exception {
        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte)0x00, StaticController.ID, (byte)0x00), false, false, null) instanceof StaticController);
        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte)0x00, StaticController.ID, (byte)0x02), false, false, null) instanceof StaticController);
        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte)0x00, StaticController.ID, PCController.ID), false, false, null) instanceof StaticController);

        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte)0x00, BinarySensor.ID, (byte)0x00), false, false, null) instanceof BinarySensor);
        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte)0x00, BinarySensor.ID, RoutingBinarySensor.ID), false, false, null) instanceof RoutingBinarySensor);

        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte)0x00, BinarySwitch.ID, (byte)0x00), false, false, null) instanceof BinarySwitch);
        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte) 0x00, BinarySwitch.ID, BinaryPowerSwitch.ID), false, false, null) instanceof BinaryPowerSwitch);

        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte) 0x00, MultilevelSwitch.ID, (byte)0x00), false, false, null) instanceof MultilevelSwitch);
        assertTrue(ZWaveNodeFactory.createNode(null, new NodeInfo((byte)0x01, (byte) 0x00, MultilevelSwitch.ID, MultilevelPowerSwitch.ID), false, false, null) instanceof MultilevelPowerSwitch);
    }
}
