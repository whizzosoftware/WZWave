/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.persist;

import java.util.HashMap;
import java.util.Map;

public class MockPersistenceContext implements PersistenceContext {
    private Map<String,Object> map = new HashMap<>();

    @Override
    public Map<String,Object> getNodeMap(int nodeId) {
        String key = Integer.toString(nodeId);
        Map<String,Object> m = (Map<String,Object>)map.get(key);
        if (m == null) {
            m = new HashMap<>();
            map.put(key, m);
        }
        return m;
    }

    @Override
    public Map<String,Object> getCommandClassMap(int nodeId, int commandClassId) {
        String key = nodeId + "." + commandClassId;
        Map<String,Object> m = (Map<String,Object>)map.get(key);
        if (m == null) {
            m = new HashMap<>();
            map.put(key, m);
        }
        return m;
    }
}
