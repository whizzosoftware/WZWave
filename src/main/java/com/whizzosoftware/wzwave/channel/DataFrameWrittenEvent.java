/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.DataFrame;

/**
 * An interface that defines a callback when data frames are written to the Z-Wave network.
 *
 * @author Dan Noguerol
 */
public class DataFrameWrittenEvent {
    private DataFrame frame;

    public DataFrameWrittenEvent(DataFrame frame) {
        this.frame = frame;
    }

    public DataFrame getFrame() {
        return frame;
    }
}
