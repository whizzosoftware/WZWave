/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.channel.event.TransactionTimeoutEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the situation where a data frame transaction times out. It will alert its handler so follow-on
 * action can be taken.
 *
 * @author Dan Noguerol
 */
public class TransactionTimeoutHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TransactionTimeoutHandler.class);

    private String id;
    private ChannelHandlerContext context;
    private ChannelInboundHandler handler;

    public TransactionTimeoutHandler(String id, ChannelHandlerContext context, ChannelInboundHandler handler) {
        this.id = id;
        this.context = context;
        this.handler = handler;
    }

    public String getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            handler.userEventTriggered(context, new TransactionTimeoutEvent(id));
        } catch (Exception e) {
            logger.error("Error timing out transaction", e);
        }
    }
}
