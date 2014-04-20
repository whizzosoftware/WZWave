/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.frame.Frame;

/**
 * Interface for class that can write frames to the Z-Wave network. This is helpful for unit tests
 * that need to check what has been written to the network.
 *
 * @author Dan Noguerol
 */
public interface ZWaveControllerWriteDelegate {
    /**
     * Starts the delegate (will be called before any frames are sent)
     */
    public void start();

    /**
     * Stops the delegate (once called, no more frames are sent)
     */
    public void stop();

    /**
     * Write a frame to the Z-Wave network.
     *
     * @param frame the frame to write
     */
    public void writeFrame(Frame frame);
}
