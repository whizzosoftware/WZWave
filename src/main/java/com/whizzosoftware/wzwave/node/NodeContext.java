/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.frame.DataFrame;

import java.util.Collection;

/**
 * An interface passed to command classes to allow them to indirectly interact with their node.
 *
 * @author Dan Noguerol
 */
public interface NodeContext {
    /**
     * Returns the node ID.
     *
     * @return a node ID
     */
    public byte getNodeId();

    /**
     * Queue a data frame for sending.
     *
     * @param d the data frame
     */
    public void queueDataFrame(DataFrame d);

    /**
     * Forces any data frames that have accumulated while the node was considered asleep
     * to be queued for immediate sending.
     */
    public void flushWakeupQueue();

    /**
     * Retrieve a command class for the node associated with this context.
     *
     * @param commandClassId the command class ID
     *
     * @return a CommandClass instance of none if not found
     */
    public CommandClass getCommandClass(byte commandClassId);

    /**
     * Retrieve all command classes for the node associated with this context.
     *
     * @return a Collection of CommandClass instances
     */
    public Collection<CommandClass> getCommandClasses();
}
