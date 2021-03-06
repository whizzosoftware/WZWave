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
    private static final int STATE_COMPLETE = 3;

    private int state;

    public RequestResponseTransaction(ZWaveChannelContext ctx, DataFrame startFrame, boolean listeningNode) {
        super(ctx, startFrame, listeningNode);
        reset();
    }

    @Override
    public boolean addFrame(ZWaveChannelContext ctx, Frame f) {
        switch (state) {
            case STATE_REQUEST_SENT:
                if (f instanceof ACK) {
                    logger.trace("Received ACK as expected");
                    state = STATE_ACK_RECEIVED;
                    return true;
                } else if (f instanceof CAN) {
                    logger.trace("Received CAN; will attempt re-send");
                    failTransaction(ctx, true);
                    return true;
                } else {
                    logger.error("Received unexpected frame for STATE_REQUEST_SENT: " + f);
                }
                break;

            case STATE_ACK_RECEIVED:
                if (f instanceof CAN) {
                    logger.trace("Received CAN; will attempt re-send");
                    failTransaction(ctx, true);
                    return true;
                } else if (f instanceof DataFrame && f.getClass() == getStartFrame().getClass()) {
                    if (((DataFrame)f).getType() == DataFrameType.RESPONSE) {
                        logger.trace("Received expected message response");
                        completeTransaction(ctx, (DataFrame)f);
                        return true;
                    } else {
                        logger.trace("Expected frame received but does not appear to be a response: {}", f);
                        failTransaction(ctx, false);
                        return true;
                    }
                } else {
                    logger.trace("Received unexpected frame for STATE_ACK_RECEIVED: {}", f);
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
        this.state = STATE_REQUEST_SENT;
    }

    private void completeTransaction(ZWaveChannelContext ctx, DataFrame finalFrame) {
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
}
