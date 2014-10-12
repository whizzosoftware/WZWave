package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorControlCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = (byte)0x33;

    public static final byte CAPABILITY_GET = 0x00;
    public static final byte CAPABILITY_REPORT = 0x01;
    public static final byte START_CAPABILITY_LEVEL_CHANGE = 0x02;
    public static final byte STATE_GET = 0x03;
    public static final byte STATE_REPORT = 0x04;
    public static final byte STATE_SET = 0x05;
    public static final byte STOP_STATE_CHANGE = 0x06;

    private Byte value1;
    private Byte value2;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_COLOR_CONTROL";
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == STATE_REPORT) {
            value1 = ccb[startIndex+2];
            value2 = ccb[startIndex+3];
            logger.debug("Received updated value: {} {}", ByteUtil.createString(value1), ByteUtil.createString(value2));
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        context.sendDataFrame(createGetv1(nodeId));
        return 1;
    }

    static public DataFrame createSetv1(byte nodeId, byte value) {
        return createSendDataFrame("COLOR_CONTROL_SET", nodeId, new byte[]{ColorControlCommandClass.ID, STATE_SET, value}, false);
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("COLOR_CONTROL_GET", nodeId, new byte[]{ColorControlCommandClass.ID, STATE_GET}, true);
    }
}
