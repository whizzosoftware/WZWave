/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import io.netty.buffer.ByteBuf;

/**
 * Encapsulates Z-Wave node information.
 *
 * @author Dan Noguerol
 */
public class NodeInfo {
    private byte basicDeviceClass;
    private byte genericDeviceClass;
    private byte specificDeviceClass;
    private byte[] commandClasses;

    public NodeInfo(byte basicDeviceClass, byte genericDeviceClass, byte specificDeviceClass, byte[] commandClasses) {
        this.basicDeviceClass = basicDeviceClass;
        this.genericDeviceClass = genericDeviceClass;
        this.specificDeviceClass = specificDeviceClass;
        this.commandClasses = commandClasses;
    }

    public NodeInfo(ByteBuf buffer, int nodeInfoLength) {
        buffer.readByte();
        basicDeviceClass = buffer.readByte();
        genericDeviceClass = buffer.readByte();
        specificDeviceClass = buffer.readByte();
        commandClasses = buffer.readBytes(nodeInfoLength - 3).array();
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

    public byte[] getCommandClasses() {
        return commandClasses;
    }
}
