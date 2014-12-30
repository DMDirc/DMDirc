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

import com.dmdirc.events.ChannelActionEvent;
import com.dmdirc.events.ChannelCtcpEvent;
import com.dmdirc.events.ChannelDisplayableEvent;
import com.dmdirc.events.ChannelGotnamesEvent;
import com.dmdirc.events.ChannelGottopicEvent;
import com.dmdirc.events.ChannelJoinEvent;
import com.dmdirc.events.ChannelKickEvent;
import com.dmdirc.events.ChannelListmodesretrievedEvent;
import com.dmdirc.events.ChannelMessageEvent;
import com.dmdirc.events.ChannelModeNoticeEvent;
import com.dmdirc.events.ChannelModechangeEvent;
import com.dmdirc.events.ChannelModesdiscoveredEvent;
import com.dmdirc.events.ChannelNickchangeEvent;
import com.dmdirc.events.ChannelNoticeEvent;
import com.dmdirc.events.ChannelNotopicEvent;
import com.dmdirc.events.ChannelPartEvent;
import com.dmdirc.events.ChannelQuitEvent;
import com.dmdirc.events.ChannelTopicChangeEvent;
import com.dmdirc.events.ChannelTopicUnsetEvent;
import com.dmdirc.events.ChannelUserAwayEvent;
import com.dmdirc.events.ChannelUserBackEvent;
import com.dmdirc.events.ChannelUserEvent;
import com.dmdirc.events.ChannelUsermodechangeEvent;
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
import com.dmdirc.parser.interfaces.callbacks.ChannelNonUserModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelPartListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelQuitListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelUserModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.OtherAwayStateListener;
import com.dmdirc.util.EventUtils;

import com.google.common.base.Strings;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Handles events for channel objects.
 */
