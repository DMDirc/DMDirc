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

}
