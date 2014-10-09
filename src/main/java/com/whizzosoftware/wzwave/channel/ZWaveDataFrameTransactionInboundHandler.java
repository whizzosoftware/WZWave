package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.ApplicationUpdate;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZWaveDataFrameTransactionInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveDataFrameTransactionInboundHandler.class);

    private DataFrameTransaction currentDataFrameTransaction;
    private boolean processingTransactionCompletion = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Frame) {
            Frame frame = (Frame) msg;
            if (hasCurrentRequestTransaction()) {
                logger.debug("Received frame within transaction context: {}", frame);
                currentDataFrameTransaction.addFrame(frame, System.currentTimeMillis());
                if (currentDataFrameTransaction.isComplete()) {

                    // flag that we're in the process of completing a frame transaction so that any code that checks
                    // will know that the transaction isn't quite done yet
                    processingTransactionCompletion = true;

                    DataFrame finalFrame = currentDataFrameTransaction.getFinalFrame();
                    logger.debug("*** Data frame transaction completed with final frame: {}", finalFrame);
                    logger.debug("");

                    // if there's an ApplicationUpdate with no node ID (e.g. when there's a app update failure), attempt
                    // to set the node ID based on the request frame that triggered it
                    if (finalFrame instanceof ApplicationUpdate) {
                        ApplicationUpdate update = (ApplicationUpdate)finalFrame;
                        if (update.getNodeId() == null && currentDataFrameTransaction.getStartFrame() instanceof RequestNodeInfo) {
                            update.setNodeId(((RequestNodeInfo)currentDataFrameTransaction.getStartFrame()).getNodeId());
                        }
                    }

                    // clear the current transaction
                    currentDataFrameTransaction = null;

                    // pass the final frame down the pipeline
                    if (finalFrame != null) {
                        ctx.fireChannelRead(finalFrame);
                    }

                    // now the frame transaction is truly done
                    processingTransactionCompletion = false;

                    // alert the outbound pipeline that a frame transaction has been completed
                    ((ZWaveQueuedOutboundHandler)ctx.pipeline().get("writeQueue")).onDataFrameTransactionComplete();
                }
            } else {
                logger.debug("Received frame outside of transaction context: {}", frame);
                ctx.fireChannelRead(msg);
            }
        }
    }

    public void onDataFrameWrite(DataFrame frame) {
        logger.debug("Detected data frame write: {}", frame);
        if (!hasCurrentRequestTransaction()) {
            currentDataFrameTransaction = frame.createTransaction(System.currentTimeMillis());
            if (currentDataFrameTransaction != null) {
                logger.debug("*** Data frame transaction started for {}", frame);
            }
        } else {
            logger.warn("Wrote a data frame with a current transaction: {}", frame);
        }
    }

    public boolean hasCurrentRequestTransaction() {
        return (processingTransactionCompletion || (currentDataFrameTransaction != null && !currentDataFrameTransaction.isComplete()));
    }
}
