/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;

/**
 * Encapsulates Z-Wave node information.
 *
 * @author Dan Noguerol
 */
public class NodeInfo {
    private byte nodeId;
    private byte basicDeviceClass;
    private byte genericDeviceClass;
    private byte specificDeviceClass;
    private byte[] commandClasses;

    public NodeInfo(byte nodeId, byte basicDeviceClass, byte genericDeviceClass, byte specificDeviceClass) {
        this(nodeId, basicDeviceClass, genericDeviceClass, specificDeviceClass, null);
    }

    public NodeInfo(byte nodeId, byte basicDeviceClass, byte genericDeviceClass, byte specificDeviceClass, byte[] commandClasses) {
        this.nodeId = nodeId;
        this.basicDeviceClass = basicDeviceClass;
        this.genericDeviceClass = genericDeviceClass;
        this.specificDeviceClass = specificDeviceClass;
        this.commandClasses = commandClasses;
    }

    public NodeInfo(byte nodeId, ByteBuf buffer, int nodeInfoLength) {
        this.nodeId = nodeId;
        buffer.readByte();
        basicDeviceClass = buffer.readByte();
        genericDeviceClass = buffer.readByte();
        specificDeviceClass = buffer.readByte();
        commandClasses = buffer.readBytes(nodeInfoLength - 3).array();
    }

    public byte getNodeId() {
        return nodeId;
    }

    public byte getBasicDeviceClass() {
        return basicDeviceClass;
    }

    public byte getGenericDeviceClass() {
        return genericDeviceClass;
    }

    public byte getSpecificDeviceClass() {
        return specificDeviceClass;
    }

    public boolean hasCommandClass(byte cclass) {
        if (commandClasses != null) {
            for (int i = 0; i < commandClasses.length; i++) {
                if (commandClasses[i] == cclass) {
                    return true;
                }
            }
        }
        return false;
    }

    public byte[] getCommandClasses() {
        return commandClasses;
    }

    @Override
    public String toString() {
        return "NodeInfo [nodeId=" + nodeId + ", basicDeviceClass=" + basicDeviceClass + ", genericDeviceClass=" + genericDeviceClass
                + ", specificDeviceClass=" + specificDeviceClass + ", commandClasses=" + Arrays.toString(commandClasses) + "]";
    }

}
