package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.controller.MockZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import com.whizzosoftware.wzwave.node.generic.StaticController;
import com.whizzosoftware.wzwave.node.specific.PCController;

import org.junit.Test;
import static org.junit.Assert.*;

public class StaticControllerTest {
    @Test
    public void testNoRequestNodeInfo() {
        MockZWaveControllerContext ctx = new MockZWaveControllerContext();
        PCController c = new PCController(ctx, (byte)0x01, new NodeProtocolInfo(BasicDeviceClasses.ROUTING_SLAVE, StaticController.ID, PCController.ID, true), null);
        // PC Controller should not send out a RequestNodeInfo frame
        assertEquals(0, ctx.getSentFrameCount());
    }
}
