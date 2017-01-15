/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.interfaces;

import com.dmdirc.parser.common.ChannelJoinRequest;

import java.util.Collection;
import java.util.Optional;

/**
 * Handles all {@link GroupChat}s on a {@link Connection}
 */
public interface GroupChatManager {

    /**
     * Retrieves the specified channel belonging to this server.
     *
     * @param channel The channel to be retrieved
     *
     * @return The appropriate channel object
     */
    Optional<GroupChat> getChannel(final String channel);

    /**
     * Retrieves the possible channel prefixes in use on this server.
     *
     * @return This server's possible channel prefixes
     */
    String getChannelPrefixes();

    /**
     * Gets a collection of all channels on this connection.
     *
     * @return collection of channels belonging to this connection
     */
    Collection<GroupChat> getChannels();

    /**
     * Determines if the specified channel name is valid. A channel name is valid if we already have
     * an existing Channel with the same name, or we have a valid parser instance and the parser
     * says it's valid.
     *
     * @param channelName The name of the channel to test
     *
     * @return True if the channel name is valid, false otherwise
     */
    boolean isValidChannelName(final String channelName);

    /**
     * Attempts to join the specified channels. If channels with the same name already exist, they
     * are (re)joined and their windows activated.
     *
     * @param requests The channel join requests to process
     *
     * @since 0.6.4
     */
    void join(final ChannelJoinRequest... requests);

    /**
     * Attempts to join the specified channels. If channels with the same name already exist, they
     * are (re)joined.
     *
     * @param focus    Whether or not to focus any new channels
     * @param requests The channel join requests to process
     *
     * @since 0.6.4
     */
    void join(final boolean focus, final ChannelJoinRequest... requests);

}
