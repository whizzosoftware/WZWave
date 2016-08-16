/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.*;

/**
 * An interface with callbacks of interest from the Z-Wave network.
 *
 * @author Dan Noguerol
 */
public interface ZWaveChannelListener {
    void onLibraryInfo(String libraryVersion);
    void onControllerInfo(int homeId, byte nodeId);
    void onNodeProtocolInfo(byte nodeId, NodeProtocolInfo nodeProtocolInfo);
    void onSendData(SendData sendData);
    void onApplicationCommand(ApplicationCommand cmd);
    void onApplicationUpdate(ApplicationUpdate update);

    /**
     * Called when an AddNodeToNetwork frame is received.
     *
     * @param addNode the received frame
     */
    void onAddNodeToNetwork(AddNodeToNetwork addNode);

    /**
     * Called when a SetDefault frame is received due to the controller being factory reset.
     */
    void onSetDefault();
}
