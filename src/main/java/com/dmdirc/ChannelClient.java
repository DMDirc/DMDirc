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

import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayPropertyMap;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.util.Comparator;
import java.util.Optional;

/**
 * Implementation of a {@link GroupChatUser}.
 */
public class ChannelClient implements GroupChatUser {

    private final User user;
    private final GroupChat groupChat;
    private final ChannelClientInfo clientInfo;
    private final DisplayPropertyMap properties;

    public ChannelClient(final User user, final GroupChat groupChat,
            final ChannelClientInfo clientInfo) {
        this.user = user;
        this.groupChat = groupChat;
        this.clientInfo = clientInfo;
        properties = new DisplayPropertyMap();
        properties.put(DisplayProperty.LINK_USER, user);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public GroupChat getGroupChat() {
        return groupChat;
    }

    @Override
    public String getImportantMode() {
        return clientInfo.getImportantModePrefix();
    }

    @Override
    public String getAllModes() {
        return clientInfo.getAllModes();
    }

    @Override
    public String getNickname() {
        return getUser().getNickname();
    }

    @Override
    public String getModePrefixedNickname() {
        return getImportantMode() + getNickname();
    }

    @Override
    public Optional<String> getUsername() {
        return getUser().getUsername();
    }

    @Override
    public Optional<String> getHostname() {
        return getUser().getHostname();
    }

    @Override
    public Optional<String> getRealname() {
        return getUser().getRealname();
    }

    public ChannelClientInfo getClientInfo() {
        return clientInfo;
    }

    @Override
    public Comparator<String> getModeComparator() {
        return clientInfo.getImportantModeComparator();
    }

    @Override
    public <T> void setDisplayProperty(final DisplayProperty<T> property, final T value) {
        properties.put(property, value);
    }

    @Override
    public <T> Optional<T> getDisplayProperty(final DisplayProperty<T> property) {
        return properties.get(property);
    }

    @Override
    public <T> void removeDisplayProperty(final DisplayProperty<T> property) {
        properties.remove(property);
    }

    @Override
    public DisplayPropertyMap getDisplayProperties() {
        return properties;
    }
}
