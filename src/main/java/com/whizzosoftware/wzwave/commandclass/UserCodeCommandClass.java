/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCodeCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = 0x63;

    public static final byte USER_CODE_SET = 0x01;
    public static final byte USER_CODE_GET = 0x02;
    public static final byte USER_CODE_REPORT = 0x03;

    public UserCodeCommandClass() {
        this(false);
    }

    public UserCodeCommandClass(boolean secure) {
        super();
        setSecure(secure);
    }

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_USER_CODE";
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        // TODO
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        return 0;
    }

    static public DataFrame createGetv1(byte nodeId, byte userId) {
        return createSendDataFrame("USER_CODE_GET", nodeId, new byte[]{UserCodeCommandClass.ID, USER_CODE_GET, userId}, true);
    }
}
