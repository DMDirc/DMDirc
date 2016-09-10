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

import com.dmdirc.events.ChannelActionEvent;
import com.dmdirc.events.ChannelCtcpEvent;
import com.dmdirc.events.ChannelGotNamesEvent;
import com.dmdirc.events.ChannelGotTopicEvent;
import com.dmdirc.events.ChannelJoinEvent;
import com.dmdirc.events.ChannelKickEvent;
import com.dmdirc.events.ChannelListModesRetrievedEvent;
import com.dmdirc.events.ChannelMessageEvent;
import com.dmdirc.events.ChannelModeChangeEvent;
import com.dmdirc.events.ChannelModeNoticeEvent;
import com.dmdirc.events.ChannelModesDiscoveredEvent;
import com.dmdirc.events.ChannelNickChangeEvent;
import com.dmdirc.events.ChannelNoModesDiscoveredEvent;
import com.dmdirc.events.ChannelNoTopicEvent;
import com.dmdirc.events.ChannelNoticeEvent;
import com.dmdirc.events.ChannelPartEvent;
import com.dmdirc.events.ChannelQuitEvent;
import com.dmdirc.events.ChannelSelfModeChangeEvent;
import com.dmdirc.events.ChannelSelfNickChangeEvent;
import com.dmdirc.events.ChannelSelfPartEvent;
import com.dmdirc.events.ChannelTopicChangeEvent;
import com.dmdirc.events.ChannelTopicUnsetEvent;
import com.dmdirc.events.ChannelUserAwayEvent;
import com.dmdirc.events.ChannelUserBackEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.events.ChannelCTCPEvent;
import com.dmdirc.parser.events.ChannelListModeEvent;
import com.dmdirc.parser.events.ChannelNamesEvent;
import com.dmdirc.parser.events.ChannelTopicEvent;
import com.dmdirc.parser.events.OtherAwayStateEvent;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;

import com.google.common.base.Strings;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.engio.mbassy.listener.Handler;

/**
 * Handles events for channel objects.
 */
public class ChannelEventHandler extends EventHandler {

    /** The channel that owns this event handler. */
    private final Channel owner;
    /** Event bus to send events on. */
    private final EventBus eventBus;
    private final GroupChatUserManager groupChatUserManager;

    public ChannelEventHandler(final Channel owner, final EventBus eventBus,
            final GroupChatUserManager groupChatUserManager) {
        this.owner = owner;
        this.eventBus = eventBus;
        this.groupChatUserManager = groupChatUserManager;
    }

    @Nonnull
    @Override
    protected Connection getConnection() {
        return owner.getConnection().get();
    }

    /**
     * Determines if the specified client represents us.
     *
     * @param client The client to be tested
     *
     * @return True if the client is ourself, false otherwise.
     */
    protected boolean isMyself(final ChannelClientInfo client) {
        return getConnection().getParser().map(Parser::getLocalClient)
                .map(c -> client.getClient().equals(c)).orElse(false);
    }

    @Handler
    public void onChannelMessage(final com.dmdirc.parser.events.ChannelMessageEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        eventBus.publishAsync(new ChannelMessageEvent(
                event.getDate(),
                owner, groupChatUserManager.getUserFromClient(event.getClient(), owner),
                event.getMessage()));
    }

    @Handler
    public void onChannelGotNames(final ChannelNamesEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        owner.setClients(event.getChannel().getChannelClients().stream()
                .map(client -> groupChatUserManager.getUserFromClient(client, owner))
                .collect(Collectors.toList()));
        eventBus.publishAsync(new ChannelGotNamesEvent(event.getDate(), owner));
    }

    @Handler
    public void onChannelTopic(final ChannelTopicEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        final ChannelInfo channel = event.getChannel();
        final LocalDateTime date = event.getDate();

        final Topic topic = Topic.create(channel.getTopic(),
                owner.getUser(getConnection().getUser(channel.getTopicSetter())).orElse(null),
                LocalDateTime.ofEpochSecond(channel.getTopicTime(), 0, ZoneOffset.UTC));

        if (event.isJoinTopic()) {
            if (Strings.isNullOrEmpty(channel.getTopic())) {
                eventBus.publishAsync(new ChannelNoTopicEvent(owner));
            } else {
                eventBus.publishAsync(new ChannelGotTopicEvent(owner, topic,
                        owner.getConnection().get().getUser(channel.getTopicSetter())));
            }
        } else {
            if (Strings.isNullOrEmpty(channel.getTopic())) {
                eventBus.publishAsync(new ChannelTopicUnsetEvent(date, owner,
                        owner.getUser(owner.getConnection().get()
                                .getUser(channel.getTopicSetter())).orElse(null)));
            } else {
                eventBus.publishAsync(new ChannelTopicChangeEvent(date, owner, topic,
                        topic.getClient().get()));

            }
        }

        final Optional<Topic> currentTopic = owner.getCurrentTopic();
        final boolean hasNewTopic = !Strings.isNullOrEmpty(channel.getTopic());
        if (!event.isJoinTopic()
                || !currentTopic.isPresent() && hasNewTopic
                || currentTopic.isPresent() && !channel.getTopic().equals(
                        owner.getCurrentTopic().get().getTopic())) {
            // Only add the topic if:
            //  - It's being set while we're in the channel (rather than discovered on join), or
            //  - We think the current topic is empty and are discovering a new one, or
            //  - The newly discovered topic is different to what we thought the current topic was.
            owner.addTopic(topic);
        }
    }

    @Handler
    public void onChannelJoin(final com.dmdirc.parser.events.ChannelJoinEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        eventBus.publish(new ChannelJoinEvent(
                event.getDate(), owner,
                groupChatUserManager.getUserFromClient(event.getClient(), owner)));
        owner.addClient(groupChatUserManager.getUserFromClient(event.getClient(), owner));
    }

