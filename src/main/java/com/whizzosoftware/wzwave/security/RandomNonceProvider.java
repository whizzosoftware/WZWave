/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.security;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A NonceProvider that provides a true random 8-byte nonce.
 *
 * @author Dan Noguerol
 */
public class RandomNonceProvider implements NonceProvider {
    private byte[] targetNonce;

    public RandomNonceProvider(byte[] targetNonce) {
        this.targetNonce = targetNonce;
    }

    @Override
    public byte[] getRandomNonce() {
        final byte[] bytes = new byte[8];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    @Override
    public byte[] getTargetNonce() {
        return targetNonce;
    }
}
