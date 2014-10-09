package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

public class ZWaveQueuedOutboundHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveQueuedOutboundHandler.class);

    private ChannelHandlerContext context;
    private Deque<DataFrame> pendingQueue = new LinkedList<DataFrame>();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        logger.debug("write: " + msg);
        if (msg instanceof DataFrame) {
            ZWaveDataFrameTransactionInboundHandler transactionHandler = (ZWaveDataFrameTransactionInboundHandler)ctx.pipeline().get("transaction");
            if (transactionHandler != null) {
                DataFrame frame = (DataFrame) msg;
                if (transactionHandler.hasCurrentRequestTransaction()) {
                    logger.debug("Queueing data frame: " + frame + "; current queue size: " + pendingQueue.size());
                    pendingQueue.add(frame);
                } else {
                    logger.debug("No transaction detected, sending data frame: {}", frame);
                    ctx.writeAndFlush(msg, promise);
                    transactionHandler.onDataFrameWrite(frame);
                }
            } else {
                logger.error("No transaction handler found; dropping data frame");
            }
        } else {
            ctx.writeAndFlush(msg, promise);
        }
    }

    public void onDataFrameTransactionComplete() {
        logger.trace("Detected data frame transaction completion");
        ZWaveDataFrameTransactionInboundHandler transactionHandler = (ZWaveDataFrameTransactionInboundHandler)context.pipeline().get("transaction");
        if (pendingQueue.size() > 0) {
            DataFrame frame = pendingQueue.pop();
            logger.debug("Sending next queued data frame: {}", frame);
            context.writeAndFlush(frame);
            transactionHandler.onDataFrameWrite(frame);
        } else {
            logger.debug("No pending data frames to send");
        }
    }
}
