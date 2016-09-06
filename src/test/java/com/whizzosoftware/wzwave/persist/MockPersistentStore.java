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

import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;

import java.util.HashMap;
import java.util.Map;

public class MockPersistentStore implements PersistentStore {
    private Map<Byte,ZWaveNode> nodes = new HashMap<>();

    @Override
    public ZWaveNode getNode(byte nodeId, NodeListener listener) {
        return nodes.get(nodeId);
    }

    @Override
    public void saveNode(ZWaveNode node) {
        nodes.put(node.getNodeId(), node);
    }
}
