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
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelClientInfo;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.KeyStroke;

/**
 * A CoreActionComponent represents a component of some object that the user can
 * use as the subject of a condition within an action.
 * @author chris
 */
public enum CoreActionComponent implements ActionComponent {
    
    /** Returns the name of the server. */
    SERVER_NAME {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Server) argument).getName(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Server.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "name"; }
    },
    
    /** Returns the network of the server. */
    SERVER_NETWORK {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Server) argument).getNetwork(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Server.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "network"; }
    },
    
    /** Returns the away reason for the server. */
    SERVER_MYAWAYREASON {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Server) argument).getAwayMessage(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Server.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "away reason"; }
    },
    
    /** Returns the nickname for the server. */
    SERVER_MYNICKNAME {
        /** {@inheritDoc} */
        public Object get(final Object argument) {
            final Server server = (Server) argument;
            
            if (server == null || server.getParser() == null || server.getParser().getMyself() == null) {
                Logger.appError(ErrorLevel.LOW, "SERVER_MYNICKNAME.get() called with null element",
                        new UnsupportedOperationException(
                        server == null ? "Server was null" :
                            server.getParser() == null ? "Parser was null" :
                                server.getParser().getMyself() == null ? "Myself was null" :
                                    "Unknown"
                        ));
                
                return "null";
            } else {
                return server.getParser().getMyself().getNickname();
            }
        }
        /** {@inheritDoc} */
        public Class appliesTo() { return Server.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "nickname"; }
    },
    
    /** Returns the name of the channel. */
    CHANNEL_NAME {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Channel) argument).getChannelInfo().getName(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Channel.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "name"; }
    },
    
    /** Returns the notification colour of the channel. */
    CHANNEL_COLOUR {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Channel) argument).getNotification(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Channel.class; }
        /** {@inheritDoc} */
        public Class getType() { return Color.class; }
        /** {@inheritDoc} */
        public String getName() { return "notification colour"; }
    },
    
    /** Returns the name of a client. */
    USER_NAME {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((ChannelClientInfo) argument).getNickname(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "nickname"; }
    },
    
    /** Returns the modes of a client. */
    USER_MODES {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((ChannelClientInfo) argument).getChanModeStr(false); }
        /** {@inheritDoc} */
        public Class appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "modes"; }
    },
    
    /** Returns the host of a client. */
    USER_HOST {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((ChannelClientInfo) argument).getClient().getHost(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "host"; }
    },
    
    /** Returns the number of common channels the client is on. */
    USER_COMCHANS {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return Integer.valueOf(((ChannelClientInfo) argument).getClient().channelCount()); }
        /** {@inheritDoc} */
        public Class appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        public Class getType() { return Integer.class; }
        /** {@inheritDoc} */
        public String getName() { return "number of common channels"; }
    },
    
    /** Returns the content of a string. */
    STRING_STRING {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return argument; }
        /** {@inheritDoc} */
        public Class appliesTo() { return String.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "content"; }
    },
    
    /** Returns the length of a string. */
    STRING_LENGTH {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((String) argument).length(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return String.class; }
        /** {@inheritDoc} */
        public Class getType() { return Integer.class; }
        /** {@inheritDoc} */
        public String getName() { return "length"; }
    },
    
    /** Returns the size of a string array. */
    STRINGARRAY_LENGTH {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return Integer.valueOf(((String[]) argument).length); }
        /** {@inheritDoc} */
        public Class appliesTo() { return String[].class; }
        /** {@inheritDoc} */
        public Class getType() { return Integer.class; }
        /** {@inheritDoc} */
        public String getName() { return "size"; }
    },
    
    /** Returns the readable representation of a date. */
    CALENDAR_FULLSTRING {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((GregorianCalendar) argument).getTime().toString(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Calendar.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "full date"; }
    },
    
    /** Returns the name of the key that was pressed. */
    KEYEVENT_KEYNAME {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return KeyEvent.getKeyText(((KeyStroke) argument).getKeyCode()); }
        /** {@inheritDoc} */
        public Class appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "key name"; }
    },
    
    /** Returns the state of the control key for a key press event. */
    KEYEVENT_CTRLSTATE {
        /** {@inheritDoc} */
        public Object get(final Object argument) {
            return Boolean.valueOf((((KeyStroke) argument).getModifiers() & KeyEvent.CTRL_DOWN_MASK) != 0);
        }
        /** {@inheritDoc} */
        public Class appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        public Class getType() { return Boolean.class; }
        /** {@inheritDoc} */
        public String getName() { return "control key state"; }
    },
    
    /** Returns the state of the shift key for a key press event. */
    KEYEVENT_SHIFTSTATE {
        /** {@inheritDoc} */
        public Object get(final Object argument) {
            return Boolean.valueOf((((KeyStroke) argument).getModifiers() & KeyEvent.SHIFT_DOWN_MASK) != 0);
        }
        /** {@inheritDoc} */
        public Class appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        public Class getType() { return Boolean.class; }
        /** {@inheritDoc} */
        public String getName() { return "shift key state"; }
    },
    
    /** Returns the state of the shift key for a key press event. */
    KEYEVENT_ALTSTATE {
        /** {@inheritDoc} */
        public Object get(final Object argument) {
            return Boolean.valueOf((((KeyStroke) argument).getModifiers() & KeyEvent.ALT_DOWN_MASK) != 0);
        }
        /** {@inheritDoc} */
        public Class appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        public Class getType() { return Boolean.class; }
        /** {@inheritDoc} */
        public String getName() { return "alt key state"; }
    },
    
    /** Returns the host of the query. */
    QUERY_HOST {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Query) argument).getHost(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Query.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "host"; }
    },
    
    /** Returns the host of the query. */
    QUERY_NICK {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Query) argument).toString(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Query.class; }
        /** {@inheritDoc} */
        public Class getType() { return String.class; }
        /** {@inheritDoc} */
        public String getName() { return "nick"; }
    },
    
    /** Returns the notification colour of the query. */
    QUERY_COLOUR {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((Query) argument).getNotification(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return Query.class; }
        /** {@inheritDoc} */
        public Class getType() { return Color.class; }
        /** {@inheritDoc} */
        public String getName() { return "notification colour"; }
    },
    
    /** Returns the notification colour of the window. */
    WINDOW_COLOUR {
        /** {@inheritDoc} */
        public Object get(final Object argument) { return ((FrameContainer) argument).getNotification(); }
        /** {@inheritDoc} */
        public Class appliesTo() { return FrameContainer.class; }
        /** {@inheritDoc} */
        public Class getType() { return Color.class; }
        /** {@inheritDoc} */
        public String getName() { return "notification colour"; }
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
