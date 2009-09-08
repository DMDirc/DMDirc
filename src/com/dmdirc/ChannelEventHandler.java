/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.interfaces.callbacks.*;

/**
 * Handles events for channel objects.
 *
 * @author chris
 */
public final class ChannelEventHandler extends EventHandler implements
        ChannelMessageListener, ChannelNamesListener, ChannelTopicListener,
        ChannelJoinListener, ChannelPartListener, ChannelKickListener,
        ChannelQuitListener, ChannelActionListener, ChannelNickChangeListener,
        ChannelModeChangeListener, ChannelUserModeChangeListener,
        ChannelCtcpListener, OtherAwayStateListener, ChannelNoticeListener,
        ChannelNonUserModeChangeListener, ChannelModeNoticeListener {

    /** The channel that owns this event handler. */
    private final Channel owner;

    /**
     * Creates a new instance of ChannelEventHandler.
     *
     * @param owner The channel that owns this event handler.
     */
    public ChannelEventHandler(final Channel owner) {
        super();

        this.owner = owner;
    }

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
    public void onChannelMessage(final Parser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChannelClient,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification(
                isMyself(cChannelClient) ? "channelSelfExternalMessage" : "channelMessage",
                CoreActionType.CHANNEL_MESSAGE, cChannelClient, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelGotNames(final Parser tParser, final ChannelInfo cChannel) {
        checkParser(tParser);

        owner.setClients(cChannel.getChannelClients());
        ActionManager.processEvent(CoreActionType.CHANNEL_GOTNAMES, null, owner);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelTopic(final Parser tParser,
            final ChannelInfo cChannel, final boolean bIsJoinTopic) {
        checkParser(tParser);

        final Topic newTopic = new Topic(cChannel.getTopic(),
                cChannel.getTopicSetter(), cChannel.getTopicTime());

        if (bIsJoinTopic) {
            owner.doNotification("channelTopicDiscovered", CoreActionType.CHANNEL_GOTTOPIC,
                    newTopic);
        } else {
            owner.doNotification("channelTopicChanged", CoreActionType.CHANNEL_TOPICCHANGE,
                    cChannel.getChannelClient(cChannel.getTopicSetter(), true), cChannel.getTopic());
        }

        owner.addTopic(newTopic);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelJoin(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient) {
        checkParser(tParser);

        owner.doNotification("channelJoin", CoreActionType.CHANNEL_JOIN, cChannelClient);
        owner.addClient(cChannelClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelPart(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sReason) {
        checkParser(tParser);

        owner.doNotification("channel"
                + (isMyself(cChannelClient) ? "Self" : "") + "Part"
                + (sReason.isEmpty() ? "" : "Reason"), CoreActionType.CHANNEL_PART,
                cChannelClient, sReason);
        owner.removeClient(cChannelClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelKick(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cKickedClient, final ChannelClientInfo cKickedByClient,
            final String sReason, final String sKickedByHost) {
        checkParser(tParser);

        owner.doNotification("channelKick" + (sReason.isEmpty() ? "" : "Reason"),
                CoreActionType.CHANNEL_KICK, cKickedByClient, cKickedClient, sReason);
        owner.removeClient(cKickedClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelQuit(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sReason) {
        checkParser(tParser);

        owner.doNotification("channelQuit" + (sReason.isEmpty() ? "" : "Reason"),
                CoreActionType.CHANNEL_QUIT, cChannelClient, sReason);
        owner.removeClient(cChannelClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelAction(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification(
                isMyself(cChannelClient) ? "channelSelfExternalAction" : "channelAction",
                CoreActionType.CHANNEL_ACTION, cChannelClient, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelNickChanged(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sOldNick) {
        checkParser(tParser);

        owner.doNotification(
                isMyself(cChannelClient) ? "channelSelfNickChange" : "channelNickChange",
                CoreActionType.CHANNEL_NICKCHANGE, cChannelClient, sOldNick);
        owner.renameClient(sOldNick, cChannelClient.getClient().getNickname());
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelModeChanged(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sHost,
            final String sModes) {
        checkParser(tParser);

        if (!owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                || !owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (sHost.isEmpty()) {
                owner.doNotification(sModes.length() <= 1 ? "channelNoModes"
                        : "channelModeDiscovered", CoreActionType.CHANNEL_MODESDISCOVERED,
                        sModes.length() <= 1 ? "" : sModes);
            } else {
                owner.doNotification(isMyself(cChannelClient) ? "channelSelfModeChanged"
                        : "channelModeChanged", CoreActionType.CHANNEL_MODECHANGE,
                        cChannelClient, sModes);
            }
        }

        owner.refreshClients();
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelUserModeChanged(final Parser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChangedClient,
            final ChannelClientInfo cSetByClient, final String sHost, final String sMode) {
        checkParser(tParser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes")) {
            String format = "channelSplitUserMode_" + sMode;

            if (!owner.getConfigManager().hasOptionString("formatter", format)) {
                format = "channelSplitUserMode_default";
            }

            owner.doNotification(format, CoreActionType.CHANNEL_USERMODECHANGE,
                    cSetByClient, cChangedClient, sMode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelCTCP(final Parser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification("channelCTCP", CoreActionType.CHANNEL_CTCP,
                cChannelClient, sType, sMessage);
        owner.getServer().sendCTCPReply(cChannelClient.getClient().getNickname(), sType, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onAwayStateOther(final Parser tParser,
            final ClientInfo client, final boolean state) {
        checkParser(tParser);

        final ChannelClientInfo channelClient = owner.getChannelInfo().getChannelClient(client);

        if (channelClient != null) {
            owner.doNotification(state ? "channelUserAway" : "channelUserBack",
                    state ? CoreActionType.CHANNEL_USERAWAY : CoreActionType.CHANNEL_USERBACK,
                    channelClient);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelNotice(final Parser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChannelClient,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification("channelNotice", CoreActionType.CHANNEL_NOTICE,
                cChannelClient, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelNonUserModeChanged(final Parser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChannelClient,
            final String sHost, final String sModes) {
        checkParser(tParser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                && owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (sHost.isEmpty()) {
                owner.doNotification(sModes.length() <= 1 ? "channelNoModes"
                        : "channelModeDiscovered", CoreActionType.CHANNEL_MODESDISCOVERED,
                        sModes.length() <= 1 ? "" : sModes);
            } else {
                owner.doNotification(isMyself(cChannelClient) ? "channelSelfModeChanged"
                        : "channelModeChanged", CoreActionType.CHANNEL_MODECHANGE,
                        cChannelClient, sModes);
            }
        }

        owner.refreshClients();
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelModeNotice(final Parser tParser, 
            final ChannelInfo cChannel, final char prefix, 
            final ChannelClientInfo cChannelClient, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("channelModeNotice", CoreActionType.CHANNEL_MODE_NOTICE,
                cChannelClient, String.valueOf(prefix), sMessage);
    }

}
