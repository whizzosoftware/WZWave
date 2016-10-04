/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.node;

import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.commandclass.CommandClass;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.SendData;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiChannelEncapsulatingNodeContextTest {
    @Test
    public void testResponseExpected() {
        MultiInstanceCommandClass micc = new MultiInstanceCommandClass();
        micc.setVersion(2);
        BinarySwitchCommandClass bscc = new BinarySwitchCommandClass();
        MockNodeContext ctx = new MockNodeContext((byte)0x01, new CommandClass[] {micc, bscc});
        MultiChannelEncapsulatingNodeContext enctx = new MultiChannelEncapsulatingNodeContext(micc, (byte)0x01, ctx);
        enctx.sendDataFrame(bscc.createSet((byte)0x01, true));
        assertEquals(1, ctx.getSentDataFrames().size());
        DataFrame f = ctx.getSentDataFrames().get(0);
        assertTrue(f instanceof SendData);
        assertFalse(((SendData)f).isResponseExpected());

        enctx.sendDataFrame(bscc.createGet((byte)0x01));
        assertEquals(2, ctx.getSentDataFrames().size());
        f = ctx.getSentDataFrames().get(1);
        assertTrue(f instanceof SendData);
        assertTrue(((SendData)f).isResponseExpected());
    }
}
