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
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        if (ccb[startIndex+1] == VERSION_REPORT) {
            int start = startIndex+2;
            library = String.format("%d", ccb[start]);
            protocol = String.format("%d.%2d", ccb[start + 1], ccb[start + 2]);
            application = String.format("%d.%2d", ccb[start + 3], ccb[start + 4]);
            logger.debug("Node {} uses library {}, protocol {} and application {}", ByteUtil.createString(context.getNodeId()), library, protocol, application);
        } else if (ccb[1] == VERSION_COMMAND_CLASS_REPORT) {
            CommandClass cc = context.getCommandClass(ccb[startIndex+2]);
            if (cc != null) {
                logger.debug(
                    "Setting command class {} to version {}",
                    cc.getName(),
                    ByteUtil.createString(ccb[startIndex+3])
                );
                cc.setVersion(ccb[startIndex+3]);
            } else {
                logger.error("Received version for unknown command class: {}", ByteUtil.createString(ccb[startIndex+2]));
            }
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        int count = 1;

        // get the device version
        context.sendDataFrame(createGet(nodeId));

        // check each command class the node has...
        for (CommandClass commandClass : context.getCommandClasses()) {
            // if this library supports more than one version of the command class, query for the version the
            // device supports
            if (commandClass.getMaxSupportedVersion() > 1) {
                context.sendDataFrame(createCommandClassGet(nodeId, commandClass.getId()));
                count++;
            }
        }

        return count;
    }

    public DataFrame createGet(byte nodeId) {
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

    public DataFrame createCommandClassGet(byte nodeId, byte commandClass) {
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
