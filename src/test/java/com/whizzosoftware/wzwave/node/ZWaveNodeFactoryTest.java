package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.node.generic.MultilevelSwitch;
import com.whizzosoftware.wzwave.node.generic.StaticController;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;
import com.whizzosoftware.wzwave.node.specific.MultilevelPowerSwitch;
import com.whizzosoftware.wzwave.node.specific.PCController;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;
import com.whizzosoftware.wzwave.persist.MockPersistenceContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZWaveNodeFactoryTest {
    @Test
    public void testCreateDiscoveredNode() throws Exception {
        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte)0x00, StaticController.ID, (byte)0x00), false, false, null) instanceof StaticController);
        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte)0x00, StaticController.ID, (byte)0x02), false, false, null) instanceof StaticController);
        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte)0x00, StaticController.ID, PCController.ID), false, false, null) instanceof StaticController);

        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte)0x00, BinarySensor.ID, (byte)0x00), false, false, null) instanceof BinarySensor);
        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte)0x00, BinarySensor.ID, RoutingBinarySensor.ID), false, false, null) instanceof RoutingBinarySensor);

        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte)0x00, BinarySwitch.ID, (byte)0x00), false, false, null) instanceof BinarySwitch);
        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte) 0x00, BinarySwitch.ID, BinaryPowerSwitch.ID), false, false, null) instanceof BinaryPowerSwitch);

        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte) 0x00, MultilevelSwitch.ID, (byte)0x00), false, false, null) instanceof MultilevelSwitch);
        assertTrue(ZWaveNodeFactory.createNode(new NodeInfo((byte)0x01, (byte) 0x00, MultilevelSwitch.ID, MultilevelPowerSwitch.ID), false, false, null) instanceof MultilevelPowerSwitch);
    }

    @Test
    public void testCreatePersistedNode() throws NodeCreationException {
        MockPersistenceContext pctx = new MockPersistenceContext();
        ZWaveNode node = ZWaveNodeFactory.createNode(new NodeInfo((byte)0x02, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID), false, true, null);
        node.save(pctx);

        node = ZWaveNodeFactory.createNode(pctx, (byte)0x02, null);
        assertEquals((byte)0x02, node.getNodeId());
        assertEquals(BasicDeviceClasses.ROUTING_SLAVE, (byte)node.getBasicDeviceClass());
        assertEquals(BinarySwitch.ID, (byte)node.getGenericDeviceClass());
        assertEquals(BinaryPowerSwitch.ID, (byte)node.getSpecificDeviceClass());
        assertEquals(2, node.getCommandClasses().size());
        for (CommandClass cc : node.getCommandClasses()) {
            assertTrue(cc.getId() == BasicCommandClass.ID || cc.getId() == BinarySwitchCommandClass.ID);
        }
    }
}
