/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.channel.ZWaveChannelContext;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;

/**
 * A DataFrameTransaction is used to identify when a data frame sent to the Z-Wave network has completed its flow.
 * The completion of the flow indicates when the next queued data frame can be sent out. In the simplest cases, a
 * transaction is complete when a single ACK is received. In more complex cases, it may be that multiple data frames
 * need to be received before the transaction is considered complete.
 *
 * @author Dan Noguerol
 */
public interface DataFrameTransaction {
    /**
     * Get the globally unique ID for this transaction.
     *
     * @return a String
     */
    String getId();

    /**
     * Returns the first frame of the transaction.
     *
     * @return a DataFrame
     */
    DataFrame getStartFrame();

    /**
     * The transaction timeout interval.
     *
     * @return an interval in milliseconds
     */
    long getTimeout();

    /**
     * Add a Frame to the transaction.
     *
     * @param ctx the channel context
     * @param f the next frame
     *
     * @return indicates whether the frame was consumed by the transaction
     */
    boolean addFrame(ZWaveChannelContext ctx, Frame f);

    /**
     * Forces a transaction timeout.
     *
     * @param ctx the channel context
     */
    void timeout(ZWaveChannelContext ctx);

    /**
     * Indicates whether the destination node for this message transaction is known to be actively listening.
     *
     * @return a boolean
     */
    boolean isListeningNode();

    /**
     * Identifies whether the transaction is complete.
     *
     * @return a boolean
     */
    boolean isComplete();

    /**
     * Resets the transaction to its initial state.
     */
    void reset();
}
