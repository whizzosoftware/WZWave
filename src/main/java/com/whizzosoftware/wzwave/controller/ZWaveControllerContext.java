package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.frame.DataFrame;

public interface ZWaveControllerContext {
    /**
     * Returns the controller node ID.
     *
     * @return a byte
     */
    byte getNodeId();

    /**
     * Sends a data frame to the Z-Wave network.
     *
     * @param frame the data frame to send
     */
    void sendDataFrame(DataFrame frame);
}
