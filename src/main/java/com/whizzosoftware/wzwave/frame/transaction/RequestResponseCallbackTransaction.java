/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.channel.ZWaveChannelContext;
import com.whizzosoftware.wzwave.channel.event.TransactionCompletedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionFailedEvent;
import com.whizzosoftware.wzwave.frame.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RequestResponseCallbackTransaction is a transaction that is considered complete when:
 *
 * 1. An ACK is received
 * 2. A response is received with retVal == 0x01
 * 3. A callback (data frame) is received
 *
 * @author Dan Noguerol
 */
class RequestResponseCallbackTransaction extends AbstractDataFrameTransaction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int STATE_REQUEST_SENT = 1;
    private static final int STATE_ACK_RECEIVED = 2;
    private static final int STATE_RESPONSE_RECEIVED = 3;
    static final int STATE_COMPLETE = 4;

    private int state;

    RequestResponseCallbackTransaction(ZWaveChannelContext ctx, DataFrame startFrame, boolean listeningNode) {
        super(ctx, startFrame, listeningNode);
        reset();
    }

    @Override
    public boolean addFrame(ZWaveChannelContext ctx, Frame bs) {
        switch (state) {

            case STATE_REQUEST_SENT:
                if (bs instanceof ACK) {
                    logger.trace("Received ACK as expected");
                    setState(STATE_ACK_RECEIVED);
                    return true;
                } else if (bs instanceof CAN) {
                    logger.trace("Received CAN; will attempt re-send");
                    failTransaction(ctx, true);
                    return true;
                } else {
                    logger.trace("Received unexpected frame for STATE_REQUEST_SENT: {}" + bs);
                }
                break;

            case STATE_ACK_RECEIVED:
                if (bs instanceof CAN) {
                    logger.trace("Received CAN; will attempt re-send");
                    failTransaction(ctx, true);
                    return true;
                } else if (bs instanceof DataFrame) {
                    DataFrame response = (DataFrame)bs;
                    if (response.getType() == DataFrameType.RESPONSE) {
                        if (wasSendSuccessful(response)) {
                            logger.trace("{} sent successfully", getStartFrame().getClass().getName());
                            setState(STATE_RESPONSE_RECEIVED);
                            return true;
                        } else {
                            logger.trace("{} not sent successfully", getStartFrame().getClass().getName());
                            failTransaction(ctx, false);
                            return true;
                        }
                    } else {
                        logger.trace("Received frame but doesn't appear to be a response: {}", bs);
                        failTransaction(ctx, false);
                    }
                } else {
                    logger.trace("Received unexpected frame for STATE_ACK_RECEIVED");
                }
                break;

            case STATE_RESPONSE_RECEIVED:
                if (bs instanceof CAN) {
                    logger.trace("Received CAN; will attempt re-send");
                    failTransaction(ctx, true);
                    return true;
                } else if (bs instanceof DataFrame) {
                    if (((DataFrame)bs).getType() == DataFrameType.REQUEST) {
                        logger.trace("Response received for {}", getStartFrame().getClass().getName());
                        completeTransaction(ctx, (DataFrame)bs);
                        return true;
                    } else {
                        logger.trace("Received data frame but doesn't appear to be a request: {}", bs);
                        failTransaction(ctx, false);
                    }
                } else {
                    logger.trace("Received unexpected frame for STATE_RETVAL_RECEIVED");
                }
                break;

        }

        return false;
    }

    @Override
    public boolean isComplete() {
        return (state == STATE_COMPLETE);
    }

    @Override
    public void reset() {
        setState(STATE_REQUEST_SENT);
    }

    protected void setState(int state) {
        this.state = state;
    }

    protected void completeTransaction(ZWaveChannelContext ctx, DataFrame finalFrame) {
        state = STATE_COMPLETE;
        ctx.fireEvent(new TransactionCompletedEvent(getId(), finalFrame));
    }

    private void failTransaction(ZWaveChannelContext ctx, boolean dueToCAN) {
        if (!attemptResend(ctx, dueToCAN)) {
            state = STATE_COMPLETE;
            logger.trace("Failing transaction {}", getId());
            ctx.fireEvent(new TransactionFailedEvent(getId(), getStartFrame()));
        }
    }

    protected boolean wasSendSuccessful(DataFrame dataFrame) {
        return true;
    }
}
