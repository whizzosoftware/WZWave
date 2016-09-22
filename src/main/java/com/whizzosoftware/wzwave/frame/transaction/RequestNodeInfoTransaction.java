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
import com.whizzosoftware.wzwave.frame.ApplicationUpdate;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;

/**
 * DataFrameTransaction implementation for RequestNodeInfo exchanges.
 *
 * @author Dan Noguerol
 */
public class RequestNodeInfoTransaction extends RequestResponseCallbackTransaction {
    public RequestNodeInfoTransaction(ZWaveChannelContext ctx, DataFrame startFrame, boolean listeningNode) {
        super(ctx, startFrame, listeningNode);
    }

    @Override
    protected void completeTransaction(ZWaveChannelContext ctx, DataFrame finalFrame) {
        setState(STATE_COMPLETE);

        // if there's an ApplicationUpdate with no node ID (e.g. when there's a app update failure), attempt
        // to set the node ID based on the request frame that triggered it
        if (finalFrame instanceof ApplicationUpdate) {
            ApplicationUpdate update = (ApplicationUpdate)finalFrame;
            if ((update.getNodeId() == null || update.getNodeId() == 0) && getStartFrame() instanceof RequestNodeInfo) {
                update.setNodeId(((RequestNodeInfo)getStartFrame()).getNodeId());
            }
        }

        ctx.fireEvent(new TransactionCompletedEvent(getId(), finalFrame));
    }

    protected boolean wasSendSuccessful(DataFrame dataFrame) {
        if (dataFrame instanceof RequestNodeInfo) {
            return ((RequestNodeInfo)dataFrame).wasSuccessfullySent();
        } else {
            return false;
        }
    }
}
