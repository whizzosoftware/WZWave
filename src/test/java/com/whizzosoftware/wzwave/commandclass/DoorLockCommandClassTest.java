package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DoorLockCommandClassTest {
    @Test
    public void testDoorLockOperationGetv1() {
        DataFrame frame = DoorLockCommandClass.createGetv1((byte)0x02);
        byte[] b = frame.getBytes();
        assertEquals(11, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x09, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x02, b[4]); // node ID
        assertEquals(0x02, b[5]); // command length
        assertEquals(0x62, b[6]); // command ID (Door lock)
        assertEquals(0x02, b[7]); // DOOR_LOCK_OPERATION_GET
        assertEquals(0x05, b[8]); // TX options
    }

    @Test
    public void testDoorLockOperationSetv1() {
        DataFrame frame = DoorLockCommandClass.createSetv1((byte)0x02, DoorLockCommandClass.MODE_SECURED);
        byte[] b = frame.getBytes();
        assertEquals(12, b.length);
        assertEquals(0x01, b[0]); // SOF
        assertEquals(0x0A, b[1]); // frame length
        assertEquals(0x00, b[2]); // request
        assertEquals(0x13, b[3]); // SendData
        assertEquals(0x02, b[4]); // node ID
        assertEquals(0x03, b[5]); // command length
        assertEquals(0x62, b[6]); // command ID (Door lock)
        assertEquals(0x01, b[7]); // DOOR_LOCK_OPERATION_SET
        assertEquals(DoorLockCommandClass.MODE_SECURED, b[8]); // mode
        assertEquals(0x05, b[9]); // TX options
    }
}
