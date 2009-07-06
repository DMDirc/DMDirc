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

import java.util.Collection;

/**
 * Holds information about a channel and allows various operations to be
 * performed on the channel.
 *
 * @since 0.6.3m2
 * @author chris
 */
public interface ChannelInfo {

    /**
     * Returns the name of this channel.
     *
     * @return The name of this channel
     */
    String getName();

    /**
     * Retrieves the current topic or subject of this channel.
     *
     * @return This channel's topic
     */
    String getTopic();

    /**
     * Retrieves the time the current topic was set, as a unix timestamp.
     *
     * @return The time the current topic was set
     */
    long getTopicTime();

    /**
     * Retrieves a textual description of the person or entity that set the
     * channel topic.
     *
     * @return The person that set the current topic
     */
    String getTopicSetter();

    /**
     * Retrieves a textual representation of the modes currently set on this
     * channel. This includes boolean and parameter modes, but not list modes.
     *
     * @return The current channel modes
     */
    String getModes();

    /**
     * Sends the specified message to this channel.
     *
     * @param message The message to be sent
     */
    void sendMessage(final String message);

    /**
     * Sends the specified action to this channel.
     *
     * @param action The action to be sent
     */
    void sendAction(String action);

    /**
     * Parts this channel with the specified reason.
     *
     * @param reason The reason for parting
     */
    void part(String reason);

    /**
     * Retrieves a channel client information object corresponding to the
     * specified client.
     *
     * @param client The client whose channel client info object is being requested
     * @return A {@link ChannelClientInfo} object corresponding to the client
     */
    ChannelClientInfo getChannelClient(ClientInfo client);

    /**
     * Retrieves a channel client information object corresponding to the
     * specified client.
     *
     * @param client The name or other textual representation of the client
     * @return A {@link ChannelClientInfo} object corresponding to the client
     */
    ChannelClientInfo getChannelClient(String client);

    /**
     * Retrieves a collection of all known clients that are present on the
     * channel.
     *
     * @return A collection of known channel clients
     */
    Collection<ChannelClientInfo> getChannelClients();

    /**
     * Retrieves the parser which created this ChannelInfo.
     *
     * @return This ChannelInfo's parser
     */
    Parser getParser();

}
