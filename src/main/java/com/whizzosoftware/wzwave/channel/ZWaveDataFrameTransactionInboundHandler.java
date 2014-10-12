package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.ApplicationUpdate;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZWaveDataFrameTransactionInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveDataFrameTransactionInboundHandler.class);

    private static final int MAX_SEND_COUNT = 2;

    private DataFrameTransaction currentDataFrameTransaction;
    private boolean processingTransactionCompletion = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Frame) {
            Frame frame = (Frame) msg;
            if (hasCurrentRequestTransaction()) {
                logger.trace("Received frame within transaction context: {}", frame);

                if (currentDataFrameTransaction.addFrame(frame)) {
                    if (currentDataFrameTransaction.isComplete()) {

                        // flag that we're in the process of completing a frame transaction so that any code that checks
                        // will know that the transaction isn't quite done yet
                        processingTransactionCompletion = true;

                        if (!currentDataFrameTransaction.hasError()) {
                            DataFrame finalFrame = currentDataFrameTransaction.getFinalFrame();
                            logger.trace("*** Data frame transaction completed with final frame: {}", finalFrame);
                            logger.trace("");

                            // if there's an ApplicationUpdate with no node ID (e.g. when there's a app update failure), attempt
                            // to set the node ID based on the request frame that triggered it
                            if (finalFrame instanceof ApplicationUpdate) {
                                ApplicationUpdate update = (ApplicationUpdate) finalFrame;
                                if ((update.getNodeId() == null || update.getNodeId() == 0) && currentDataFrameTransaction.getStartFrame() instanceof RequestNodeInfo) {
                                    update.setNodeId(((RequestNodeInfo) currentDataFrameTransaction.getStartFrame()).getNodeId());
                                }
                            }

                            // pass the final frame down the pipeline
                            if (finalFrame != null) {
                                ctx.fireChannelRead(finalFrame);
                            }

                        } else {
                            DataFrame startFrame = currentDataFrameTransaction.getStartFrame();
                            if (startFrame.getSendCount() < MAX_SEND_COUNT) {
                                logger.debug("Transaction has failed - resending initial request");
                                ctx.channel().writeAndFlush(startFrame);
                            } else {
                                logger.debug("Transaction has failed and has exceeded max resends - aborting");
                            }
                        }

                        // clear the current transaction
                        currentDataFrameTransaction = null;

                        // now the frame transaction is truly done
                        processingTransactionCompletion = false;

                        // alert the outbound pipeline that a frame transaction has been completed
                        ChannelPipeline pipeline = ctx.pipeline();
                        if (pipeline != null) {
                            ZWaveQueuedOutboundHandler writeHandler = (ZWaveQueuedOutboundHandler) ctx.pipeline().get("writeQueue");
                            if (writeHandler != null) {
                                writeHandler.onDataFrameTransactionComplete();
                            }
                        }
                    }
                } else {
                    logger.trace("Transaction didn't consume frame so passing it along");
                    ctx.fireChannelRead(msg);
                }
            } else {
                logger.trace("Received frame outside of transaction context: {}", frame);
                ctx.fireChannelRead(msg);
            }
        }
    }

    public void onDataFrameWrite(DataFrame frame) {
        logger.trace("Detected data frame write: {}", frame);
        if (!hasCurrentRequestTransaction()) {
            currentDataFrameTransaction = frame.createTransaction();
            if (currentDataFrameTransaction != null) {
                logger.trace("*** Data frame transaction started for {}", frame);
            }
        } else {
            logger.trace("Wrote a data frame with a current transaction: {}", frame);
        }
    }

    public boolean hasCurrentRequestTransaction() {
        return (processingTransactionCompletion || (currentDataFrameTransaction != null && !currentDataFrameTransaction.isComplete()));
    }
}
