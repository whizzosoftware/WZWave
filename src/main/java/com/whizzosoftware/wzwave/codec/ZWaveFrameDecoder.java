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
import io.netty.buffer.ByteBufUtil;
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

    private State state;
    private int currentDataFrameLength;
    private DataFrame currentDataFrame;
    private ByteBuf previousBuf;

    enum State {
        WAITING_FOR_DATA_FRAME_START,
        READ_DATA_FRAME_LENGTH,
        WAITING_FOR_DATA_FRAME_DATA,
        READ_DATA_FRAME_CHECKSUM
    }

    public ZWaveFrameDecoder() {
        this.state = State.WAITING_FOR_DATA_FRAME_START;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        logger.debug("RCVD {}", ByteUtil.createString(in));

        ByteBuf data ;
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

        while (data.readableBytes() > 0) {
            switch (state) {
                case WAITING_FOR_DATA_FRAME_START:
                    if (!lookForDataFrameStart(data, out)) {
                        return;
                    }
                    this.state = State.READ_DATA_FRAME_LENGTH;
                    // fall through
                case READ_DATA_FRAME_LENGTH:
                    if (data.readableBytes() < 1) {
                        return;
                    }
                    currentDataFrameLength = data.getByte(data.readerIndex() + 1);
                    this.state = State.WAITING_FOR_DATA_FRAME_DATA;
                    // fall through
                case WAITING_FOR_DATA_FRAME_DATA:
                    if (data.readableBytes() < currentDataFrameLength + 2) {
                        logger.trace("Received incomplete data; will wait for more");
                        if (data.readableBytes() > 0) {
                            logger.trace("Had extra bytes so saving for next round");
                            previousBuf = data;
                        }
                        return;
                    }
                    currentDataFrame = createDataFrame(data);
                    this.state = State.READ_DATA_FRAME_CHECKSUM;
                    // fall through
                case READ_DATA_FRAME_CHECKSUM:
                    if (data.readableBytes() < 1) {
                        return;
                    }
                    byte checksum = data.readByte();
                    // TODO: calculate and verify checksum
                    if (currentDataFrame != null) {
                        logger.debug("\"\"\"\" {}", currentDataFrame);
                        out.add(currentDataFrame);
                    }
                    resetDecoder();
            }
        }

        logger.trace("Done processing received data");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        resetDecoder();
    }

    protected void resetDecoder() {
        this.state = State.WAITING_FOR_DATA_FRAME_START;
        currentDataFrameLength = 0;
        currentDataFrame = null;
    }

    /**
     * Reads through the buffer until a data frame SOF is found. Process all ACK, NAK and CAN frames found along
     * the way.
     *
     * @param buffer the ByteBuf to process
     * @param out the output list
     *
     * @return true if an SOF was found before the buffer was exhausted
     */
    protected boolean lookForDataFrameStart(ByteBuf buffer, List<Object> out) {
        while (buffer.readableBytes() > 0) {
            byte b = buffer.readByte();
            if (b == ACK.ID) {
                out.add(new ACK());
            } else if (b == NAK.ID) {
                out.add(new NAK());
            } else if (b == CAN.ID) {
                out.add(new CAN());
            } else if (b == DataFrame.START_OF_FRAME) {
                buffer.readerIndex(buffer.readerIndex() - 1);
                return true;
            } else {
                logger.debug("Ignoring unexpected byte {}", ByteUtil.createString(b));
            }
        }
        return false;
    }

    /**
     * Creates a Z-Wave DataFrame from a ByteBuf.
     *
     * @param buf the buffer to process
     *
     * @return a DataFrame instance (or null if a valid one wasn't found)
     */
    protected DataFrame createDataFrame(ByteBuf buf) {
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
