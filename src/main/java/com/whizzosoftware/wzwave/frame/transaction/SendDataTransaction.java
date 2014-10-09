/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SendDataTransaction is a transaction that is considered complete when:
 *
 * 1. An ACK is received
 * 2. A response is received with retVal == 0x01
 * 3. An request is received
 * 4. (Optional) An ApplicationCommand is received
 *
 * @author Dan Noguerol
 */
public class SendDataTransaction extends AbstractDataFrameTransaction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long DEFAULT_RESPONSE_TIMEOUT = 2000;

    private static final int STATE_REQUEST_SENT = 1;
    private static final int STATE_ACK_RECEIVED = 2;
    private static final int STATE_RESPONSE_RECEIVED = 3;
    private static final int STATE_REQUEST_RECEIVED = 4;
    private static final int STATE_COMPLETE = 5;

    private DataFrame finalFrame;
    private int state;
    private boolean isResponseExpected;
    private long responseReceiveTime;

    /**
     * Constructor.
     *
     * @param startFrame the frame that started the transaction
     * @param startTime the time the start frame was sent
     * @param isResponseExpected indicates if a response is expected
     */
    public SendDataTransaction(SendData startFrame, long startTime, boolean isResponseExpected) {
        super(startFrame, startTime);
        this.state = STATE_REQUEST_SENT;
        this.isResponseExpected = isResponseExpected;
    }

    public Byte getNodeId() {
        return ((SendData)getStartFrame()).getNodeId();
    }

    @Override
    public void addFrame(Frame bs, long now) {
        super.addFrame(bs, now);

        switch (state) {

            case STATE_REQUEST_SENT:
                if (bs instanceof ACK) {
                    logger.debug("Received ACK as expected");
                    state = STATE_ACK_RECEIVED;
                } else {
                    setError("Received unexpected frame for STATE_REQUEST_SENT: " + bs);
                }
                break;

            case STATE_ACK_RECEIVED:
                if (bs instanceof DataFrame) {
                    DataFrame response = (DataFrame)bs;
                    if (response.getType() == DataFrameType.RESPONSE) {
                        logger.debug("{} sent successfully", getStartFrame().getClass().getName());
                        state = STATE_RESPONSE_RECEIVED;
                    } else {
                        setError("Received frame but doesn't appear to be a response: " + bs);
                    }
                } else {
                    setError("Received unexpected frame for STATE_ACK_RECEIVED");
                }
                break;

            case STATE_RESPONSE_RECEIVED:
                if (bs instanceof DataFrame) {
                    if (((DataFrame)bs).getType() == DataFrameType.REQUEST) {
                        logger.debug("Response received for {}", getStartFrame().getClass().getName());
                        responseReceiveTime = now;
                        // if we shouldn't expect a response, the transaction is complete
                        if (!isResponseExpected) {
                            state = STATE_COMPLETE;
                        // otherwise, wait for the response
                        } else {
                            state = STATE_REQUEST_RECEIVED;
                        }
                    } else {
                        setError("Received data frame but doesn't appear to be a request: " + bs);
                    }
                } else {
                    setError("Received unexpected frame for STATE_RETVAL_RECEIVED");
                }
                break;

            case STATE_REQUEST_RECEIVED:
                if (bs instanceof ApplicationCommand) {
                    logger.debug("Application command received for {}", getStartFrame().getClass().getName());
                    state = STATE_COMPLETE;
                    finalFrame = (DataFrame)bs;
                } else {
                    setError("Received unexpected frame for STATE_REQUEST_RECEIVED");
                }
                break;
        }
    }

    @Override
    public boolean hasError(long now) {
        // make sure we're not waiting more than 2 seconds for a response...
        return state == STATE_REQUEST_RECEIVED &&
            isResponseExpected &&
            (now - responseReceiveTime > DEFAULT_RESPONSE_TIMEOUT) ||
            super.hasError(now);
    }

    @Override
    public boolean isComplete() {
        return (state == STATE_COMPLETE);
    }

    @Override
    public DataFrame getFinalFrame() {
        return finalFrame;
    }
}
