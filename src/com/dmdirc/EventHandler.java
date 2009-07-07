/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;

/**
 * Abstracts some behaviour used by Event Handlers.
 * 
 * @author chris
 */
public abstract class EventHandler implements CallbackInterface {
    
    /**
     * Registers all callbacks that this event handler implements with the
     * owner's parser.
     */
    @SuppressWarnings("unchecked")
    public void registerCallbacks() {
        final CallbackManager cbm = getServer().getParser().getCallbackManager();
        
        try {
            for (Class iface : this.getClass().getInterfaces()) {
                if (CallbackInterface.class.isAssignableFrom(iface)) {
                    addCallback(cbm, iface);
                }
            }
        } catch (CallbackNotFoundException exception) {
            Logger.appError(ErrorLevel.FATAL, "Unable to register callbacks",
                    exception);
        }
    }
    
    /**
     * Unregisters all callbacks that have been registered by this event handler.
     */
    public void unregisterCallbacks() {
        if (getServer().getParser() != null) {
            getServer().getParser().getCallbackManager().delAllCallback(this);
        }
    }
    
    /**
     * Adds a callback to this event handler.
     * 
     * @param <T> The type of callback to be added
     * @param cbm The callback manager to use
     * @param type The type of the callback to be added
     * @throws com.dmdirc.parser.irc.callbacks.CallbackNotFoundException
     * if the specified callback isn't found
     */
    @SuppressWarnings("unchecked")
    protected abstract <T extends CallbackInterface> void addCallback(
            final CallbackManager cbm, final Class<T> type) throws CallbackNotFoundException;
    
    /**
     * Retrieves the server belonging to this EventHandler's owner.
     *
     * @since 0.6.3m1
     * @return This EventHandler's expected server
     */
    protected abstract Server getServer();
    
    /**
     * Checks that the specified parser is the same as the one the server is
     * currently claiming to be using. If it isn't, we raise an exception to
     * prevent further (erroneous) processing.
     * 
     * @param parser The parser to check
     */
    protected void checkParser(final Parser parser) {
        if (parser != getServer().getParser()) {
            parser.disconnect("Shouldn't be in use");
            throw new IllegalArgumentException("Event called from a parser that's not in use (#"
                    + getServer().getStatus().getParserID(parser)
                    + ").\n\n " + getServer().getStatus().getTransitionHistory());
        }
    }    

}
