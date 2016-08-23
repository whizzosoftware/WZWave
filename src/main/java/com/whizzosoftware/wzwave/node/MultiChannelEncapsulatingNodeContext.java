/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.frame.DataFrame;

import java.util.Collection;

/**
 * A NodeContext implementation that wrappers another NodeContext but encapsulates all SendData frames in a
 * multi-instance command before queueing them. This is needed so that command classes within endpoints can
 * send their data correctly.
 *
 * @author Dan Noguerol
 */
public class MultiChannelEncapsulatingNodeContext implements NodeContext {
    private MultiInstanceCommandClass micc;
    private byte destEndpoint;
    private NodeContext context;

    public MultiChannelEncapsulatingNodeContext(MultiInstanceCommandClass micc, byte destEndpoint, NodeContext context) {
        this.micc = micc;
        this.destEndpoint = destEndpoint;
        this.context = context;
    }

    @Override
    public byte getNodeId() {
        return context.getNodeId();
    }

    @Override
    public void sendDataFrame(DataFrame d) {
        DataFrame ed = micc.createMultiChannelCommandEncapsulation((byte)0, destEndpoint, d, true);
        if (ed == null) {
            ed = d;
        }
        context.sendDataFrame(ed);
    }

    @Override
    public void flushWakeupQueue() {
        context.flushWakeupQueue();
    }

    @Override
    public CommandClass getCommandClass(byte commandClassId) {
        return context.getCommandClass(commandClassId);
    }

    @Override
    public Collection<CommandClass> getCommandClasses() {
        return context.getCommandClasses();
    }
}
