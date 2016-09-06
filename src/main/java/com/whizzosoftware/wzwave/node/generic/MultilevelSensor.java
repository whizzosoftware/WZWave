/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.node.generic;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.commandclass.MultilevelSensorCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;

/**
 * A Multilevel Sensor node.
 *
 * @author Dan Noguerol
 */
public class MultilevelSensor extends ZWaveNode {
    public static final byte ID = 0x21;

    public MultilevelSensor(ZWaveControllerContext context, NodeInfo info, boolean newlyIncluded, boolean listening, NodeListener listener) {
        super(context, info, newlyIncluded, listening, listener);

        addCommandClass(BasicCommandClass.ID, new BasicCommandClass());
        addCommandClass(MultilevelSensorCommandClass.ID, new MultilevelSensorCommandClass());
    }

    protected CommandClass performBasicCommandClassMapping(BasicCommandClass cc) {
        // Basic commands should get mapped to MultilevelSensor commands
        return getCommandClass(MultilevelSensorCommandClass.ID);
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {
    }
}
