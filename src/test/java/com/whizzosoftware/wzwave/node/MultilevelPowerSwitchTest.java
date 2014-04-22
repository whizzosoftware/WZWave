package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.node.specific.MultilevelPowerSwitch;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import org.junit.Test;

public class MultilevelPowerSwitchTest {
    @Test
    public void testInitialization() {
        MultilevelPowerSwitch ps = new MultilevelPowerSwitch((byte)1, new NodeProtocolInfo(new byte[] {0x01, 0x09, 0x01, 0x41, (byte)0xD3, (byte)0x9C, 0x00, 0x04, 0x11, 0x01, (byte)0xED}), null);
    }
}
