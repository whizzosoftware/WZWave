/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.node.specific;

import com.whizzosoftware.wzwave.commandclass.*;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.generic.EntryControl;

public class SecureKeypadDoorLock extends EntryControl {
    static public final byte ID = 0x03;

    public SecureKeypadDoorLock(ZWaveControllerContext context, NodeInfo info, boolean newlyIncluded, boolean listening, NodeListener listener) {
        super(context, info, listening, newlyIncluded, true, listener);

        addCommandClass(DoorLockCommandClass.ID, new DoorLockCommandClass(true));
        addCommandClass(ManufacturerSpecificCommandClass.ID, new ManufacturerSpecificCommandClass());
        addCommandClass(SecurityCommandClass.ID, new SecurityCommandClass(true));
        addCommandClass(UserCodeCommandClass.ID, new UserCodeCommandClass(true));
        addCommandClass(VersionCommandClass.ID, new VersionCommandClass());
    }
}
