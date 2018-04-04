package com.whizzosoftware.wzwave.commandclass;

import org.junit.Test;

import static com.whizzosoftware.wzwave.commandclass.CentralSceneCommandClass.SceneCommand.BEING_HELD;
import static com.whizzosoftware.wzwave.commandclass.CentralSceneCommandClass.SceneCommand.PUSHED;
import static com.whizzosoftware.wzwave.commandclass.CentralSceneCommandClass.SceneCommand.RELEASED_AFTER_HOLD;
import static org.junit.Assert.*;

public class CentralSceneCommandClassTest {
    @Test
    public void testReport() {
        CentralSceneCommandClass cc = new CentralSceneCommandClass();

        cc.onApplicationCommand(null, new byte[] { 0x5B, 0x03, 0x01, 0x00, 0x01 }, 0);
        assertEquals((Integer) 1, cc.getSceneNumber());
        assertEquals(PUSHED, cc.getSceneCommand());
        assertEquals((Integer) 1, cc.getPushCount());

        cc.onApplicationCommand(null, new byte[] { 0x5B, 0x03, 0x02, 0x03, 0x02 }, 0);
        assertEquals((Integer) 2, cc.getSceneNumber());
        assertEquals(PUSHED, cc.getSceneCommand());
        assertEquals((Integer) 2, cc.getPushCount());

        cc.onApplicationCommand(null, new byte[] { 0x5B, 0x03, 0x03, 0x02, 0x03 }, 0);
        assertEquals(BEING_HELD, cc.getSceneCommand());

        cc.onApplicationCommand(null, new byte[] { 0x5B, 0x03, 0x04, 0x01, 0x03 }, 0);
        assertEquals(RELEASED_AFTER_HOLD, cc.getSceneCommand());

        cc.setVersion(3);
        cc.onApplicationCommand(null, new byte[] { 0x5B, 0x03, 0x05, (byte) 0x83, 0x04 }, 0);
        assertEquals((Integer) 4, cc.getSceneNumber());
        assertEquals(PUSHED, cc.getSceneCommand());
        assertEquals((Integer) 2, cc.getPushCount());
        assertEquals(true, cc.getSlowRefresh());
    }
}
