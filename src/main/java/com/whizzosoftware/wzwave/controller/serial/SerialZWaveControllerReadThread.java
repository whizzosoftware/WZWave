/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.serial;

import com.whizzosoftware.wzwave.frame.parser.FrameParser;
import com.whizzosoftware.wzwave.util.ByteUtil;
import com.whizzosoftware.wzwave.frame.parser.FrameListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A thread that reads frames from the Z-Wave network, parses them and dispatches
 * them to a FrameListener.
 *
 * @author Dan Noguerol
 */
public class SerialZWaveControllerReadThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SerialChannel serialChannel;
    private FrameParser parser;

    public SerialZWaveControllerReadThread(SerialChannel serialChannel, FrameListener frameListener) {
        this.serialChannel = serialChannel;
        this.parser = new FrameParser(frameListener);
        setName("Z-Wave Controller Read");
    }

    public void run() {
        byte[] buffer = new byte[8092];

        logger.debug("Z-Wave controller read thread starting");

        while (!isInterrupted()) {
            // TODO: can we make readBytes() block
            try {
                int count = serialChannel.readBytes(buffer, buffer.length);
                if (count > 0) {
                    logger.trace("Read " + count + " bytes: " + ByteUtil.createString(buffer, count));
                    parser.addBytes(buffer, count);
                }
            } catch (IOException e) {
                logger.error("Error reading from serial port", e);
            }
        }

        logger.debug("Z-Wave controller read thread exiting");
    }
}
