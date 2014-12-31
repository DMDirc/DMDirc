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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.User;
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.interfaces.ClientInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Implementation of a {@link User}.
 */
public class Client implements User {

    private final Collection<GroupChat> groupChats;
    private final Connection connection;
    private final ClientInfo clientInfo;
    private String nickname;
    private Optional<String> username;
    private Optional<String> hostname;
    private Optional<String> realname;
    private Optional<String> awayMessage;

    public Client(final String nickname, final Connection connection, final ClientInfo clientInfo) {
        this(nickname, connection, Optional.empty(), Optional.empty(), Optional.empty(), clientInfo);
    }

    public Client(final String nickname, final Connection connection,
            final Optional<String> username,
            final Optional<String> hostname,
            final Optional<String> realname,
            final ClientInfo clientInfo) {
        this.nickname = nickname;
        this.connection = connection;
        this.username = username;
        this.hostname = hostname;
        this.realname = realname;
        this.clientInfo = clientInfo;
        groupChats = new ArrayList<>();
        awayMessage = Optional.empty();
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    @Override
    public Optional<String> getUsername() {
        return username;
    }

    @Override
    public void setUsername(final Optional<String> username) {
        this.username = username;
    }

    @Override
    public Optional<String> getHostname() {
        return hostname;
    }

    @Override
    public void setHostname(final Optional<String> hostname) {
        this.hostname = hostname;
    }

    @Override
    public Optional<String> getRealname() {
        return realname;
    }

    @Override
    public void setRealname(final Optional<String> realname) {
        this.realname = realname;
    }

    @Override
    public Collection<GroupChat> getGroupChats() {
        return Collections.unmodifiableCollection(groupChats);
    }

    @Override
    public void addGroupChat(final GroupChat groupChat) {
        groupChats.add(groupChat);
    }

    @Override
    public void delGroupChat(final GroupChat groupChat) {
        groupChats.remove(groupChat);
    }

    @Override
    public Optional<String> getAwayMessage() {
        return awayMessage;
    }

    @Override
    public void setAwayMessage(final Optional<String> awayMessage) {
        this.awayMessage = awayMessage;
    }

    @Override
    public AwayState getAwayState() {
        return clientInfo.getAwayState();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
}
