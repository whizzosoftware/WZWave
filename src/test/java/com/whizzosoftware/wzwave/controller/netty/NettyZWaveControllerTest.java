/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.controller.netty;

import com.whizzosoftware.wzwave.MockChannel;
import com.whizzosoftware.wzwave.channel.event.SendDataTransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import com.whizzosoftware.wzwave.frame.OutboundDataFrame;
import com.whizzosoftware.wzwave.frame.SendData;
import com.whizzosoftware.wzwave.node.*;
import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;
import com.whizzosoftware.wzwave.persist.MockPersistentStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.*;

public class NettyZWaveControllerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testOnZWaveControllerInfo() throws IOException {
        NettyZWaveController c = new NettyZWaveController("/dev/null", folder.newFolder());
        MockZWaveControllerListener l = new MockZWaveControllerListener();
        c.setListener(l);
        c.onLibraryInfo("Z-Wave 2.7.8\u0000");
        c.onControllerInfo(22727648, (byte)1);
        assertEquals("Z-Wave 2.7.8\u0000", l.getLibraryVersion());
        assertEquals(22727648, (int)l.getHomeId());
        assertEquals(1, (byte)l.getNodeId());
    }

    @Test
    public void testNewPersistentListeningNodeInterview() throws IOException {
        MockChannel channel = new MockChannel();
        MockZWaveControllerListener l = new MockZWaveControllerListener();
        NettyZWaveController c = new NettyZWaveController("/dev/null", folder.newFolder());
        c.setChannel(channel);
        c.setListener(l);
        assertEquals(0, c.getNodes().size());
        assertEquals(0, channel.getWrittenMessageCount());

        c.onNodeProtocolInfo((byte)0x02, new NodeProtocolInfo(BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID, true));
        assertEquals(1, c.getNodes().size());
        assertEquals(1, channel.getWrittenMessageCount());
        assertTrue(channel.getWrittenMessage(0) instanceof OutboundDataFrame);
        assertTrue(((OutboundDataFrame)channel.getWrittenMessage(0)).getDataFrame() instanceof SendData);
        assertEquals(ZWaveNodeState.Ping, c.getNode((byte)0x02).getState());
    }

    @Test
    public void testNewPersistentNonListeningNodeInterview() throws IOException {
        MockChannel channel = new MockChannel();
        MockZWaveControllerListener l = new MockZWaveControllerListener();
        NettyZWaveController c = new NettyZWaveController("/dev/null", folder.newFolder());
        c.setChannel(channel);
        c.setListener(l);
        assertEquals(0, c.getNodes().size());
        assertEquals(0, channel.getWrittenMessageCount());

        c.onNodeProtocolInfo((byte)0x02, new NodeProtocolInfo(BasicDeviceClasses.ROUTING_SLAVE, BinarySensor.ID, RoutingBinarySensor.ID, false));
        assertEquals(1, c.getNodes().size());
        assertEquals(1, channel.getWrittenMessageCount());
        assertEquals(ZWaveNodeState.Ping, c.getNode((byte)0x02).getState());
    }


    @Test
    public void testExistingPersistentListeningNodeInterview() throws IOException {
        MockChannel channel = new MockChannel();
        MockZWaveControllerListener l = new MockZWaveControllerListener();
        MockPersistentStore store = new MockPersistentStore();
        ZWaveNode node = new BinaryPowerSwitch(new NodeInfo((byte)0x02, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID), true, null);
        store.saveNode(node);
        NettyZWaveController c = new NettyZWaveController("/dev/null", store);
        c.setChannel(channel);
        c.setListener(l);
        assertEquals(0, c.getNodes().size());
        assertEquals(0, channel.getWrittenMessageCount());
        assertNull(node.getState());

        c.onNodeProtocolInfo((byte)0x02, new NodeProtocolInfo(BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID, true));
        assertEquals(1, c.getNodes().size());
        assertEquals(1, channel.getWrittenMessageCount());
        assertEquals(ZWaveNodeState.Ping, node.getState());

        c.onTransactionComplete(new SendDataTransactionCompletedEvent("foo", null, (byte)0x02));
        assertEquals(4, channel.getWrittenMessageCount());
        assertEquals(ZWaveNodeState.NodeInfo, node.getState());
    }

    private class MockZWaveControllerListener implements ZWaveControllerListener {
        private String libraryVersion;
        private Integer homeId;
        private Byte nodeId;

        String getLibraryVersion() {
            return libraryVersion;
        }

        Integer getHomeId() {
            return homeId;
        }

        Byte getNodeId() {
            return nodeId;
        }

        @Override
        public void onZWaveNodeAdded(ZWaveEndpoint node) {

        }

        @Override
        public void onZWaveNodeUpdated(ZWaveEndpoint node) {

        }

        @Override
        public void onZWaveConnectionFailure(Throwable t) {

        }

        @Override
        public void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId) {
            this.libraryVersion = libraryVersion;
            this.homeId = homeId;
            this.nodeId = nodeId;
        }

        @Override
        public void onZWaveInclusionStarted() {

        }

        @Override
        public void onZWaveInclusion(NodeInfo nodeInfo, boolean success) {

        }

        @Override
        public void onZWaveInclusionStopped() {

        }

        @Override
        public void onZWaveExclusionStarted() {

        }

        @Override
        public void onZWaveExclusion(NodeInfo nodeInfo, boolean success) {

        }

        @Override
        public void onZWaveExclusionStopped() {

        }
    }
}
