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

import com.whizzosoftware.wzwave.frame.DataFrame;

/**
 * A user event that indicates a Z-Wave data frame transaction has failed.
 *
 * @author Dan Noguerol
 */
public class TransactionFailedEvent {
    private String id;
    private Byte nodeId;
    private DataFrame startFrame;

    /**
     * Constructor.
     *
     * @param id the transaction ID
     * @param startFrame the data frame that initiated the transaction
     */
    public TransactionFailedEvent(String id, DataFrame startFrame) {
        this(id, startFrame, null);
    }

    /**
     * Constructor.
     *
     * @param id the transaction ID
     * @param startFrame the data frame that initiated the transaction
     * @param nodeId the node ID associated with the transaction
     */
    public TransactionFailedEvent(String id, DataFrame startFrame, Byte nodeId) {
        this.id = id;
        this.nodeId = nodeId;
        this.startFrame = startFrame;
    }

    public String getId() {
        return id;
    }

    public Byte getNodeId() {
        return nodeId;
    }

    public boolean hasStartFrame() {
        return (startFrame != null);
    }

    public DataFrame getStartFrame() {
        return startFrame;
    }

    @Override
    public String toString()
    {
        return "TransactionFailedEvent{" +
                "id=" + getId() +
                ", nodeId=" + getNodeId() +
                '}';
    }
}
