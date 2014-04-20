/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.generic;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.BinarySensorCommandClass;
import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;

/**
 * A Binary Sensor node.
 *
 * @author Dan Noguerol
 */
public class BinarySensor extends ZWaveNode {
    public static final byte ID = 0x20;

    private Byte value = null;

    public BinarySensor(byte nodeId, NodeProtocolInfo info) {
        super(nodeId, info);

        addCommandClass(BasicCommandClass.ID, new BasicCommandClass());
        addCommandClass(BinarySensorCommandClass.ID, new BinarySensorCommandClass());
    }

    public CommandClass getCommandClass(byte commandClassId) {
        if (commandClassId == 0x20) {
            return getCommandClass(BinarySensorCommandClass.ID);
        } else {
            return super.getCommandClass(commandClassId);
        }
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {
        queueDataFrame(BinarySensorCommandClass.createGet(getNodeId()));
    }

    public Boolean isSensorIdle() {
        BinarySensorCommandClass cc = (BinarySensorCommandClass)getCommandClass(BinarySensorCommandClass.ID);
        if (cc != null) {
            return cc.isIdle();
        } else {
            return null;
        }
    }
}
