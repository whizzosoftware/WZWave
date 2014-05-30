/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import org.junit.Test;
import static org.junit.Assert.*;

public class CommandClassFactoryTest {
    @Test
    public void testCommandClassCreate() {
        assertTrue(CommandClassFactory.createCommandClass(BasicCommandClass.ID) instanceof BasicCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(BatteryCommandClass.ID) instanceof BatteryCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(BinarySensorCommandClass.ID) instanceof BinarySensorCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(BinarySwitchCommandClass.ID) instanceof BinarySwitchCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(ManufacturerSpecificCommandClass.ID) instanceof ManufacturerSpecificCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(MeterCommandClass.ID) instanceof MeterCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(MultiInstanceCommandClass.ID) instanceof MultiInstanceCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(MultilevelSensorCommandClass.ID) instanceof MultilevelSensorCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(MultilevelSwitchCommandClass.ID) instanceof MultilevelSwitchCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(VersionCommandClass.ID) instanceof VersionCommandClass);
        assertTrue(CommandClassFactory.createCommandClass(WakeUpCommandClass.ID) instanceof WakeUpCommandClass);
    }
}
