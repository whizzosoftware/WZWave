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
import com.whizzosoftware.wzwave.channel.event.TransactionFailedEvent;
import com.whizzosoftware.wzwave.channel.event.TransactionStartedEvent;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.OutboundDataFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Abstract base class for all DataFrameTransaction implementations.
 *
 * @author Dan Noguerol
 */
abstract class AbstractDataFrameTransaction implements DataFrameTransaction {
    private static final int MAX_SEND_COUNT = 2;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String id = UUID.randomUUID().toString();
    private DataFrame startFrame;
    private boolean listeningNode;

    AbstractDataFrameTransaction(ZWaveChannelContext ctx, DataFrame startFrame, boolean listeningNode) {
        this.startFrame = startFrame;
        this.listeningNode = listeningNode;
        ctx.fireEvent(new TransactionStartedEvent(getId()));
    }

    public String getId() {
        return id;
    }

    public long getTimeout() {
        return 2000;
    }

    public DataFrame getStartFrame() {
        return startFrame;
    }

    public void timeout(ZWaveChannelContext ctx) {
        attemptResend(ctx, false);
    }

    @Override
    public boolean isListeningNode() {
        return listeningNode;
    }

    /**
     * Attempts to re-send the data frame that initiated this transaction.
     *
     * @param ctx the ChannelHandlerContext
     * @param dueToCAN indicates whether the re-send attempt was due to a CAN frame that was received
     *
     * @return boolean indicating whether re-send was attempted
     */
    boolean attemptResend(ZWaveChannelContext ctx, boolean dueToCAN) {
        if (startFrame.getSendCount() < MAX_SEND_COUNT) {
            logger.debug("Transaction {} has failed - will reset and resend initial request", getId());
            reset();
            // if a CAN was received, then we decrement the send count by one so this attempt doesn't count towards the maximum resend count
            if (dueToCAN) {
                startFrame.decrementSendCount();
            }
            ctx.writeFrame(new OutboundDataFrame(startFrame, isListeningNode()));
            return true;
        } else {
            logger.debug("Exceeded max transaction resends");
            ctx.fireEvent(new TransactionFailedEvent(getId(), startFrame));
            return false;
        }
    }

}
