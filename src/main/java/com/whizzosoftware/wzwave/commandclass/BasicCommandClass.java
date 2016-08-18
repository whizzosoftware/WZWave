/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
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

/**
 * Basic Command Class
 *
 * @author Dan Noguerol
 */
public class BasicCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte BASIC_SET = 0x01;
    public static final byte BASIC_GET = 0x02;
    public static final byte BASIC_REPORT = 0x03;

    public static final byte ID = (byte)0x20;

    private Byte value;

    public BasicCommandClass() {
        this(false);
    }

    public BasicCommandClass(boolean secure) {
        super();
        setSecure(secure);
    }

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
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == BASIC_REPORT || ccb[startIndex+1] == BASIC_SET) {
            value = ccb[startIndex+2];
            logger.debug("Received updated value: {}", ByteUtil.createString(value));
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
        return createSendDataFrame("BASIC_SET", nodeId, new byte[]{BasicCommandClass.ID, BASIC_SET, value}, false);
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("BASIC_GET", nodeId, new byte[]{BasicCommandClass.ID, BASIC_GET}, true);
    }
}
