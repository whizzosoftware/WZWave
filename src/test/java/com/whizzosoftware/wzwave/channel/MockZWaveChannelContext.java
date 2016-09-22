/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.OutboundDataFrame;

import java.util.ArrayList;
import java.util.List;

public class MockZWaveChannelContext implements ZWaveChannelContext {
    private final List<Object> writeQueue = new ArrayList<>();
    private final List<Object> userEvents = new ArrayList<>();

    public List<Object> getWriteQueue() {
        return writeQueue;
    }

    public List<Object> getUserEvents() {
        return userEvents;
    }

    @Override
    public void fireEvent(Object o) {
        userEvents.add(o);
    }

    @Override
    public void writeFrame(OutboundDataFrame f) {
        writeQueue.add(f);
    }
}
