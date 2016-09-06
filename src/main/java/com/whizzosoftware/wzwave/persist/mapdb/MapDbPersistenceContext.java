/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.persist.mapdb;

import com.whizzosoftware.wzwave.persist.PersistenceContext;
import org.mapdb.DB;

import java.util.Map;

/**
 * A MapDb implementation of PersistenceContext.
 *
 * @author Dan Noguerol
 */
public class MapDbPersistenceContext implements PersistenceContext {
    private DB db;

    public MapDbPersistenceContext(DB db) {
        this.db = db;
    }

    @Override
    public Map<String, Object> getNodeMap(int nodeId) {
        String key = Integer.toString(nodeId);
        return db.createHashMap(key).makeOrGet();
    }

    @Override
    public Map<String, Object> getCommandClassMap(int nodeId, int commandClassId) {
        String key = nodeId + "." + commandClassId;
        return db.createHashMap(key).makeOrGet();
    }
}
