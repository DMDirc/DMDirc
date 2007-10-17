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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
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
        IUnknownNotice, IUserModeChanged, IInvite {
    
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
    
    /**
     * Checks that the specified parser is the same as the one the server is
     * currently claiming to be using. If it isn't, we raise an exception to
     * prevent further (erroneous) processing.
     */
    private void checkParser(final IRCParser parser) {
        if (parser != owner.getParser()) {
            throw new IllegalArgumentException("Event called from a parser that's not in use."
                    + "\nActual parser: " + owner.getParser().hashCode()
                    + "\nPassed parser: " + parser.hashCode());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onChannelSelfJoin(final IRCParser tParser, final ChannelInfo cChannel) {
        checkParser(tParser);
        owner.addChannel(cChannel);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPrivateMessage(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);
        owner.addQuery(sHost);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPrivateAction(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);
        owner.addQuery(sHost);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onErrorInfo(final IRCParser tParser, final ParserError errorInfo) {
        checkParser(tParser);
        
        final ErrorLevel errorLevel = ErrorLevel.UNKNOWN;
        
        if (errorInfo.isException()) {
            Logger.appError(errorLevel, errorInfo.getData(), errorInfo.getException());
        } else {
            Logger.appError(errorLevel, errorInfo.getData(),
                    new Exception("Parser exception.\n\n\tLast line:\t" //NOPMD
                    + errorInfo.getLastLine() + "\n\tServer:\t" + owner.getName() + "\n"));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPrivateCTCP(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);
        owner.onPrivateCTCP(sType, sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPrivateCTCPReply(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);
        owner.onPrivateCTCPReply(sType, sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSocketClosed(final IRCParser tParser) {
        checkParser(tParser);
        owner.onSocketClosed();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPrivateNotice(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);
        owner.onPrivateNotice(sMessage, sHost);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onMOTDStart(final IRCParser tParser, final String sData) {
        checkParser(tParser);
        owner.onMOTDStart(sData);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onMOTDLine(final IRCParser tParser, final String sData) {
        checkParser(tParser);
        owner.onMOTDLine(sData);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onMOTDEnd(final IRCParser tParser, final boolean noMOTD) {
        checkParser(tParser);
        owner.onMOTDEnd();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onNumeric(final IRCParser tParser, final int numeric,
            final String[] token) {
        checkParser(tParser);
        owner.onNumeric(numeric, token);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onGotNetwork(final IRCParser tParser, final String networkName,
            final String ircdVersion, final String ircdType) {
        checkParser(tParser);
        owner.onGotNetwork(networkName, ircdType);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPingFailed(final IRCParser tParser) {
        checkParser(tParser);
        owner.onPingFailed();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPingSuccess(final IRCParser tParser) {
        checkParser(tParser);
        
        ActionManager.processEvent(CoreActionType.SERVER_GOTPING, null, owner,
                Long.valueOf(tParser.getServerLag()));
    }
    
    /** {@inheritDoc} */
    @Override
    public void onAwayState(final IRCParser tParser, final boolean currentState,
            final String reason) {
        checkParser(tParser);
        owner.onAwayState(currentState, reason);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onConnectError(final IRCParser tParser, final ParserError errorInfo) {
        checkParser(tParser);
        owner.onConnectError(errorInfo);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onAwayStateOther(final IRCParser tParser, final ClientInfo client,
            final boolean state) {
        checkParser(tParser);
        
        // Just proxy it
        for (String chan : owner.getChannels()) {
            owner.getChannel(chan).onAwayStateOther(tParser, client, state);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onNickInUse(final IRCParser tParser, final String nickname) {
        owner.onNickInUse(nickname);
        checkParser(tParser);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPost005(final IRCParser tParser) {
        checkParser(tParser);
        owner.onPost005();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onNoticeAuth(final IRCParser tParser, final String sData) {
        checkParser(tParser);
        owner.onNoticeAuth(sData);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnknownNotice(final IRCParser tParser, final String sMessage,
            final String sTarget, final String sHost) {
        checkParser(tParser);
        owner.onUnknownNotice(sMessage, sTarget, sHost);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUserModeChanged(final IRCParser tParser,
            final ClientInfo cClient, final String sSetBy, final String sModes) {
        checkParser(tParser);
        owner.onUserModeChanged(cClient, sSetBy, sModes);
    }

    /** {@inheritDoc} */
    @Override
    public void onInvite(final IRCParser tParser, final String userHost,
            final String channel) {
        checkParser(tParser);
        
        owner.addInvite(new Invite(owner, channel, userHost));
        owner.doNotification("inviteReceived",
                CoreActionType.SERVER_INVITERECEIVED, 
                owner, tParser.getClientInfoOrFake(userHost), channel);
    }
}