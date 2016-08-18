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

import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class NettyZWaveControllerTest {
    @Test
    public void testOnZWaveControllerInfo() {
        NettyZWaveController c = new NettyZWaveController("/dev/null");
        MockZWaveControllerListener l = new MockZWaveControllerListener();
        c.setListener(l);
        c.onLibraryInfo("Z-Wave 2.7.8\u0000");
        c.onControllerInfo(22727648, (byte)1);
        assertEquals("Z-Wave 2.7.8\u0000", l.getLibraryVersion());
        assertEquals(22727648, (int)l.getHomeId());
        assertEquals(1, (byte)l.getNodeId());
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
