/*******************************************************************************
 * Copyright (c) 2019 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IndicatorCommandClassTest {
    private IndicatorCommandClass icc;

    @Before
    public void before() {
        icc = new IndicatorCommandClass();
    }

    @Test
    public void testDefault() {
        assertThat(icc.isOn()).isNull();
    }

    @Test
    public void testStartup() {
        NodeContext nodeContext = mock(NodeContext.class);

        assertThat(icc.queueStartupMessages(nodeContext, (byte) 0x34)).isEqualTo(1);

        ArgumentCaptor<DataFrame> dataFrameCaptor = ArgumentCaptor.forClass(DataFrame.class);
        verify(nodeContext).sendDataFrame(dataFrameCaptor.capture());
        assertThat(dataFrameCaptor.getValue().getBytes()).containsSequence(commandPayloadOf(0x34, 0x02)); // NodeId, DataLenght, CommandClass, Command
    }

    @Test
    public void testGet() {
        DataFrame dataFrame = icc.createGet((byte) 0x45);
        assertThat(dataFrame.getBytes()).containsSequence(commandPayloadOf(0x45, 0x02));
    }

    @Test
    public void testSetOff() {
        DataFrame dataFrame = icc.createSet((byte) 0x55, false);
        assertThat(dataFrame.getBytes()).containsSequence(commandPayloadOf(0x55, 0x01, 0x00));
    }

    @Test
    public void testSetOn() {
        DataFrame dataFrame = icc.createSet((byte) 0x28, true);
        assertThat(dataFrame.getBytes()).containsSequence(commandPayloadOf(0x28, 0x01, 0xFF));
    }

    @Test
    public void testCallbackOff() {
        NodeContext nodeContext = mock(NodeContext.class);

        icc.onApplicationCommand(nodeContext, commandPayloadOf(0x34, 0x03, 0x00), 2);

        assertThat(icc.isOn()).isFalse();
    }

    @Test
    public void testCallbackOn01() {
        NodeContext nodeContext = mock(NodeContext.class);

        icc.onApplicationCommand(nodeContext, commandPayloadOf(0x34, 0x03, 0x01), 2);

        assertThat(icc.isOn()).isTrue();
    }

    @Test
    public void testCallbackOn63() {
        NodeContext nodeContext = mock(NodeContext.class);

        icc.onApplicationCommand(nodeContext, commandPayloadOf(0x34, 0x03, 0x63), 2);

        assertThat(icc.isOn()).isTrue();
    }

    @Test
    public void testCallbackOnFF() {
        NodeContext nodeContext = mock(NodeContext.class);

        icc.onApplicationCommand(nodeContext, commandPayloadOf(0x34, 0x03, 0xFF), 2);

        assertThat(icc.isOn()).isTrue();
    }

    @Test
    public void testCallbackInvalidValue() {
        NodeContext nodeContext = mock(NodeContext.class);

        icc.onApplicationCommand(nodeContext, commandPayloadOf(0x34, 0x03, 0x64), 2);

        assertThat(icc.isOn()).isNull();
    }

    @Test
    public void testCallbackInvalidCommand() {
        NodeContext nodeContext = mock(NodeContext.class);

        icc.onApplicationCommand(nodeContext, commandPayloadOf(0x34, 0x02, 0xFF), 2);

        assertThat(icc.isOn()).isNull();
    }

    private byte[] commandPayloadOf(int nodeId, int command, int... values) {
        byte[] payload = new byte[4 + values.length];
        payload[0] = (byte) nodeId;
        payload[1] = (byte) (2 + values.length); // Command length
        payload[2] = (byte) 0x87; // Command Class Id
        payload[3] = (byte) command;
        for (int i = 0; i < values.length; i++)
        {
            payload[4+i] = (byte) values[i];
        }
        return payload;
    }
}
