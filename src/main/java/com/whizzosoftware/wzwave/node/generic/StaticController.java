/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.generic;

import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Static Controller node.
 *
 * @author Dan Noguerol
 */
public class StaticController extends ZWaveNode {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = 0x02;

    public StaticController(ZWaveControllerContext context, byte nodeId, NodeProtocolInfo info, NodeListener listener) {
        super(context, nodeId, info, listener);
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {
    }

    @Override
    protected boolean shouldSendRequestNodeInfo() {
        return false;
    }
}
