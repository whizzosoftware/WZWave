/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.AddNodeToNetwork;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;

/**
 * An AddNodeToNetworkTransaction is considered complete when an AddNodeToNetwork frame is received
 * from the controller.
 *
 * @author Dan Noguerol
 */
public class AddNodeToNetworkTransaction extends AbstractDataFrameTransaction {
    private DataFrame finalFrame;

    public AddNodeToNetworkTransaction(DataFrame startFrame) {
        super(startFrame);
    }

    @Override
    public boolean addFrame(Frame f) {
        if (f instanceof AddNodeToNetwork) {
            finalFrame = (DataFrame)f;
            return true;
        }
        return false;
    }

    @Override
    public boolean isComplete() {
        return (finalFrame != null);
    }

    @Override
    public long getTimeout() {
        return -1;
    }

    @Override
    public DataFrame getFinalFrame() {
        return finalFrame;
    }
}
