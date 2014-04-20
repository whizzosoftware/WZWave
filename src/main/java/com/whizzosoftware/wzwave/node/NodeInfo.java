/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

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

    public NodeInfo(byte[] data) {
        this(data, 0);
    }

    public NodeInfo(byte[] data, int start) {
        basicDeviceClass = data[start+1];
        genericDeviceClass = data[start+2];
        specificDeviceClass = data[start+3];
        commandClasses = new byte[data[start] - 3];
        System.arraycopy(data, start+4, commandClasses, 0, data[start] - 3);
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
