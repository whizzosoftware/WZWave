package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import io.netty.buffer.ByteBuf;

public class GetSUCNodeId extends DataFrame {
    public static final byte ID = (byte)0x56;

    private byte sucNodeId;

    public GetSUCNodeId(ByteBuf buffer) {
        super(buffer);
        sucNodeId = buffer.readByte();
    }

    public byte getSucNodeId() {
        return sucNodeId;
    }

    @Override
    public DataFrameTransaction createTransaction() {
        return null;
    }
}
