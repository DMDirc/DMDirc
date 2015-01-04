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

package com.dmdirc;

import com.dmdirc.interfaces.InviteManager;
import com.dmdirc.interfaces.User;

import java.util.Date;

/**
 * Model for a channel invitation.
 */
public class Invite {

    /** The manager associated with this invite. */
    private final InviteManager inviteManager;
    /** The channel this invite is for. */
    private final String channel;
    /** The time this invite was created. */
    private final long timestamp;
    /** The source of this invite. */
    private final User source;

    /**
     * Creates a new instance of Invite.
     *
     * @param inviteManager The manager that this invite is associated with.
     * @param channel    The channel that this invite is for
     * @param source     The source of this invite
     */
    public Invite(final InviteManager inviteManager, final String channel, final User source) {
        this.inviteManager = inviteManager;
        this.channel = channel;
        this.source = source;
        this.timestamp = new Date().getTime();
    }

    public String getChannel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Retrieves the source of this invite.
     *
     * @return This invite's source
     */
    public User getSource() {
        return source;
    }

    /**
     * Join the channel that belongs to this invite.
     */
    public void accept() {
        inviteManager.acceptInvites(this);
    }

    /**
     * Decline this invite removing it from the invite list.
     */
    public void decline() {
        inviteManager.removeInvite(this);
    }

}
