package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.DataFrame;

public class DataFrameWrittenEvent {
    private DataFrame frame;

    public DataFrameWrittenEvent(DataFrame frame) {
        this.frame = frame;
    }

    public DataFrame getFrame() {
        return frame;
    }
}
