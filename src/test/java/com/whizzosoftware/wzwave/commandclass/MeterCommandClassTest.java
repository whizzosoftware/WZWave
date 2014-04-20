package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.MockFrameListener;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.parser.FrameParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeterCommandClassTest {
    @Test
    public void testMeterReport() {
        MockFrameListener listener = new MockFrameListener();
        FrameParser parser = new FrameParser(listener);
        parser.addBytes(new byte[] {0x01, 0x14, 0x00, 0x04, 0x00, 0x11, 0x0E, 0x32, 0x02, 0x21, 0x64, 0x00, 0x00, 0x00, 0x0c, 0x00, (byte)0x82, 0x00, 0x00, 0x00, 0x02, (byte)0xe4}, 22);
        MeterCommandClass cc = new MeterCommandClass();
        DataFrame frame = (DataFrame)listener.messages.get(0);
        cc.onDataFrame(frame, null);

        assertEquals(MeterCommandClass.MeterType.Electric, cc.getMeterType());
        assertEquals((Integer)130, cc.getDelta());
        assertEquals((Double)0.012, cc.getCurrentValue());
        assertEquals((Double)0.002, cc.getPreviousValue());
    }

}
