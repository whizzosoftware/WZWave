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
 * A user event that indicates a Z-Wave data frame transaction has completed.
 *
 * @author Dan Noguerol
 */
public class TransactionCompletedEvent {
    private String id;
    private DataFrame frame;
    private Byte nodeId;

    /**
     * Constructor.
     *
     * @param id the transaction ID
     * @param frame the data frame associated with the completed transaction
     */
    public TransactionCompletedEvent(String id, DataFrame frame) {
        this.id = id;
        this.frame = frame;
    }

    /**
     * Constructor.
     *
     * @param id the transaction ID
     * @param frame the data frame associated with the completed transaction
     * @param nodeId the node ID associated with the transaction
     */
    public TransactionCompletedEvent(String id, DataFrame frame, byte nodeId) {
        this(id, frame);
        this.nodeId = nodeId;
    }

    public String getId() {
        return id;
    }

    public boolean hasFrame() {
        return (frame != null);
    }

    public DataFrame getFrame() {
        return frame;
    }

    public Byte getNodeId() {
        return nodeId;
    }

    @Override
    public String toString()
    {
        return "TransactionCompletedEvent{" +
                "id=" + getId() +
                ", nodeId=" + getNodeId() +
                '}';
    }
}
