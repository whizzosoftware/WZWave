/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.serial;

import com.whizzosoftware.wzwave.controller.ZWaveControllerWriteDelegate;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A thread that queues frames destined for the Z-Wave network and sends them in order.
 *
 * @author Dan Noguerol
 */
public class SerialZWaveControllerWriteThread implements Runnable, ZWaveControllerWriteDelegate {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SerialChannel serialChannel;
    private Thread thread;
    private final BlockingQueue<Frame> writeQueue = new LinkedBlockingQueue<Frame>();

    public SerialZWaveControllerWriteThread(SerialChannel serialChannel) {
        this.serialChannel = serialChannel;
    }

    @Override
    synchronized public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setName("Z-Wave Controller Write");
            thread.start();
        }
    }

    @Override
    synchronized public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void writeFrame(Frame frame) {
        logger.trace("Attempting to write: " + frame);
        writeQueue.add(frame);
    }

    public void run() {
        logger.debug("Z-Wave controller write thread starting");

        try {
            while (!thread.isInterrupted()) {
                // get the next item in the queue
                logger.trace("Waiting for next message to write");
                Frame bs = writeQueue.take();

                if (bs instanceof DataFrame) {
                    DataFrame currentMessage = (DataFrame)bs;
                    while (currentMessage != null && !currentMessage.isSendCountMaxExceeded()) {
                        if (writeByteStream(currentMessage)) {
                            currentMessage = null;
                        } else {
                            try {
                                // sleep with exponentially increasing delay
                                Thread.sleep(100 * currentMessage.getSendCount());
                            } catch (InterruptedException e) { // NO-OP
                            }
                        }
                    }
                } else {
                    writeByteStream(bs);
                }
            }
        } catch (InterruptedException e) {
            logger.error("Z-Wave controller write thread interrupted");
        }

        logger.debug("Z-Wave controller write thread exiting");
    }

    private boolean writeByteStream(Frame bs) {
        if (bs instanceof DataFrame) {
            ((DataFrame)bs).setSendTime(System.currentTimeMillis());
        }
        byte[] b = bs.getBytes();
        logger.trace("Sending bytes : " + ByteUtil.createString(bs));
        try {
            return (serialChannel.writeBytes(b, b.length) == b.length);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
