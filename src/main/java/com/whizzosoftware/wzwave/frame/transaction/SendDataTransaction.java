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

    private static final int STATE_REQUEST_SENT = 1;
    private static final int STATE_ACK_RECEIVED = 2;
    private static final int STATE_RESPONSE_RECEIVED = 3;
    private static final int STATE_REQUEST_RECEIVED = 4;
    private static final int STATE_COMPLETE = 5;

    private DataFrame finalFrame;
    private int state;
    private boolean isResponseExpected;

    /**
     * Constructor.
     *
     * @param startFrame the frame that started the transaction
     * @param isResponseExpected indicates if a response is expected
     */
    public SendDataTransaction(SendData startFrame, boolean isResponseExpected) {
        super(startFrame);
        this.isResponseExpected = isResponseExpected;
        reset();
    }

    public Byte getNodeId() {
        return ((SendData)getStartFrame()).getNodeId();
    }

    @Override
    public boolean addFrame(Frame bs) {
        switch (state) {

            case STATE_REQUEST_SENT:
                if (bs instanceof ACK) {
                    logger.trace("Received ACK as expected");
                    state = STATE_ACK_RECEIVED;
                    return true;
                } else if (bs instanceof CAN) {
                    setError("Received CAN; will re-send");
                    return true;
                } else {
                    logger.warn("Received unexpected frame for STATE_REQUEST_SENT: {}", bs);
                }
                break;

            case STATE_ACK_RECEIVED:
                if (bs instanceof CAN) {
                    setError("Received CAN; will re-send");
                    return true;
                } else if (bs instanceof SendData) {
                    if (((SendData)bs).getType() == DataFrameType.RESPONSE) {
                        logger.trace("{} sent successfully", getStartFrame().getClass().getName());
                        state = STATE_RESPONSE_RECEIVED;
                        return true;
                    } else {
                        setError("Received frame but doesn't appear to be a response: " + bs);
                    }
                } else {
                    logger.warn("Received unexpected frame for STATE_ACK_RECEIVED: {}", bs);
                }
                break;

            case STATE_RESPONSE_RECEIVED:
                if (bs instanceof CAN) {
                    setError("Received CAN; will re-send");
                    return true;
                } else if (bs instanceof DataFrame) {
                    if (((DataFrame)bs).getType() == DataFrameType.REQUEST) {
                        logger.trace("Response received for {}", getStartFrame().getClass().getName());
                        // if we shouldn't expect a response, the transaction is complete
                        if (!isResponseExpected) {
                            state = STATE_COMPLETE;
                        // otherwise, wait for the response
                        } else {
                            state = STATE_REQUEST_RECEIVED;
                        }
                        return true;
                    } else {
                        setError("Received data frame but doesn't appear to be a request: " + bs);
                    }
                } else {
                    logger.warn("Received unexpected frame for STATE_RETVAL_RECEIVED: {}", bs);
                }
                break;

            case STATE_REQUEST_RECEIVED:
                if (bs instanceof CAN) {
                    setError("Received CAN; will re-send");
                    return true;
                } else if (bs instanceof ApplicationCommand) {
                    logger.trace("Application command received for {}", getStartFrame().getClass().getName());
                    state = STATE_COMPLETE;
                    finalFrame = (DataFrame)bs;
                    return true;
                } else {
                    logger.warn("Received unexpected frame for STATE_REQUEST_RECEIVED: {}", bs);
                }
                break;
        }

        return false;
    }

    @Override
    public boolean isComplete() {
        return (state == STATE_COMPLETE || hasError());
    }

    @Override
    public DataFrame getFinalFrame() {
        return finalFrame;
    }

    @Override
    public void reset() {
        finalFrame = null;
        state = STATE_REQUEST_SENT;
    }
}
