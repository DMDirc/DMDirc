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
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import com.dmdirc.parser.interfaces.callbacks.ChannelActionListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelKickListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelListModeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelModeNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelPartListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelQuitListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
import com.dmdirc.parser.interfaces.callbacks.OtherAwayStateListener;
import com.dmdirc.util.EventUtils;

import com.google.common.base.Strings;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Handles events for channel objects.
 */
public class ChannelEventHandler extends EventHandler implements
        ChannelMessageListener, ChannelNamesListener, ChannelTopicListener,
        ChannelJoinListener, ChannelPartListener, ChannelKickListener,
        ChannelQuitListener, ChannelActionListener, ChannelNickChangeListener,
        ChannelModeChangeListener, ChannelCtcpListener, OtherAwayStateListener,
        ChannelNoticeListener, ChannelModeNoticeListener, ChannelListModeListener {

    /** The channel that owns this event handler. */
    private final Channel owner;
    /** Event bus to send events on. */
    private final DMDircMBassador eventBus;
    private final GroupChatUserManager groupChatUserManager;

    public ChannelEventHandler(final Channel owner, final DMDircMBassador eventBus,
            final GroupChatUserManager groupChatUserManager) {
        super(eventBus);
        this.owner = owner;
        this.eventBus = eventBus;
        this.groupChatUserManager = groupChatUserManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends CallbackInterface> void addCallback(final CallbackManager cbm,
            final Class<T> type) {
        if (OtherAwayStateListener.class.equals(type)) {
            cbm.addCallback(type, (T) this);
        } else {
            cbm.addCallback(type, (T) this, owner.getChannelInfo().getName());
        }
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

    @Override
    public void onChannelMessage(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String message, final String host) {
        checkParser(parser);

        eventBus.publishAsync(new ChannelMessageEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner), message));
    }

    @Override
    public void onChannelGotNames(final Parser parser, final Date date, final ChannelInfo channel) {
        checkParser(parser);

        owner.setClients(channel.getChannelClients().stream()
                .map(client -> groupChatUserManager.getUserFromClient(client, owner))
                .collect(Collectors.toList()));
        eventBus.publishAsync(new ChannelGotNamesEvent(date.getTime(), owner));
    }

    @Override
    public void onChannelTopic(final Parser parser, final Date date,
            final ChannelInfo channel, final boolean isJoinTopic) {
        checkParser(parser);

        final Topic topic = Topic.create(channel.getTopic(),
                owner.getUser(getConnection().getUser(channel.getTopicSetter())).orElse(null),
                channel.getTopicTime());

        if (isJoinTopic) {
            if (Strings.isNullOrEmpty(channel.getTopic())) {
                eventBus.publishAsync(new ChannelNoTopicEvent(owner));
            } else {
                eventBus.publishAsync(new ChannelGotTopicEvent(owner, topic,
                        owner.getConnection().get().getUser(channel.getTopicSetter())));
            }
        } else {
            if (Strings.isNullOrEmpty(channel.getTopic())) {
                eventBus.publishAsync(new ChannelTopicUnsetEvent(date.getTime(), owner,
                        owner.getUser(owner.getConnection().get()
                                .getUser(channel.getTopicSetter())).orElse(null)));
            } else {
                eventBus.publishAsync(new ChannelTopicChangeEvent(date.getTime(), owner, topic,
                        topic.getClient().get()));

            }
        }

        final Optional<Topic> currentTopic = owner.getCurrentTopic();
        final boolean hasNewTopic = !Strings.isNullOrEmpty(channel.getTopic());
        if (!isJoinTopic
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

    @Override
    public void onChannelJoin(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client) {
        checkParser(parser);

        eventBus.publish(new ChannelJoinEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner)));
        owner.addClient(groupChatUserManager.getUserFromClient(client, owner));
    }

    @Override
    public void onChannelPart(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        if (isMyself(client)) {
            eventBus.publishAsync(new ChannelSelfPartEvent(date.getTime(), owner,
                    groupChatUserManager.getUserFromClient(client, owner), reason));
        } else {
            eventBus.publishAsync(new ChannelPartEvent(date.getTime(), owner,
                    groupChatUserManager.getUserFromClient(client, owner), reason));
        }
        owner.removeClient(groupChatUserManager.getUserFromClient(client, owner));
    }

    @Override
    public void onChannelKick(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo kickedClient, final ChannelClientInfo client,
            final String reason, final String host) {
        checkParser(parser);

        eventBus.publishAsync(new ChannelKickEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner),
                groupChatUserManager.getUserFromClient(kickedClient, owner), reason));
        owner.removeClient(groupChatUserManager.getUserFromClient(kickedClient, owner));
    }

    @Override
    public void onChannelQuit(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        eventBus.publishAsync(new ChannelQuitEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner), reason));
        owner.removeClient(groupChatUserManager.getUserFromClient(client, owner));
    }

    @Override
    public void onChannelAction(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        eventBus.publishAsync(new ChannelActionEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner), message));
    }

    @Override
    public void onChannelNickChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String oldNick) {
        checkParser(parser);

        owner.renameClient(oldNick, client.getClient().getNickname());

        if (isMyself(client)) {
            eventBus.publishAsync(
                    new ChannelSelfNickChangeEvent(date.getTime(), owner,
                            groupChatUserManager.getUserFromClient(client, owner), oldNick));
        } else {
            eventBus.publishAsync(
                    new ChannelNickChangeEvent(date.getTime(), owner,
                            groupChatUserManager.getUserFromClient(client, owner), oldNick));
        }
    }

    @Override
    public void onChannelModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String host,
            final String modes) {
        checkParser(parser);

        if (host.isEmpty()) {
            final ChannelModesDiscoveredEvent event = new ChannelModesDiscoveredEvent(
                    date.getTime(), owner, modes.length() <= 1 ? "" : modes);
            final String format = EventUtils.postDisplayable(eventBus, event,
                    modes.length() <= 1 ? "channelNoModes" : "channelModeDiscovered");
            owner.doNotification(date, format, modes.length() <= 1 ? "" : modes);
        } else if (isMyself(client)) {
            eventBus.publishAsync(new ChannelSelfModeChangeEvent(date.getTime(), owner,
                    groupChatUserManager.getUserFromClient(client, owner), modes));
        } else {
            eventBus.publishAsync(new ChannelModeChangeEvent(date.getTime(), owner,
                    groupChatUserManager.getUserFromClient(client, owner), modes));
        }

        owner.refreshClients();
    }

    @Override
    public void onChannelCTCP(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String type, final String message, final String host) {
        checkParser(parser);

        final ChannelCtcpEvent event = new ChannelCtcpEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner),type, message);
        eventBus.publish(event);
        if (!event.isHandled()) {
            getConnection().sendCTCPReply(client.getClient().getNickname(), type, message);
        }
    }

    @Override
    public void onAwayStateOther(final Parser parser, final Date date,
            final ClientInfo client, final AwayState oldState, final AwayState state) {
        checkParser(parser);

        owner.getUser(owner.getConnection().get().getUser(client.getNickname())).ifPresent(c -> {
            if (state == AwayState.AWAY) {
                eventBus.publishAsync(new ChannelUserAwayEvent(date.getTime(), owner, c));
            } else {
                eventBus.publishAsync(new ChannelUserBackEvent(date.getTime(), owner, c));
            }
        });
    }

    @Override
    public void onChannelNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String message, final String host) {
        checkParser(parser);

        eventBus.publishAsync(new ChannelNoticeEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner), message));
    }

    @Override
    public void onChannelModeNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final char prefix,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        eventBus.publishAsync(new ChannelModeNoticeEvent(date.getTime(), owner,
                groupChatUserManager.getUserFromClient(client, owner), String.valueOf(prefix),
                message));
    }

    @Override
    public void onChannelGotListModes(final Parser parser, final Date date,
            final ChannelInfo channel, final char mode) {
        checkParser(parser);

        final ChannelListModesRetrievedEvent event = new ChannelListModesRetrievedEvent(
                date.getTime(), owner, mode);
        final String format = EventUtils.postDisplayable(eventBus, event,
                "channelListModeRetrieved");
        owner.doNotification(date, format, mode);
    }

}
