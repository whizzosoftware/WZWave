/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node.specific;

import com.whizzosoftware.wzwave.commandclass.AlarmSensorCommandClass;
import com.whizzosoftware.wzwave.commandclass.ManufacturerSpecificCommandClass;
import com.whizzosoftware.wzwave.commandclass.VersionCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.NodeProtocolInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.generic.AlarmSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicZensorNetAlarmSensor extends AlarmSensor {
    static public final byte ID = 0x03;

    private static final Logger logger = LoggerFactory.getLogger(RoutingBinarySensor.class);

    public BasicZensorNetAlarmSensor(ZWaveControllerContext context, byte nodeId, NodeProtocolInfo info, NodeListener listener) {
        super(context, nodeId, info, listener);

        addCommandClass(AlarmSensor.ID, new AlarmSensorCommandClass());
        addCommandClass(ManufacturerSpecificCommandClass.ID, new ManufacturerSpecificCommandClass());
        addCommandClass(VersionCommandClass.ID, new VersionCommandClass());
    }
}
