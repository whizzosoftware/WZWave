/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.codec;

import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
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

    private ByteBuf previousBuf;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("RCVD: {}", ByteUtil.createString(in));
        }

        ByteBuf data;

        // if there was data left from the last decode call, create a ByteBuf that contains both
        // previous and new data
        if (previousBuf != null) {
            CompositeByteBuf cbuf = Unpooled.compositeBuffer();
            cbuf.addComponent(previousBuf);
            cbuf.addComponent(in);
            cbuf.writerIndex(previousBuf.readableBytes() + in.readableBytes());
            data = cbuf;
            previousBuf = null;
        } else {
            data = in;
        }

        while (data.isReadable()) {
            // check for single ACK/NAK/CAN
            if (data.readableBytes() == 1 && isSingleByteFrame(data, data.readerIndex())) {
                out.add(createSingleByteFrame(data));
            } else {
                boolean foundFrame = false;
                // search for a valid frame in the data
                for (int searchStartIx = data.readerIndex(); searchStartIx < data.readerIndex() + data.readableBytes(); searchStartIx++) {
                    if (data.getByte(searchStartIx) == DataFrame.START_OF_FRAME) {
                        int frameEndIx = scanForFrame(data, searchStartIx);
                        if (frameEndIx > 0) {
                            if (searchStartIx > data.readerIndex() && isSingleByteFrame(data, searchStartIx - 1)) {
                                data.readerIndex(searchStartIx - 1);
                                out.add(createSingleByteFrame(data));
                            } else {
                                data.readerIndex(searchStartIx);
                            }
                            out.add(createDataFrame(data));
                            data.readByte(); // discard checksum
                            foundFrame = true;
                        }
                    }
                }
                if (!foundFrame) {
                    break;
                }
            }
        }

        if (data.readableBytes() > 0) {
            previousBuf = data;
        }

        logger.trace("Done processing received data");
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
        } else if (b == CAN.ID) {
            return new CAN();
        } else {
            return null;
        }
    }

    private int scanForFrame(ByteBuf data, int startIndex) {
        int readableBytes = data.readableBytes();
        if (data.getByte(startIndex) == DataFrame.START_OF_FRAME && startIndex + 1 < data.readerIndex() + readableBytes) {
            byte frameLen = data.getByte(startIndex + 1);
            int checksumIx = startIndex + frameLen + 1;
            if (frameLen > 0 && checksumIx < data.readerIndex() + readableBytes) {
                byte frameChecksum = data.getByte(checksumIx);
                byte checksum = 0;
                for (int i = startIndex + 1; i < checksumIx; i++) {
                    checksum ^= data.getByte(i);
                }
                checksum = (byte)(~checksum);
                if (frameChecksum == checksum) {
                    return checksumIx;
                }
            }
        }
        return -1;
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
            }
        }
        return null;
    }
}
