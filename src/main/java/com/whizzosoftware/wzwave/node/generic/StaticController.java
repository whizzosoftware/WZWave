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

import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.persist.PersistenceContext;

/**
 * A Static Controller node.
 *
 * @author Dan Noguerol
 */
public class StaticController extends ZWaveNode {
    public static final byte ID = 0x02;

    public StaticController(NodeInfo info, NodeListener listener) {
        super(info, true, listener);
    }

    public StaticController(PersistenceContext pctx, Byte nodeId, NodeListener listener) {
        super(pctx, nodeId, listener);
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {
    }

    @Override
    protected boolean shouldRequestNodeInfo() {
        return false;
    }

    @Override
    protected boolean shouldRequestState() {
        return false;
    }
}
