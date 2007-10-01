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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.ParserError;
import com.dmdirc.parser.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.callbacks.interfaces.*;

/**
 * Handles parser events for a Server object.
 *
 * @author chris
 */
public final class ServerEventHandler implements IChannelSelfJoin, IPrivateMessage,
        IPrivateAction, IErrorInfo, IPrivateCTCP, IPrivateCTCPReply,
        ISocketClosed, IPrivateNotice, IMOTDStart, IMOTDLine, IMOTDEnd,
        INumeric, IGotNetwork, IPingFailed, IPingSuccess, IAwayState,
        IConnectError, IAwayStateOther, INickInUse, IPost005, INoticeAuth,
        IUnknownNotice, IUserModeChanged {
    
    private static final String CALLBACK_PREFIX = "com.dmdirc.parser.callbacks.interfaces.I";
    
    private final Server owner;
    
    /**
     * Creates a new instance of ServerEventHandler.
     *
     * @param owner The Server instance that we're handling events for
     */
    public ServerEventHandler(final Server owner) {
        super();
        
        this.owner = owner;
    }
    
    /**
     * Registers all callbacks that this event handler implements with the
     * owner's parser.
     */
    public void registerCallbacks() {
        try {
            for (Class iface : this.getClass().getInterfaces()) {
                if (iface.getName().startsWith(CALLBACK_PREFIX)) {
                    owner.getParser().getCallbackManager().addCallback(
                            "on" + iface.getName().substring(CALLBACK_PREFIX.length()), this);
                }
            }
        } catch (CallbackNotFoundException exception) {
            Logger.appError(ErrorLevel.FATAL, "Unable to register callbacks",
                    exception);
        }
    }
    
    /** {@inheritDoc} */
    public void onChannelSelfJoin(final IRCParser tParser, final ChannelInfo cChannel) {
        owner.onChannelSelfJoin(tParser, cChannel);
    }
    
    /** {@inheritDoc} */
    public void onPrivateMessage(final IRCParser tParser, final String sMessage,
            final String sHost) {
        owner.onPrivateMessage(tParser, sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    public void onPrivateAction(final IRCParser tParser, final String sMessage,
            final String sHost) {
        owner.onPrivateAction(tParser, sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    public void onErrorInfo(final IRCParser tParser, final ParserError errorInfo) {
        owner.onErrorInfo(tParser, errorInfo);
    }
    
    /** {@inheritDoc} */
    public void onPrivateCTCP(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        owner.onPrivateCTCP(tParser, sType, sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    public void onPrivateCTCPReply(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        owner.onPrivateCTCPReply(tParser, sType, sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    public void onSocketClosed(final IRCParser tParser) {
        owner.onSocketClosed(tParser);
    }
    
    /** {@inheritDoc} */
    public void onPrivateNotice(final IRCParser tParser, final String sMessage,
            final String sHost) {
        owner.onPrivateNotice(tParser, sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    public void onMOTDStart(final IRCParser tParser, final String sData) {
        owner.onMOTDStart(tParser, sData);
    }
    
    /** {@inheritDoc} */
    public void onMOTDLine(final IRCParser tParser, final String sData) {
        owner.onMOTDLine(tParser, sData);
    }
    
    /** {@inheritDoc} */
    public void onMOTDEnd(final IRCParser tParser, final boolean noMOTD) {
        owner.onMOTDEnd(tParser, noMOTD);
    }
    
    /** {@inheritDoc} */
    public void onNumeric(final IRCParser tParser, final int numeric,
            final String[] token) {
        owner.onNumeric(tParser, numeric, token);
    }
    
    /** {@inheritDoc} */
    public void onGotNetwork(final IRCParser tParser, final String networkName,
            final String ircdVersion, final String ircdType) {
        owner.onGotNetwork(tParser, networkName, ircdVersion, ircdType);
    }
    
    /** {@inheritDoc} */
    public void onPingFailed(final IRCParser tParser) {
        owner.onPingFailed(tParser);
    }
    
    /** {@inheritDoc} */
    public void onPingSuccess(final IRCParser tParser) {
        owner.onPingSuccess(tParser);
    }
    
    /** {@inheritDoc} */
    public void onAwayState(final IRCParser tParser, final boolean currentState,
            final String reason) {
        owner.onAwayState(tParser, currentState, reason);
    }
    
    /** {@inheritDoc} */
    public void onConnectError(final IRCParser tParser, final ParserError errorInfo) {
        owner.onConnectError(tParser, errorInfo);
    }
    
    /** {@inheritDoc} */
    public void onAwayStateOther(final IRCParser tParser, final ClientInfo client,
            final boolean state) {
        owner.onAwayStateOther(tParser, client, state);
    }
    
    /** {@inheritDoc} */
    public void onNickInUse(final IRCParser tParser, final String nickname) {
        owner.onNickInUse(tParser, nickname);
    }
    
    /** {@inheritDoc} */
    public void onPost005(final IRCParser tParser) {
        owner.onPost005(tParser);
    }
    
    /** {@inheritDoc} */
    public void onNoticeAuth(final IRCParser tParser, final String sData) {
        owner.onNoticeAuth(tParser, sData);
    }
    
    /** {@inheritDoc} */
    public void onUnknownNotice(final IRCParser tParser, final String sMessage,
            final String sTarget, final String sHost) {
        owner.onUnknownNotice(tParser, sMessage, sTarget, sHost);
    }
    
    /** {@inheritDoc} */
    public void onUserModeChanged(final IRCParser tParser,
            final ClientInfo cClient, final String sSetBy, final String sModes) {
        owner.onUserModeChanged(tParser, cClient, sSetBy, sModes);
    }
}