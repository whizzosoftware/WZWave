package com.whizzosoftware.wzwave.channel;

import com.whizzosoftware.wzwave.frame.*;

public interface ZWaveChannelListener {
    public void onLibraryInfo(String libraryVersion);
    public void onControllerInfo(int homeId, byte nodeId);
    public void onNodeProtocolInfo(byte nodeId, NodeProtocolInfo nodeProtocolInfo);
    public void onSendData(SendData sendData);
    public void onApplicationCommand(ApplicationCommand cmd);
    public void onApplicationUpdate(ApplicationUpdate update);
}
