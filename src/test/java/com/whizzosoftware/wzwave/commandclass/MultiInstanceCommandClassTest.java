/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.SendData;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultiInstanceCommandClassTest {
    @Test
    public void testCommandEncapsulation() {
        DataFrame cmd = BinarySwitchCommandClass.createSetv1((byte)22, true);
        DataFrame ecmd = MultiInstanceCommandClass.createMultiChannelCmdEncapv2((byte)1, (byte)3, cmd, true);
        byte[] data = ecmd.getBytes();
        assertEquals(16, data.length);
        assertEquals(0x01, data[0]); // SOF
        assertEquals(14, data[1]); // frame length
        assertEquals(0x00, data[2]); // request
        assertEquals(0x13, data[3]); // SendData
        assertEquals(22, data[4]); // node ID
        assertEquals(7, data[5]); // command length
        assertEquals(MultiInstanceCommandClass.ID, data[6]); // command class ID
        assertEquals(0x0D, data[7]); // MULTI_CHANNEL_CMD_ENCAP
        assertEquals(0x01, data[8]); // source endpoint
        assertEquals(0x03, data[9]); // destination endpoint
        assertEquals(BinarySwitchCommandClass.ID, data[10]); // command class ID
        assertEquals(0x01, data[11]); // SWITCH_BINARY_SET
        assertEquals((byte)0xFF, data[12]); // true
        assertEquals(0x05, data[13]); // TX options
    }
}
