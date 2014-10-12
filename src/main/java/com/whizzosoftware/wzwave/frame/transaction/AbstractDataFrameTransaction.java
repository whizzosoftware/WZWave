/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;
import com.whizzosoftware.wzwave.frame.NAK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all DataFrameTransaction implementations.
 *
 * @author Dan Noguerol
 */
abstract public class AbstractDataFrameTransaction implements DataFrameTransaction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DataFrame startFrame;
    private boolean hasError;

    public AbstractDataFrameTransaction(DataFrame startFrame) {
        this.startFrame = startFrame;
    }

    public DataFrame getStartFrame() {
        return startFrame;
    }

    public boolean hasError() {
        return hasError;
    }

    protected void setError(String msg) {
        logger.error(msg);
        this.hasError = true;
    }
}