public class ChannelEventHandler extends EventHandler implements
        ChannelMessageListener, ChannelNamesListener, ChannelTopicListener,
        ChannelJoinListener, ChannelPartListener, ChannelKickListener,
        ChannelQuitListener, ChannelActionListener, ChannelNickChangeListener,
        ChannelModeChangeListener, ChannelUserModeChangeListener,
        ChannelCtcpListener, OtherAwayStateListener, ChannelNoticeListener,
        ChannelNonUserModeChangeListener, ChannelModeNoticeListener,
        ChannelListModeListener {

    /** The channel that owns this event handler. */
    private final Channel owner;
    /** Event bus to send events on. */
    private final DMDircMBassador eventBus;

    public ChannelEventHandler(final Channel owner, final DMDircMBassador eventBus) {
        super(eventBus);
        this.owner = owner;
        this.eventBus = eventBus;
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

        final ChannelMessageEvent event = new ChannelMessageEvent(owner,
                owner.getUserFromClient(client), message);
        final String format = EventUtils.postDisplayable(eventBus, event,
                isMyself(client) ? "channelSelfExternalMessage" : "channelMessage");
        owner.doNotification(date, format, client, message);
    }

    @Override
    public void onChannelGotNames(final Parser parser, final Date date, final ChannelInfo channel) {
        checkParser(parser);

        owner.setClients(channel.getChannelClients());
        eventBus.publishAsync(new ChannelGotnamesEvent(owner));
    }

    @Override
    public void onChannelTopic(final Parser parser, final Date date,
            final ChannelInfo channel, final boolean isJoinTopic) {
        checkParser(parser);

        if (isJoinTopic) {
            if (Strings.isNullOrEmpty(channel.getTopic())) {
                final ChannelNotopicEvent event = new ChannelNotopicEvent(owner);
                final String format = EventUtils.postDisplayable(eventBus, event, "channelNoTopic");
                owner.doNotification(date, format);
            } else {
                final Topic newTopic = new Topic(channel.getTopic(),
                        getConnection().getUser(channel.getTopicSetter()),
                        channel.getTopicTime());
                final ChannelGottopicEvent event = new ChannelGottopicEvent(owner, newTopic);
                final String format = EventUtils.postDisplayable(eventBus, event,
                        "channelTopicDiscovered");
                owner.doNotification(date, format, newTopic);
            }
        } else {
            final ChannelDisplayableEvent event;
            final String format;
            if (Strings.isNullOrEmpty(channel.getTopic())) {
                event = new ChannelTopicChangeEvent(owner,
                        channel.getChannelClient(channel.getTopicSetter(), true),
                        channel.getTopic());
                format = EventUtils.postDisplayable(eventBus, event,
                        "channelTopicRemoved");
            } else {
                event = new ChannelTopicUnsetEvent(owner,
                        channel.getChannelClient(channel.getTopicSetter(), true));
                format = EventUtils.postDisplayable(eventBus, event,
                        "channelTopicChanged");
            }
            owner.doNotification(date, format, channel.getChannelClient(channel.getTopicSetter(),
                    true), channel.getTopic());
        }

        final Optional<Topic> currentTopic = owner.getCurrentTopic();
        final boolean hasNewTopic = !Strings.isNullOrEmpty(channel.getTopic());
        if (!isJoinTopic
                || (!currentTopic.isPresent() && hasNewTopic)
                || (currentTopic.isPresent() && !channel.getTopic().equals(
                        owner.getCurrentTopic().get().getTopic()))) {
            // Only add the topic if:
            //  - It's being set while we're in the channel (rather than discovered on join), or
            //  - We think the current topic is empty and are discovering a new one, or
            //  - The newly discovered topic is different to what we thought the current topic was.
            owner.addTopic(new Topic(channel.getTopic(),
                    getConnection().getUser(channel.getTopicSetter()),
                    channel.getTopicTime()));
        }
    }

    @Override
    public void onChannelJoin(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client) {
        checkParser(parser);

        final ChannelJoinEvent event = new ChannelJoinEvent(owner, client);
        final String format = EventUtils.postDisplayable(eventBus, event, "channelJoin");
        owner.doNotification(date, format, client);
        owner.addClient(client);
    }

    @Override
    public void onChannelPart(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        final ChannelPartEvent event = new ChannelPartEvent(owner, client, reason);
        final String format = EventUtils.postDisplayable(eventBus, event,
                "channel"
                + (isMyself(client) ? "Self" : "") + "Part"
                + (reason.isEmpty() ? "" : "Reason"));
        owner.doNotification(date, format, client, reason);
        owner.removeClient(client);
    }

    @Override
    public void onChannelKick(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo kickedClient, final ChannelClientInfo client,
            final String reason, final String host) {
        checkParser(parser);

        final ChannelKickEvent event = new ChannelKickEvent(owner, client, kickedClient, reason);
        final String format = EventUtils.postDisplayable(eventBus, event,
                "channelKick" + (reason.isEmpty() ? "" : "Reason"));
        owner.doNotification(date, format, client, kickedClient, reason);
        owner.removeClient(kickedClient);
    }

    @Override
    public void onChannelQuit(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        final ChannelQuitEvent event = new ChannelQuitEvent(owner, client, reason);
        final String format = EventUtils.postDisplayable(eventBus, event,
                "channelQuit" + (reason.isEmpty() ? "" : "Reason"));
        owner.doNotification(date, format, client, reason);
        owner.removeClient(client);
    }

    @Override
    public void onChannelAction(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        final ChannelActionEvent event = new ChannelActionEvent(owner,
                owner.getUserFromClient(client), message);
        final String format = EventUtils.postDisplayable(eventBus, event,
                isMyself(client) ? "channelSelfExternalAction" : "channelAction");
        owner.doNotification(date, format, client, message);
    }

    @Override
    public void onChannelNickChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String oldNick) {
        checkParser(parser);

        final ChannelNickchangeEvent event = new ChannelNickchangeEvent(owner, client, oldNick);
        final String format = EventUtils.postDisplayable(eventBus, event,
                isMyself(client) ? "channelSelfNickChange" : "channelNickChange");
        owner.doNotification(date, format, client, oldNick);
        owner.renameClient(oldNick, client.getClient().getNickname());
    }

    @Override
    public void onChannelModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String host,
            final String modes) {
        checkParser(parser);

        if (!owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                || !owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (host.isEmpty()) {
                final ChannelModesdiscoveredEvent event = new ChannelModesdiscoveredEvent(owner,
                        modes.length() <= 1 ? "" : modes);
                final String format = EventUtils.postDisplayable(eventBus, event,
                        modes.length() <= 1 ? "channelNoModes" : "channelModeDiscovered");
                owner.doNotification(date, format, modes.length() <= 1 ? "" : modes);
            } else {
                final ChannelModechangeEvent event = new ChannelModechangeEvent(owner, client, modes);
                final String format = EventUtils.postDisplayable(eventBus, event,
                        isMyself(client) ? "channelSelfModeChanged" : "channelModeChanged");
                owner.doNotification(date, format, client, modes);
            }
        }

        owner.refreshClients();
    }

    @Override
    public void onChannelUserModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo targetClient,
            final ChannelClientInfo client, final String host, final String mode) {
        checkParser(parser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes")) {
            String format = "channelSplitUserMode_" + mode;

            if (!owner.getConfigManager().hasOptionString("formatter", format)) {
                format = "channelSplitUserMode_default";
            }

            final ChannelUsermodechangeEvent event = new ChannelUsermodechangeEvent(owner, client,
                    targetClient, mode);
            final String result = EventUtils.postDisplayable(eventBus, event, format);
            owner.doNotification(date, result, client, targetClient, mode);
        }
    }

    @Override
    public void onChannelCTCP(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String type, final String message, final String host) {
        checkParser(parser);

        final ChannelCtcpEvent event = new ChannelCtcpEvent(owner, client, type, message);
        final String format = EventUtils.postDisplayable(eventBus, event, "channelCTCP");
        if (!event.isHandled()) {
            getConnection().sendCTCPReply(client.getClient().getNickname(), type, message);
        }
        owner.doNotification(date, format, client, type, message);
    }

    @Override
    public void onAwayStateOther(final Parser parser, final Date date,
            final ClientInfo client, final AwayState oldState, final AwayState state) {
        checkParser(parser);

        final ChannelClientInfo channelClient = owner.getChannelInfo().getChannelClient(client);

        if (channelClient != null) {
            final boolean away = state == AwayState.AWAY;
            final boolean discovered = oldState == AwayState.UNKNOWN;

            final ChannelUserEvent event = away
                    ? new ChannelUserAwayEvent(date.getTime(), owner, channelClient)
                    : new ChannelUserBackEvent(date.getTime(), owner, channelClient);
            final String format = EventUtils.postDisplayable(eventBus, event,
                    (away ? "channelUserAway" : "channelUserBack")
                    + (discovered ? "Discovered" : ""));
            owner.doNotification(date, format, channelClient);

        }
    }

    @Override
    public void onChannelNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String message, final String host) {
        checkParser(parser);

        final ChannelNoticeEvent event = new ChannelNoticeEvent(owner,
                owner.getUserFromClient(client), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "channelNotice");
        owner.doNotification(date, format, client, message);
    }

    @Override
    public void onChannelNonUserModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String host, final String modes) {
        checkParser(parser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                && owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (host.isEmpty()) {
                final ChannelModesdiscoveredEvent event = new ChannelModesdiscoveredEvent(owner,
                        modes.length() <= 1 ? "" : modes);
                final String format = EventUtils.postDisplayable(eventBus, event,
                        modes.length() <= 1 ? "channelNoModes" : "channelModeDiscovered");
                owner.doNotification(date, format, modes.length() <= 1 ? "" : modes);
            } else {
                final ChannelModechangeEvent event = new ChannelModechangeEvent(owner, client, modes);
                final String format = EventUtils.postDisplayable(eventBus, event,
                        isMyself(client) ? "channelSelfModeChanged" : "channelModeChanged");
                owner.doNotification(date, format, client, modes);
            }
        }

        owner.refreshClients();
    }

    @Override
    public void onChannelModeNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final char prefix,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        final ChannelModeNoticeEvent event = new ChannelModeNoticeEvent(owner, client,
                String.valueOf(prefix), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "channelModeNotice");
        owner.doNotification(date, format, client, String.valueOf(prefix), message);
    }

    @Override
    public void onChannelGotListModes(final Parser parser, final Date date,
            final ChannelInfo channel, final char mode) {
        checkParser(parser);

        final ChannelListmodesretrievedEvent event = new ChannelListmodesretrievedEvent(owner, mode);
        final String format = EventUtils.postDisplayable(eventBus, event,
                "channelListModeRetrieved");
        owner.doNotification(date, format, mode);
    }

}
