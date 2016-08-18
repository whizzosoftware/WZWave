/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.frame.DataFrame;

/**
 * An interface for accessing controller information.
 *
 * @author Dan Noguerol
 */
public interface ZWaveControllerContext {
    /**
     * Returns the controller node ID.
     *
     * @return a byte
     */
    byte getNodeId();

    /**
     * Returns the controller network key.
     *
     * @return a byte array
     */
    byte[] getNetworkKey();

    /**
     * Returns the controller's frame encryption key (derived from the network key).
     *
     * @return a byte array
     */
    byte[] getFrameEncryptionKey();

    /**
     * Returns the controller's data origin authentication key (derived from the network key).
     *
     * @return a byte array
     */
    byte[] getDataOriginAuthenticationKey();

    /**
     * Sends a data frame to the Z-Wave network.
     *
     * @param frame the data frame to send
     */
    void sendDataFrame(DataFrame frame);
}
