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

public class SendDataTransactionCompletedEvent extends TransactionCompletedEvent {
    public SendDataTransactionCompletedEvent(String id, DataFrame frame, byte nodeId) {
        super(id, frame, nodeId);
    }

    @Override
    public String toString()
    {
        return "SendDataTransactionCompletedEvent{" +
                "id=" + getId() +
                ", nodeId=" + getNodeId() +
                '}';
    }
}
