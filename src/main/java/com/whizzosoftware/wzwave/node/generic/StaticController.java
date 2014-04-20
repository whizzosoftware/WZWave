/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.generic;

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
    public static final byte ID = 0x02;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public StaticController(byte nodeId, NodeProtocolInfo info) {
        super(nodeId, info);
    }

    @Override
    protected void refresh(boolean deferIfNotListening) {

    }
}
