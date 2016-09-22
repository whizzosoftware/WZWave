/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.DataFrame;

import java.util.Collection;

public class WrapperedNodeContext implements NodeContext {
    private ZWaveControllerContext context;
    private ZWaveNode node;

    public WrapperedNodeContext(ZWaveControllerContext context, ZWaveNode node) {
        this.context = context;
        this.node = node;
    }

    @Override
    public byte getNodeId() {
        return node.getNodeId();
    }

    @Override
    public void sendDataFrame(DataFrame d) {
        node.sendDataFrame(context, d);
    }

    @Override
    public void setSleeping(boolean sleeping) {
        node.setSleeping(context, sleeping);
    }

    @Override
    public CommandClass getCommandClass(byte commandClassId) {
        return node.getCommandClass(commandClassId);
    }

    @Override
    public Collection<CommandClass> getCommandClasses() {
        return node.getCommandClasses();
    }
}
