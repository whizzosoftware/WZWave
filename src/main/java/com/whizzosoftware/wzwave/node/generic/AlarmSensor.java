/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.generic;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;

/**
 * An alarm sensor node.
 *
 * @author Dan Noguerol
 */
public class AlarmSensor extends ZWaveNode {
    public static final byte ID = (byte)0xA1;

    public AlarmSensor(ZWaveControllerContext context, byte nodeId, NodeProtocolInfo info, NodeListener listener) {
        super(context, nodeId, info, listener);

        addCommandClass(BasicCommandClass.ID, new BasicCommandClass());
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {
    }
}
