package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CentralSceneCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = 0x5B;
    public static final byte CENTRAL_SCENE_NOTIFICATION = 0x03;

    private Integer sequenceNumber;
    private Integer sceneNumber;
    private SceneCommand sceneCommand;
    private Integer pushCount;
    private Boolean slowRefresh;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_CENTRAL_SCENE";
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if(ccb[startIndex + 1] != CENTRAL_SCENE_NOTIFICATION) {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex + 1]));
            return;
        }
        switch(getVersion()) {
            case 3:
                slowRefresh = (ccb[startIndex + 3] & 0x80) == 0x80;
            case 1:
            case 2:
                sequenceNumber = (int) ccb[startIndex + 2];
                sceneNumber = (int) ccb[startIndex + 4];
                int attributes = ccb[3] & 0x07;
                switch(attributes) {
                    case 0x01:
                        sceneCommand = SceneCommand.RELEASED_AFTER_HOLD;
                        pushCount = null;
                        break;
                    case 0x02:
                        sceneCommand = SceneCommand.BEING_HELD;
                        pushCount = null;
                        break;
                    default:
                        sceneCommand = SceneCommand.PUSHED;
                        pushCount = attributes == 0x00 ? 1 : attributes - 1;
                        break;
                }
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        return 0;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public Integer getSceneNumber() {
        return sceneNumber;
    }

    public SceneCommand getSceneCommand() {
        return sceneCommand;
    }

    public Integer getPushCount() {
        return pushCount;
    }

    public Boolean getSlowRefresh() {
        return slowRefresh;
    }

    public enum SceneCommand {
        PUSHED, BEING_HELD, RELEASED_AFTER_HOLD
    }
}
