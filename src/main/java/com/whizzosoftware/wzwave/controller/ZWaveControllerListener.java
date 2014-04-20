/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.node.ZWaveNode;

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
    public void onZWaveNodeAdded(ZWaveNode node);

    /**
     * Callback when an existing Z-Wave node is updated.
     *
     * @param node the updated Z-Wave node
     */
    public void onZWaveNodeUpdate(ZWaveNode node);
}
