package com.whizzosoftware.wzwave.codec;

import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for converting a Z-Wave frame into a stream of bytes.
 *
 * @author Dan Noguerol
 */
public class ZWaveFrameEncoder extends MessageToByteEncoder<Frame> {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveFrameEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Frame frame, ByteBuf byteBuf) throws Exception {
        byte[] bytes = frame.getBytes();
        logger.debug("Sending: " + ByteUtil.createString(bytes, bytes.length));
        byteBuf.writeBytes(bytes);
    }
}
