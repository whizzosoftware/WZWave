/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;

/**
 * An interface passed to command classes to allow them to send data frames. This makes
 * unit testing a lot easier.
 *
 * @author Dan Noguerol
 */
public interface DataQueue {
    /**
     * Queue a data frame for sending.
     *
     * @param d the data frame
     */
    public void queueDataFrame(DataFrame d);

    /**
     * Forces any data frames that have been queued up while a node was considered asleep
     * to be queued for immediate sending.
     */
    public void flushWakeupQueue();
}
