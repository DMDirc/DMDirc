/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc;

import com.dmdirc.events.ServerInviteExpiredEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.InviteManager;
import com.dmdirc.parser.common.ChannelJoinRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages invites on a {@link Connection}.
 */
public class InviteManagerImpl implements InviteManager {

    /** A list of outstanding invites. */
    private final List<Invite> invites = new CopyOnWriteArrayList<>();

    /** The connection this manager works on. */
    private final Connection connection;

    public InviteManagerImpl(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addInvite(final Invite invite) {
        invites.stream()
                .filter(oldInvite -> oldInvite.getChannel().equals(invite.getChannel()))
                .forEach(this::removeInvite);

        invites.add(invite);
    }

    @Override
    public void acceptInvites(final Invite... invites) {
        final ChannelJoinRequest[] requests = new ChannelJoinRequest[invites.length];

        for (int i = 0; i < invites.length; i++) {
            requests[i] = new ChannelJoinRequest(invites[i].getChannel());
        }

        connection.getGroupChatManager().join(requests);
    }

    @Override
    public void acceptInvites() {
        acceptInvites(invites.toArray(new Invite[invites.size()]));
    }

    @Override
    public void removeInvites(final String channel) {
        new ArrayList<>(invites).stream().filter(invite -> invite.getChannel().equals(channel))
                .forEach(this::removeInvite);
    }

    @Override
    public void removeInvites() {
        new ArrayList<>(invites).forEach(this::removeInvite);
    }

    @Override
    public void removeInvite(final Invite invite) {
        invites.remove(invite);
        connection.getWindowModel().getEventBus().publishAsync(
                new ServerInviteExpiredEvent(connection, invite));
    }

    @Override
    public List<Invite> getInvites() {
        return Collections.unmodifiableList(invites);
    }

}
