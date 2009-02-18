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

package com.dmdirc.actions.metatypes;

import com.dmdirc.Server;
import com.dmdirc.actions.interfaces.ActionMetaType;
import com.dmdirc.parser.irc.ClientInfo;

/**
 * Defines server-related events.
 *
 * @author Chris
 */
public enum ServerEvents implements ActionMetaType {
    
    /** Server ping event type. */
    SERVER_PING(new String[]{"server", "ping"}, Server.class, Long.class),
    /** Server numeric event type. */
    SERVER_NUMERIC(new String[]{"server", "numeric", "arguments"}, Server.class, Integer.class, String[].class),
    /** Server event with argument. */
    SERVER_EVENT_WITH_ARG(new String[]{"server", "message"}, Server.class, String.class),
    /** Server nick change. */
    SERVER_NICKCHANGE(new String[]{"server", "old nickname", "new nickname"}, Server.class, String.class, String.class),
    /** Server event, with source and argument. */
    SERVER_SOURCED_EVENT_WITH_ARG(new String[]{"server", "user", "message"}, Server.class, ClientInfo.class, String.class),
    /** Server CTCP event. */
    SERVER_CTCP_EVENT(new String[]{"server", "user", "type", "content"}, Server.class, ClientInfo.class, String.class, String.class),
    /** Server event with argument. */
    SERVER_UNKNOWN_EVENT(new String[]{"server", "source", "target", "message"}, Server.class, String.class, String.class, String.class),
    /** Server invite event. */
    SERVER_INVITE(new String[]{"server", "source", "channel"}, Server.class, ClientInfo.class, String.class),
    /** Server event type. */
    SERVER_EVENT(new String[]{"server"}, Server.class);
    
    /** The names of the arguments for this meta type. */
    private String[] argNames;
    /** The classes of the arguments for this meta type. */
    private Class[] argTypes;
    
    /**
     * Creates a new instance of this meta-type.
     *
     * @param argNames The names of the meta-type's arguments
     * @param argTypes The types of the meta-type's arguments
     */
    ServerEvents(final String[] argNames, final Class ... argTypes) {
        this.argNames = argNames;
        this.argTypes = argTypes;
    }
    
    /** {@inheritDoc} */
    @Override
    public int getArity() {
        return argNames.length;
    }
    
    /** {@inheritDoc} */
    @Override
    public Class[] getArgTypes() {
        return argTypes;
    }
    
    /** {@inheritDoc} */
    @Override
    public String[] getArgNames() {
        return argNames;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getGroup() {
        return "Server/Private Events";
    }    
    
}
