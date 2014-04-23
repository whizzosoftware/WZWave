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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multilevel Sensor Command Class
 *
 * @author Dan Noguerol
 */
public class MultilevelSensorCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte SENSOR_MULTILEVEL_SUPPORTED_GET = 0x01;
    private static final byte SENSOR_MULTILEVEL_SUPPORTED_REPORT = 0x02;
    private static final byte SENSOR_MULTILEVEL_GET = 0x04;
    private static final byte SENSOR_MULTILEVEL_REPORT = 0x05;

    public static final byte ID = 0x31;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_SENSOR_MULTILEVEL";
    }

    @Override
    public void onApplicationCommand(byte[] ccb, int startIndex, NodeContext context) {
        logger.debug("onDataFrame()");
    }

    @Override
    public void queueStartupMessages(byte nodeId, NodeContext context) {
        context.queueDataFrame(createGetv1(nodeId));
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("SENSOR_MULTILEVEL_GET", nodeId, new byte[]{MultilevelSensorCommandClass.ID, SENSOR_MULTILEVEL_GET}, true);
    }
}
