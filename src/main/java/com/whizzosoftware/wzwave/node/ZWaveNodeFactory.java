/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import com.whizzosoftware.wzwave.node.generic.MultilevelSwitch;
import com.whizzosoftware.wzwave.node.generic.StaticController;
import com.whizzosoftware.wzwave.node.specific.BinaryPowerSwitch;
import com.whizzosoftware.wzwave.node.specific.MultilevelPowerSwitch;
import com.whizzosoftware.wzwave.node.specific.PCController;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;

/**
 * Factory for creating Z-Wave nodes from a NodeProtocolInfo instance.
 *
 * @author Dan Noguerol
 */
public class ZWaveNodeFactory {

    public static ZWaveNode createNode(byte nodeId, NodeProtocolInfo info, NodeListener listener) {
        switch (info.getGenericDeviceClass()) {

            case StaticController.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case PCController.ID:
                        return new PCController(nodeId, info, listener);
                    default:
                        return new StaticController(nodeId, info, listener);
                }
            }

            case BinarySensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case RoutingBinarySensor.ID:
                        return new RoutingBinarySensor(nodeId, info, listener);
                    default:
                        return new BinarySensor(nodeId, info, listener);
                }
            }

            case BinarySwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case BinaryPowerSwitch.ID:
                        return new BinaryPowerSwitch(nodeId, info, listener);
                    default:
                        return new BinarySwitch(nodeId, info, listener);
                }
            }

            case MultilevelSwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case MultilevelPowerSwitch.ID:
                        return new MultilevelPowerSwitch(nodeId, info, listener);
                    default:
                        return new MultilevelSwitch(nodeId, info, listener);
                }
            }

            default:
                return null;
        }
    }
}
