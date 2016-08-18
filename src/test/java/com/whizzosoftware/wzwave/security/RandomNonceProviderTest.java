package com.whizzosoftware.wzwave.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomNonceProviderTest {
    @Test
    public void testGenerateNonce() {
        RandomNonceProvider p = new RandomNonceProvider(null);
        byte[] nonce1 = p.getRandomNonce();
        byte[] nonce2 = p.getRandomNonce();
        assertEquals(8, nonce1.length);
        assertEquals(8, nonce2.length);
        int match = 0;
        for (int i=0; i < 8; i++) {
            if (nonce1[i] == nonce2[i]) {
                match++;
            }
        }
        assertTrue(match < 4);
    }
}
