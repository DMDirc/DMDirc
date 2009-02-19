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

package com.dmdirc;

import com.dmdirc.parser.irc.ClientInfo;

import java.util.Date;

/**
 * Model for a channel invitation.
 *
 * @author Chris
 */
public class Invite {
    
    /** The server this invite was on. */
    private final Server server;
    
    /** The channel this invite is for. */
    private final String channel;
    
    /** The time this invite was created. */
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
     * Retrieves the server that this invite is associated with.
     * 
     * @return This invite's server
     */
    public Server getServer() {
        return server;
    }

    /**
     * Retrieves the name of the channel that this invite is for.
     * 
     * @return This invite's channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Retrieves the timestamp that this invite was received at.
     * 
     * @return This invite's timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Retrieves the nickname, ident and hostname of this invite's source.
     * 
     * @return This invite's source
     */
    public String[] getSource() {
        return ClientInfo.parseHostFull(source);
    }
    
    /**
     * Join the channel that belongs to this invite.
     */
    public void accept() {
        server.getParser().joinChannel(channel);
        
        server.removeInvite(this);
    }
    
}
