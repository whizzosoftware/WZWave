/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

/**
 * Abstract base class for all Z-Wave frames.
 *
 * @author Dan Noguerol
 */
abstract public class Frame {
    abstract public byte[] getBytes();
}
