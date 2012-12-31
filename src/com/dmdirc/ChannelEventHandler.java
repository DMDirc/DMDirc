/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.*; //NOPMD

import java.util.Date;

import lombok.AllArgsConstructor;

/**
 * Handles events for channel objects.
 */
@AllArgsConstructor
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    protected Server getServer() {
        return owner.getServer();
    }

    /**
     * Determines if the specified client represents us.
     *
     * @param client The client to be tested
     * @return True if the client is ourself, false otherwise.
     */
    protected boolean isMyself(final ChannelClientInfo client) {
        return client.getClient().equals(owner.getServer().getParser().getLocalClient());
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelMessage(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification(date,
                isMyself(client) ? "channelSelfExternalMessage" : "channelMessage",
                CoreActionType.CHANNEL_MESSAGE, client, message);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelGotNames(final Parser parser, final Date date, final ChannelInfo channel) {
        checkParser(parser);

        owner.setClients(channel.getChannelClients());
        ActionManager.getActionManager().triggerEvent(
                CoreActionType.CHANNEL_GOTNAMES, null, owner);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelTopic(final Parser parser, final Date date,
            final ChannelInfo channel, final boolean isJoinTopic) {
        checkParser(parser);

        final Topic newTopic = new Topic(channel.getTopic(),
                channel.getTopicSetter(), channel.getTopicTime());

        if (isJoinTopic) {
            if (newTopic.getTopic().isEmpty()) {
                owner.doNotification(date, "channelNoTopic", CoreActionType.CHANNEL_NOTOPIC);
            } else {
                owner.doNotification(date, "channelTopicDiscovered", CoreActionType.CHANNEL_GOTTOPIC,
                        newTopic);
            }
        } else {
            owner.doNotification(date, channel.getTopic().isEmpty()
                    ? "channelTopicRemoved" : "channelTopicChanged",
                    CoreActionType.CHANNEL_TOPICCHANGE,
                    channel.getChannelClient(channel.getTopicSetter(), true), channel.getTopic());
        }

        if (!isJoinTopic
                || (owner.getCurrentTopic() == null && !newTopic.getTopic().isEmpty())
                || (owner.getCurrentTopic() != null
                && !newTopic.getTopic().equals(owner.getCurrentTopic().getTopic()))) {
            // Only add the topic if it's being changed when we're on the
            // channel (i.e., not a "joinTopic"), or if it's different to the
            // one we're expecting
            owner.addTopic(newTopic);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelJoin(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client) {
        checkParser(parser);

        owner.doNotification(date, "channelJoin", CoreActionType.CHANNEL_JOIN, client);
        owner.addClient(client);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelPart(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        owner.doNotification(date, "channel"
                + (isMyself(client) ? "Self" : "") + "Part"
                + (reason.isEmpty() ? "" : "Reason"), CoreActionType.CHANNEL_PART,
                client, reason);
        owner.removeClient(client);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelKick(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo kickedClient, final ChannelClientInfo client,
            final String reason, final String host) {
        checkParser(parser);

        owner.doNotification(date, "channelKick" + (reason.isEmpty() ? "" : "Reason"),
                CoreActionType.CHANNEL_KICK, client, kickedClient, reason);
        owner.removeClient(kickedClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelQuit(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        owner.doNotification(date, "channelQuit" + (reason.isEmpty() ? "" : "Reason"),
                CoreActionType.CHANNEL_QUIT, client, reason);
        owner.removeClient(client);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelAction(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification(date,
                isMyself(client) ? "channelSelfExternalAction" : "channelAction",
                CoreActionType.CHANNEL_ACTION, client, message);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelNickChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String oldNick) {
        checkParser(parser);

        owner.doNotification(date,
                isMyself(client) ? "channelSelfNickChange" : "channelNickChange",
                CoreActionType.CHANNEL_NICKCHANGE, client, oldNick);
        owner.renameClient(oldNick, client.getClient().getNickname());
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String host,
            final String modes) {
        checkParser(parser);

        if (!owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                || !owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (host.isEmpty()) {
                owner.doNotification(date, modes.length() <= 1 ? "channelNoModes"
                        : "channelModeDiscovered", CoreActionType.CHANNEL_MODESDISCOVERED,
                        modes.length() <= 1 ? "" : modes);
            } else {
                owner.doNotification(date, isMyself(client) ? "channelSelfModeChanged"
                        : "channelModeChanged", CoreActionType.CHANNEL_MODECHANGE,
                        client, modes);
            }
        }

        owner.refreshClients();
    }

    /** {@inheritDoc} */
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

            owner.doNotification(date, format, CoreActionType.CHANNEL_USERMODECHANGE,
                    client, targetClient, mode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelCTCP(final Parser parser, final Date date,
            final ChannelInfo channel,  final ChannelClientInfo client,
            final String type, final String message, final String host) {
        checkParser(parser);

        if (owner.doNotification(date, "channelCTCP", CoreActionType.CHANNEL_CTCP,
                client, type, message)) {
            owner.getServer().sendCTCPReply(client.getClient().getNickname(),
                    type, message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onAwayStateOther(final Parser parser, final Date date,
            final ClientInfo client, final AwayState oldState, final AwayState state) {
        checkParser(parser);

        final ChannelClientInfo channelClient = owner.getChannelInfo().getChannelClient(client);

        if (channelClient != null) {
            final boolean away = state == AwayState.AWAY;
            final boolean discovered = oldState == AwayState.UNKNOWN;

            owner.doNotification(date, (away ? "channelUserAway" : "channelUserBack")
                    + (discovered ? "Discovered" : ""),
                    away ? CoreActionType.CHANNEL_USERAWAY : CoreActionType.CHANNEL_USERBACK,
                    channelClient);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification(date, "channelNotice", CoreActionType.CHANNEL_NOTICE,
                client, message);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelNonUserModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String host, final String modes) {
        checkParser(parser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                && owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (host.isEmpty()) {
                owner.doNotification(date, modes.length() <= 1 ? "channelNoModes"
                        : "channelModeDiscovered", CoreActionType.CHANNEL_MODESDISCOVERED,
                        modes.length() <= 1 ? "" : modes);
            } else {
                owner.doNotification(date, isMyself(client) ? "channelSelfModeChanged"
                        : "channelModeChanged", CoreActionType.CHANNEL_MODECHANGE,
                        client, modes);
            }
        }

        owner.refreshClients();
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelModeNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final char prefix,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification(date, "channelModeNotice", CoreActionType.CHANNEL_MODE_NOTICE,
                client, String.valueOf(prefix), message);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelGotListModes(final Parser parser, final Date date,
            final ChannelInfo channel, final char mode) {
        checkParser(parser);

        owner.doNotification(date, "channelListModeRetrieved",
                CoreActionType.CHANNEL_LISTMODERETRIEVED, Character.valueOf(mode));
    }

}
