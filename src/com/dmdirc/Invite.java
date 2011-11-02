/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import java.util.Date;

import lombok.Getter;

/**
 * Model for a channel invitation.
 */
@SuppressWarnings("PMD.UnusedPrivateField")
public class Invite {

    /** The server this invite was on. */
    @Getter
    private final Server server;

    /** The channel this invite is for. */
    @Getter
    private final String channel;

    /** The time this invite was created. */
    @Getter
    private final long timestamp;

    /** The source of this invite. */
    private final String source;

    /**
     * Creates a new instance of Invite.
     *
     * @param server The server that this invite was received for
     * @param channel The channel that this invite is for
     * @param source The source of this invite
     */
    public Invite(final Server server, final String channel, final String source) {
        this.server = server;
        this.channel = channel;
        this.source = source;
        this.timestamp = new Date().getTime();
    }

    /**
     * Retrieves the source of this invite.
     *
     * @return This invite's source
     * @see Server#parseHostmask(java.lang.String)
     */
    public String[] getSource() {
        return server.parseHostmask(source);
    }

    /**
     * Join the channel that belongs to this invite.
     */
    public void accept() {
        server.acceptInvites(this);
    }

    /**
     * Decline this invite removing it from the invite list.
     */
    public void decline() {
        server.removeInvite(this);
    }
}
