/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel.event;

/**
 * An event sent when a node's sleeping state has changed.
 *
 * @author Dan Noguerol
 */
public class NodeSleepChangeEvent {
    private byte nodeId;
    private boolean sleeping;

    public NodeSleepChangeEvent(byte nodeId, boolean sleeping) {
        this.nodeId = nodeId;
        this.sleeping = sleeping;
    }

    public byte getNodeId() {
        return nodeId;
    }

    public boolean isSleeping() {
        return sleeping;
    }
}
