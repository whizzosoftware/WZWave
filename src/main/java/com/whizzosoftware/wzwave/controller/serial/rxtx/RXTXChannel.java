/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.serial.rxtx;

import com.whizzosoftware.wzwave.controller.serial.SerialChannel;
import gnu.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

/**
 * An RXTX implementation of the SerialChannel interface.
 *
 * @author Dan Noguerol
 */
public class RXTXChannel implements SerialChannel, SerialPortEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    public RXTXChannel(String port) throws IOException {
        try {
            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);
            serialPort = (SerialPort)portId.open("Z-Wave Java", 5000);
            try {
                serialPort.addEventListener(this);
            } catch (TooManyListenersException e) {
                logger.error("Error adding event listener to serial port", e);
            }
            serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (NoSuchPortException e) {
            throw new IOException(e);
        } catch (PortInUseException e) {
            throw new IOException(e);
        } catch (UnsupportedCommOperationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setBaudRate(int baudRate) {
        try {
            serialPort.setSerialPortParams(baudRate, serialPort.getDataBits(), serialPort.getStopBits(), serialPort.getParity());
        } catch (UnsupportedCommOperationException e) {
            logger.error("Error setting baud rate", e);
        }
    }

    @Override
    public void setNumStopBits(int stopBits) {
        int s;
        switch (stopBits) {
            default:
                s = SerialPort.STOPBITS_1;
                break;
        }

        try {
            serialPort.setSerialPortParams(serialPort.getBaudRate(), serialPort.getDataBits(), s, serialPort.getParity());
        } catch (UnsupportedCommOperationException e) {
            logger.error("Error setting stop bits", e);
        }
    }

    @Override
    public void setParity(int parity) {
        int p;

        switch (parity) {
            case SerialChannel.PARITY_EVEN:
                p = SerialPort.PARITY_EVEN;
                break;
            case SerialChannel.PARITY_ODD:
                p = SerialPort.PARITY_ODD;
                break;
            default:
                p = SerialPort.PARITY_NONE;
                break;
        }

        try {
            serialPort.setSerialPortParams(serialPort.getBaudRate(), serialPort.getDataBits(), serialPort.getStopBits(), p);
        } catch (UnsupportedCommOperationException e) {
            logger.error("Error setting parity", e);
        }
    }

    @Override
    public boolean openPort() {
        logger.debug("Opening serial port");
        try {
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            return true;
        } catch (IOException e) {
            logger.error("Error opening serial port", e);
            return false;
        }
    }

    @Override
    public int readBytes(byte[] data, int length) throws IOException {
        return inputStream.read(data, 0, length);
    }

    @Override
    public int writeBytes(byte[] data, int length) throws IOException {
        outputStream.write(data, 0, length);
        return length;
    }

    @Override
    public void close() {
        logger.debug("Closing serial port");
        serialPort.removeEventListener();
        new Thread(new Runnable() {
            @Override
            public void run() {
                serialPort.close();
                logger.debug("Serial port successfully closed");
            }
        }).start();
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        // NO-OP
        System.out.println("serialEvent: " + serialPortEvent);
    }
}
