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

package com.dmdirc.events;

import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;

import com.google.common.collect.Lists;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * Fired when there is a major change to the list of users.
 */
public class NickListClientsChangedEvent extends NickListEvent {

    private final Collection<GroupChatUser> users;

    public NickListClientsChangedEvent(final LocalDateTime timestamp, final GroupChat channel,
            final Iterable<GroupChatUser> users) {
        super(timestamp, channel);
        this.users = Lists.newArrayList(users);
    }

    public NickListClientsChangedEvent(final GroupChat channel,
            final Iterable<GroupChatUser> users) {
        super(channel);
        this.users = Lists.newArrayList(users);
    }

    public Collection<GroupChatUser> getUsers() {
        return Collections.unmodifiableCollection(users);
    }
}
