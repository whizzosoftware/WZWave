package com.whizzosoftware.wzwave;

import com.whizzosoftware.wzwave.channel.*;
import com.whizzosoftware.wzwave.codec.ZWaveFrameDecoder;
import com.whizzosoftware.wzwave.codec.ZWaveFrameEncoder;
import com.whizzosoftware.wzwave.frame.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class PipelineTest {
    MockChannel channel = null;

    @Test
    public void testPipeline() {
        final Object lock = new Object();
        final BlockingQueue queue = new LinkedBlockingQueue();
        TestInboundHandler handler = new TestInboundHandler(queue);
        final ZWaveChannelInboundHandler inboundHandler = new ZWaveChannelInboundHandler(handler);
        final ZWaveFrameDecoder decoder = new ZWaveFrameDecoder();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new OioEventLoopGroup());
        bootstrap.channel(MockChannel.class);
        bootstrap.handler(new ChannelInitializer<MockChannel>() {
            @Override
            protected void initChannel(MockChannel channel) throws Exception {
                PipelineTest.this.channel = channel;
                channel.pipeline().addLast("decoder", decoder);
                channel.pipeline().addLast("ack", new AcknowledgementInboundHandler());
                channel.pipeline().addLast("transaction", new ZWaveDataFrameTransactionInboundHandler());
                channel.pipeline().addLast("handler", inboundHandler);
                channel.pipeline().addLast("encoder", new ZWaveFrameEncoder());
                channel.pipeline().addLast("writeQueue", new ZWaveQueuedOutboundHandler());
            }
        });
        bootstrap.connect(new RxtxDeviceAddress("/dev/foo")).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        // wait for Netty to initialize everything
        synchronized (lock) {
            try {
                lock.wait(2000);
            } catch (InterruptedException ignored) {}
        }

        // write the startup frames
        channel.write(new Version()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channel.queueResponse(new byte[] {0x06, 0x01, 0x10, 0x01, 0x15, 0x5A, 0x2D, 0x57, 0x61, 0x76, 0x65, 0x20, 0x33, 0x2E, 0x39, 0x39, 0x00, 0x01, (byte)0x95});
            }
        });
        channel.write(new MemoryGetId()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channel.queueResponse(new byte[] {0x06});
                channel.queueResponse(new byte[] {0x01, 0x08, 0x01, 0x20, (byte)0xDD, 0x3A, 0x1D, (byte)0xD7, 0x01, (byte)0xFA});
            }
        });
        channel.write(new InitData()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channel.queueResponse(new byte[] {0x06});
                channel.queueResponse(new byte[] {0x01, 0x25, 0x01});
                channel.queueResponse(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1D, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
                channel.queueResponse(new byte[] {0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1D, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, (byte)0xC3});
            }
        });

        for (int i=0; i < 2; i++) {
            try {
                if (queue.poll(10000, TimeUnit.SECONDS) == null) {
                    fail("Should have received a response");
                }
                switch (i) {
                    case 0:
                        assertTrue(handler.getLibraryVersion().startsWith("Z-Wave 3.99"));
                        break;
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    class TestInboundHandler implements ZWaveChannelListener {
        private String libraryVersion;
        private int homeId;
        private byte nodeId;
        private Map<Byte,NodeProtocolInfo> nodeProtocolInfoMap = new HashMap<>();
        private List<SendData> sendDataList = new ArrayList<>();
        private List<ApplicationCommand> applicationCommandList = new ArrayList<>();
        private List<ApplicationUpdate> applicationUpdateList = new ArrayList<>();
        private final BlockingQueue queue;

        public TestInboundHandler(BlockingQueue queue) {
            this.queue = queue;
        }

        @Override
        public void onLibraryInfo(String libraryVersion) {
            this.libraryVersion = libraryVersion;
            notifyLock();
        }

        @Override
        public void onControllerInfo(int homeId, byte nodeId) {
            this.homeId = homeId;
            this.nodeId = nodeId;
            notifyLock();
        }

        @Override
        public void onNodeProtocolInfo(byte nodeId, NodeProtocolInfo nodeProtocolInfo) {
            nodeProtocolInfoMap.put(nodeId, nodeProtocolInfo);
            notifyLock();
        }

        @Override
        public void onSendData(SendData sendData) {
            sendDataList.add(sendData);
            notifyLock();
        }

        @Override
        public void onApplicationCommand(ApplicationCommand cmd) {
            applicationCommandList.add(cmd);
            notifyLock();
        }

        @Override
        public void onApplicationUpdate(ApplicationUpdate update) {
            applicationUpdateList.add(update);
            notifyLock();
        }

        @Override
        public void onAddNodeToNetwork(AddNodeToNetwork update) {

        }

        @Override
        public void onRemoveNodeFromNetwork(RemoveNodeFromNetwork removeNode) {

        }

        @Override
        public void onSetDefault() {

        }

        public void clearNodeProtocolInfoMap() {
            nodeProtocolInfoMap.clear();
        }

        public void clearSendDataList() {
            sendDataList.clear();
        }

        public void clearApplicationCommandList() {
            applicationCommandList.clear();
        }

        public void clearApplicationUpdateList() {
            applicationUpdateList.clear();
        }

        public String getLibraryVersion() {
            return libraryVersion;
        }

        public int getHomeId() {
            return homeId;
        }

        public byte getNodeId() {
            return nodeId;
        }

        public Map<Byte, NodeProtocolInfo> getNodeProtocolInfoMap() {
            return nodeProtocolInfoMap;
        }

        public List<SendData> getSendDataList() {
            return sendDataList;
        }

        public List<ApplicationCommand> getApplicationCommandList() {
            return applicationCommandList;
        }

        public List<ApplicationUpdate> getApplicationUpdateList() {
            return applicationUpdateList;
        }

        private void notifyLock() {
            try {
                queue.put(new Object());
            } catch (InterruptedException ignored) {}
        }
    }
}


