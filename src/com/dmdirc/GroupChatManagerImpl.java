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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatManager;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Manages group chats for a {@link Connection}.
 */
public class GroupChatManagerImpl implements GroupChatManager {

    private final Connection connection;

    /** Factory to use for creating channels. */
    private final ChannelFactory channelFactory;

    /** Factory to use to create new identities. */
    private final IdentityFactory identityFactory;

    /** Open channels that currently exist on the server. */
    private final ChannelMap channels = new ChannelMap();

    /** A set of channels we want to join without focusing. */
    private final Collection<String> backgroundChannels = new HashSet<>();

    private final ScheduledExecutorService executorService;

    /** The future used when a who timer is scheduled. */
    private ScheduledFuture<?> whoTimerFuture;

    public GroupChatManagerImpl(final Connection connection,
            final IdentityFactory identityFactory,
            final ChannelFactory channelFactory,
            final ScheduledExecutorService executorService) {
        this.connection = connection;
        this.identityFactory = identityFactory;
        this.channelFactory = channelFactory;
        this.executorService = executorService;
    }

    @Override
    public Optional<GroupChat> getChannel(final String channel) {
        return channels.get(channel).map(c -> (GroupChat) c);
    }

    @Override
    public String getChannelPrefixes() {
        return connection.getParser().map(Parser::getChannelPrefixes).orElse("#%");
    }

    @Override
    public Collection<GroupChat> getChannels() {
        return channels.getAll().parallelStream()
                .map(c -> (GroupChat) c)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValidChannelName(final String channelName) {
        return getChannel(channelName).isPresent()
                || connection.getParser().map(p -> p.isValidChannelName(channelName)).orElse(false);
    }

    @Override
    public void join(final ChannelJoinRequest... requests) {
        join(true, requests);
    }

    @Override
    public void join(final boolean focus, final ChannelJoinRequest... requests) {
        final Optional<Parser> parser = connection.getParser();
        parser.ifPresent(p -> {
            final Collection<ChannelJoinRequest> pending = new ArrayList<>();

            for (ChannelJoinRequest request : requests) {
                connection.removeInvites(request.getName());

                final String name;
                if (p.isValidChannelName(request.getName())) {
                    name = request.getName();
                } else {
                    name = p.getChannelPrefixes().substring(0, 1) + request.getName();
                }

                if (!getChannel(name).map(GroupChat::isOnChannel).orElse(false)) {
                    if (!focus) {
                        backgroundChannels.add(name);
                    }

                    pending.add(request);
                }
            }

            p.joinChannels(pending.toArray(new ChannelJoinRequest[pending.size()]));
        });
    }

    public void delChannel(final String chan) {
        connection.getWindowModel().getTabCompleter().removeEntry(TabCompletionType.CHANNEL, chan);
        channels.remove(chan);
    }

    public void addChannel(final ChannelInfo chan) {
        addChannel(chan,
                !backgroundChannels.contains(chan.getName())
                        || connection.getWindowModel().getConfigManager()
                        .getOptionBool("general", "hidechannels"));
    }

    public void addChannel(final ChannelInfo chan, final boolean focus) {
        if (connection.getState() == ServerState.CLOSING) {
            // Can't join channels while the server is closing
            return;
        }

        backgroundChannels.remove(chan.getName());

        final Optional<Channel> channel = channels.get(chan.getName());
        if (channel.isPresent()) {
            channel.get().setChannelInfo(chan);
            channel.get().selfJoin();
        } else {
            final ConfigProviderMigrator channelConfig = identityFactory.createMigratableConfig(
                    connection.getProtocol(), connection.getIrcd(), connection.getNetwork(),
                    connection.getAddress(), chan.getName());
            final Channel newChan = channelFactory.getChannel(
                    (Server) connection, // TODO: Icky. Make Channel not care?
                    chan, channelConfig);
            connection.getWindowModel().getTabCompleter().addEntry(TabCompletionType.CHANNEL,
                    chan.getName());
            channels.add(newChan);
        }
    }

    public void handleDisconnect() {
        channels.resetAll();
        backgroundChannels.clear();

        if (connection.getWindowModel().getConfigManager()
                .getOptionBool("general", "closechannelsonquit")) {
            channels.closeAll();
        }
    }

    public void closeAll() {
        channels.closeAll();
    }

    public void addLineToAll(final String messageType, final Date date, final Object... args) {
        channels.addLineToAll(messageType, date, args);
    }

    public void handleSocketClosed() {
        if (whoTimerFuture != null) {
            whoTimerFuture.cancel(false);
        }

        channels.resetAll();

        if (connection.getWindowModel().getConfigManager()
                .getOptionBool("general", "closechannelsondisconnect")) {
            channels.closeAll();
        }
    }

    public void handleConnected() {
        channels.setStringConverter(connection.getParser().get().getStringConverter());

        final List<ChannelJoinRequest> requests = new ArrayList<>();
        if (connection.getWindowModel().getConfigManager()
                .getOptionBool("general", "rejoinchannels")) {
            requests.addAll(channels.asJoinRequests());
        }
        join(requests.toArray(new ChannelJoinRequest[requests.size()]));

        final int whoTime = connection.getWindowModel().getConfigManager()
                .getOptionInt("general", "whotime");
        whoTimerFuture = executorService.scheduleAtFixedRate(
                channels.getWhoRunnable(), whoTime, whoTime, TimeUnit.MILLISECONDS);
    }

}
