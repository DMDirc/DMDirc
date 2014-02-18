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

package com.dmdirc.interfaces;

import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.util.Collection;

/**
 * Interface for objects interested in receiving updates pertaining to a channel's list of active
 * users (the 'nicklist').
 *
 * @since 0.6.5
 */
public interface NicklistListener {

    /**
     * Called to indicate the client list has been extensively updated, and any cached data should
     * be discarded and replaced with the specified set of clients.
     *
     * @param clients The new set of clients for the channel
     */
    void clientListUpdated(Collection<ChannelClientInfo> clients);

    /**
     * Called to indicate a member of the channel has had their nicklist entry changed in some
     * manner, and their display text, colours, etc, should be re-read from the object.
     */
    void clientListUpdated();

    /**
     * Called to indicate a new client has been added to the nicklist
     *
     * @param client The new client that has been added
     */
    void clientAdded(ChannelClientInfo client);

    /**
     * Called to indicate a client has been removed from the nicklist
     *
     * @param client The client that has been removed
     */
    void clientRemoved(ChannelClientInfo client);

}
