package com.whizzosoftware.wzwave;

import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.frame.parser.FrameListener;

import java.util.ArrayList;
import java.util.List;

public class MockFrameListener implements FrameListener {
    public List<Frame> messages = new ArrayList<Frame>();

    public void clear() {
        messages.clear();
    }

    @Override
    public void onACK() {
        messages.add(new ACK());
    }

    @Override
    public void onNAK() {
        messages.add(new NAK());
    }

    @Override
    public void onCAN() {
        messages.add(new CAN());
    }

    @Override
    public void onDataFrame(DataFrame frame) {
        messages.add(frame);
    }
}
