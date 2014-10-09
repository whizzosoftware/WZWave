package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
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
        assertTrue(ZWaveNodeFactory.createNode(null, (byte)0x01, new NodeProtocolInfo((byte)0x00, StaticController.ID, (byte)0x00, false), null) instanceof StaticController);
        assertTrue(ZWaveNodeFactory.createNode(null, (byte)0x01, new NodeProtocolInfo((byte)0x00, StaticController.ID, (byte)0x02, false), null) instanceof StaticController);
        assertTrue(ZWaveNodeFactory.createNode(null, (byte) 0x01, new NodeProtocolInfo((byte) 0x00, StaticController.ID, PCController.ID, false), null) instanceof StaticController);

        assertTrue(ZWaveNodeFactory.createNode(null, (byte) 0x01, new NodeProtocolInfo((byte) 0x00, BinarySensor.ID, (byte)0x00, false), null) instanceof BinarySensor);
        assertTrue(ZWaveNodeFactory.createNode(null, (byte) 0x01, new NodeProtocolInfo((byte) 0x00, BinarySensor.ID, RoutingBinarySensor.ID, false), null) instanceof RoutingBinarySensor);

        assertTrue(ZWaveNodeFactory.createNode(null, (byte) 0x01, new NodeProtocolInfo((byte) 0x00, BinarySwitch.ID, (byte)0x00, false), null) instanceof BinarySwitch);
        assertTrue(ZWaveNodeFactory.createNode(null, (byte) 0x01, new NodeProtocolInfo((byte) 0x00, BinarySwitch.ID, BinaryPowerSwitch.ID, false), null) instanceof BinaryPowerSwitch);

        assertTrue(ZWaveNodeFactory.createNode(null, (byte) 0x01, new NodeProtocolInfo((byte) 0x00, MultilevelSwitch.ID, (byte)0x00, false), null) instanceof MultilevelSwitch);
        assertTrue(ZWaveNodeFactory.createNode(null, (byte) 0x01, new NodeProtocolInfo((byte) 0x00, MultilevelSwitch.ID, MultilevelPowerSwitch.ID, false), null) instanceof MultilevelPowerSwitch);
    }
}
