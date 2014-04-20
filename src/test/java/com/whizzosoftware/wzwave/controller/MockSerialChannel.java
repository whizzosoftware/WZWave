package com.whizzosoftware.wzwave.controller;

import com.whizzosoftware.wzwave.controller.serial.SerialChannel;

public class MockSerialChannel implements SerialChannel {
    @Override
    public void setBaudRate(int baudRate) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNumStopBits(int stopBits) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setParity(int parity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean openPort() {
        return true;
    }

    @Override
    public int readBytes(byte[] data, int length) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int writeBytes(byte[] data, int length) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {

    }
}
