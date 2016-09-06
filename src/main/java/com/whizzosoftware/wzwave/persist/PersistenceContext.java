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

import java.util.Map;

/**
 * A context interface passed to nodes and command classes when they are saving state.
 *
 * @author Dan Noguerol
 */
public interface PersistenceContext {
    /**
     * Get a map for saving node state.
     *
     * @param nodeId the node ID being saved
     *
     * @return a Map
     */
    Map<String,Object> getNodeMap(int nodeId);

    /**
     * Get a map for saving command class state.
     *
     * @param nodeId the node ID that owns the command class
     * @param commandClassId the command class ID being saved
     *
     * @return a Map
     */
    Map<String,Object> getCommandClassMap(int nodeId, int commandClassId);
}
