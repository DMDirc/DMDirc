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
import com.dmdirc.Server;
import com.dmdirc.parser.ChannelClientInfo;

import java.awt.Color;
import java.util.GregorianCalendar;

/**
 * A CoreActionComponent represents a component of some object that the user can
 * use as the subject of a condition within an action.
 * @author chris
 */
public enum CoreActionComponent implements ActionComponent {
    
    SERVER_NAME {
        public Object get(final Object argument) { return ((Server) argument).getName(); }
        public Class appliesTo() { return Server.class; }
        public Class getType() { return String.class; }
        public String getName() { return "name"; }
    },
    
    SERVER_NETWORK {
        public Object get(final Object argument) { return ((Server) argument).getNetwork(); }
        public Class appliesTo() { return Server.class; }
        public Class getType() { return String.class; }
        public String getName() { return "network"; }
    },
    
    SERVER_MYAWAYREASON {
        public Object get(final Object argument) { return ((Server) argument).getAwayMessage(); }
        public Class appliesTo() { return Server.class; }
        public Class getType() { return String.class; }
        public String getName() { return "away reason"; }
    },  
    
    SERVER_MYNICKNAME {
        public Object get(final Object argument) { return ((Server) argument).getParser().getMyself().getNickname(); }
        public Class appliesTo() { return Server.class; }
        public Class getType() { return String.class; }
        public String getName() { return "nickname"; }
    },    
    
    CHANNEL_NAME {
        public Object get(final Object argument) { return ((Channel) argument).getChannelInfo().getName(); }
        public Class appliesTo() { return Channel.class; }
        public Class getType() { return String.class; }
        public String getName() { return "name"; }
    },
    
    CHANNEL_COLOUR {
        public Object get(final Object argument) { return ((Channel) argument).getNotification(); }
        public Class appliesTo() { return Channel.class; }
        public Class getType() { return Color.class; }
        public String getName() { return "notification colour"; }
    },
    
    USER_NAME {
        public Object get(final Object argument) { return ((ChannelClientInfo) argument).getNickname(); }
        public Class appliesTo() { return ChannelClientInfo.class; }
        public Class getType() { return String.class; }
        public String getName() { return "nickname"; }
    },
    
    USER_MODES {
        public Object get(final Object argument) { return ((ChannelClientInfo) argument).getChanModeStr(false); }
        public Class appliesTo() { return ChannelClientInfo.class; }
        public Class getType() { return String.class; }
        public String getName() { return "modes"; }
    },
    
    USER_HOST {
        public Object get(final Object argument) { return ((ChannelClientInfo) argument).getClient().getHost(); }
        public Class appliesTo() { return ChannelClientInfo.class; }
        public Class getType() { return String.class; }
        public String getName() { return "host"; }
    },
    
    STRING_STRING {
        public Object get(final Object argument) { return argument; }
        public Class appliesTo() { return String.class; }
        public Class getType() { return String.class; }
        public String getName() { return "content"; }
    },
    
    STRING_LENGTH {
        public Object get(final Object argument) { return ((String) argument).length(); }
        public Class appliesTo() { return String.class; }
        public Class getType() { return Integer.class; }
        public String getName() { return "length"; }
    },
    
    STRINGARRAY_LENGTH {
        public Object get(final Object argument) { return Integer.valueOf(((String[]) argument).length); }
        public Class appliesTo() { return String[].class; }
        public Class getType() { return Integer.class; }
        public String getName() { return "size"; }
    },
    
    CALENDAR_FULLSTRING {
        public Object get(final Object argument) { return ((GregorianCalendar) argument).getTime().toString(); }
        public Class appliesTo() { return GregorianCalendar.class; }
        public Class getType() { return String.class; }
        public String getName() { return "full date"; }
    };
    
    /** {@inheritDoc} */
    public abstract Object get(Object argument);
    
    /** {@inheritDoc} */
    public abstract Class appliesTo();
    
    /** {@inheritDoc} */
    public abstract Class getType();
    
    /** {@inheritDoc} */
    public abstract String getName();
    
}
