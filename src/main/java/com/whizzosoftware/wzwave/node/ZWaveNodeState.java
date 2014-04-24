/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

/**
 * Enumeration for Z-Wave node state.
 *
 * @author Dan Noguerol
 */
public enum ZWaveNodeState {
    NodeInfo,
    RetrieveVersionPending,
    RetrieveVersionCompleted,
    RetrieveStatePending,
    RetrieveStateCompleted,
    Started
}
