/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.frame.DataFrame;

import java.util.Collection;

/**
 * Interface representing a physical Z-Wave controller.
 *
 * @author Dan Noguerol
 */
public interface ZWaveController {
    /**
     * Sets a listener for Z-Wave events.
     *
     * @param listener the listener
     */
    void setListener(ZWaveControllerListener listener);

    /**
     * Start the controller (i.e. start processing events).
     */
    void start();

    /**
     * Stops the controller (i.e. stop processing events).
     */
    void stop();

    /**
     * Returns the home ID of this controller.
     *
     * @return an int
     */
    int getHomeId();

    /**
     * Returns the node ID of this controller.
     *
     * @return a byte
     */
    byte getNodeId();

    /**
     * Returns the controller's Z-Wave library version
     *
     * @return the version String
     */
    String getLibraryVersion();

    /**
     * Returns the collection of nodes this controller knows about.
     *
     * @return a Collection of nodes
     */
    Collection<ZWaveNode> getNodes();

    /**
     * Returns a specific node.
     *
     * @param nodeId the ID of the node
     *
     * @return a ZWaveNode instance (or null if not found)
     */
    ZWaveNode getNode(byte nodeId);

    /**
     * Sends a data frame to the Z-Wave network.
     *
     * @param dataFrame the data frame
     */
    void sendDataFrame(DataFrame dataFrame);
}
