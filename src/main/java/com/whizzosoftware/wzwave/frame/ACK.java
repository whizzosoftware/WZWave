/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

/**
 * An acknowledgement (ACK) frame. This is an indicator to a source node that a
 * destination node has received a frame sent to it.
 *
 * @author Dan Noguerol
 */
public class ACK extends Frame {
    public static final byte ID = 0x06;

    @Override
    public byte[] getBytes() {
        return new byte[] { ID };
    }

    public String toString() {
        return "ACK";
    }
}
