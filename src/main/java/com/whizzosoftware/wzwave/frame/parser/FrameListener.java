/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.parser;

import com.whizzosoftware.wzwave.frame.DataFrame;

/**
 * Interface for classes that listen for incoming Z-Wave frames.
 *
 * @author Dan Noguerol
 */
public interface FrameListener {
    /**
     * Called when an acknowledgement (ACK) frame is received.
     */
    public void onACK();

    /**
     * Called when a negative acknowledgement (NAK) frame is received.
     */
    public void onNAK();

    /**
     * Called when a cancel (CAN) frame is received.
     */
    public void onCAN();

    /**
     * Called when a data frame is received.
     *
     * @param frame the data frame
     */
    public void onDataFrame(DataFrame frame);
}
