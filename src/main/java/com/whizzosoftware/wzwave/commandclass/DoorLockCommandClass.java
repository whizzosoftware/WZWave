/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoorLockCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = 0x62;

    public static final byte DOOR_LOCK_OPERATION_SET = 0x01;
    public static final byte DOOR_LOCK_OPERATION_GET = 0x02;
    public static final byte DOOR_LOCK_OPERATION_REPORT = 0x03;
    public static final byte DOOR_LOCK_CONFIGURATION_SET = 0x04;
    public static final byte DOOR_LOCK_CONFIGURATION_GET = 0x05;
    public static final byte DOOR_LOCK_CONFIGURATION_REPORT = 0x06;

    public static final byte MODE_UNSECURED = 0x00;
    public static final byte MODE_UNSECURED_WITH_TIMEOUT = 0x01;
    public static final byte MODE_UNSECURED_FOR_INSIDE_DOOR_HANDLES = 0x10;
    public static final byte MODE_UNSECURED_FOR_INSIDE_DOOR_HANDLES_WITH_TIMEOUT = 0x11;
    public static final byte MODE_UNSECURED_FOR_OUTSIDE_DOOR_HANDLES = 0x20;
    public static final byte MODE_UNSECURED_FOR_OUTSIDE_DOOR_HANDLES_WITH_TIMEOUT = 0x21;
    public static final byte MODE_SECURED = (byte)0xFF;

    private Byte doorLockMode;
    private Byte outsideDoorHandlesMode;
    private Byte insideDoorHandlesMode;
    private Byte doorCondition;
    private Byte lockTimeoutMinutes;
    private Byte lockTimeoutSeconds;

    public DoorLockCommandClass() {
        this(false);
    }

    public DoorLockCommandClass(boolean secure) {
        super();
        setSecure(secure);
    }

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_DOOR_LOCK";
    }

    public Byte getDoorLockMode() {
        return doorLockMode;
    }

    public Byte getOutsideDoorHandlesMode() {
        return outsideDoorHandlesMode;
    }

    public Byte getInsideDoorHandlesMode() {
        return insideDoorHandlesMode;
    }

    public Byte getDoorCondition() {
        return doorCondition;
    }

    public Byte getLockTimeoutMinutes() {
        return lockTimeoutMinutes;
    }

    public Byte getLockTimeoutSeconds() {
        return lockTimeoutSeconds;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == DOOR_LOCK_OPERATION_REPORT) {
            doorLockMode = ccb[startIndex+2];
            outsideDoorHandlesMode = ccb[startIndex+3];
            insideDoorHandlesMode = ccb[startIndex+3];
            doorCondition = ccb[startIndex+4];
            lockTimeoutMinutes = ccb[startIndex+5];
            lockTimeoutSeconds = ccb[startIndex+6];
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        context.sendDataFrame(createGetv1(nodeId));
        return 1;
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("DOOR_LOCK_OPERATION_GET", nodeId, new byte[]{DoorLockCommandClass.ID, DOOR_LOCK_OPERATION_GET}, true);
    }

    static public DataFrame createSetv1(byte nodeId, byte mode) {
        return createSendDataFrame("DOOR_LOCK_OPERATION_SET", nodeId, new byte[]{DoorLockCommandClass.ID, DOOR_LOCK_OPERATION_SET, mode}, true);
    }
}
