package com.whizzosoftware.wzwave.frame.transaction;

import com.whizzosoftware.wzwave.frame.ACK;
import com.whizzosoftware.wzwave.frame.CAN;
import com.whizzosoftware.wzwave.frame.Version;
import org.junit.Test;
import static org.junit.Assert.*;

public class RequestResponseTransactionTest {
    @Test
    public void testSuccess() {
        RequestResponseTransaction t = new RequestResponseTransaction(new Version(), true);
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        t.addFrame(new ACK());
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        t.addFrame(new Version("version", (byte)0x01));
        assertTrue(t.isComplete());
        assertFalse(t.hasError());
    }

    @Test
    public void testCANBeforeACK() {
        RequestResponseTransaction t = new RequestResponseTransaction(new Version(), true);
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        assertTrue(t.addFrame(new CAN()));
        assertTrue(t.isComplete());
        assertTrue(t.hasError());
    }

    @Test
    public void testCANAfterACK() {
        RequestResponseTransaction t = new RequestResponseTransaction(new Version(), true);
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        assertTrue(t.addFrame(new ACK()));
        assertFalse(t.isComplete());
        assertFalse(t.hasError());

        assertTrue(t.addFrame(new CAN()));
        assertTrue(t.isComplete());
        assertTrue(t.hasError());
    }
}
