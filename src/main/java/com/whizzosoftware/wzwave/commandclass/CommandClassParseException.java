/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

/**
 * Exception thrown when there is a problem parsing command class data.
 *
 * @author Dan Noguerol
 */
public class CommandClassParseException extends Exception {
    public CommandClassParseException(String message) {
        super(message);
    }

    public CommandClassParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
