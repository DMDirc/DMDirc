/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
    
    private final Channel owner;
    
    public ChannelEventHandler(final Channel owner) {
        this.owner = owner;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void addCallback(final CallbackManager cbm, final String name)
            throws CallbackNotFoundException {
        if (name.equals("OnAwayStateOther")) {
            cbm.addCallback(name, this);
        } else {
            cbm.addCallback(name, this, owner.getChannelInfo().getName());
        }
    }    

    /** {@inheritDoc} */
    @Override
    protected IRCParser getParser() {
        return owner.getServer().getParser();
    }
    
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
        owner.onChannelGotNames();
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelTopic(final IRCParser tParser, final ChannelInfo cChannel,
            final boolean bIsJoinTopic) {
        checkParser(tParser);
        owner.onChannelTopic(bIsJoinTopic);
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
            final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
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
            final ChannelClientInfo cChannelClient, final String sHost, final String sModes) {
        checkParser(tParser);
        owner.onChannelModeChanged(cChannelClient, sHost, sModes);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelUserModeChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChangedClient, final ChannelClientInfo cSetByClient,
            final String sHost, final String sMode) {
        checkParser(tParser);
        owner.onChannelUserModeChanged(cChangedClient, cSetByClient, sMode);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelCTCP(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sType, 
            final String sMessage, final String sHost) {
        checkParser(tParser);
        owner.onChannelCTCP(cChannelClient, sType, sMessage);
    }

    /** {@inheritDoc} */
    @Override    
    public void onAwayStateOther(final IRCParser tParser, final ClientInfo client, 
            final boolean state) {
        checkParser(tParser);
        owner.onAwayStateOther(client, state);
    }

}
