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

package com.dmdirc.actions.metatypes;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionMetaType;
import com.dmdirc.parser.interfaces.ClientInfo;

/**
 * Defines server-related events.
 */
public enum ServerEvents implements ActionMetaType {

    /** Server ping event type. */
    SERVER_PING(new String[]{"server", "ping"}, Connection.class, Long.class),
    /** Server numeric event type. */
    SERVER_NUMERIC(new String[]{"server", "numeric", "arguments"}, Connection.class, Integer.class,
            String[].class),
    /** Server event with argument. */
    SERVER_EVENT_WITH_ARG(new String[]{"server", "message"}, Connection.class, String.class),
    /** Server nick change. */
    SERVER_NICKCHANGE(new String[]{"server", "old nickname", "new nickname"}, Connection.class,
            String.class, String.class),
    /** Server event, with source and argument. */
    SERVER_SOURCED_EVENT_WITH_ARG(new String[]{"server", "user", "message"}, Connection.class,
            ClientInfo.class, String.class),
    /** Server CTCP event. */
    SERVER_CTCP_EVENT(new String[]{"server", "user", "type", "content"}, Connection.class,
            ClientInfo.class, String.class, String.class),
    /** Server event with argument. */
    SERVER_UNKNOWN_EVENT(new String[]{"server", "source", "target", "message"}, Connection.class,
            String.class, String.class, String.class),
    /** Server invite event. */
    SERVER_INVITE(new String[]{"server", "source", "channel"}, Connection.class, ClientInfo.class,
            String.class),
    /** Server event type. */
    SERVER_EVENT(new String[]{"server"}, Connection.class);
    /** The names of the arguments for this meta type. */
    private final String[] argNames;
    /** The classes of the arguments for this meta type. */
    private final Class<?>[] argTypes;

    /**
     * Creates a new instance of this meta-type.
     *
     * @param argNames The names of the meta-type's arguments
     * @param argTypes The types of the meta-type's arguments
     */
    ServerEvents(final String[] argNames, final Class<?>... argTypes) {
        this.argNames = argNames;
        this.argTypes = argTypes;
    }

    @Override
    public int getArity() {
        return argNames.length;
    }

    @Override
    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    @Override
    public String[] getArgNames() {
        return argNames;
    }

    @Override
    public String getGroup() {
        return "Server/Private Events";
    }

}
