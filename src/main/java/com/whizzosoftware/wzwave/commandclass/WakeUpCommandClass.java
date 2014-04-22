/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.ApplicationCommand;
import com.whizzosoftware.wzwave.node.NodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wake Up Command Class
 *
 * @author Dan Noguerol
 */
public class WakeUpCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte WAKE_UP_INTERVAL_SET = 0x04;
    private static final byte WAKE_UP_INTERVAL_GET = 0x05;
    private static final byte WAKE_UP_INTERVAL_REPORT = 0x06;
    private static final byte WAKE_UP_NOTIFICATION = 0x07;
    private static final byte WAKE_UP_NO_MORE_INFORMATION = 0x08;

    public static final byte ID = (byte)0x84;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_WAKE_UP";
    }

    @Override
    public void onDataFrame(DataFrame m, NodeContext context) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();
            if (ccb[1] == WAKE_UP_NOTIFICATION) {
                logger.debug("Received wake up notification; flushing wakeup command queue");
                context.flushWakeupQueue();
            } else {
                logger.warn("Ignoring unsupported message: " + m);
            }
        } else {
            logger.error("Received unexpected message: " + m);
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, NodeContext context) {
    }
}
