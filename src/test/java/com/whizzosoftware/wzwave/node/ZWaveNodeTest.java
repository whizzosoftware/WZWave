/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.controller.MockZWaveControllerContext;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ZWaveNodeTest {
    @Test
    public void testLifecycleStartupListeningNodeWithFailedPing() {
        MockZWaveControllerContext ctx = new MockZWaveControllerContext();
        MockNode node = new MockNode((byte)0x02, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID, true, null);
        node.startInterview(ctx);
        assertEquals(ZWaveNodeState.Ping, node.getState());
        assertEquals(1, ctx.getSentFrames().size());
        node.onSendDataCallback(ctx, false);
        assertFalse(node.isAvailable());
        assertFalse(node.isSleeping());
        assertTrue(node.isStarted());
    }

    @Test
    public void testLifecycleStartupNonListeningNodeWithFailedPing() {
        MockZWaveControllerContext ctx = new MockZWaveControllerContext();
        MockNode node = new MockNode((byte)0x02, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID, false, null);
        node.startInterview(ctx);
        assertEquals(ZWaveNodeState.Ping, node.getState());
        assertEquals(1, ctx.getSentFrames().size());
        node.onSendDataCallback(ctx, false);
        assertTrue(node.isAvailable());
        assertTrue(node.isSleeping());
        assertTrue(node.isStarted());
    }

    private class MockNode extends ZWaveNode {
        public MockNode(byte nodeId, byte basicDeviceClass, byte genericDeviceClass, byte specificDeviceClass, boolean isListeningNode, NodeListener listener) {
            super(new NodeInfo(nodeId, basicDeviceClass, genericDeviceClass, specificDeviceClass), isListeningNode, listener);
        }

        @Override
        protected void refresh(boolean deferIfNotListening) {

        }
    }
}
