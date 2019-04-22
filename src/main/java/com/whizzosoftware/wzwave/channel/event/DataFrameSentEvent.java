/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel.event;

import com.whizzosoftware.wzwave.frame.DataFrame;

/**
 * An event fired when a data frame is sent to the Z-Wave network.
 *
 * @author Dan Noguerol
 */
public class DataFrameSentEvent {
    private DataFrame dataFrame;
    private boolean listeningNode;

    /**
     * Constructor.
     *
     * @param dataFrame the data frame that was sent
     * @param listeningNode indicates if the destination node is a listening node
     */
    public DataFrameSentEvent(DataFrame dataFrame, boolean listeningNode) {
        this.dataFrame = dataFrame;
        this.listeningNode = listeningNode;
    }

    public DataFrame getDataFrame() {
        return dataFrame;
    }

    public boolean isListeningNode() {
        return listeningNode;
    }

    @Override
    public String toString()
    {
        return "DataFrameSentEvent{" +
                "listeningNode=" + isListeningNode() +
                '}';
    }
}
