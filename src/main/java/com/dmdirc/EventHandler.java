/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.interfaces.Parser;

import javax.annotation.Nonnull;

/**
 * Abstracts some behaviour used by Event Handlers.
 */
public abstract class EventHandler {

    /**
     * Creates a new instance.
     */
    protected EventHandler() {
    }

    /**
     * Registers all callbacks that this event handler implements with the owner's parser.
     */
    public void registerCallbacks() {
        getConnection().getParser().get().getCallbackManager().subscribe(this);
    }

    /**
     * Unregisters all callbacks that have been registered by this event handler.
     */
    public void unregisterCallbacks() {
        getConnection().getParser().map(Parser::getCallbackManager).ifPresent(cm -> cm.unsubscribe(this));
    }

    /**
     * Retrieves the connection that this event handler is for.
     *
     * @since 0.6.3m1
     * @return This EventHandler's expected connection.
     */
    @Nonnull
    protected abstract Connection getConnection();

}
