/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.RequestNodeInfo;

/**
 * DataFrameTransaction implementation for RequestNodeInfo exchanges.
 *
 * @author Dan Noguerol
 */
public class RequestNodeInfoTransaction extends RequestResponseRequestTransaction {
    public RequestNodeInfoTransaction(DataFrame startFrame, boolean listeningNode) {
        super(startFrame, listeningNode);
    }

    protected boolean wasSendSuccessful(DataFrame dataFrame) {
        if (dataFrame instanceof RequestNodeInfo) {
            return ((RequestNodeInfo)dataFrame).wasSuccessfullySent();
        } else {
            return false;
        }
    }
}
