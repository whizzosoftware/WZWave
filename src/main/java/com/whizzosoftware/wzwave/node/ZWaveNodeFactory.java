/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.node.generic.*;
import com.whizzosoftware.wzwave.node.specific.*;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import com.whizzosoftware.wzwave.util.ByteUtil;

/**
 * Factory for creating Z-Wave nodes from a NodeProtocolInfo instance.
 *
 * @author Dan Noguerol
 */
public class ZWaveNodeFactory {

    public static ZWaveNode createNode(ZWaveControllerContext context, byte nodeId, NodeProtocolInfo info, NodeListener listener) throws NodeCreationException {
        switch (info.getGenericDeviceClass()) {

            case AlarmSensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case BasicRoutingAlarmSensor.ID:
                        return new BasicRoutingAlarmSensor(context, nodeId, info, listener);
                    case RoutingAlarmSensor.ID:
                        return new RoutingAlarmSensor(context, nodeId, info, listener);
                    case BasicZensorNetAlarmSensor.ID:
                        return new BasicZensorNetAlarmSensor(context, nodeId, info, listener);
                    case ZensorNetAlarmSensor.ID:
                        return new ZensorNetAlarmSensor(context, nodeId, info, listener);
                    case AdvancedZensorNetAlarmSensor.ID:
                        return new AdvancedZensorNetAlarmSensor(context, nodeId, info, listener);
                    case BasicRoutingSmokeSensor.ID:
                        return new BasicRoutingSmokeSensor(context, nodeId, info, listener);
                    case RoutingSmokeSensor.ID:
                        return new RoutingSmokeSensor(context, nodeId, info, listener);
                    case BasicZensorNetSmokeSensor.ID:
                        return new BasicZensorNetSmokeSensor(context, nodeId, info, listener);
                    case ZensorNetSmokeSensor.ID:
                        return new ZensorNetSmokeSensor(context, nodeId, info, listener);
                    case AdvancedZensorNetSmokeSensor.ID:
                        return new AdvancedZensorNetSmokeSensor(context, nodeId, info, listener);
                    default:
                        return new AlarmSensor(context, nodeId, info, listener);
                }
            }

            case BinarySensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case RoutingBinarySensor.ID:
                        return new RoutingBinarySensor(context, nodeId, info, listener);
                    default:
                        return new BinarySensor(context, nodeId, info, listener);
                }
            }

            case BinarySwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case BinaryPowerSwitch.ID:
                        return new BinaryPowerSwitch(context, nodeId, info, listener);
                    default:
                        return new BinarySwitch(context, nodeId, info, listener);
                }
            }

            case MultilevelSwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case MultilevelPowerSwitch.ID:
                        return new MultilevelPowerSwitch(context, nodeId, info, listener);
                    default:
                        return new MultilevelSwitch(context, nodeId, info, listener);
                }
            }

            case StaticController.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case PCController.ID:
                        return new PCController(context, nodeId, info, listener);
                    default:
                        return new StaticController(context, nodeId, info, listener);
                }
            }

            default:
                throw new NodeCreationException("Unable to create node " + nodeId + " due to unknown generic device class: " + ByteUtil.createString(info.getGenericDeviceClass()));
        }
    }
}
