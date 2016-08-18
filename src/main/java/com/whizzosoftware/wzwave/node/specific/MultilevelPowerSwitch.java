/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.specific;

import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.generic.MultilevelSwitch;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;

/**
 * A Multilevel Power Switch node.
 *
 * @author Dan Noguerol
 */
public class MultilevelPowerSwitch extends MultilevelSwitch {
    static public final byte ID = 0x01;

    public MultilevelPowerSwitch(ZWaveControllerContext context, NodeInfo info, boolean newlyIncluded, boolean listening, NodeListener listener) {
        super(context, info, listening, newlyIncluded, listener);
    }
}
