/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.DataFrameType;
import com.whizzosoftware.wzwave.frame.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RequestResponseTransaction is a transaction that is considered complete when:
 *
 * 1. An ACK is received
 * 2. A response is received with the same type as the request
 *
 * @author Dan Noguerol
 */
public class RequestResponseTransaction extends AbstractDataFrameTransaction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int STATE_REQUEST_SENT = 1;
    private static final int STATE_ACK_RECEIVED = 2;
    private static final int STATE_RESPONSE_RECEIVED = 3;

    private DataFrame finalFrame;
    private int state;

    public RequestResponseTransaction(DataFrame startFrame, long sendTime) {
        super(startFrame, sendTime);
        this.state = STATE_REQUEST_SENT;
    }

    @Override
    public void addFrame(Frame f, long now) {
        super.addFrame(f, now);

        switch (state) {
            case STATE_REQUEST_SENT:
                if (f instanceof ACK) {
                    logger.debug("Received ACK as expected");
                    state = STATE_ACK_RECEIVED;
                } else {
                    logger.error("Received unexpected frame for STATE_REQUEST_SENT: " + f);
                }
                break;

            case STATE_ACK_RECEIVED:
                if (f instanceof DataFrame && f.getClass() == getStartFrame().getClass()) {
                    if (((DataFrame)f).getType() == DataFrameType.RESPONSE) {
                        logger.debug("Received expected message response");
                        state = STATE_RESPONSE_RECEIVED;
                        finalFrame = ((DataFrame)f);
                    } else {
                        setError("Expected frame received but does not appear to be a response: " + f);
                    }
                } else {
                    setError("Received unexpected frame for STATE_ACK_RECEIVED: " + f);
                }
                break;
        }
    }

    @Override
    public boolean isComplete() {
        return (state == STATE_RESPONSE_RECEIVED);
    }

    @Override
    public DataFrame getFinalFrame() {
        return finalFrame;
    }
}
