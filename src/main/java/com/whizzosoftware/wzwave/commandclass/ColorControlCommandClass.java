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

    public static final byte CAPABILITY_ID_WARM_WHITE = 0x00;
    public static final byte CAPABILITY_ID_COLD_WHITE = 0x01;
    public static final byte CAPABILITY_ID_RED = 0x02;
    public static final byte CAPABILITY_ID_GREEN = 0x03;
    public static final byte CAPABILITY_ID_BLUE = 0x04;

    private Byte capabilityId;
    private Byte value;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_COLOR_CONTROL";
    }

    public Byte getCapabilityId() {
        return capabilityId;
    }

    public Byte getValue() {
        return value;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == STATE_REPORT) {
            capabilityId = ccb[startIndex+2];
            value = ccb[startIndex+3];
            logger.debug("Received capability ID {} with value {}", ByteUtil.createString(capabilityId), ByteUtil.createString(value));
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        return 0;
    }

    public DataFrame createGet(byte nodeId, byte capabilityId) {
        return createSendDataFrame("COLOR_CONTROL_GET", nodeId, new byte[]{ColorControlCommandClass.ID, STATE_GET, capabilityId}, true);
    }

    public DataFrame createSet(byte nodeId, byte capabilityId, byte value) {
        return createSendDataFrame("COLOR_CONTROL_SET", nodeId, new byte[]{ColorControlCommandClass.ID, STATE_SET, 0x02, capabilityId, value}, false);
    }
}
