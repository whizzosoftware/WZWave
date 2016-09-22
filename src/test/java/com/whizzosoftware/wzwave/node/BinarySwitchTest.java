package com.whizzosoftware.wzwave.node;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.commandclass.VersionCommandClass;
import com.whizzosoftware.wzwave.controller.MockZWaveControllerContext;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;

public class BinarySwitchTest {
    @Test
    public void testStart() {
        // create new binary switch
        // make sure it's set to listening to startup commands don't go to wakeup queue
        MockZWaveControllerContext context = new MockZWaveControllerContext();
        BinarySwitch bs = new BinarySwitch(
            new NodeInfo((byte)0x02, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID),
            true,
            null
        );
        bs.startInterview(context);
        assertEquals(1, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(0) instanceof SendData);
        context.clearSentFrames();

        // response with successful ping
        bs.onSendDataCallback(context, true);
        assertEquals(1, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(0) instanceof RequestNodeInfo);
        context.clearSentFrames();

        // response with two command classes
        bs.onApplicationUpdate(context, new ApplicationUpdate(
            DataFrameType.REQUEST,
            ApplicationUpdate.UPDATE_STATE_NODE_INFO_RECEIVED,
            (byte)0x02,
            new NodeInfo(
                (byte)0x02,
                BasicDeviceClasses.ROUTING_SLAVE,
                BinarySwitch.ID,
                BinaryPowerSwitch.ID,
                new byte[] {VersionCommandClass.ID}
            )
        ));
        assertEquals(ZWaveNodeState.RetrieveVersionSent, bs.getState());

        // verify that two version requests were made
        assertEquals(1, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(0) instanceof SendData);
        assertEquals((byte) 0x86, ((SendData) context.getSentFrames().get(0)).getSendData()[0]);

        context.clearSentFrames();

        // respond with first version response (node)
        bs.onApplicationCommand(context, new ApplicationCommand(
            DataFrameType.REQUEST,
            (byte)0x00,
            (byte)0x29,
            new byte[] {(byte)0x86, 0x12, 0x03, 0x03, 0x43, 0x01, 0x01}
        ));
        assertEquals("1. 1", ((VersionCommandClass)bs.getCommandClass(VersionCommandClass.ID)).getApplication());
        assertEquals("3", ((VersionCommandClass)bs.getCommandClass(VersionCommandClass.ID)).getLibrary());
        assertEquals("3.67", ((VersionCommandClass)bs.getCommandClass(VersionCommandClass.ID)).getProtocol());

        assertEquals(ZWaveNodeState.RetrieveStateSent, bs.getState());
        assertEquals(2, context.getSentFrameCount());
        context.clearSentFrames();

        // respond with basic get response
        bs.onApplicationCommand(context, new ApplicationCommand(
                DataFrameType.REQUEST,
                (byte)0x00,
                (byte)0x29,
                new byte[] {BasicCommandClass.ID, BasicCommandClass.BASIC_REPORT, (byte)0xff}
        ));

        assertEquals(ZWaveNodeState.RetrieveStateSent, bs.getState());
        assertEquals(0, context.getSentFrameCount());

        // respond with switch binary get response
        bs.onApplicationCommand(context, new ApplicationCommand(
                DataFrameType.REQUEST,
                (byte)0x00,
                (byte)0x29,
                new byte[] {BinarySwitchCommandClass.ID, BinarySwitchCommandClass.SWITCH_BINARY_REPORT, (byte)0xff}
        ));

        assertEquals(ZWaveNodeState.Started, bs.getState());
        assertEquals(0, context.getSentFrameCount());
    }

    @Test
    public void testBasicReportMapping() {
        MockZWaveControllerContext context = new MockZWaveControllerContext();

        // create new binary switch
        BinarySwitch bs = new BinarySwitch(
            new NodeInfo((byte)0x01, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID),
            false,
            null
        );

        // assert that the new switch has the appropriate command classes and its initial values are null (undefined)
        assertTrue(bs.hasCommandClass(BinarySwitchCommandClass.ID));

        // since BASIC_REPORT is supposed to get mapped to SWITCH_BINARY_REPORT, send a new SWITCH_BINARY_REPORT (value=0xFF)
        // message to node and verify COMMAND_CLASS_SWITCH_BINARY is updated properly
        bs.onApplicationCommand(context, new ApplicationCommand(DataFrameType.REQUEST, (byte)0x00, (byte)0x02, new byte[] {BasicCommandClass.ID, BasicCommandClass.BASIC_REPORT, (byte)0xFF}));

        assertTrue(BinarySwitch.isOn(bs));
    }

    @Test
    public void testBinarySwitchReport() {
        MockZWaveControllerContext context = new MockZWaveControllerContext();

        // create new binary switch
        BinarySwitch bs = new BinarySwitch(
            new NodeInfo((byte)0x01, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID),
            false,
            null
        );

        // assert that the new switch has the appropriate command class and its initial value is null (undefined)
        assertTrue(bs.hasCommandClass(BinarySwitchCommandClass.ID));
        assertNull(BinarySwitch.isOn(bs));

        // send a new SWITCH_BINARY_REPORT (value=0xFF) to node and verify COMMAND_CLASS_SWITCH_BINARY is updated properly
        bs.onApplicationCommand(context, new ApplicationCommand(DataFrameType.REQUEST, (byte)0x00, (byte)0x02, new byte[] {BinarySwitchCommandClass.ID, BinarySwitchCommandClass.SWITCH_BINARY_REPORT, (byte)0xFF}));
        assertTrue(BinarySwitch.isOn(bs));

        // send a new SWITCH_BINARY_REPORT (off) to node and verify COMMAND_CLASS_SWITCH_BINARY is updated properly
        bs.onApplicationCommand(context, new ApplicationCommand(DataFrameType.REQUEST, (byte) 0x00, (byte) 0x02, new byte[]{BinarySwitchCommandClass.ID, BinarySwitchCommandClass.SWITCH_BINARY_REPORT, (byte) 0x00}));
        assertFalse(BinarySwitch.isOn(bs));
    }

    @Test
    public void testListeningNodeStartupFailure() {
        // This tests that when a node that should be listening (according to its NodeInfo) fails to provide a response to
        // RequestNodeInfo, that it's flagged as both started and inactive.
        MockZWaveControllerContext context = new MockZWaveControllerContext();
        BinarySwitch bs = new BinarySwitch(
            new NodeInfo((byte)0x02, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID),
            true,
            null
        );
        bs.startInterview(context);
        assertEquals(1, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(0) instanceof SendData);
        assertNull(bs.isAvailable());
        context.clearSentFrames();

        bs.onSendDataCallback(context, true);
        assertEquals(1, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(0) instanceof RequestNodeInfo);
        assertTrue(bs.isAvailable());

        // simulate receiving of failed RequestNodeInfo response
        bs.onApplicationUpdate(context, new ApplicationUpdate(DataFrameType.RESPONSE, ApplicationUpdate.UPDATE_STATE_NODE_INFO_REQ_FAILED, (byte)0x00));
        assertTrue(bs.isAvailable());

        // assert that a retry was sent
        assertEquals(2, context.getSentFrameCount());
        assertTrue(context.getSentFrames().get(1) instanceof RequestNodeInfo);

        // simulate receiving of failed RequestNodeInfo response
        bs.onApplicationUpdate(context, new ApplicationUpdate(DataFrameType.RESPONSE, ApplicationUpdate.UPDATE_STATE_NODE_INFO_REQ_FAILED, (byte)0x00));

        assertEquals(ZWaveNodeState.Started, bs.getState());
        assertFalse(bs.isAvailable());
    }
}
