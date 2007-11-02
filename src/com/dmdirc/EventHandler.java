/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.callbacks.CallbackManager;
import com.dmdirc.parser.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.callbacks.interfaces.ICallbackInterface;

/**
 * Abstracts some behaviour used by Event Handlers.
 * 
 * @author chris
 */
public abstract class EventHandler implements ICallbackInterface {
    
    /** The prefix indicating that the interface is a parser callback. */
    private static final String CALLBACK_PREFIX = "com.dmdirc.parser.callbacks.interfaces.I";
    
    /**
     * Registers all callbacks that this event handler implements with the
     * owner's parser.
     */
    public void registerCallbacks() {
        final CallbackManager cbm = getParser().getCallbackManager();
        
        try {
            for (Class iface : this.getClass().getInterfaces()) {
                if (iface.getName().startsWith(CALLBACK_PREFIX)) {
                    addCallback(cbm, "on" + iface.getName().substring(CALLBACK_PREFIX.length()));
                }
            }
        } catch (CallbackNotFoundException exception) {
            Logger.appError(ErrorLevel.FATAL, "Unable to register callbacks",
                    exception);
        }
    }    
    
    protected abstract void addCallback(CallbackManager cbm, String name) 
            throws CallbackNotFoundException;
    
    /**
     * Retrieves the parser belonging to this EventHandler's owner.
     * 
     * @return This EventHandler's expected parser
     */
    protected abstract IRCParser getParser();
    
    /**
     * Checks that the specified parser is the same as the one the server is
     * currently claiming to be using. If it isn't, we raise an exception to
     * prevent further (erroneous) processing.
     * 
     * @param parser The parser to check
     */
    protected void checkParser(final IRCParser parser) {
        if (parser != getParser()) {
            throw new IllegalArgumentException("Event called from a parser that's not in use."
                    + "\nActual parser: " + getParser().hashCode()
                    + "\nPassed parser: " + parser.hashCode());
        }
    }    

}
