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
 * Interface for providing 8-byte nonce values to the Z-Wave encryption algorithm.
 *
 * @author Dan Noguerol
 */
public interface NonceProvider {
    /**
     * Provides a random 8 byte nonce.
     *
     * @return a byte array
     */
    byte[] getRandomNonce();

    /**
     * Provides the target device 8 byte nonce.
     *
     * @return a byte array
     */
    byte[] getTargetNonce();
}
