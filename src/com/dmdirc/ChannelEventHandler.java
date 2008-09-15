/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.callbacks.CallbackManager;
import com.dmdirc.parser.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.callbacks.interfaces.*;

/**
 * Handles events for channel objects.
 *
 * @author chris
 */
public final class ChannelEventHandler extends EventHandler implements
        IChannelMessage, IChannelGotNames, IChannelTopic, IChannelJoin,
        IChannelPart, IChannelKick, IChannelQuit, IChannelAction,
        IChannelNickChanged, IChannelModeChanged, IChannelUserModeChanged,
        IChannelCTCP, IAwayStateOther {

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
    @Override
    protected void addCallback(final CallbackManager cbm, final String name)
            throws CallbackNotFoundException {
        if ("onAwayStateOther".equals(name)) {
            cbm.addCallback(name, this);
        } else {
            cbm.addCallback(name, this, owner.getChannelInfo().getName());
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
        return client.getClient().equals(owner.getServer().getParser().getMyself());
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelMessage(final IRCParser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChannelClient,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification(
                isMyself(cChannelClient) ? "channelSelfExternalMessage" : "channelMessage",
                CoreActionType.CHANNEL_MESSAGE, cChannelClient, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelGotNames(final IRCParser tParser, final ChannelInfo cChannel) {
        checkParser(tParser);

        owner.setClients(cChannel.getChannelClients());
        ActionManager.processEvent(CoreActionType.CHANNEL_GOTNAMES, null, owner);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelTopic(final IRCParser tParser,
            final ChannelInfo cChannel, final boolean bIsJoinTopic) {
        checkParser(tParser);

        final Topic newTopic = new Topic(cChannel.getTopic(),
                cChannel.getTopicUser(), cChannel.getTopicTime());

        if (bIsJoinTopic) {
            owner.doNotification("channelTopicDiscovered", CoreActionType.CHANNEL_GOTTOPIC,
                    newTopic);
        } else {
            owner.doNotification("channelTopicChanged", CoreActionType.CHANNEL_TOPICCHANGE,
                    cChannel.getUser(cChannel.getTopicUser(), true), cChannel.getTopic());
        }

        owner.addTopic(newTopic);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelJoin(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient) {
        checkParser(tParser);

        owner.doNotification("channelJoin", CoreActionType.CHANNEL_JOIN, cChannelClient);
        owner.addClient(cChannelClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelPart(final IRCParser tParser, final ChannelInfo cChannel,
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
    public void onChannelKick(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cKickedClient, final ChannelClientInfo cKickedByClient,
            final String sReason, final String sKickedByHost) {
        checkParser(tParser);

        owner.doNotification("channelKick" + (sReason.isEmpty() ? "" : "Reason"),
                CoreActionType.CHANNEL_KICK, cKickedByClient, cKickedClient, sReason);
        owner.removeClient(cKickedClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelQuit(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sReason) {
        checkParser(tParser);

        owner.doNotification("channelQuit" + (sReason.isEmpty() ? "" : "Reason"),
                CoreActionType.CHANNEL_QUIT, cChannelClient, sReason);
        owner.removeClient(cChannelClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelAction(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification(
                isMyself(cChannelClient) ? "channelSelfExternalAction" : "channelAction",
                CoreActionType.CHANNEL_ACTION, cChannelClient, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelNickChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sOldNick) {
        checkParser(tParser);

        owner.doNotification(
                isMyself(cChannelClient) ? "channelSelfNickChange" : "channelNickChange",
                CoreActionType.CHANNEL_NICKCHANGE, cChannelClient, sOldNick);
        owner.renameClient(sOldNick, cChannelClient.getNickname());
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelModeChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sHost,
            final String sModes) {
        checkParser(tParser);

        if (sHost.isEmpty()) {
            owner.doNotification(sModes.length() <= 1 ? "channelNoModes"
                    : "channelModeDiscovered", CoreActionType.CHANNEL_MODESDISCOVERED,
                    sModes.length() <= 1 ? "" : sModes);
        } else {
            owner.doNotification(isMyself(cChannelClient) ? "channelSelfModeChanged"
                    : "channelModeChanged", CoreActionType.CHANNEL_MODECHANGE,
                    cChannelClient, sModes);
        }

        owner.refreshClients();
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelUserModeChanged(final IRCParser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChangedClient,
            final ChannelClientInfo cSetByClient, final String sHost, final String sMode) {
        checkParser(tParser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes", false)) {
            String format = "channelSplitUserMode_" + sMode;

            if (!owner.getConfigManager().hasOption("formatter", format)) {
                format = "channelSplitUserMode_default";
            }

            owner.doNotification(format, CoreActionType.CHANNEL_USERMODECHANGE,
                    cSetByClient, cChangedClient, sMode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelCTCP(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification("channelCTCP", CoreActionType.CHANNEL_CTCP,
                cChannelClient, sType, sMessage);
        owner.getServer().sendCTCPReply(cChannelClient.getNickname(), sType, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onAwayStateOther(final IRCParser tParser,
            final ClientInfo client, final boolean state) {
        checkParser(tParser);

        final ChannelClientInfo channelClient = owner.getChannelInfo().getUser(client);

        if (channelClient != null) {
            owner.doNotification(state ? "channelUserAway" : "channelUserBack",
                    state ? CoreActionType.CHANNEL_USERAWAY : CoreActionType.CHANNEL_USERBACK,
                    channelClient);
        }
    }

}
