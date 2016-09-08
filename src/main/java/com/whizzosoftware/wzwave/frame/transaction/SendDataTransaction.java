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
 * 3. A callback is received
 * 4. An ApplicationCommand is received (if the original SendData frame was flagged as requiring a response)
 *
 * @author Dan Noguerol
 */
public class SendDataTransaction extends AbstractDataFrameTransaction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int STATE_REQUEST_SENT = 1;
    private static final int STATE_ACK_RECEIVED = 2;
    private static final int STATE_RESPONSE_RECEIVED = 3;
    private static final int STATE_CALLBACK_RECEIVED = 4;
    private static final int STATE_COMPLETE = 5;

    private static final int TRANSMIT_COMPLETE_OK = 0;
    private static final int TRANSMIT_COMPLETE_NO_ACK = 1;
    private static final int TRANSMIT_COMPLETE_FAIL = 2;

    private DataFrame finalFrame;
    private int state;
    private boolean isResponseExpected;
    private boolean applicationCommandReceived;

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
                    setError("Received CAN; will re-send", true);
                    return true;
                } else {
                    logger.warn("Received unexpected frame for STATE_REQUEST_SENT: {}", bs);
                }
                break;

            case STATE_ACK_RECEIVED:
                if (bs instanceof CAN) {
                    setError("Received CAN; will re-send", true);
                    return true;
                } else if (bs instanceof SendData) {
                    if (((SendData)bs).getType() == DataFrameType.RESPONSE) {
                        logger.trace("{} sent successfully", getStartFrame().getClass().getName());
                        state = STATE_RESPONSE_RECEIVED;
                        return true;
                    } else {
                        setError("Received frame but doesn't appear to be a response: " + bs, false);
                    }
                } else {
                    logger.warn("Received unexpected frame for STATE_ACK_RECEIVED: {}", bs);
                }
                break;

            case STATE_RESPONSE_RECEIVED:
                if (bs instanceof CAN) {
                    setError("Received CAN; will re-send", true);
                    return true;
                } else if (bs instanceof SendData) {
                    SendData sd = (SendData)bs;
                    if (sd.getType() == DataFrameType.REQUEST) {
                        logger.trace("SendData callback received with Tx status {}", sd.getTx());
                        // if the controller told us the transmission was ACKed, move on
                        if (sd.getTx() == TRANSMIT_COMPLETE_OK) {
                            // if we shouldn't expect a response, the transaction is complete
                            if (!isResponseExpected || applicationCommandReceived) {
                                state = STATE_COMPLETE;
                                // otherwise, wait for the response
                            } else {
                                state = STATE_CALLBACK_RECEIVED;
                            }
                        } else if (sd.getTx() == TRANSMIT_COMPLETE_NO_ACK) {
                            state = STATE_COMPLETE;
                            setNoACK(true);
                            setError("Received no ACK from target node; possibly asleep", false);
                        } else if (sd.getTx() == TRANSMIT_COMPLETE_FAIL){
                            state = STATE_COMPLETE;
                            setError("Transmission failure due to possible network congestion", false);
                        }
                        return true;
                    } else {
                        setError("Received data frame but doesn't appear to be a SendData callback: " + bs, false);
                    }
                } else if (bs instanceof ApplicationCommand) {
                    // sometimes the ApplicationCommand is returned before the SendData callback; flag that case here
                    applicationCommandReceived = true;
                } else {
                    logger.warn("Received unexpected frame for STATE_RETVAL_RECEIVED: {}", bs);
                }
                break;

            case STATE_CALLBACK_RECEIVED:
                if (bs instanceof CAN) {
                    setError("Received CAN; will re-send", true);
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
