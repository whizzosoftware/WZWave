/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.serial;

import java.io.IOException;

/**
 * An interface representing a serial channel. This prevents us from coupling to any
 * particular serial implementation (e.g. RXTX).
 *
 * @author Dan Noguerol
 */
public interface SerialChannel {
    public static final int PARITY_NONE = 0;
    public static final int PARITY_EVEN = 1;
    public static final int PARITY_ODD  = 2;

    public static final int ONE_STOP_BIT = 0;

    public void setBaudRate(int baudRate);
    public void setNumStopBits(int stopBits);
    public void setParity(int parity);
    public boolean openPort();
    public int readBytes(byte[] data, int length) throws IOException;
    public int writeBytes(byte[] data, int length) throws IOException;
    public void close();
}
