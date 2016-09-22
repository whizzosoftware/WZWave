/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame;

/**
 * A wrapper class that provides an indication of whether an outbound data frame is destined for a listening node.
 * This is important to know in transmission failure situations.
 *
 * @author Dan Noguerol
 */
public class OutboundDataFrame {
    private DataFrame dataFrame;
    private boolean isListeningNode;

    public OutboundDataFrame(DataFrame dataFrame, boolean isListeningNode) {
        this.dataFrame = dataFrame;
        this.isListeningNode = isListeningNode;
    }

    public boolean hasDataFrame() {
        return (dataFrame != null);
    }

    public DataFrame getDataFrame() {
        return dataFrame;
    }

    public boolean isListeningNode() {
        return isListeningNode;
    }

    public boolean matchesTransaction(String transactionId) {
        return (transactionId != null && transactionId.equals(dataFrame.getTransactionId()));
    }

    public String toString() {
        return dataFrame.toString();
    }
}
