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

public class SendDataTransactionFailedEvent extends TransactionFailedEvent {
    private boolean listeningNode;
    private boolean tgtNodeACKReceived;

    public SendDataTransactionFailedEvent(String id, DataFrame startFrame, byte nodeId, boolean listeningNode, boolean tgtNodeACKReceived) {
        super(id, startFrame, nodeId);
        this.listeningNode = listeningNode;
        this.tgtNodeACKReceived = tgtNodeACKReceived;
    }

    public boolean isListeningNode() {
        return listeningNode;
    }

    public boolean isTargetNodeACKReceived() {
        return tgtNodeACKReceived;
    }

    @Override
    public String toString()
    {
        return "SendDataTransactionFailedEvent{" +
                "id=" + getId() +
                ", nodeId=" + getNodeId() +
                ", listeningNode=" + isListeningNode() +
                ", tgtNodeACKReceived=" + isTargetNodeACKReceived() +
                '}';
    }
}
