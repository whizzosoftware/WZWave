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

import com.whizzosoftware.wzwave.commandclass.SecurityCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.frame.ApplicationCommand;
import com.whizzosoftware.wzwave.node.*;
import com.whizzosoftware.wzwave.node.generic.EntryControl;
import com.whizzosoftware.wzwave.node.specific.SecureKeypadDoorLock;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

public class NettyZWaveControllerTest {
    @Test
    public void testOnZWaveControllerInfo() throws GeneralSecurityException {
        NettyZWaveController c = new NettyZWaveController("/dev/null", new byte[] {0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF});
        MockZWaveControllerListener l = new MockZWaveControllerListener();
        c.setListener(l);
        c.onLibraryInfo("Z-Wave 2.7.8\u0000");
        c.onControllerInfo(22727648, (byte)1);
        assertEquals("Z-Wave 2.7.8\u0000", l.getLibraryVersion());
        assertEquals(22727648, (int)l.getHomeId());
        assertEquals(1, (byte)l.getNodeId());
    }

    @Test
    public void testSecureDeviceInclusion() throws GeneralSecurityException {
        byte nodeId = 0x03;
        NettyZWaveController c = new NettyZWaveController("/dev/null", new byte[] {0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF, 0xB, 0xE, 0xE, 0xF});
        c.setNodeId((byte)0x01);
        MockZWaveControllerListener l = new MockZWaveControllerListener();
        c.setListener(l);
        c.onZWaveInclusion(nodeId, new NodeInfo(nodeId, BasicDeviceClasses.SLAVE, EntryControl.ID, SecureKeypadDoorLock.ID, new byte[] {SecurityCommandClass.ID}), true);
        assertEquals(ZWaveNodeState.SchemeGetSent, c.getNode(nodeId).getState());
        c.onApplicationCommand(new ApplicationCommand(Unpooled.copiedBuffer(new byte[] {0x01, 0x09, 0x00, 0x04, 0x00, 0x03, 0x03, (byte)0x98, 0x05, 0x00, 0x6F})));
        assertEquals(ZWaveNodeState.NonceGetSent, c.getNode(nodeId).getState());
        c.onApplicationCommand(new ApplicationCommand(Unpooled.copiedBuffer(new byte[] {0x01, 0x10, 0x00, 0x04, 0x00, 0x03, 0x0A, (byte)0x98, (byte)0x80, (byte)0xD8, 0x74, (byte)0xDA, (byte)0x8C, (byte)0xD9, 0x7D, (byte)0xCD, (byte)0xC5, (byte)0xAC})));
        assertEquals(ZWaveNodeState.NetworkKeySent, c.getNode(nodeId).getState());
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
        public void onZWaveInclusion(Byte nodeId, NodeInfo nodeInfo, boolean success) {

        }

        @Override
        public void onZWaveInclusionStopped() {

        }

        @Override
        public void onZWaveExclusionStarted() {

        }

        @Override
        public void onZWaveExclusion(Byte nodeId, NodeInfo nodeInfo, boolean success) {

        }

        @Override
        public void onZWaveExclusionStopped() {

        }
    }
}
