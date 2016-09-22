/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.OutboundDataFrame;

/**
 * An interface that allows firing events and writing data frames.
 *
 * @author Dan Noguerol
 */
public interface ZWaveChannelContext {
    void fireEvent(Object o);
    void writeFrame(OutboundDataFrame f);
}
