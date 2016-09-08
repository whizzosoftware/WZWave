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

import com.whizzosoftware.wzwave.frame.AddNodeToNetwork;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.Frame;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NodeInclusionTransaction transaction is initiated when an AddNodeToNetwork frame is received from the Z-Wave
 * network with the ADD_NODE_STATUS_NODE_FOUND status. The transaction is finished when another AddNodeToNetwork
 * frame is received with either the ADD_NODE_STATUS_PROTOCOL_DONE or ADD_NODE_STATUS_FAILED statuses.
 *
 * @author Dan Noguerol
 */
public class NodeInclusionTransaction extends AbstractDataFrameTransaction {
    private static final Logger logger = LoggerFactory.getLogger(NodeInclusionTransaction.class);

    private DataFrame finalFrame;
    private boolean finished = false;

    public NodeInclusionTransaction(DataFrame startFrame) {
        super(startFrame, true);
    }

    @Override
    public boolean addFrame(Frame f) {
        if (f instanceof AddNodeToNetwork) {
            switch (((AddNodeToNetwork)f).getStatus()) {
                case AddNodeToNetwork.ADD_NODE_STATUS_ADDING_CONTROLLER:
                case AddNodeToNetwork.ADD_NODE_STATUS_ADDING_SLAVE:
                    logger.debug("A node has been found that wants to be included: {}", ByteUtil.createString(((AddNodeToNetwork)f).getSource()));
                    finalFrame = (DataFrame)f;
                    return true;
                case AddNodeToNetwork.ADD_NODE_STATUS_PROTOCOL_DONE:
                    logger.trace("AddNodeToNetwork is complete");
                    finished = true;
                    return true;
                case AddNodeToNetwork.ADD_NODE_STATUS_FAILED:
                    logger.error("AddNodeToNetwork failed");
                    finalFrame = (DataFrame)f;
                    finished = true;
                    setError("Node addition failed", false);
                    return true;
                default:
                    logger.trace("Unknown frame received in transaction; passing it along");
            }
        }
        return false;
    }

    @Override
    public boolean isComplete() {
        return finished;
    }

    @Override
    public DataFrame getFinalFrame() {
        return finalFrame;
    }

    @Override
    public void reset() {
        finalFrame = null;
        finished = false;
    }
}
