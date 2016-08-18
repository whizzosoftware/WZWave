/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.generic;

import com.whizzosoftware.wzwave.commandclass.BasicCommandClass;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Binary Switch node.
 *
 * @author Dan Noguerol
 */
public class BinarySwitch extends ZWaveNode {
    public static final byte ID = 0x10;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BinarySwitch(ZWaveControllerContext context, NodeInfo info, boolean newlyIncluded, boolean listening, NodeListener listener) {
        super(context, info, newlyIncluded, listening, listener);

        addCommandClass(BasicCommandClass.ID, new BasicCommandClass());
        addCommandClass(BinarySwitchCommandClass.ID, new BinarySwitchCommandClass());
    }

    protected CommandClass performBasicCommandClassMapping(BasicCommandClass cc) {
        // Basic commands should get mapped to BinarySwitch commands
        return getCommandClass(BinarySwitchCommandClass.ID);
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {
        // TODO
//        sendDataFrame(BinarySwitchCommandClass.createGetv1(getNodeId()), deferIfNotListening);
    }

    static public Boolean isOn(ZWaveEndpoint endpoint) {
        BinarySwitchCommandClass cc = (BinarySwitchCommandClass)endpoint.getCommandClass(BinarySwitchCommandClass.ID);
        if (cc != null) {
            return cc.isOn();
        } else {
            return null;
        }
    }
}
