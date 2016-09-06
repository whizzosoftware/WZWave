/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.frame.DataFrame;

import java.util.*;

public class MockNodeContext implements NodeContext {
    private byte id;
    private Map<Byte,CommandClass> commandClassMap = new HashMap<>();
    private List<DataFrame> sentDataFrames = new ArrayList<>();

    public MockNodeContext(byte id, CommandClass[] classes) {
        this.id = id;
        if (classes != null) {
            for (CommandClass c : classes) {
                commandClassMap.put(c.getId(), c);
            }
        }
    }

    @Override
    public byte getNodeId() {
        return id;
    }

    @Override
    public void sendDataFrame(DataFrame d) {
        sentDataFrames.add(d);
    }

    public List<DataFrame> getSentDataFrames() {
        return sentDataFrames;
    }

    public void clearSentDataFrames() {
        sentDataFrames.clear();
    }

    @Override
    public void flushWakeupQueue() {

    }

    @Override
    public CommandClass getCommandClass(byte commandClassId) {
        return commandClassMap.get(commandClassId);
    }

    @Override
    public Collection<CommandClass> getCommandClasses() {
        return commandClassMap.values();
    }
}
