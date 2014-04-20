/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.specific;

import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;

/**
 * A Binary Power Switch node.
 *
 * @author Dan Noguerol
 */
public class BinaryPowerSwitch extends BinarySwitch {
    static public final byte ID = 0x01;

    public BinaryPowerSwitch(byte nodeId, NodeProtocolInfo info) {
        super(nodeId, info);
    }
}
