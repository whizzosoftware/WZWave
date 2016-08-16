/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.node.ZWaveEndpoint;

/**
 * Listener interface for Z-Wave related events.
 *
 * @author Dan Noguerol
 */
public interface ZWaveControllerListener {
    /**
     * Callback when a new Z-Wave node is discovered.
     *
     * @param node the new Z-Wave node
     */
    void onZWaveNodeAdded(ZWaveEndpoint node);

    /**
     * Callback when an existing Z-Wave node is updated.
     *
     * @param node the updated Z-Wave node
     */
    void onZWaveNodeUpdated(ZWaveEndpoint node);

    /**
     * Callback when the initialization of the Z-Wave network has failed.
     *
     * @param t the cause
     */
    void onZWaveConnectionFailure(Throwable t);

    /**
     * Callback indicating the library has determined information about the Z-Wave controller.
     *
     * @param libraryVersion the library version of the controller
     * @param homeId the home ID of the controller
     * @param nodeId the node ID of the controller
     */
    void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId);

    /**
     * Callback indicating the Z-Wave controller has started network inclusion mode.
     */
    void onZWaveAddNodeToNetworkStarted();

    /**
     * Callback indicating the Z-Wave controller has stopped network inclusion mode.
     */
    void onZWaveAddNodeToNetworkStopped();
}
