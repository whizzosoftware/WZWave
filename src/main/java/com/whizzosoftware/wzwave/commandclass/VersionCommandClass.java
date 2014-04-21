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
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version Command Class
 *
 * @author Dan Noguerol
 */
public class VersionCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = (byte)0x86;

    private static final byte VERSION_GET = 0x11;
    private static final byte VERSION_REPORT = 0x12;
    private static final byte VERSION_COMMAND_CLASS_GET = 0x13;
    private static final byte VERSION_COMMAND_CLASS_REPORT = 0x14;

    private String library;
    private String protocol;
    private String application;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_VERSION";
    }

    public String getLibrary() {
        return library;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getApplication() {
        return application;
    }

    @Override
    public void onDataFrame(DataFrame m, DataQueue queue) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();
            if (ccb[1] == VERSION_REPORT) {
                int startIndex = 2;
                library = String.format("%d", ccb[startIndex]);
                protocol = String.format("%d.%2d", ccb[startIndex + 1], ccb[startIndex + 2]);
                application = String.format("%d.%2d", ccb[startIndex + 3], ccb[startIndex + 4]);
            } else if (ccb[1] == VERSION_COMMAND_CLASS_REPORT) {
                CommandClass cc = queue.getCommandClass(ccb[2]);
                if (cc != null) {
                    logger.debug(
                        "Setting command class {} to version {}",
                        ByteUtil.createString(ccb[2]),
                        ByteUtil.createString(ccb[3])
                    );
                    cc.setVersion(ccb[3]);
                } else {
                    logger.error("Recieved version for unknown command class: {}", ByteUtil.createString(ccb[2]));
                }
            } else {
                logger.warn("Ignoring unsupported message: " + m);
            }
        } else {
            logger.error("Received unexpected message: " + m);
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, DataQueue queue) {
        for (CommandClass commandClass : queue.getCommandClasses()) {
            if (commandClass.getMaxSupportedVersion() > 1) {
                queue.queueDataFrame(createCommandClassGet(nodeId, commandClass.getId()));
            }
        }
    }

    static public DataFrame createGet(byte nodeId) {
        return createSendDataFrame(
            "VERSION_GET",
            nodeId,
            new byte[]{
                VersionCommandClass.ID,
                VERSION_GET
            },
            true
        );
    }

    static public DataFrame createCommandClassGet(byte nodeId, byte commandClass) {
        return createSendDataFrame(
            "VERSION_COMMAND_CLASS_GET",
            nodeId,
            new byte[] {
                VersionCommandClass.ID,
                VERSION_COMMAND_CLASS_GET,
                commandClass
            },
            true
        );
    }
}
