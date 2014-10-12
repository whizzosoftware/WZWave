package com.whizzosoftware.wzwave.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTimeout implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TransactionTimeout.class);

    private String id;
    private ChannelHandlerContext context;
    private ChannelInboundHandler handler;

    public TransactionTimeout(String id, ChannelHandlerContext context, ChannelInboundHandler handler) {
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
            handler.channelRead(context, this);
        } catch (Exception e) {
            logger.error("Error timing out transaction", e);
        }
    }
}
