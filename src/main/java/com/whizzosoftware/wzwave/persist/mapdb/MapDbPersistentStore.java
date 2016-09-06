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

import com.whizzosoftware.wzwave.node.NodeCreationException;
import com.whizzosoftware.wzwave.node.NodeListener;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import com.whizzosoftware.wzwave.node.ZWaveNodeFactory;
import com.whizzosoftware.wzwave.persist.PersistentStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;

/**
 * A MapDb implementation of PersistentStore.
 *
 * @author Dan Noguerol
 */
public class MapDbPersistentStore implements PersistentStore {
    private DB db;

    public MapDbPersistentStore(File dataDirectory) {
        db = DBMaker.newFileDB(new File(dataDirectory, "store")).make();
    }

    public MapDbPersistenceContext getContext() {
        return new MapDbPersistenceContext(db);
    }

    @Override
    public ZWaveNode getNode(byte nodeId, NodeListener listener) throws NodeCreationException {
        return ZWaveNodeFactory.createNode(getContext(), nodeId, listener);
    }

    @Override
    public void saveNode(ZWaveNode node) {
        node.save(getContext());
        db.commit();
    }
}
