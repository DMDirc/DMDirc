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
        cbm.addCallback(name, this, owner.getChannelInfo().getName());
    }    

    /** {@inheritDoc} */
    @Override
    protected IRCParser getParser() {
        return owner.getServer().getParser();
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelMessage(final IRCParser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChannelClient,
            final String sMessage, final String sHost) {
        checkParser(tParser);
        owner.onChannelMessage(tParser, cChannel, cChannelClient, sMessage, sHost);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelGotNames(final IRCParser tParser, final ChannelInfo cChannel) {
        checkParser(tParser);
        owner.onChannelGotNames(tParser, cChannel);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelTopic(final IRCParser tParser, final ChannelInfo cChannel,
            final boolean bIsJoinTopic) {
        checkParser(tParser);
        owner.onChannelTopic(tParser, cChannel, bIsJoinTopic);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelJoin(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient) {
        checkParser(tParser);
        owner.onChannelJoin(tParser, cChannel, cChannelClient);
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelPart(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sReason) {
        checkParser(tParser);
        owner.onChannelPart(tParser, cChannel, cChannelClient, sReason);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelKick(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cKickedClient, final ChannelClientInfo cKickedByClient,
            final String sReason, final String sKickedByHost) {
        checkParser(tParser);
        owner.onChannelKick(tParser, cChannel, cKickedClient, cKickedByClient,
                sReason, sKickedByHost);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelQuit(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sReason) {
        checkParser(tParser);
        owner.onChannelQuit(tParser, cChannel, cChannelClient, sReason);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelAction(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
        checkParser(tParser);
        owner.onChannelAction(tParser, cChannel, cChannelClient, sMessage, sHost);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelNickChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sOldNick) {
        checkParser(tParser);
        owner.onChannelNickChanged(tParser, cChannel, cChannelClient, sOldNick);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelModeChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sHost, final String sModes) {
        checkParser(tParser);
        owner.onChannelModeChanged(tParser, cChannel, cChannelClient, sHost, sModes);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelUserModeChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChangedClient, final ChannelClientInfo cSetByClient,
            final String sHost, final String sMode) {
        checkParser(tParser);
        owner.onChannelUserModeChanged(tParser, cChannel, cChangedClient,
                cSetByClient, sHost, sMode);
    }

    /** {@inheritDoc} */
    @Override    
    public void onChannelCTCP(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sType, 
            final String sMessage, final String sHost) {
        checkParser(tParser);
        owner.onChannelCTCP(tParser, cChannel, cChannelClient, sType, sMessage, sHost);
    }

    /** {@inheritDoc} */
    @Override    
    public void onAwayStateOther(final IRCParser tParser, final ClientInfo client, 
            final boolean state) {
        checkParser(tParser);
        owner.onAwayStateOther(tParser, client, state);
    }

}
