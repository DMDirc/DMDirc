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

import com.dmdirc.Invite;

import java.util.List;

/**
 * Handles invites on a {@link Connection}
 */
public interface InviteManager {
    /**
     * Attempts to accept the specified invites, and join the corresponding channels.
     *
     * @param invites The invites to process
     *
     * @since 0.6.4
     */
    void acceptInvites(Invite... invites);

    /**
     * Attempts to accept all active invites for this server, and join the corresponding channels.
     *
     * @since 0.6.4
     */
    void acceptInvites();

    /**
     * Adds an invite to this server, and fires the appropriate listeners.
     *
     * @param invite The invite to be added
     */
    void addInvite(Invite invite);

    /**
     * Returns the list of invites for this server.
     *
     * @return Invite list
     */
    List<Invite> getInvites();

    /**
     * Removes an invite from this server, and fires the appropriate listeners.
     *
     * @param invite The invite to be removed
     */
    void removeInvite(Invite invite);

    /**
     * Removes all invites for the specified channel.
     *
     * @param channel The channel to remove invites for
     */
    void removeInvites(String channel);

    /**
     * Removes all invites for all channels.
     */
    void removeInvites();
}
