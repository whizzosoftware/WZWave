/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import org.junit.Test;
import static org.junit.Assert.*;

public class ApplicationUpdateTest {
    @Test
    public void testConstructorWithNodeInfoReceived() {
        ApplicationUpdate au = new ApplicationUpdate(new byte[] {0x01, 16, 0x00, 0x49, (byte)0x84, 0x02, 0x0a, 0x04, 0x10, 0x01, 0x25, 0x27, 0x75, 0x73, (byte)0x86, 0x72, 0x77, (byte)0xb8});
        assertEquals(ApplicationUpdate.UPDATE_STATE_NODE_INFO_RECEIVED, au.getState());
        assertEquals((byte)0x02, au.getNodeId());
        assertNotNull(au.getNodeInfo());
        assertEquals((byte)0x04, au.getNodeInfo().getBasicDeviceClass());
        assertEquals((byte)0x10, au.getNodeInfo().getGenericDeviceClass());
        assertEquals((byte)0x01, au.getNodeInfo().getSpecificDeviceClass());
        assertEquals(7, au.getNodeInfo().getCommandClasses().length);
        assertEquals((byte)0x25, au.getNodeInfo().getCommandClasses()[0]);
        assertEquals((byte)0x27, au.getNodeInfo().getCommandClasses()[1]);
        assertEquals((byte)0x75, au.getNodeInfo().getCommandClasses()[2]);
        assertEquals((byte)0x73, au.getNodeInfo().getCommandClasses()[3]);
        assertEquals((byte)0x86, au.getNodeInfo().getCommandClasses()[4]);
        assertEquals((byte)0x72, au.getNodeInfo().getCommandClasses()[5]);
        assertEquals((byte)0x77, au.getNodeInfo().getCommandClasses()[6]);
    }

    @Test
    public void testConstructorWithNodeInfoRequestFailed() {
        ApplicationUpdate au = new ApplicationUpdate(new byte[] {0x01, 6, 0x00, 0x49, (byte)0x81, 0x00, 0x00, 0x31});
        assertEquals(ApplicationUpdate.UPDATE_STATE_NODE_INFO_REQ_FAILED, au.getState());
    }
}
