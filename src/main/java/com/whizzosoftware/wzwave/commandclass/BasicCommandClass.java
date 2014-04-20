/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.ApplicationCommand;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Command Class
 *
 * @author Dan Noguerol
 */
public class BasicCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte BASIC_SET = 0x01;
    private static final byte BASIC_GET = 0x02;
    private static final byte BASIC_REPORT = 0x03;

    public static final byte ID = (byte)0x20;

    private Byte value;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_BASIC";
    }

    public Byte getValue() {
        return value;
    }

    @Override
    public void onDataFrame(DataFrame m, DataQueue queue) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();
            if (ccb[1] == BASIC_REPORT || ccb[1] == BASIC_SET) {
                value = ccb[2];
                logger.debug("Received updated value: " + ByteUtil.createString(value));
            } else {
                logger.warn("Ignoring unsupported message: " + m);
            }
        } else {
            logger.error("Received unexpected message: " + m);
        }

    }

    @Override
    public void queueStartupMessages(byte nodeId, DataQueue queue) {
        queue.queueDataFrame(createGet(nodeId));
    }

    static public DataFrame createSet(byte nodeId, byte value) {
        return createSendDataFrame("BASIC_SET", nodeId, new byte[]{BasicCommandClass.ID, BASIC_SET, value}, false);
    }

    static public DataFrame createGet(byte nodeId) {
        return createSendDataFrame("BASIC_GET", nodeId, new byte[]{BasicCommandClass.ID, BASIC_GET}, true);
    }
}
