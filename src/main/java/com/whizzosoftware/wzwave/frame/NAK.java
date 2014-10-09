/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

/**
 * A negative acknowledgement (NAK) frame.
 *
 * @author Dan Noguerol
 */
public class NAK extends Frame {
    public static final byte ID = 0x15;

    @Override
    public byte[] getBytes() {
        return new byte[] { ID };
    }
}
