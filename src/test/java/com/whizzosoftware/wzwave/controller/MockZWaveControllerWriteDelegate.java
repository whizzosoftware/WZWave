/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.frame.Frame;

import java.util.ArrayList;
import java.util.List;

public class MockZWaveControllerWriteDelegate implements ZWaveControllerWriteDelegate {
    private List<Frame> frameList = new ArrayList<Frame>();

    public List<Frame> getFrameList() {
        return frameList;
    }

    public int getFrameCount() {
        return frameList.size();
    }

    @Override
    public void writeFrame(Frame bs) {
        frameList.add(bs);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
