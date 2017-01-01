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

    private final Connection connection;
    private final ClientInfo clientInfo;

    public Client(final Connection connection, final ClientInfo clientInfo) {
        this.connection = connection;
        this.clientInfo = clientInfo;
    }

    @Override
    public String getNickname() {
        return clientInfo.getNickname();
    }

    @Override
    public Optional<String> getUsername() {
        if (clientInfo.getUsername().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(clientInfo.getUsername());
        }
    }

    @Override
    public Optional<String> getHostname() {
        if (clientInfo.getHostname().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(clientInfo.getHostname());
        }
    }

    @Override
    public Optional<String> getRealname() {
        if (clientInfo.getRealname().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(clientInfo.getRealname());
        }
    }

    @Override
    public Collection<GroupChat> getGroupChats() {
        final Collection<GroupChat> channels = new ArrayList<>();
        clientInfo.getChannelClients().forEach(c -> connection.getGroupChatManager()
                .getChannel(c.getChannel().getName()).ifPresent(channels::add));
        return Collections.unmodifiableCollection(channels);
    }

    @Override
    public Optional<String> getAwayMessage() {
        if (clientInfo.getAwayReason().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(clientInfo.getAwayReason());
        }
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
