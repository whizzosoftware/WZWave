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

import com.whizzosoftware.wzwave.frame.DataFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Abstract base class for all DataFrameTransaction implementations.
 *
 * @author Dan Noguerol
 */
abstract public class AbstractDataFrameTransaction implements DataFrameTransaction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String id = UUID.randomUUID().toString();
    private DataFrame startFrame;
    private boolean listeningNode;
    private boolean hasError;
    private boolean shouldRetry = true;

    public AbstractDataFrameTransaction(DataFrame startFrame, boolean listeningNode) {
        this.startFrame = startFrame;
        this.listeningNode = listeningNode;
    }

    public String getId() {
        return id;
    }

    public long getTimeout() {
        return 2000;
    }

    public DataFrame getStartFrame() {
        return startFrame;
    }

    @Override
    public boolean isListeningNode() {
        return listeningNode;
    }

    public boolean hasError() {
        return hasError;
    }

    @Override
    public boolean shouldRetry() {
        return shouldRetry;
    }

    protected void setError(String msg, boolean shouldRetry) {
        logger.error(msg);
        this.hasError = true;
        this.shouldRetry = shouldRetry;
    }
}
