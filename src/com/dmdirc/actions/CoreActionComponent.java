/*
 * Copyright (c) 2006-2014 DMDirc Developers
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
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.messages.Styliser;

import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.KeyStroke;

/**
 * A CoreActionComponent represents a component of some object that the user can
 * use as the subject of a condition within an action.
 */
public enum CoreActionComponent implements ActionComponent {

    /** Returns the name of the server. */
    SERVER_NAME {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Connection) arg).getAddress(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "name"; }
    },

    /** Returns the network of the server. */
    SERVER_NETWORK {
        /** {@inheritDoc} */
        @Override
        @ComponentOptions(requireConnected = true)
        public Object get(final Object arg) { return ((Connection) arg).getNetwork(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "network"; }
    },

    /**
     * Returns the protocol of the server.
     *
     * @since 0.6.4
     */
    SERVER_PROTOCOL {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Connection) arg).getProtocol(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "protocol"; }
    },

    /** Returns the away reason for the server. */
    SERVER_MYAWAYREASON {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Connection) arg).getAwayMessage(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "away reason"; }
    },

    /** Returns the channel umodes for the server. */
    SERVER_CHANNELUMODES {
        /** {@inheritDoc} */
        @Override
        @ComponentOptions(requireConnected = true)
        public Object get(final Object arg) { return ((Connection) arg).getParser().getChannelUserModes(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "list of channel usermodes"; }
    },

    /** Returns the nickname for the server. */
    SERVER_MYNICKNAME {
        /** {@inheritDoc} */
        @Override
        @ComponentOptions(requireConnected = true)
        public Object get(final Object arg) {
            final Connection server = (Connection) arg;

            if (server == null || server.getParser() == null) {
                Logger.appError(ErrorLevel.LOW, "SERVER_MYNICKNAME.get() called with null element",
                        new UnsupportedOperationException(
                        server == null ? "Server was null"
                            : server.getParser() == null ? "Parser was null"
                            : "Unknown"
                        ));

                return "null";
            } else {
                return server.getParser().getLocalClient().getNickname();
            }
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "nickname"; }
    },

    /** Returns the name of the server. */
    SERVER_PROFILE {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Connection) arg).getProfile(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return ConfigProvider.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "profile"; }
    },

    /** Returns the name of the channel. */
    CHANNEL_NAME {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Channel) arg).getChannelInfo().getName(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Channel.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "name"; }
    },

    /** Returns the notification colour of the channel. */
    CHANNEL_COLOUR {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Channel) arg).getNotification(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Channel.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Colour.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "notification colour"; }
    },

    /** Returns the name of a client. */
    CLIENT_NAME {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((ClientInfo) arg).getNickname(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return ClientInfo.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "nickname"; }
    },

    /** Returns the host of a client. */
    CLIENT_HOST {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((ClientInfo) arg).getHostname(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return ClientInfo.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "host"; }
    },

    /** Returns the name of a client. */
    USER_NAME {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((ChannelClientInfo) arg).getClient().getNickname(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "nickname"; }
    },

    /** Returns the modes of a client. */
    USER_MODES {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((ChannelClientInfo) arg).getAllModes(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "modes"; }
    },

    /** Returns the host of a client. */
    USER_HOST {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((ChannelClientInfo) arg).getClient().getHostname(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "host"; }
    },

    /** Returns the number of common channels the client is on. */
    USER_COMCHANS {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return Integer.valueOf(((ChannelClientInfo) arg).getClient().getChannelCount()); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return ChannelClientInfo.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "number of common channels"; }
    },

    /** Returns the content of a string. */
    STRING_STRING {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return arg; }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "content"; }
    },

    /** Returns the content of a string, stripped of formatting. */
    STRING_STRIPPED {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return Styliser.stipControlCodes((String) arg); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "content (without formatting)"; }
    },

    /** Returns the length of a string. */
    STRING_LENGTH {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((String) arg).length(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "length"; }
    },

    /** Returns the size of a string array. */
    STRINGARRAY_LENGTH {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return Integer.valueOf(((String[]) arg).length); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return String[].class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "size"; }
    },

    /** Returns the readable representation of a date. */
    CALENDAR_FULLSTRING {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((GregorianCalendar) arg).getTime().toString(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Calendar.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "full date"; }
    },

    /** Returns the name of the key that was pressed. */
    KEYEVENT_KEYNAME {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return KeyEvent.getKeyText(((KeyStroke) arg).getKeyCode()); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "key name"; }
    },

    /** Returns the state of the control key for a key press event. */
    KEYEVENT_CTRLSTATE {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) {
            return Boolean.valueOf((((KeyStroke) arg).getModifiers() & KeyEvent.CTRL_DOWN_MASK) != 0);
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Boolean.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "control key state"; }
    },

    /** Returns the state of the shift key for a key press event. */
    KEYEVENT_SHIFTSTATE {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) {
            return Boolean.valueOf((((KeyStroke) arg).getModifiers() & KeyEvent.SHIFT_DOWN_MASK) != 0);
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Boolean.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "shift key state"; }
    },

    /** Returns the state of the shift key for a key press event. */
    KEYEVENT_ALTSTATE {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) {
            return Boolean.valueOf((((KeyStroke) arg).getModifiers() & KeyEvent.ALT_DOWN_MASK) != 0);
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return KeyStroke.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Boolean.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "alt key state"; }
    },

    /** Returns the host of the query. */
    QUERY_HOST {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Query) arg).getHost(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Query.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "host"; }
    },

    /** Returns the host of the query. */
    QUERY_NICK {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Query) arg).getName(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Query.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "nick"; }
    },

    /** Returns the notification colour of the query. */
    QUERY_COLOUR {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((Query) arg).getNotification(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Query.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Colour.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "notification colour"; }
    },

    /** The name of a window. */
    WINDOW_NAME {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((FrameContainer) arg).getName(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return FrameContainer.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "name"; }
    },

    /** Returns the notification colour of the window. */
    WINDOW_COLOUR {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((FrameContainer) arg).getNotification(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return FrameContainer.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Colour.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "notification colour"; }
    },

    /**
     * Returns the server of the window.
     *
     * @since 0.6.4
     */
    WINDOW_SERVER {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) {
            return ((Window) arg).getContainer().getConnection();
        }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Window.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Connection.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "server"; }
    },

    /** Returns the name of an identity. */
    IDENTITY_NAME {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return ((ConfigProvider) arg).getName(); }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return ConfigProvider.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return String.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "name"; }
    },

    /** Returns the value of an integer. */
    INTEGER_VALUE {
        /** {@inheritDoc} */
        @Override
        public Object get(final Object arg) { return (Integer) arg; }
        /** {@inheritDoc} */
        @Override
        public Class<?> appliesTo() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public Class<?> getType() { return Integer.class; }
        /** {@inheritDoc} */
        @Override
        public String getName() { return "value"; }
    };

}
