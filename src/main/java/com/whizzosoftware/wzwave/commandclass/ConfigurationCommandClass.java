/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Configuration Command Class
 *
 * @author Linus Brimstedt
 */
public class ConfigurationCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte CONFIGURATION_SET = 0x04;
    public static final byte CONFIGURATION_GET = 0x05;
    public static final byte CONFIGURATION_REPORT = 0x06;

    public static final byte ID = 0x70;

    private HashMap<Byte, Long> retrievedValues = new HashMap<>();

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_CONFIGURATION";
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex + 1] == CONFIGURATION_REPORT) {
            byte param = ccb[startIndex + 2];
            int size = ccb[startIndex + 3] & 0x7;
            long value = 0;
            switch (size) {
                case 3:
                    value = ccb[startIndex + 7];
                    value <<= 8;
                    value = ccb[startIndex + 6];
                    value <<= 8;
                case 2:
                    value = ccb[startIndex + 5];
                    value <<= 8;
                case 1:
                    value = ccb[startIndex + 4];

            }
            logger.debug("Parameter {} had the value {}", param, value);
            retrievedValues.put(param, value);
            return;
        }
        logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex + 1]));
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        return 0;
    }

    public DataFrame createGet(byte nodeId, byte parameter) {
        retrievedValues.put(parameter, null);
        return createSendDataFrame("CONFIGURATION_GET", nodeId, new byte[]{ConfigurationCommandClass.ID, CONFIGURATION_GET, parameter}, true);
    }

    public Long getLastParameterValue(byte parameter) {
        return retrievedValues.get(parameter);

    }

    public DataFrame createSetParamToValue(byte nodeId, byte parameter, byte value) {
        return createSendDataFrame("CONFIGURATION_SET", nodeId, new byte[]{ConfigurationCommandClass.ID, CONFIGURATION_SET, parameter, 1, value}, false);
    }

    public DataFrame createSetParamToValue(byte nodeId, byte parameter, short value) {
        ByteBuffer b = ByteBuffer.allocate(2);
        b.putShort(value);

        return createSendDataFrame("CONFIGURATION_SET", nodeId, new byte[]{ConfigurationCommandClass.ID, CONFIGURATION_SET, parameter, 2, b.array()[0], b.array()[1]}, false);
    }

    public DataFrame createSetParamToValue(byte nodeId, byte parameter, int value) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(value);

        return createSendDataFrame("CONFIGURATION_SET", nodeId, new byte[]{ConfigurationCommandClass.ID, CONFIGURATION_SET, parameter, 3, b.array()[0], b.array()[1], b.array()[2], b.array()[3]}, false);
    }

    @Override
    public String toString() {
        return "ConfigurationCommandClass{" +
                "version=" + getVersion() +
                '}';
    }
}
