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

package com.dmdirc.actions;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.PrivateChat;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.messages.Styliser;

import java.awt.AWTKeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Optional;

import javax.swing.KeyStroke;

/**
 * A CoreActionComponent represents a component of some object that the user can use as the subject
 * of a condition within an action.
 */
public enum CoreActionComponent implements ActionComponent {

    /** Returns the name of the server. */
    SERVER_NAME {
        @Override
        public Object get(final Object arg) {
            return ((Connection) arg).getAddress();
        }

        @Override
        public Class<?> appliesTo() {
            return Connection.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "name";
        }
    },
    /** Returns the network of the server. */
    SERVER_NETWORK {
        @Override
        @ComponentOptions(requireConnected = true)
        public Object get(final Object arg) {
            return ((Connection) arg).getNetwork();
        }

        @Override
        public Class<?> appliesTo() {
            return Connection.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "network";
        }
    },
    /**
     * Returns the protocol of the server.
     *
     * @since 0.6.4
     */
    SERVER_PROTOCOL {
        @Override
        public Object get(final Object arg) {
            return ((Connection) arg).getProtocol();
        }

        @Override
        public Class<?> appliesTo() {
            return Connection.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "protocol";
        }
    },
    /** Returns the away reason for the server. */
    SERVER_MYAWAYREASON {
        @Override
        public Object get(final Object arg) {
            return ((Connection) arg).getAwayMessage();
        }

        @Override
        public Class<?> appliesTo() {
            return Connection.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "away reason";
        }
    },
    /** Returns the channel umodes for the server. */
    SERVER_CHANNELUMODES {
        @Override
        @ComponentOptions(requireConnected = true)
        public Object get(final Object arg) {
            return ((Connection) arg).getParser().get().getChannelUserModes();
        }

        @Override
        public Class<?> appliesTo() {
            return Connection.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "list of channel usermodes";
        }
    },
    /** Returns the nickname for the server. */
    SERVER_MYNICKNAME {
        @Override
        @ComponentOptions(requireConnected = true)
        public Object get(final Object arg) {
            return Optional.ofNullable((Connection) arg)
                    .flatMap(Connection::getParser)
                    .map(Parser::getLocalClient)
                    .map(LocalClientInfo::getNickname)
                    .orElse("null");
        }

        @Override
        public Class<?> appliesTo() {
            return Connection.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "nickname";
        }
    },
    /** Returns the name of the server. */
    SERVER_PROFILE {
        @Override
        public Object get(final Object arg) {
            return ((Connection) arg).getProfile();
        }

        @Override
        public Class<?> appliesTo() {
            return Connection.class;
        }

        @Override
        public Class<?> getType() {
            return ConfigProvider.class;
        }

        @Override
        public String getName() {
            return "profile";
        }
    },
    /** Returns the name of the channel. */
    CHANNEL_NAME {
        @Override
        public Object get(final Object arg) {
            return ((Channel) arg).getChannelInfo().getName();
        }

        @Override
        public Class<?> appliesTo() {
            return Channel.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "name";
        }
    },
    /** Returns the name of a client. */
    CLIENT_NAME {
        @Override
        public Object get(final Object arg) {
            return ((ClientInfo) arg).getNickname();
        }

        @Override
        public Class<?> appliesTo() {
            return ClientInfo.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "nickname";
        }
    },
    /** Returns the host of a client. */
    CLIENT_HOST {
        @Override
        public Object get(final Object arg) {
            return ((ClientInfo) arg).getHostname();
        }

        @Override
        public Class<?> appliesTo() {
            return ClientInfo.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "host";
        }
    },
    /** Returns the name of a client. */
    USER_NAME {
        @Override
        public Object get(final Object arg) {
            return ((ChannelClientInfo) arg).getClient().getNickname();
        }

        @Override
        public Class<?> appliesTo() {
            return ChannelClientInfo.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "nickname";
        }
    },
    /** Returns the modes of a client. */
    USER_MODES {
        @Override
        public Object get(final Object arg) {
            return ((ChannelClientInfo) arg).getAllModes();
        }

        @Override
        public Class<?> appliesTo() {
            return ChannelClientInfo.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "modes";
        }
    },
    /** Returns the host of a client. */
    USER_HOST {
        @Override
        public Object get(final Object arg) {
            return ((ChannelClientInfo) arg).getClient().getHostname();
        }

        @Override
        public Class<?> appliesTo() {
            return ChannelClientInfo.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "host";
        }
    },
    /**
     * Returns the number of common channels the client is on.
     */
    USER_COMCHANS {
        @Override
        public Object get(final Object arg) {
            return ((ChannelClientInfo) arg).getClient().getChannelCount();
        }

        @Override
        public Class<?> appliesTo() {
            return ChannelClientInfo.class;
        }

        @Override
        public Class<?> getType() {
            return Integer.class;
        }

        @Override
        public String getName() {
            return "number of common channels";
        }
    },
    /** Returns the content of a string. */
    STRING_STRING {
        @Override
        public Object get(final Object arg) {
            return arg;
        }

        @Override
        public Class<?> appliesTo() {
            return String.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "content";
        }
    },
    /**
     * Returns the content of a string, stripped of formatting.
     */
    STRING_STRIPPED {
        @Override
        public Object get(final Object arg) {
            return Styliser.stipControlCodes((String) arg);
        }

        @Override
        public Class<?> appliesTo() {
            return String.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "content (without formatting)";
        }
    },
    /** Returns the length of a string. */
    STRING_LENGTH {
        @Override
        public Object get(final Object arg) {
            return ((CharSequence) arg).length();
        }

        @Override
        public Class<?> appliesTo() {
            return String.class;
        }

        @Override
        public Class<?> getType() {
            return Integer.class;
        }

        @Override
        public String getName() {
            return "length";
        }
    },
    /** Returns the size of a string array. */
    STRINGARRAY_LENGTH {
        @Override
        public Object get(final Object arg) {
            return ((String[]) arg).length;
        }

        @Override
        public Class<?> appliesTo() {
            return String[].class;
        }

        @Override
        public Class<?> getType() {
            return Integer.class;
        }

        @Override
        public String getName() {
            return "size";
        }
    },
    /**
     * Returns the readable representation of a date.
     */
    CALENDAR_FULLSTRING {
        @Override
        public Object get(final Object arg) {
            return ((Calendar) arg).getTime().toString();
        }

        @Override
        public Class<?> appliesTo() {
            return Calendar.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "full date";
        }
    },
    /**
     * Returns the name of the key that was pressed.
     */
    KEYEVENT_KEYNAME {
        @Override
        public Object get(final Object arg) {
            return KeyEvent.getKeyText(((AWTKeyStroke) arg).getKeyCode());
        }

        @Override
        public Class<?> appliesTo() {
            return KeyStroke.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "key name";
        }
    },
    /**
     * Returns the state of the control key for a key press event.
     */
    KEYEVENT_CTRLSTATE {
        @Override
        public Object get(final Object arg) {
            return (((AWTKeyStroke) arg).getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0;
        }

        @Override
        public Class<?> appliesTo() {
            return KeyStroke.class;
        }

        @Override
        public Class<?> getType() {
            return Boolean.class;
        }

        @Override
        public String getName() {
            return "control key state";
        }
    },
    /**
     * Returns the state of the shift key for a key press event.
     */
    KEYEVENT_SHIFTSTATE {
        @Override
        public Object get(final Object arg) {
            return (((AWTKeyStroke) arg).getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0;
        }

        @Override
        public Class<?> appliesTo() {
            return KeyStroke.class;
        }

        @Override
        public Class<?> getType() {
            return Boolean.class;
        }

        @Override
        public String getName() {
            return "shift key state";
        }
    },
    /**
     * Returns the state of the shift key for a key press event.
     */
    KEYEVENT_ALTSTATE {
        @Override
        public Object get(final Object arg) {
            return (((AWTKeyStroke) arg).getModifiers() & InputEvent.ALT_DOWN_MASK) != 0;
        }

        @Override
        public Class<?> appliesTo() {
            return KeyStroke.class;
        }

        @Override
        public Class<?> getType() {
            return Boolean.class;
        }

        @Override
        public String getName() {
            return "alt key state";
        }
    },
    /** Returns the host of the query. */
    QUERY_HOST {
        @Override
        public Object get(final Object arg) {
            return ((PrivateChat) arg).getHost();
        }

        @Override
        public Class<?> appliesTo() {
            return Query.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "host";
        }
    },
    /** Returns the host of the query. */
    QUERY_NICK {
        @Override
        public Object get(final Object arg) {
            return ((FrameContainer) arg).getName();
        }

        @Override
        public Class<?> appliesTo() {
            return Query.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "nick";
        }
    },
    /** The name of a window. */
    WINDOW_NAME {
        @Override
        public Object get(final Object arg) {
            return ((FrameContainer) arg).getName();
        }

        @Override
        public Class<?> appliesTo() {
            return FrameContainer.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "name";
        }
    },
    /**
     * Returns the server of the window.
     *
     * @since 0.6.4
     */
    WINDOW_SERVER {
        @Override
        public Object get(final Object arg) {
            return ((Window) arg).getContainer().getConnection().orElse(null);
        }

        @Override
        public Class<?> appliesTo() {
            return Window.class;
        }

        @Override
        public Class<?> getType() {
            return Connection.class;
        }

        @Override
        public String getName() {
            return "server";
        }
    },
    /** Returns the name of an identity. */
    IDENTITY_NAME {
        @Override
        public Object get(final Object arg) {
            return ((ConfigProvider) arg).getName();
        }

        @Override
        public Class<?> appliesTo() {
            return ConfigProvider.class;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "name";
        }
    },
    /** Returns the value of an integer. */
    INTEGER_VALUE {
        @Override
        public Object get(final Object arg) {
            return arg;
        }

        @Override
        public Class<?> appliesTo() {
            return Integer.class;
        }

        @Override
        public Class<?> getType() {
            return Integer.class;
        }

        @Override
        public String getName() {
            return "value";
        }
    }

}
