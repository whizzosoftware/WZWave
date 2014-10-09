package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.controller.MockZWaveControllerContext;
import com.whizzosoftware.wzwave.node.generic.MultilevelSwitch;
import com.whizzosoftware.wzwave.node.specific.MultilevelPowerSwitch;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import org.junit.Test;

public class MultilevelPowerSwitchTest {
    @Test
    public void testInitialization() {
        MockZWaveControllerContext context = new MockZWaveControllerContext();
        MultilevelPowerSwitch ps = new MultilevelPowerSwitch(
            context,
            (byte)0x01,
            new NodeProtocolInfo(BasicDeviceClasses.ROUTING_SLAVE, MultilevelSwitch.ID, MultilevelPowerSwitch.ID, true),
            null
        );
    }
}
