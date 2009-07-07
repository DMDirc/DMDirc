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

package com.dmdirc.parser.irc.callbacks;

import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import com.dmdirc.parser.irc.IRCParser;

/**
 * Handles callbacks for the IRC Parser.
 * 
 * @author chris
 */
public class IRCCallbackManager extends CallbackManager<IRCParser> {

    public IRCCallbackManager(final IRCParser parser) {
        super(parser);
    }

    /** {@inheritDoc} */
    @Override
    protected void initialise(final IRCParser parser) {
        for (Class<?> type : CLASSES) {
            if (type.isAnnotationPresent(SpecificCallback.class)) {
                addCallbackType(new CallbackObjectSpecific(parser, this,
                        type.asSubclass(CallbackInterface.class)));
            } else {
                addCallbackType(new CallbackObject(parser, this,
                        type.asSubclass(CallbackInterface.class)));
            }
        }
    }

}
