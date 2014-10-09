package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcknowledgementInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveChannelInboundHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof DataFrame) {
            logger.trace("Sending ACK for received data frame: {}", msg);
            ctx.channel().writeAndFlush(new ACK());
        }
        ctx.fireChannelRead(msg);
    }
}
