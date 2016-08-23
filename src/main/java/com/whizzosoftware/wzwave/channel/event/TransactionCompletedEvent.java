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
 * A user event that indicates a Z-Wave data frame transaction has completed.
 *
 * @author Dan Noguerol
 */
public class TransactionCompletedEvent {
    private String id;
    private boolean timeout;

    public TransactionCompletedEvent(String id, boolean timeout) {
        this.id = id;
        this.timeout = timeout;
    }

    public String getId() {
        return id;
    }

    public boolean isTimeout() {
        return timeout;
    }
}
