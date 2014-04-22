/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

/**
 * Interface for classes that want to monitor node events.
 *
 * @author Dan Noguerol
 */
public interface NodeListener {
    /**
     * Called when a node is started.
     *
     * @param node the node that was started
     */
    public void onNodeStarted(ZWaveNode node);
}
