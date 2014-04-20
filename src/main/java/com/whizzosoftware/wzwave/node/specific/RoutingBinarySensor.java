/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.specific;

import com.whizzosoftware.wzwave.commandclass.WakeUpCommandClass;
import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Routing Binary Sensor node.
 *
 * @author Dan Noguerol
 */
public class RoutingBinarySensor extends BinarySensor {
    static public final byte ID = 0x01;

    private static final Logger logger = LoggerFactory.getLogger(RoutingBinarySensor.class);

    public RoutingBinarySensor(byte nodeId, NodeProtocolInfo info) {
        super(nodeId, info);

        addCommandClass(WakeUpCommandClass.ID, new WakeUpCommandClass());
    }

    public Byte getBatteryLevel() {
        return null;
    }

    public Boolean isBatteryLow() {
        return false;
    }
}
