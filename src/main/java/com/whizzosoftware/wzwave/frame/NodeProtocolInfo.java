/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.RequestResponseTransaction;
import com.whizzosoftware.wzwave.util.ByteUtil;

/**
 * Retrieves the information frame for a specific node.
 *
 * @author Dan Noguerol
 */
public class NodeProtocolInfo extends DataFrame {
    public static final byte ID = 0x41;

    private byte nodeId;
    private boolean listening;
    private boolean beaming;
    private boolean routing;
    private int maxBaudRate;
    private int version;
    private boolean security;
    private byte basicDeviceClass;
    private byte genericDeviceClass;
    private byte specificDeviceClass;

    public NodeProtocolInfo(byte nodeId) {
        super(DataFrameType.REQUEST, ID, new byte[] { nodeId });
        this.nodeId = nodeId;
    }

    public NodeProtocolInfo(byte basicDeviceClass, byte genericDeviceClass, byte specificDeviceClass, boolean listening) {
        super(DataFrameType.REQUEST, ID, null);

        this.basicDeviceClass = basicDeviceClass;
        this.genericDeviceClass = genericDeviceClass;
        this.specificDeviceClass = specificDeviceClass;
        this.listening = listening;
    }

    public NodeProtocolInfo(byte[] bytes) {
        super(bytes);

        // capabilities
        listening = ((bytes[4] & 0x80 ) != 0);
        beaming = ((bytes[5] & 0x10) != 0);
        routing = ((bytes[4] & 0x40 ) != 0);

        maxBaudRate = 9600;
        if ((bytes[4] & 0x38) == 0x10) {
            maxBaudRate = 40000;
        }

        version = (bytes[4] & 0x07) + 1;
        security = ((bytes[5] & 0x01) != 0);

        // device classes
        basicDeviceClass = bytes[7];
        genericDeviceClass = bytes[8];
        specificDeviceClass = bytes[9];
    }

    public boolean isListening() {
        return listening;
    }

    public boolean isBeaming() {
        return beaming;
    }

    public boolean isRouting() {
        return routing;
    }

    public int getMaxBaudRate() {
        return maxBaudRate;
    }

    public int getVersion() {
        return version;
    }

    public boolean hasSecurity() {
        return security;
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

    public String toString() {
        StringBuilder sb = new StringBuilder("NodeProtocolInfo[").append(nodeId).append("]");
        if (getType() == DataFrameType.RESPONSE) {
            sb.append("(Basic=").append(ByteUtil.createString(basicDeviceClass)).append(",");
            sb.append("Generic=").append(ByteUtil.createString(genericDeviceClass)).append(",");
            sb.append("Specific=").append(ByteUtil.createString(specificDeviceClass)).append(")");
        } else {
            sb.append(".Request");
        }
        return sb.toString();
    }

    @Override
    public DataFrameTransaction createTransaction(long startTime) {
        return new RequestResponseTransaction(this, startTime);
    }
}
