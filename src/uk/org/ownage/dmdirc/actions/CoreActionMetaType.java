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

package uk.org.ownage.dmdirc.actions;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Query;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;

/**
 * An enumeration of different types of actions (the type determines the
 * parameters an action expects).
 * @author chris
 */
public enum CoreActionMetaType {
    
    SERVER_EVENT(1, Server.class),
    CHANNEL_EVENT(1, Channel.class),
    QUERY_EVENT(1, Query.class),
    
    SERVER_EVENT_WITH_ARG(2, Server.class, String.class),
    QUERY_EVENT_WITH_ARG(2, Query.class, String.class),
    
    CHANNEL_SOURCED_EVENT(2, Channel.class, ChannelClientInfo.class),
    CHANNEL_SOURCED_EVENT_WITH_ARG(3, Channel.class, ChannelClientInfo.class, String.class),
    CHANNEL_SOURCED_EVENT_WITH_VICTIM(4, Channel.class, ChannelClientInfo.class, ChannelClientInfo.class, String.class);
    
    /** The arity of this type. */
    private final int arity;
    
    /** The types of argument this type expects. */
    private final Class[] argTypes;
    
    /**
     * Constructs an instance of an CoreActionMetaType.
     * 
     * @param arity The arity of the action type
     */
    CoreActionMetaType(final int arity, final Class ... argTypes) {
        this.arity = arity;
        this.argTypes = argTypes;
    }
    
    /**
     * Retrieves the arity of an CoreActionMetaType.
     * 
     * @return The arity of this action type
     */
    public int getArity() {
        return arity;
    }
    
    /**
     * Retrieves the type of arguments that actiontypes of this metatype should
     * expect.
     * @return The type of arguments expected
     */
    public Class[] getArgTypes() {
        return argTypes.clone();
    }
    
}
