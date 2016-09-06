/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.node.specific;

import com.whizzosoftware.wzwave.commandclass.AlarmSensorCommandClass;
import com.whizzosoftware.wzwave.commandclass.BatteryCommandClass;
import com.whizzosoftware.wzwave.commandclass.ManufacturerSpecificCommandClass;
import com.whizzosoftware.wzwave.commandclass.VersionCommandClass;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.generic.AlarmSensor;

public class RoutingSmokeSensor extends AlarmSensor {
    static public final byte ID = 0x07;

    public RoutingSmokeSensor(NodeInfo info, boolean newlyIncluded, boolean listening, NodeListener listener) {
        super(info, listening, newlyIncluded, listener);

        addCommandClass(AlarmSensor.ID, new AlarmSensorCommandClass());
        addCommandClass(ManufacturerSpecificCommandClass.ID, new ManufacturerSpecificCommandClass());
        addCommandClass(VersionCommandClass.ID, new VersionCommandClass());
        addCommandClass(BatteryCommandClass.ID, new BatteryCommandClass());
    }
}
