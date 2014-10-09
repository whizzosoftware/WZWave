package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.frame.DataFrame;

import java.util.ArrayList;
import java.util.List;

public class MockZWaveControllerContext implements ZWaveControllerContext {
    private List<DataFrame> sentFrames = new ArrayList<DataFrame>();

    @Override
    public void sendDataFrame(DataFrame frame) {
        sentFrames.add(frame);
    }

    public int getSentFrameCount() {
        return sentFrames.size();
    }

    public List<DataFrame> getSentFrames() {
        return sentFrames;
    }

    public void clearSentFrames() {
        sentFrames.clear();
    }
}
