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

package com.dmdirc.parser.irc;

import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.CallbackObject;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import java.util.HashMap;
import java.util.Map;

/**
 * A callback object for the IRC parser.
 *
 * @since 0.6.3m2
 * @author chris
 */
public class IRCCallbackObject extends CallbackObject {

    /** A map of interfaces to the classes which should be instansiated for them. */
    protected static Map<Class<?>, Class<?>> IMPL_MAP = new HashMap<Class<?>, Class<?>>();

    static {
        IMPL_MAP.put(ChannelClientInfo.class, IRCChannelClientInfo.class);
        IMPL_MAP.put(ChannelInfo.class, IRCChannelInfo.class);
        IMPL_MAP.put(ClientInfo.class, IRCClientInfo.class);
        IMPL_MAP.put(LocalClientInfo.class, IRCClientInfo.class);
    }

    public IRCCallbackObject(Parser parser, CallbackManager<?> manager,
            Class<? extends CallbackInterface> type) {
        super(parser, manager, type);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<?> getImplementation(Class<?> type) {
        return IMPL_MAP.containsKey(type) ? IMPL_MAP.get(type) : type;
    }

}
