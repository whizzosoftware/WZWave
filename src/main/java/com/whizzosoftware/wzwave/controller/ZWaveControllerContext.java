/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.frame.DataFrame;

public interface ZWaveControllerContext {
    /**
     * Returns the controller node ID.
     *
     * @return a byte
     */
    byte getNodeId();

    /**
     * Sends a data frame to the Z-Wave network.
     *
     * @param frame the data frame to send
     * @param isListeningNode indicates whether the node is known to be a listening node
     */
    void sendDataFrame(DataFrame frame, boolean isListeningNode);

    /**
     * Sends an event to the WZWave runtime.
     *
     * @param e the event
     */
    void sendEvent(Object e);
}
