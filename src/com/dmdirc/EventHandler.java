/*
 * Copyright (c) 2006-2015 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc;

import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;

import javax.annotation.Nonnull;

/**
 * Abstracts some behaviour used by Event Handlers.
 */
public abstract class EventHandler implements CallbackInterface {

    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance.
     *
     * @param eventBus The event bus to post errors to.
     */
    protected EventHandler(final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Registers all callbacks that this event handler implements with the owner's parser.
     */
    public void registerCallbacks() {
        final CallbackManager cbm = getConnection().getParser().get().getCallbackManager();

        try {
            for (Class<?> iface : getClass().getInterfaces()) {
                if (CallbackInterface.class.isAssignableFrom(iface)) {
                    addCallback(cbm, iface.asSubclass(CallbackInterface.class));
                }
            }
        } catch (CallbackNotFoundException exception) {
            eventBus.publishAsync(new AppErrorEvent(ErrorLevel.FATAL, exception,
                    "Unable to register callbacks", ""));
        }
    }

    /**
     * Unregisters all callbacks that have been registered by this event handler.
     */
    public void unregisterCallbacks() {
        getConnection().getParser().map(Parser::getCallbackManager)
                .ifPresent(cm -> cm.delAllCallback(this));
    }

    /**
     * Adds a callback to this event handler.
     *
     * @param <T>  The type of callback to be added
     * @param cbm  The callback manager to use
     * @param type The type of the callback to be added
     *
     * @throws CallbackNotFoundException if the specified callback isn't found
     */
    protected abstract <T extends CallbackInterface> void addCallback(
            final CallbackManager cbm, final Class<T> type);

    /**
     * Retrieves the connection that this event handler is for.
     *
     * @since 0.6.3m1
     * @return This EventHandler's expected connection.
     */
    @Nonnull
    protected abstract Connection getConnection();

    /**
     * Checks that the specified parser is the same as the one the server is currently claiming to
     * be using. If it isn't, we raise an exception to prevent further (erroneous) processing.
     *
     * @param parser The parser to check
     */
    protected void checkParser(final Parser parser) {
        if (parser != getConnection().getParser().orElse(null)) {
            parser.disconnect("Shouldn't be in use");
            throw new IllegalArgumentException("Event called from a parser that's not in use (#"
                    + getConnection().getStatus().getParserID(parser)
                    + ").\n\n " + getConnection().getStatus().getTransitionHistory());
        }
    }

}
