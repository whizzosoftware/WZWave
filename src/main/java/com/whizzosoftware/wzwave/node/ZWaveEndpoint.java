/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.CommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract base class for Z-Wave nodes. This allows common handling of "top-level" nodes as well as
 * multi-channel endpoint nodes.
 *
 * @author Dan Noguerol
 */
abstract public class ZWaveEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private byte nodeId;
    private Byte genericDeviceClass;
    private Byte specificDeviceClass;
    private final Map<Byte,CommandClass> commandClassMap = new HashMap<Byte,CommandClass>();

    public ZWaveEndpoint(byte nodeId, Byte genericDeviceClass, Byte specificDeviceClass) {
        this.nodeId = nodeId;
        this.genericDeviceClass = genericDeviceClass;
        this.specificDeviceClass = specificDeviceClass;
    }

    public byte getNodeId() {
        return nodeId;
    }

    public Byte getGenericDeviceClass() {
        return genericDeviceClass;
    }

    public Byte getSpecificDeviceClass() {
        return specificDeviceClass;
    }

    public boolean hasCommandClass(byte commandClassId) {
        return commandClassMap.containsKey(commandClassId);
    }

    public Collection<CommandClass> getCommandClasses() {
        return commandClassMap.values();
    }

    public CommandClass getCommandClass(byte commandClassId) {
        return commandClassMap.get(commandClassId);
    }

    public void addCommandClass(byte commandClassId, CommandClass commandClass) {
        if (!commandClassMap.containsKey(commandClassId)) {
            logger.debug("Registering command class: {}", commandClass.getName());
            commandClassMap.put(commandClassId, commandClass);
        }
    }
}
