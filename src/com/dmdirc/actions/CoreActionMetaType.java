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

package com.dmdirc.actions;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.plugins.Plugin;

import javax.swing.KeyStroke;

/**
 * An enumeration of different types of actions (the type determines the
 * parameters an action expects).
 * @author chris
 */
public enum CoreActionMetaType implements ActionMetaType {
    
    /** Client event type. */
    CLIENT_EVENT(0, new String[]{}),
    /** Client event type, with a frame argument. */
    CLIENT_EVENT_WITH_FRAME(1, new String[]{"frame"}, FrameContainer.class),
    /** Client event type, with a key argument. */
    CLIENT_EVENT_WITH_KEY(1, new String[]{"key event"}, KeyStroke.class),
    /** Client event with an origin and editable buffer. */
    CLIENT_EVENT_WITH_BUFFER(2, new String[]{"origin", "buffer"}, FrameContainer.class, StringBuffer.class),    
    
    /** Plugin event type. */
    PLUGIN_EVENT(1, new String[]{"plugin"}, Plugin.class),
    
    /** Unknown command event type. */
    UNKNOWN_COMMAND(3, new String[]{"source", "command", "arguments"}, FrameContainer.class, String.class, String[].class),
    
    /** Server ping event type. */
    SERVER_PING(2, new String[]{"server", "ping"}, Server.class, Long.class),
    /** Server numeric event type. */
    SERVER_NUMERIC(3, new String[]{"server", "numeric", "arguments"}, Server.class, Integer.class, String[].class),
    
    /** Server event type. */
    SERVER_EVENT(1, new String[]{"server"}, Server.class),
    /** Channel event type. */
    CHANNEL_EVENT(1, new String[]{"channel"}, Channel.class),
    /** Query event type. */
    QUERY_EVENT(1, new String[]{"query"}, Query.class),
    
    /** Server event with argument. */
    SERVER_EVENT_WITH_ARG(2, new String[]{"server", "message"}, Server.class, String.class),
    /** Server event, with source and argument. */
    SERVER_SOURCED_EVENT_WITH_ARG(3, new String[]{"server", "user", "message"}, Server.class, ClientInfo.class, String.class),
    /** Server CTCP event. */
    SERVER_CTCP_EVENT(4, new String[]{"server", "user", "type", "content"}, Server.class, ClientInfo.class, String.class, String.class),
    
    /** Query event with argument. */
    QUERY_EVENT_WITH_ARG(2, new String[]{"query", "message"}, Query.class, String.class),
    
    /** Channel event with source. */
    CHANNEL_SOURCED_EVENT(2, new String[]{"channel", "user"}, Channel.class, ChannelClientInfo.class),
    /** Chanel event with source and argument. */
    CHANNEL_SOURCED_EVENT_WITH_ARG(3, new String[]{"channel", "user", "message"}, Channel.class, ChannelClientInfo.class, String.class),
    /** Channel event with source and victim. */
    CHANNEL_SOURCED_EVENT_WITH_VICTIM(4, new String[]{"channel", "user", "victim", "message"}, Channel.class, ChannelClientInfo.class, ChannelClientInfo.class, String.class);
    
    /** The arity of this type. */
    private final int arity;
    
    /** The types of argument this type expects. */
    private final Class[] argTypes;
    
    /** The names of the arguments that this type expects. */
    private final String[] argNames;
    
    /**
     * Constructs an instance of an CoreActionMetaType.
     * @param arity The arity of the action type
     * @param argNames The names of the arguments
     * @param argTypes The types of the argument
     */
    CoreActionMetaType(final int arity, final String[] argNames, final Class ... argTypes) {
        this.arity = arity;
        this.argNames = argNames;
        this.argTypes = argTypes;
    }
    
    /** {@inheritDoc} */
    public int getArity() {
        return arity;
    }
    
    /** {@inheritDoc} */
    public Class[] getArgTypes() {
        return argTypes.clone();
    }
    
    /** {@inheritDoc} */
    public String[] getArgNames() {
        return argNames.clone();
    }
    
}
