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
    public void flushWakeupQueue() {
        node.flushWakeupQueue(context);
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
