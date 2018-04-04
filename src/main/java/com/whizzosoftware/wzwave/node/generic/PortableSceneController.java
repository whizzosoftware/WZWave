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
import com.whizzosoftware.wzwave.commandclass.VersionCommandClass;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.persist.PersistenceContext;


public class PortableSceneController extends ZWaveNode {
    public static final byte ID = 0x01;

    private Byte value = null;

    public PortableSceneController(NodeInfo info, boolean listening, NodeListener listener) {
        super(info, listening, listener);

        addCommandClass(BasicCommandClass.ID, new BasicCommandClass());
        addCommandClass(VersionCommandClass.ID, new VersionCommandClass());
    }

    public PortableSceneController(PersistenceContext pctx, Byte nodeId, NodeListener listener) {
        super(pctx, nodeId, listener);
    }

    public Byte getValue() {
        return value;
    }

    public CommandClass getCommandClass(byte commandClassId) {
        return super.getCommandClass(commandClassId);
    }

    @Override
    protected void refresh(boolean deferIfNotListening) { }
}
