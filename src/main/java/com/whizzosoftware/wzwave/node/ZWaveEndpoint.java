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

import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.commandclass.CommandClassFactory;
import com.whizzosoftware.wzwave.persist.PersistenceContext;
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
    private final Map<Byte,CommandClass> commandClassMap = new HashMap<>();

    public ZWaveEndpoint(byte nodeId, Byte genericDeviceClass, Byte specificDeviceClass) {
        this.nodeId = nodeId;
        this.genericDeviceClass = genericDeviceClass;
        this.specificDeviceClass = specificDeviceClass;
    }

    public ZWaveEndpoint(PersistenceContext ctx, byte nodeId) {
        this.nodeId = nodeId;
        restore(ctx, nodeId);
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
            logger.trace("Registering command class: {}", commandClass.getName());
            commandClassMap.put(commandClassId, commandClass);
        }
    }

    public Map<String,Object> restore(PersistenceContext ctx, byte nodeId) {
        Map<String,Object> map = ctx.getNodeMap(nodeId);
        this.genericDeviceClass = (Byte)map.get("genericDeviceClass");
        this.specificDeviceClass = (Byte)map.get("specificDeviceClass");
        Object[] a = (Object[])map.get("commandClasses");
        if (a != null) {
            for (Object o : a) {
                byte b = (byte)o;
                CommandClass cc = CommandClassFactory.createCommandClass(b);
                cc.restore(ctx, nodeId);
                commandClassMap.put(cc.getId(), cc);
            }
        }
        return map;
    }

    public Map<String,Object> save(PersistenceContext ctx) {
        // save node info
        Map<String,Object> map = ctx.getNodeMap(getNodeId());
        map.put("clazz", getClass().getName());
        map.put("nodeId", Byte.toString(nodeId));
        if (genericDeviceClass != null) {
            map.put("genericDeviceClass", genericDeviceClass);
        }
        if (specificDeviceClass != null) {
            map.put("specificDeviceClass", specificDeviceClass);
        }

        map.put("commandClasses", commandClassMap.keySet().toArray());

        // save command class info
        for (CommandClass cc : commandClassMap.values()) {
            cc.save(ctx, getNodeId());
        }

        return map;
    }
}
