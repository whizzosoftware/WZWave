package com.whizzosoftware.wzwave.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.oio.AbstractOioByteChannel;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class MockChannel extends AbstractOioByteChannel {
    private List<byte[]> responseQueue = new ArrayList<>();
    private List<byte[]> writeList = new ArrayList<>();
    private long lastResponse;

    public MockChannel() {
        super((Channel)null);
    }

    public void queueResponse(byte[] bytes) {
        responseQueue.add(bytes);
    }

    @Override
    protected int available() {
        long now = System.currentTimeMillis();
        if (responseQueue.size() > 0 && now - lastResponse > 100) {
            return responseQueue.get(0).length;
        } else {
            return 0;
        }
    }

    @Override
    protected int doReadBytes(ByteBuf byteBuf) throws Exception {
        long now = System.currentTimeMillis();
        if (responseQueue.size() > 0) {
            byte[] bytes = responseQueue.get(0);
            responseQueue.remove(0);
            byteBuf.writeBytes(bytes);
            lastResponse = now;
            return bytes.length;
        } else {
            return 0;
        }
    }

    @Override
    protected void doWriteBytes(ByteBuf byteBuf) throws Exception {
        ByteBuf buf = byteBuf.readBytes(byteBuf.readableBytes());
        if (buf.isReadable()) {
            writeList.add(buf.array());
        }
    }

    @Override
    protected void doWriteFileRegion(FileRegion fileRegion) throws Exception {

    }

    @Override
    protected void doConnect(SocketAddress socketAddress, SocketAddress socketAddress1) throws Exception {

    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doBind(SocketAddress socketAddress) throws Exception {

    }

    @Override
    protected void doDisconnect() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {

    }

    @Override
    public ChannelConfig config() {
        return new DefaultChannelConfig(this);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
