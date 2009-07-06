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

package com.dmdirc.parser.interfaces;

import com.dmdirc.parser.irc.IRCStringConverter;
import java.util.Collection;

/**
 * A parser connects to a back-end chat system and handles all communication
 * with it.
 *
 * @since 0.6.3m2
 * @author chris
 */
public interface Parser extends Runnable {

    /**
     * Disconnect from server. This method will quit and automatically close the
     * socket without waiting for the server.
     *
     * @param message Reason for quitting.
     */
    void disconnect(String message);

    /**
     * Join a channel with no key.
     *
     * @param channel Name of channel to join
     */
    void joinChannel(String channel);

    /**
     * Joins a channel with the specified key.
     *
     * @param channel Name of channel to join
     * @param key The key required to join the channel
     */
    void joinChannel(String channel, String key);

    /**
     * Retrieves a channel information object for the specified channel.
     *
     * @param channel Name of the channel to retrieve an information object for
     * @return A corresponding channel info object
     */
    ChannelInfo getChannel(String channel);

    /**
     * Retrieves a collection of all known channels.
     *
     * @return A collection of known channels
     */
    Collection<? extends ChannelInfo> getChannels();

    /**
     * Set the IP address that this parser will bind to
     *
     * @param ip IP to bind to
     */
    void setBindIP(String ip);

    /**
     * Determines the maximimum length a message of the specified type may be.
     *
     * @param type Type of message (eg PRIVMSG)
     * @param target Target of message (eg channel name)
     * @return The maximum length of the message
     */
    int getMaxLength(String type, String target);

    /**
     * Returns a {@link ClientInfo} object which represents the locally
     * connected client.
     *
     * @return An info object for the local client
     */
    LocalClientInfo getLocalClient();

    /**
     * Sends a raw message directly to the backend system. The message will
     * need to be of the appropriate format for whatever system is in use.
     * 
     * @param message The message to be sent
     */
    void sendRawMessage(String message);

    /**
     * Retrieves an object that can be used to convert between upper- and lower-
     * case strings in the relevant charset for the backend system.
     *
     * @return A string convertor for this parser
     */
    IRCStringConverter getStringConverter();

    /**
     * Determines whether the specified channel name is valid or not for this
     * parser.
     *
     * @param name The name of the channel to be tested
     * @return True if the channel name is valid, false otherwise
     */
    boolean isValidChannelName(String name);

}
