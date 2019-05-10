/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.codec;

import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class responsible for receiving a stream of bytes and converting them into one or more Z-Wave frames.
 *
 * @author Dan Noguerol
 */
public class ZWaveFrameDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveFrameDecoder.class);

    // Visible for testing
    @Override
    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        super.callDecode(ctx, in, out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (logger.isDebugEnabled()) {
            logger.debug("RCVD: {}", ByteUtil.createString(in));
        }

        if (isSingleByteFrame(in, in.readerIndex())) {
            out.add(createSingleByteFrame(in));
        }
        else if (isDataFrame(in, in.readerIndex()))
        {
            DataFrame dataFrame = tryCreateDataFrame(in);
            if (dataFrame != null) {
                out.add(dataFrame);
            }
        } else {
            in.readByte(); // discard invalid START OF FRAME
        }

        logger.trace("Done processing received data: {}", out);
    }

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.isReadable()) {
            decode(ctx, in, out);
        }
    }

    private boolean isSingleByteFrame(ByteBuf data, int ix) {
        byte b = data.getByte(ix);
        return (b == ACK.ID || b == NAK.ID || b == CAN.ID);
    }

    private Frame createSingleByteFrame(ByteBuf data) {
        byte b = data.readByte();
        if (b == ACK.ID) {
            return new ACK();
        } else if (b == NAK.ID) {
            return new NAK();
        } else {
            return new CAN();
        }
    }

    private boolean isDataFrame(ByteBuf in, int readerIndex) {
        return in.getByte(readerIndex) == DataFrame.START_OF_FRAME;
    }

    private DataFrame tryCreateDataFrame(ByteBuf in) {
        if (isFullDataFrame(in, in.readerIndex()))
        {
            int frameLength = peekLength(in, in.readerIndex());
            byte calculatedChecksum = calculateChecksum(in, in.readerIndex() + 1, in.readerIndex() + 1 + frameLength);
            byte frameChecksum = peekChecksum(in, in.readerIndex(), frameLength);
            if (calculatedChecksum != frameChecksum)
            {
                in.readBytes(frameLength + 2); // discard frame
                throw new CorruptedFrameException("Invalid frame checksum calc=" + ByteUtil.createString(calculatedChecksum) + " field=" + ByteUtil.createString(frameChecksum));
            }
            ByteBuf frameBuffer = in.readSlice(frameLength + 1);
            in.readByte(); // discard checksum
            return createDataFrame(frameBuffer);
        } else {
            return null;
        }
    }

    private boolean isFullDataFrame(ByteBuf in, int readerIndex) {
        if (in.readableBytes() >= 2) {
            int length = peekLength(in, readerIndex);
            return in.readableBytes() >= length + 2;
        }
        else
        {
            return false;
        }
    }

    private int peekLength(ByteBuf in, int readerIndex) {
        return in.getByte(readerIndex + 1);
    }

    private byte peekChecksum(ByteBuf in, int readerIndex, int frameLength) {
        return in.getByte(readerIndex + 1 + frameLength);
    }

    private byte calculateChecksum(ByteBuf data, int startIndex, int checksumIx) {
        byte checksum = 0;
        for (int i = startIndex; i < checksumIx; i++) {
            checksum ^= data.getByte(i);
        }
        checksum = (byte)(~checksum);
        return checksum;
    }

    /**
     * Creates a Z-Wave DataFrame from a ByteBuf.
     *
     * @param buf the buffer to process
     *
     * @return a DataFrame instance (or null if a valid one wasn't found)
     */
    private DataFrame createDataFrame(ByteBuf buf) {
        if (buf.readableBytes() > 3) {
            byte messageType = buf.getByte(buf.readerIndex() + 3);

            switch (messageType) {
                case Version.ID:
                    return new Version(buf);
                case MemoryGetId.ID:
                    return new MemoryGetId(buf);
                case InitData.ID:
                    return new InitData(buf);
                case NodeProtocolInfo.ID:
                    return new NodeProtocolInfo(buf);
                case SendData.ID:
                    return new SendData(buf);
                case ApplicationCommand.ID:
                    return new ApplicationCommand(buf);
                case ApplicationUpdate.ID:
                    return new ApplicationUpdate(buf);
                case RequestNodeInfo.ID:
                    return new RequestNodeInfo(buf);
                case GetRoutingInfo.ID:
                    return new GetRoutingInfo(buf);
                case GetSUCNodeId.ID:
                    return new GetSUCNodeId(buf);
                case AddNodeToNetwork.ID:
                    return new AddNodeToNetwork(buf);
                case RemoveNodeFromNetwork.ID:
                    return new RemoveNodeFromNetwork(buf);
                case SetDefault.ID:
                    return new SetDefault(buf);
            }
        }
        return null;
    }
}
