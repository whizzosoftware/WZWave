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
import com.whizzosoftware.wzwave.persist.PersistenceContext;
import com.whizzosoftware.wzwave.util.ByteUtil;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating Z-Wave nodes from a NodeProtocolInfo instance.
 *
 * @author Dan Noguerol
 */
public class ZWaveNodeFactory {

    public static ZWaveNode createNode(NodeInfo info, boolean newlyIncluded, boolean listening, NodeListener listener) throws NodeCreationException {
        switch (info.getGenericDeviceClass()) {

            case AlarmSensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case BasicRoutingAlarmSensor.ID:
                        return new BasicRoutingAlarmSensor(info, newlyIncluded, listening, listener);
                    case RoutingAlarmSensor.ID:
                        return new RoutingAlarmSensor(info, newlyIncluded,  listening, listener);
                    case BasicZensorNetAlarmSensor.ID:
                        return new BasicZensorNetAlarmSensor(info, newlyIncluded,  listening, listener);
                    case ZensorNetAlarmSensor.ID:
                        return new ZensorNetAlarmSensor(info, newlyIncluded,  listening, listener);
                    case AdvancedZensorNetAlarmSensor.ID:
                        return new AdvancedZensorNetAlarmSensor(info, newlyIncluded,  listening, listener);
                    case BasicRoutingSmokeSensor.ID:
                        return new BasicRoutingSmokeSensor(info, newlyIncluded,  listening, listener);
                    case RoutingSmokeSensor.ID:
                        return new RoutingSmokeSensor(info, newlyIncluded,  listening, listener);
                    case BasicZensorNetSmokeSensor.ID:
                        return new BasicZensorNetSmokeSensor(info, newlyIncluded,  listening, listener);
                    case ZensorNetSmokeSensor.ID:
                        return new ZensorNetSmokeSensor(info, newlyIncluded,  listening, listener);
                    case AdvancedZensorNetSmokeSensor.ID:
                        return new AdvancedZensorNetSmokeSensor(info, newlyIncluded,  listening, listener);
                    default:
                        return new AlarmSensor(info, newlyIncluded,  listening, listener);
                }
            }

            case BinarySensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case RoutingBinarySensor.ID:
                        return new RoutingBinarySensor(info, newlyIncluded,  listening, listener);
                    default:
                        return new BinarySensor(info, newlyIncluded,  listening, listener);
                }
            }

            case BinarySwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case BinaryPowerSwitch.ID:
                        return new BinaryPowerSwitch(info, newlyIncluded, listening, listener);
                    default:
                        return new BinarySwitch(info, newlyIncluded,  listening, listener);
                }
            }

            case Meter.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case SimpleMeter.ID:
                        return new SimpleMeter(info, newlyIncluded, listening, listener);
                    default:
                        return new Meter(info, newlyIncluded, listening, listener);
                }
            }

            case MultilevelSensor.ID: {
                switch (info.getSpecificDeviceClass()) {
                    default:
                        return new MultilevelSensor(info, newlyIncluded, listening, listener);
                }
            }

            case MultilevelSwitch.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case MultilevelPowerSwitch.ID:
                        return new MultilevelPowerSwitch(info, newlyIncluded,  listening, listener);
                    default:
                        return new MultilevelSwitch(info, newlyIncluded,  listening, listener);
                }
            }

            case StaticController.ID: {
                switch (info.getSpecificDeviceClass()) {
                    case PCController.ID:
                        return new PCController(info, listener);
                    default:
                        return new StaticController(info, listening, listener);
                }
            }

            default:
                throw new NodeCreationException("Unable to create node " + info.getNodeId() + " due to unknown generic device class: " + ByteUtil.createString(info.getGenericDeviceClass()));
        }
    }

    public static ZWaveNode createNode(PersistenceContext pctx, byte nodeId, NodeListener listener) throws NodeCreationException {
        try {
            Map<String,Object> nodeMap = pctx.getNodeMap(nodeId);
            String clazz = (String)nodeMap.get("clazz");
            if (clazz != null) {
                Class c = ZWaveNodeFactory.class.getClassLoader().loadClass(clazz);
                Constructor cc = c.getConstructor(PersistenceContext.class, Byte.class, NodeListener.class);
                return (ZWaveNode)cc.newInstance(pctx, nodeId, listener);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new NodeCreationException("Unable to create node " + nodeId, e);
        }
    }
}
