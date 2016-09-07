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
import com.whizzosoftware.wzwave.commandclass.MultilevelSwitchCommandClass;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.persist.PersistenceContext;

/**
 * A Multilevel Switch node.
 *
 * @author Dan Noguerol
 */
public class MultilevelSwitch extends ZWaveNode {
    public static final byte ID = 0x11;

    public MultilevelSwitch(NodeInfo info, boolean listening, NodeListener listener) {
        super(info, listening, listener);

        addCommandClass(BasicCommandClass.ID, new BasicCommandClass());
        addCommandClass(MultilevelSwitchCommandClass.ID, new MultilevelSwitchCommandClass());
    }

    public MultilevelSwitch(PersistenceContext pctx, Byte nodeId, NodeListener listener) {
        super(pctx, nodeId, listener);
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {
//        queueDataFrame(MultilevelSwitchCommandClass.createGetv1(getNodeId()));
    }

    public Byte getLevel() {
        MultilevelSwitchCommandClass cc = (MultilevelSwitchCommandClass)getCommandClass(MultilevelSwitchCommandClass.ID);
        if (cc != null) {
            return cc.getLevel();
        } else {
            return null;
        }
    }
}
