package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.commandclass.VersionCommandClass;
import com.whizzosoftware.wzwave.controller.MockZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;

import org.junit.Test;
import static org.junit.Assert.*;

public class MultiInstanceBinaryPowerSwitchTest {
    @Test
    public void testInitializationWithIdenticalEndpointsv2() {
        byte nodeId = 0x2C;
        byte rxStatus = 0x00;

        MockZWaveControllerContext context = new MockZWaveControllerContext();
        BinaryPowerSwitch ps = new BinaryPowerSwitch(
            new NodeInfo(nodeId, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID),
            false,
            true,
            null
        );
        ps.startInterview(context);

        // confirm that a RequestNodeInfo frame was sent
        assertEquals(1, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(0) instanceof RequestNodeInfo);
        assertEquals(ZWaveNodeState.NodeInfo, ps.getState());
        context.clearSentFrames();

        // respond with an ApplictionUpdate indicating the device has two command classes
        ps.onDataFrameReceived(context, new ApplicationUpdate(
            DataFrameType.REQUEST,
            ApplicationUpdate.UPDATE_STATE_NODE_INFO_RECEIVED,
            nodeId,
            new NodeInfo(
                nodeId,
                BasicDeviceClasses.ROUTING_SLAVE,
                BinarySwitch.ID,
                BinaryPowerSwitch.ID,
                new byte[] {VersionCommandClass.ID, MultiInstanceCommandClass.ID}
            )
        ));

        // there was a version command class so we should now be in RetrieveVersionSent
        assertEquals(ZWaveNodeState.RetrieveVersionSent, ps.getState());

        // confirm that two SendData frames were sent (one will be the request for the device version and the other for the multi instance command class)
        assertEquals(2, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(0) instanceof SendData);
        assertTrue(context.getSentFrames().get(1) instanceof SendData);
        context.clearSentFrames();

        // respond with the device version
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {(byte)0x86, 0x12, 0x03, 0x03, 0x43, 0x01, 0x01}
        ));
        assertEquals(0, context.getSentFrameCount());
        assertEquals(ZWaveNodeState.RetrieveVersionSent, ps.getState());

        // respond with the MICC version (version 2)
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {(byte)0x86, 0x14, 0x60, 0x02}
        ));

        // confirm that the device has requested the current command class values
        // we should see BASIC_GET, SWITCH_BINARY_GET and MULTI_CHANNEL_END_POINT_GET
        assertEquals(3, context.getSentFrameCount());
        for (int i=0; i < 3; i++) {
            byte cc = ((SendData)context.getSentFrames().get(i)).getSendData()[0];
            byte t = ((SendData)context.getSentFrames().get(i)).getSendData()[1];
            assertTrue((cc == BasicCommandClass.ID && t == BasicCommandClass.BASIC_GET) || (cc == BinarySwitchCommandClass.ID && t == BinarySwitchCommandClass.SWITCH_BINARY_GET) || (cc == MultiInstanceCommandClass.ID && t == MultiInstanceCommandClass.MULTI_CHANNEL_END_POINT_GET));
        }
        assertEquals(ZWaveNodeState.RetrieveStateSent, ps.getState());
        context.clearSentFrames();

        // response with basic report
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {BasicCommandClass.ID, BasicCommandClass.BASIC_REPORT, (byte)0xFF}
        ));
        assertEquals(0, context.getSentFrameCount());
        assertEquals(ZWaveNodeState.RetrieveStateSent, ps.getState());

        // respond with SWITCH_BINARY_REPORT
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {BinarySwitchCommandClass.ID, BinarySwitchCommandClass.SWITCH_BINARY_REPORT, (byte)0xFF}
        ));
        assertEquals(0, context.getSentFrameCount());
        assertEquals(ZWaveNodeState.RetrieveStateSent, ps.getState());

        // respond with MULTI_CHANNEL_END_POINT_REPORT with 2 identical endpoints
        byte endpointCount = 0x02;
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {
                MultiInstanceCommandClass.ID,
                MultiInstanceCommandClass.MULTI_CHANNEL_END_POINT_REPORT,
                MultiInstanceCommandClass.IDENTICAL_ENDPOINTS,
                endpointCount
            }
        ));

        // we expect to see a single MULTI_CHANNEL_CAPABILITY_GET since all endpoints are identical
        assertEquals(1, context.getSentFrameCount());
        assertEquals(MultiInstanceCommandClass.ID, ((SendData)context.getSentFrames().get(0)).getSendData()[0]);
        assertEquals(MultiInstanceCommandClass.MULTI_CHANNEL_CAPABILITY_GET, ((SendData)context.getSentFrames().get(0)).getSendData()[1]);
        assertEquals(ZWaveNodeState.RetrieveStateSent, ps.getState());
        context.clearSentFrames();

        // respond with MULTI_CHANNEL_CAPABILITY_REPORT
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {MultiInstanceCommandClass.ID, MultiInstanceCommandClass.MULTI_CHANNEL_CAPABILITY_REPORT, 0x01, 0x10, 0x01, BinarySwitchCommandClass.ID}
        ));

        MultiInstanceCommandClass micc = (MultiInstanceCommandClass)ps.getCommandClass(MultiInstanceCommandClass.ID);
        assertEquals(2, micc.getEndpoints().size());
        ZWaveMultiChannelEndpoint ep = micc.getEndpoint((byte)1);
        BinarySwitchCommandClass bscc = (BinarySwitchCommandClass)ep.getCommandClass(BinarySwitchCommandClass.ID);
        assertNull(bscc.isOn());
        ep = micc.getEndpoint((byte)2);
        bscc = (BinarySwitchCommandClass)ep.getCommandClass(BinarySwitchCommandClass.ID);
        assertNull(bscc.isOn());

        // we expect to see two MULTI_CHANNEL_CMD_ENCAP requests wrappering SWITCH_BINARY_GETs (one for each endpoint)
        assertEquals(2, context.getSentFrameCount());
        assertEquals(ZWaveNodeState.RetrieveStateSent, ps.getState());
        assertEquals(MultiInstanceCommandClass.ID, ((SendData)context.getSentFrames().get(0)).getSendData()[0]);
        assertEquals(MultiInstanceCommandClass.MULTI_CHANNEL_CMD_ENCAP, ((SendData)context.getSentFrames().get(0)).getSendData()[1]);
        assertEquals(MultiInstanceCommandClass.ID, ((SendData)context.getSentFrames().get(1)).getSendData()[0]);
        assertEquals(MultiInstanceCommandClass.MULTI_CHANNEL_CMD_ENCAP, ((SendData)context.getSentFrames().get(1)).getSendData()[1]);
        context.clearSentFrames();

        // respond with first SWITCH_BINARY_GET wrappered in a MULTI_CHANNEL_CMD_ENCAP
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {
                MultiInstanceCommandClass.ID,
                MultiInstanceCommandClass.MULTI_CHANNEL_CMD_ENCAP,
                0x01, // endpoint number
                0x00,
                BinarySwitchCommandClass.ID,
                BinarySwitchCommandClass.SWITCH_BINARY_REPORT,
                (byte)0xFF // switch value
            }
        ));

        // respond with second SWITCH_BINARY_GET wrappered in a MULTI_CHANNEL_CMD_ENCAP
        ps.onDataFrameReceived(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            rxStatus,
            nodeId,
            new byte[] {
                MultiInstanceCommandClass.ID,
                MultiInstanceCommandClass.MULTI_CHANNEL_CMD_ENCAP,
                0x02, // endpoint number
                0x00,
                BinarySwitchCommandClass.ID,
                BinarySwitchCommandClass.SWITCH_BINARY_REPORT,
                (byte)0x00 // switch value
            }
        ));

        // device should now be fully initialized
        assertEquals(ZWaveNodeState.Started, ps.getState());

        // confirm that values were applied properly
        micc = (MultiInstanceCommandClass)ps.getCommandClass(MultiInstanceCommandClass.ID);
        ep = micc.getEndpoint((byte)1);
        bscc = (BinarySwitchCommandClass)ep.getCommandClass(BinarySwitchCommandClass.ID);
        assertTrue(bscc.isOn());
        ep = micc.getEndpoint((byte)2);
        bscc = (BinarySwitchCommandClass)ep.getCommandClass(BinarySwitchCommandClass.ID);
        assertFalse(bscc.isOn());
    }
}
