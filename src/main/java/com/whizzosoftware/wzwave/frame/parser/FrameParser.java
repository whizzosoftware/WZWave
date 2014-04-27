/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.parser;

import com.whizzosoftware.wzwave.frame.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;

/**
 * Class responsible for receiving bytes from the serial port and converting them into the appropriate calls
 * to the FrameListener.
 *
 * @author Dan Noguerol
 */
public class FrameParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static int STATE_NEW_FRAME = 0;
    private final static int STATE_FRAME_LENGTH = 1;
    private final static int STATE_FRAME_DATA = 2;
    private final static int STATE_CHECKSUM = 3;

    private FrameListener frameListener;
    private ArrayDeque<Byte> byteBuffer = new ArrayDeque<Byte>();
    private int state = STATE_NEW_FRAME;
    private byte currentFrameLength;
    private int currentFrameIndex;
    private byte[] currentFrameData;

    public FrameParser(FrameListener frameListener) {
        this.frameListener = frameListener;
    }

    public void addBytes(byte[] bytes, int count) {
        for (int i=0; i < count; i++) {
            byteBuffer.add(bytes[i]);
        }
        processBuffer();
    }

    protected void processBuffer() {
        try {
            Byte b = byteBuffer.pop();
            while (b != null) {
                switch (state) {

                    case STATE_NEW_FRAME:
                        currentFrameLength = 0;
                        currentFrameIndex = 0;
                        currentFrameData = null;
                        switch (b) {
                            case 0x01:
                                state = STATE_FRAME_LENGTH;
                                break;
                            case 0x06:
                                frameListener.onACK();
                                break;
                            case 0x15:
                                frameListener.onNAK();
                                break;
                            case 0x18:
                                frameListener.onCAN();
                                break;
                            default:
                                logger.debug("Ignoring unexpected byte {}", b);
                                break;
                        }
                        break;

                    case STATE_FRAME_LENGTH:
                        currentFrameLength = b;
                        currentFrameData = new byte[currentFrameLength + 2];
                        currentFrameData[0] = 0x01;
                        currentFrameData[1] = currentFrameLength;
                        currentFrameIndex = 2;
                        state = STATE_FRAME_DATA;
                        break;

                    case STATE_FRAME_DATA:
                        currentFrameData[currentFrameIndex++] = b;
                        if (currentFrameIndex == currentFrameLength + 1) {
                            state = STATE_CHECKSUM;
                        }
                        break;

                    case STATE_CHECKSUM:
                        // TODO: verify the checksum
                        currentFrameData[currentFrameIndex] = b;
                        frameListener.onDataFrame(createDataFrame(currentFrameData));
                        state = STATE_NEW_FRAME;
                        break;
                }

                b = byteBuffer.pop();
            }
        } catch (NoSuchElementException e) {
            // NO-OP
        }
    }

    protected DataFrame createDataFrame(byte[] buffer) {
        byte messageType = buffer[3];

        switch (messageType) {
            case Version.ID:
                return new Version(buffer);
            case MemoryGetId.ID:
                return new MemoryGetId(buffer);
            case InitData.ID:
                return new InitData(buffer);
            case NodeProtocolInfo.ID:
                return new NodeProtocolInfo(buffer);
            case SendData.ID:
                return new SendData(buffer);
            case ApplicationCommand.ID:
                return new ApplicationCommand(buffer);
            case ApplicationUpdate.ID:
                return new ApplicationUpdate(buffer);
            case RequestNodeInfo.ID:
                return new RequestNodeInfo(buffer);
            case GetRoutingInfo.ID:
                return new GetRoutingInfo(buffer);
        }
        return null;
    }
}
