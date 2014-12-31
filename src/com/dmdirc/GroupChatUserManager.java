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

package com.dmdirc;

import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class GroupChatUserManager {

    private final GroupChatUserFactory groupChatUserFactory;
    private final Map<ChannelClientInfo, GroupChatUser> userCache;

    @Inject
    public GroupChatUserManager(final GroupChatUserFactory groupChatUserFactory) {
        this.groupChatUserFactory = groupChatUserFactory;
        userCache = new HashMap<>();
    }

    public GroupChatUser getUserFromClient(final ChannelClientInfo client,
            final GroupChat groupChat) {
        return getUserFromClient(client, groupChat.getConnection().get()
                .getUser(client.getClient().getNickname()), groupChat);
    }

    public GroupChatUser getUserFromClient(final ChannelClientInfo client,
            final User user, final GroupChat groupChat) {
        userCache.putIfAbsent(client, groupChatUserFactory.getGroupChatUser(user, groupChat, client));
        return userCache.get(client);
    }
}
