/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.persist;

import com.whizzosoftware.wzwave.node.BasicDeviceClasses;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;
import com.whizzosoftware.wzwave.persist.mapdb.MapDbPersistentStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class MapDbPersistentStoreTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSaveAndRestoreNode() throws Exception {
        ZWaveNode node = new BinaryPowerSwitch(new NodeInfo((byte)0x02, BasicDeviceClasses.ROUTING_SLAVE, BinarySwitch.ID, BinaryPowerSwitch.ID), false, true, null);
        MapDbPersistentStore store = new MapDbPersistentStore(folder.newFolder());
        store.saveNode(node);
        node = store.getNode((byte)0x02, null);
        assertEquals(0x02, node.getNodeId());
        assertEquals(BasicDeviceClasses.ROUTING_SLAVE, (byte)node.getBasicDeviceClass());
        assertEquals(BinarySwitch.ID, (byte)node.getGenericDeviceClass());
        assertEquals(BinaryPowerSwitch.ID, (byte)node.getSpecificDeviceClass());
        assertEquals(2, node.getCommandClasses().size());
        assertTrue(node.isListening());
    }
}
