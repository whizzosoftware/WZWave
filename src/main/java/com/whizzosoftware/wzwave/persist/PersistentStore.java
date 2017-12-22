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

import com.whizzosoftware.wzwave.node.NodeCreationException;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;

/**
 * An interface for persistent storage.
 *
 * @author Dan Noguerol
 */
public interface PersistentStore {
    /**
     * Retrieve a persisted node.
     *
     * @param nodeId the node ID
     * @param listener a listener
     *
     * @return a ZWaveNode (or null if not found)
     * @throws NodeCreationException if node data is found but fails to be restored
     */
    ZWaveNode getNode(byte nodeId, NodeListener listener) throws NodeCreationException;

    /**
     * Persists a node.
     *
     * @param node the node to save
     */
    void saveNode(ZWaveNode node);

    /**
     * Closes the persistent store.
     */
    void close();
}
