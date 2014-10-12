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
    public void onLibraryInfo(String libraryVersion);
    public void onControllerInfo(int homeId, byte nodeId);
    public void onNodeProtocolInfo(byte nodeId, NodeProtocolInfo nodeProtocolInfo);
    public void onSendData(SendData sendData);
    public void onApplicationCommand(ApplicationCommand cmd);
    public void onApplicationUpdate(ApplicationUpdate update);
}