    @Handler
    public void onChannelPart(final com.dmdirc.parser.events.ChannelPartEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        final ChannelClientInfo client = event.getClient();
        final LocalDateTime date = event.getDate();
        final String reason = event.getReason();

        if (isMyself(client)) {
            eventBus.publishAsync(new ChannelSelfPartEvent(date, owner,
                    groupChatUserManager.getUserFromClient(client, owner), reason));
        } else {
            eventBus.publishAsync(new ChannelPartEvent(date, owner,
                    groupChatUserManager.getUserFromClient(client, owner), reason));
        }
        owner.removeClient(groupChatUserManager.getUserFromClient(client, owner));
    }

    @Handler
    public void onChannelKick(final com.dmdirc.parser.events.ChannelKickEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }
        final ChannelClientInfo kickedClient = event.getKickedClient();

        eventBus.publishAsync(new ChannelKickEvent(
                event.getDate(), owner,
                groupChatUserManager.getUserFromClient(event.getClient(), owner),
                groupChatUserManager.getUserFromClient(kickedClient, owner), event.getReason()));
        owner.removeClient(groupChatUserManager.getUserFromClient(kickedClient, owner));
    }

    @Handler
    public void onChannelQuit(final com.dmdirc.parser.events.ChannelQuitEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        eventBus.publishAsync(new ChannelQuitEvent(
                event.getDate(), owner,
                groupChatUserManager.getUserFromClient(event.getClient(), owner),
                event.getReason()));
        owner.removeClient(groupChatUserManager.getUserFromClient(event.getClient(), owner));
    }

    @Handler
    public void onChannelAction(final com.dmdirc.parser.events.ChannelActionEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        eventBus.publishAsync(new ChannelActionEvent(
                event.getDate(), owner,
                groupChatUserManager.getUserFromClient(event.getClient(), owner),
                event.getMessage()));
    }

    @Handler
    public void onChannelNickChanged(final com.dmdirc.parser.events.ChannelNickChangeEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        final String oldNick = event.getOldNick();
        final ChannelClientInfo client = event.getClient();

        owner.renameClient(oldNick, client.getClient().getNickname());

        if (isMyself(client)) {
            eventBus.publishAsync(
                    new ChannelSelfNickChangeEvent(
                            event.getDate(),
                            owner, groupChatUserManager.getUserFromClient(client, owner), oldNick));
        } else {
            eventBus.publishAsync(
                    new ChannelNickChangeEvent(
                            event.getDate(),
                            owner, groupChatUserManager.getUserFromClient(client, owner), oldNick));
        }
    }

    @Handler
    public void onChannelModeChanged(final com.dmdirc.parser.events.ChannelModeChangeEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        final String host = event.getHost();
        final String modes = event.getModes();
        final ChannelClientInfo client = event.getClient();
        final LocalDateTime date = event.getDate();

        if (host.isEmpty()) {
            if (modes.length() <= 1) {
                eventBus.publishAsync(new ChannelNoModesDiscoveredEvent(date, owner));
            } else {
                eventBus.publishAsync(
                        new ChannelModesDiscoveredEvent(date, owner,modes));
            }
        } else if (isMyself(client)) {
            eventBus.publishAsync(new ChannelSelfModeChangeEvent(date, owner,
                    groupChatUserManager.getUserFromClient(client, owner), modes));
        } else {
            eventBus.publishAsync(new ChannelModeChangeEvent(date, owner,
                    groupChatUserManager.getUserFromClient(client, owner), modes));
        }

        owner.refreshClients();
    }

    @Handler
    public void onChannelCTCP(final ChannelCTCPEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        final ChannelClientInfo client = event.getClient();
        final String message = event.getMessage();
        final LocalDateTime date = event.getDate();
        final String type = event.getType();

        final ChannelCtcpEvent coreEvent = new ChannelCtcpEvent(date, owner,
                groupChatUserManager.getUserFromClient(client, owner),type, message);
        eventBus.publish(coreEvent);
        if (!coreEvent.isHandled()) {
            getConnection().sendCTCPReply(client.getClient().getNickname(), type, message);
        }
    }

    @Handler
    public void onAwayStateOther(final OtherAwayStateEvent event) {
        owner.getUser(owner.getConnection().get().getUser(event.getClient().getNickname()))
                .ifPresent(c -> {
                    if (event.getNewState() == AwayState.AWAY) {
                        eventBus.publishAsync(
                                new ChannelUserAwayEvent(event.getDate(), owner, c));
                    } else {
                        eventBus.publishAsync(
                                new ChannelUserBackEvent(event.getDate(), owner, c));
                    }
                });
    }

    @Handler
    public void onChannelNotice(final com.dmdirc.parser.events.ChannelNoticeEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        eventBus.publishAsync(new ChannelNoticeEvent(
                event.getDate(), owner,
                groupChatUserManager.getUserFromClient(event.getClient(), owner),
                event.getMessage()));
    }

    @Handler
    public void onChannelModeNotice(final com.dmdirc.parser.events.ChannelModeNoticeEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        eventBus.publishAsync(new ChannelModeNoticeEvent(
                event.getDate(), owner,
                groupChatUserManager.getUserFromClient(event.getClient(), owner), String.valueOf
                (event.getPrefix()), event.getMessage()));
    }

    @Handler
    public void onChannelGotListModes(final ChannelListModeEvent event) {
        if (!checkChannel(event.getChannel())) {
            return;
        }

        eventBus.publishAsync(new ChannelListModesRetrievedEvent(
                event.getDate(), owner, event.getMode()));
    }

    private boolean checkChannel(final ChannelInfo channelInfo) {
        return owner.getChannelInfo().equals(channelInfo);
    }

}
