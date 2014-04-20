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
    private long startTime;
    private long lastRetryTime;
    private int retryCount;
    private boolean hasError;
    private boolean hasPendingNAK;

    public AbstractDataFrameTransaction(DataFrame startFrame, long startTime) {
        this.startFrame = startFrame;
        this.startTime = startTime;
    }

    public DataFrame getStartFrame() {
        return startFrame;
    }

    @Override
    public void addFrame(Frame f) {
        if (f instanceof NAK) {
            hasPendingNAK = true;
        }
    }

    public boolean shouldRetry(long now) {
        return ((hasPendingNAK && retryCount < 2) || (retryCount == 0 && now - startTime >= 2000) || (retryCount == 1 && now - lastRetryTime >= 2000));
    }

    public void incrementRetryCount(long now) {
        retryCount++;
        lastRetryTime = now;
        hasPendingNAK = false;
    }

    public boolean hasError(long now) {
        return (hasError || (retryCount >= 2 && now - lastRetryTime >= 2000) || (hasPendingNAK && retryCount >= 2));
    }

    protected void setError(String msg) {
        logger.error(msg);
        this.hasError = true;
    }
}
