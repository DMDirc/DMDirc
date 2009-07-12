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

import com.dmdirc.parser.common.ChannelListModeItem;
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
     * Changes the topic of this channel.
     *
     * @param topic This channel's new topic
     */
    void setTopic(String topic);

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
     * Retrieves the value of the specified mode if it's set.
     *
     * @param mode The mode to retrieve
     * @return The value for the specified mode or an empty string if it's not set
     */
    String getMode(char mode);

    /**
     * Retrieves the known values for the specified list mode.
     *
     * @param mode The list mode to be retrieved
     * @return A collection of known list mode items
     */
    Collection<ChannelListModeItem> getListMode(char mode);

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
     * Sends a WHO request to get details about people who are on the channel.
     */
    void sendWho();

    /**
     * Adjust the modes on this channel. This method should queue modes to be
     * sent in one go, according to the configuration/behaviour of the backend
     * system. If fewer modes are altered than the queue accepts, the
     * flushModes() method must be called.
     *
     * @param add Whether to add or remove the specified mode
     * @param mode Character The character representation of the mode to be changed
     * @param parameter Optional parameter needed to make change
     */
    void alterMode(boolean add, final Character mode, String parameter);

    /**
     * Flushes any mode changes that have been queued by the
     * {@link #alterMode(boolean, Character, String)} method.
     */
    void flushModes();

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
     * @return A {@link ChannelClientInfo} object corresponding to the client,
     * or null if none was found
     */
    ChannelClientInfo getChannelClient(String client);

    /**
     * Retrieves a channel client information object corresponding to the
     * specified client. If the client doesn't exist and the value of
     * <code>create</code> is <code>true</code>, a new fake client is created.
     *
     * @param client The name or other textual representation of the client
     * @param create Whether or not to create the client if it doesn't exist
     * @return A {@link ChannelClientInfo} object corresponding to the client
     */
    ChannelClientInfo getChannelClient(String client, boolean create);

    /**
     * Retrieves a collection of all known clients that are present on the
     * channel.
     *
     * @return A collection of known channel clients
     */
    Collection<ChannelClientInfo> getChannelClients();

    /**
     * Retrieves the number of clients known to exist in this channel.
     *
     * @return The number of clients known in this channel
     */
    int getChannelClientCount();

    /**
     * Retrieves the parser which created this ChannelInfo.
     *
     * @return This ChannelInfo's parser
     */
    Parser getParser();

}
