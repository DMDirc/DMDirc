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

import java.util.Map;

/**
 * Describes a client that is present on a channel and provides methods to
 * interact with it.
 *
 * @since 0.6.3m2
 * @author chris
 */
public interface ChannelClientInfo extends Comparable<ChannelClientInfo> {

    /**
     * Retrieves the {@link ClientInfo} object which this object corresponds
     * to.
     * 
     * @return The client info object which this object represents
     */
    ClientInfo getClient();

    /**
     * Retrieves the {@link ChannelInfo} object for the channel which this
     * object is associated with.
     *
     * @return The corresponding ChannelInfo object
     */
    ChannelInfo getChannel();

    /**
     * Returns the most important mode that the client holds in its prefix
     * form (e.g. @, +, etc).
     *
     * @return The most important mode the client holds, or an empty string
     */
    String getImportantModePrefix();

    /**
     * Returns the most important mode that the client holds in its textual
     * form (e.g. o, v, etc)
     *
     * @return The most important mode the client holds, or an empty string
     */
    String getImportantMode();

    /**
     * Returns a list of all modes known to be held by the client, in their
     * textual for (e.g. o, v, etc)
     *
     * @return All modes the client holds, or an empty string
     */
    String getAllModes();

    /**
     * Retrieves a {@link Map} which can be used to store arbitrary data
     * about the channel client.
     *
     * @return A map used for storing arbitrary data
     */
    Map<Object, Object> getMap();

    /**
     * Kicks this client from the channel.
     *
     * @param message The kick message to use
     */
    void kick(String message);

}
