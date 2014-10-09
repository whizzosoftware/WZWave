package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.frame.DataFrame;

public interface ZWaveControllerContext {
    public void sendDataFrame(DataFrame frame);
}
