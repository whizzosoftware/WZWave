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

/**
 * A NonceProvider implementation that allows overriding the random 8-byte nonce with a known value. This is
 * used for unit testing.
 *
 * @author Dan Noguerol
 */
public class StaticNonceProvider implements NonceProvider {
    private byte[] randomNonce;
    private byte[] targetNonce;

    public StaticNonceProvider(byte[] randomNonce, byte[] targetNonce) {
        this.randomNonce = randomNonce;
        this.targetNonce = targetNonce;
    }

    @Override
    public byte[] getRandomNonce() {
        return randomNonce;
    }

    @Override
    public byte[] getTargetNonce() {
        return targetNonce;
    }
}
