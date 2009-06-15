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
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.irc.ChannelInfo;
import com.dmdirc.parser.irc.ClientInfo;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.ParserError;
import com.dmdirc.parser.irc.callbacks.CallbackManager;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.irc.callbacks.interfaces.*;

/**
 * Handles parser events for a Server object.
 *
 * @author chris
 */
public final class ServerEventHandler extends EventHandler
        implements IChannelSelfJoin, IPrivateMessage, IPrivateAction,
        IErrorInfo, IPrivateCTCP, IPrivateCTCPReply, ISocketClosed,
        IPrivateNotice, IMOTDStart, IMOTDLine, IMOTDEnd, INumeric, IPingFailed,
        IPingSuccess, IAwayState, IConnectError, INickInUse, IPost005,
        INoticeAuth, IUnknownNotice, IUserModeChanged, IInvite, IWallop,
        IWalluser, IWallDesync, INickChanged, IServerError, IPingSent,
        IUserModeDiscovered {

    /** The server instance that owns this event handler. */
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

    /** {@inheritDoc} */
    @Override
    protected void addCallback(final CallbackManager cbm, final String name)
            throws CallbackNotFoundException {
        cbm.addCallback(name, this);
    }

    /** {@inheritDoc} */
    @Override
    protected Server getServer() {
        return owner;
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

        if (!owner.hasQuery(sHost)) {
            owner.addQuery(sHost);
            owner.getQuery(sHost).onPrivateMessage(tParser, sMessage, sHost);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateAction(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        if (!owner.hasQuery(sHost)) {
            owner.addQuery(sHost);
            owner.getQuery(sHost).onPrivateAction(tParser, sMessage, sHost);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onErrorInfo(final IRCParser tParser, final ParserError errorInfo) {
        final ErrorLevel errorLevel = ErrorLevel.UNKNOWN;

        final Exception ex = (errorInfo.isException()) ? errorInfo.getException()
                : new Exception("Parser exception.\n\n\tLast line:\t" //NOPMD
                + errorInfo.getLastLine() + "\n\tServer:\t" + owner.getName() + "\n");

        if (errorInfo.isUserError()) {
            Logger.userError(errorLevel, errorInfo.getData(), ex);
        } else {
            Logger.appError(errorLevel, errorInfo.getData(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateCTCP(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification("privateCTCP", CoreActionType.SERVER_CTCP,
                tParser.getClientInfoOrFake(sHost), sType, sMessage);

        owner.sendCTCPReply(ClientInfo.parseHost(sHost), sType, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateCTCPReply(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification("privateCTCPreply", CoreActionType.SERVER_CTCPR,
                tParser.getClientInfoOrFake(sHost), sType, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onSocketClosed(final IRCParser tParser) {
        if (owner.getParser() == tParser) {
            owner.onSocketClosed();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateNotice(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("privateNotice", CoreActionType.SERVER_NOTICE,
                tParser.getClientInfoOrFake(sHost), sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDStart(final IRCParser tParser, final String sData) {
        checkParser(tParser);

        owner.doNotification("motdStart", CoreActionType.SERVER_MOTDSTART, sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDLine(final IRCParser tParser, final String sData) {
        checkParser(tParser);

        owner.doNotification("motdLine", CoreActionType.SERVER_MOTDLINE, sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDEnd(final IRCParser tParser, final boolean noMOTD, final String sData) {
        checkParser(tParser);

        owner.doNotification("motdEnd", CoreActionType.SERVER_MOTDEND, sData);
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
    public void onPingFailed(final IRCParser tParser) {
        checkParser(tParser);
        owner.onPingFailed();
    }

    /** {@inheritDoc} */
    @Override
    public void onPingSent(final IRCParser tParser) {
        checkParser(tParser);

        ActionManager.processEvent(CoreActionType.SERVER_PINGSENT, null, owner);
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

        owner.updateAwayState(currentState ? reason : null);

        if (currentState) {
            owner.doNotification("away", CoreActionType.SERVER_AWAY, reason);
        } else {
            owner.doNotification("back", CoreActionType.SERVER_BACK);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onConnectError(final IRCParser tParser, final ParserError errorInfo) {
        checkParser(tParser);
        owner.onConnectError(errorInfo);
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

        owner.doNotification("authNotice", CoreActionType.SERVER_AUTHNOTICE, sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnknownNotice(final IRCParser tParser, final String sMessage,
            final String sTarget, final String sHost) {
        checkParser(tParser);

        owner.doNotification("unknownNotice", CoreActionType.SERVER_UNKNOWNNOTICE,
                sHost, sTarget, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onUserModeChanged(final IRCParser tParser,
            final ClientInfo cClient, final String sSetBy, final String sModes) {
        checkParser(tParser);

        owner.doNotification("userModeChanged", CoreActionType.SERVER_USERMODES,
                tParser.getClientInfoOrFake(sSetBy), sModes);
    }

    /** {@inheritDoc} */
    @Override
    public void onUserModeDiscovered(final IRCParser tParser, final ClientInfo cClient,
             final String sModes) {
        checkParser(tParser);

        owner.doNotification("userModeDiscovered", CoreActionType.SERVER_USERMODES,
                cClient, sModes);
    }

    /** {@inheritDoc} */
    @Override
    public void onInvite(final IRCParser tParser, final String userHost,
            final String channel) {
        checkParser(tParser);

        owner.addInvite(new Invite(owner, channel, userHost));
        owner.doNotification("inviteReceived",
                CoreActionType.SERVER_INVITERECEIVED,
                tParser.getClientInfoOrFake(userHost), channel);
    }

    /** {@inheritDoc} */
    @Override
    public void onWallop(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("wallop", CoreActionType.SERVER_WALLOPS,
                tParser.getClientInfoOrFake(sHost), sMessage);

    }

    /** {@inheritDoc} */
    @Override
    public void onWalluser(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("walluser", CoreActionType.SERVER_WALLUSERS,
                tParser.getClientInfoOrFake(sHost), sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onWallDesync(final IRCParser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("walldesync", CoreActionType.SERVER_WALLDESYNC,
                tParser.getClientInfoOrFake(sHost), sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onNickChanged(final IRCParser tParser, final ClientInfo cClient,
            final String sOldNick) {
        checkParser(tParser);

        if (cClient.equals(tParser.getMyself())) {
            owner.doNotification("selfNickChange", CoreActionType.SERVER_NICKCHANGE,
                    sOldNick, cClient.getNickname());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onServerError(final IRCParser tParser, final String sMessage) {
        checkParser(tParser);

        owner.doNotification("serverError", CoreActionType.SERVER_ERROR, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkParser(final IRCParser parser) {
        super.checkParser(parser);
        
        if (owner.getState() != ServerState.CONNECTED
                && owner.getState() != ServerState.CONNECTING
                && owner.getState() != ServerState.DISCONNECTING) {
            throw new IllegalArgumentException("Event called from a parser that " +
                    "shouldn't be in use.\nState history:\n"
                    + owner.getStatus().getTransitionHistory());
        }
    }

}
