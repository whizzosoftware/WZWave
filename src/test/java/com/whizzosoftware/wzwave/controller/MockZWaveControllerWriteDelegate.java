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
