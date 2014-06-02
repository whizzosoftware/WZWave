/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.NAK;
import org.junit.Test;
import static org.junit.Assert.*;

public class AbstractDataFrameTransactionTest {
    @Test
    public void testRetryDueToTimeout() {
        long startTime = System.currentTimeMillis();
        DataFrameTransaction t = new MockDataFrameTransaction(null, startTime);

        // < first timeout check
        assertFalse(t.shouldRetry(startTime));
        assertFalse(t.hasError(startTime));
        assertFalse(t.shouldRetry(startTime + 1000));
        assertFalse(t.hasError(startTime + 1000));
        // first timeout check
        assertTrue(t.shouldRetry(startTime + 2000));
        assertFalse(t.hasError(startTime + 2000));

        long newStartTime = startTime + 2000;
        t.incrementRetryCount(newStartTime);

        // < second timeout check
        assertFalse(t.shouldRetry(newStartTime));
        assertFalse(t.hasError(newStartTime));
        assertFalse(t.shouldRetry(newStartTime + 1000));
        assertFalse(t.hasError(newStartTime + 1000));
        // second timeout check
        assertTrue(t.shouldRetry(newStartTime + 2000));
        assertFalse(t.hasError(newStartTime + 2000));

        newStartTime = newStartTime + 2000;
        t.incrementRetryCount(newStartTime);

        // < third timeout check
        assertFalse(t.shouldRetry(newStartTime));
        assertFalse(t.hasError(newStartTime));
        assertFalse(t.shouldRetry(newStartTime + 1000));
        assertFalse(t.hasError(newStartTime + 1000));
        // third timeout check (should be false since we won't retry more than twice
        assertFalse(t.shouldRetry(newStartTime + 2000));
        assertTrue(t.hasError(newStartTime + 2000));
    }

    @Test
    public void testRetryDueToNAK() {
        long startTime = System.currentTimeMillis();

        DataFrameTransaction t = new MockDataFrameTransaction(null, startTime);
        assertFalse(t.shouldRetry(startTime));
        assertFalse(t.hasError(startTime));

        // receive first NAK
        t.addFrame(new NAK(), startTime);
        assertTrue(t.shouldRetry(startTime));
        assertFalse(t.hasError(startTime));

        // retry after first NAK
        t.incrementRetryCount(startTime);
        assertFalse(t.shouldRetry(startTime));
        assertFalse(t.hasError(startTime));

        // receive second NAK
        t.addFrame(new NAK(), startTime);
        assertTrue(t.shouldRetry(startTime));
        assertFalse(t.hasError(startTime));

        // retry after second NAK
        t.incrementRetryCount(startTime);
        assertFalse(t.shouldRetry(startTime));
        assertFalse(t.hasError(startTime));

        // receive third NAK
        t.addFrame(new NAK(), startTime);
        assertFalse(t.shouldRetry(startTime));
        assertTrue(t.hasError(startTime));
    }

    private class MockDataFrameTransaction extends AbstractDataFrameTransaction {
        public MockDataFrameTransaction(DataFrame startFrame, long startTime) {
            super(startFrame, startTime);
        }

        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public DataFrame getFinalData() {
            return null;
        }
    }
}
