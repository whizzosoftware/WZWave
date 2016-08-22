/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
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

    public static ZWaveNode createNode(ZWaveControllerContext context, NodeInfo info, boolean newlyIncluded, boolean listening, NodeListener listener) throws NodeCreationException {
        switch (info.getGenericDeviceClass()) {

            case AlarmSensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case BasicRoutingAlarmSensor.ID:
                        return new BasicRoutingAlarmSensor(context, info, newlyIncluded, listening, listener);
                    case RoutingAlarmSensor.ID:
                        return new RoutingAlarmSensor(context, info, newlyIncluded,  listening, listener);
                    case BasicZensorNetAlarmSensor.ID:
                        return new BasicZensorNetAlarmSensor(context, info, newlyIncluded,  listening, listener);
                    case ZensorNetAlarmSensor.ID:
                        return new ZensorNetAlarmSensor(context, info, newlyIncluded,  listening, listener);
                    case AdvancedZensorNetAlarmSensor.ID:
                        return new AdvancedZensorNetAlarmSensor(context, info, newlyIncluded,  listening, listener);
                    case BasicRoutingSmokeSensor.ID:
                        return new BasicRoutingSmokeSensor(context, info, newlyIncluded,  listening, listener);
                    case RoutingSmokeSensor.ID:
                        return new RoutingSmokeSensor(context, info, newlyIncluded,  listening, listener);
                    case BasicZensorNetSmokeSensor.ID:
                        return new BasicZensorNetSmokeSensor(context, info, newlyIncluded,  listening, listener);
                    case ZensorNetSmokeSensor.ID:
                        return new ZensorNetSmokeSensor(context, info, newlyIncluded,  listening, listener);
                    case AdvancedZensorNetSmokeSensor.ID:
                        return new AdvancedZensorNetSmokeSensor(context, info, newlyIncluded,  listening, listener);
                    default:
                        return new AlarmSensor(context, info, newlyIncluded,  listening, listener);
                }
            }

            case BinarySensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case RoutingBinarySensor.ID:
                        return new RoutingBinarySensor(context, info, newlyIncluded,  listening, listener);
                    default:
                        return new BinarySensor(context, info, newlyIncluded,  listening, listener);
                }
            }

            case BinarySwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case BinaryPowerSwitch.ID:
                        return new BinaryPowerSwitch(context, info, newlyIncluded, listening, listener);
                    default:
                        return new BinarySwitch(context, info, newlyIncluded,  listening, listener);
                }
            }

            case EntryControl.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case SecureKeypadDoorLock.ID:
                        return new SecureKeypadDoorLock(context, info, newlyIncluded, listening, listener);
                    default:
                        return new EntryControl(context, info, newlyIncluded, listening, false, listener);
                }
            }

            case Meter.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case SimpleMeter.ID:
                        return new SimpleMeter(context, info, newlyIncluded, listening, listener);
                    default:
                        return new Meter(context, info, newlyIncluded, listening, listener);
                }
            }

            case MultilevelSwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case MultilevelPowerSwitch.ID:
                        return new MultilevelPowerSwitch(context, info, newlyIncluded,  listening, listener);
                    default:
                        return new MultilevelSwitch(context, info, newlyIncluded,  listening, listener);
                }
            }

            case StaticController.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case PCController.ID:
                        return new PCController(context, info, listener);
                    default:
                        return new StaticController(context, info, listening, listener);
                }
            }

            default:
                throw new NodeCreationException("Unable to create node " + info.getNodeId() + " due to unknown generic device class: " + ByteUtil.createString(info.getGenericDeviceClass()));
        }
    }
}
