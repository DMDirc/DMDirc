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
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.irc.ParserError;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.callbacks.*;

/**
 * Handles parser events for a Server object.
 *
 * @author chris
 */
public final class ServerEventHandler extends EventHandler
        implements ChannelSelfJoinListener, PrivateMessageListener, PrivateActionListener,
        ErrorInfoListener, PrivateCtcpListener, PrivateCtcpReplyListener, SocketCloseListener,
        PrivateNoticeListener, MotdStartListener, MotdLineListener, MotdEndListener, NumericListener, PingFailureListener,
        PingSuccessListener, AwayStateListener, ConnectErrorListener, NickInUseListener, Post005Listener,
        AuthNoticeListener, UnknownNoticeListener, UserModeChangeListener, InviteListener, WallopListener,
        WalluserListener, WallDesyncListener, NickChangeListener, ServerErrorListener, PingSentListener,
        UserModeDiscoveryListener {

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
    @SuppressWarnings("unchecked")
    protected <T extends CallbackInterface> void addCallback(
            final CallbackManager cbm, final Class<T> type) throws CallbackNotFoundException {
        cbm.addCallback(type, (T) this);
    }

    /** {@inheritDoc} */
    @Override
    protected Server getServer() {
        return owner;
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelSelfJoin(final Parser tParser, final ChannelInfo cChannel) {
        checkParser(tParser);
        owner.addChannel(cChannel);
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateMessage(final Parser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        if (!owner.hasQuery(sHost)) {
            owner.addQuery(sHost);
            owner.getQuery(sHost).onPrivateMessage(tParser, sMessage, sHost);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateAction(final Parser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        if (!owner.hasQuery(sHost)) {
            owner.addQuery(sHost);
            owner.getQuery(sHost).onPrivateAction(tParser, sMessage, sHost);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onErrorInfo(final Parser tParser, final ParserError errorInfo) {
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
    public void onPrivateCTCP(final Parser tParser, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification("privateCTCP", CoreActionType.SERVER_CTCP,
                owner.getParser().getClient(sHost), sType, sMessage);

        owner.sendCTCPReply(tParser.parseHostmask(sHost)[0], sType, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateCTCPReply(final Parser tParser, final String sType,
            final String sMessage, final String sHost) {
        checkParser(tParser);

        owner.doNotification("privateCTCPreply", CoreActionType.SERVER_CTCPR,
                owner.getParser().getClient(sHost), sType, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onSocketClosed(final Parser tParser) {
        if (owner.getParser() == tParser) {
            owner.onSocketClosed();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateNotice(final Parser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("privateNotice", CoreActionType.SERVER_NOTICE,
                owner.getParser().getClient(sHost), sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDStart(final Parser tParser, final String sData) {
        checkParser(tParser);

        owner.doNotification("motdStart", CoreActionType.SERVER_MOTDSTART, sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDLine(final Parser tParser, final String sData) {
        checkParser(tParser);

        owner.doNotification("motdLine", CoreActionType.SERVER_MOTDLINE, sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDEnd(final Parser tParser, final boolean noMOTD, final String sData) {
        checkParser(tParser);

        owner.doNotification("motdEnd", CoreActionType.SERVER_MOTDEND, sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onNumeric(final Parser tParser, final int numeric,
            final String[] token) {
        checkParser(tParser);
        owner.onNumeric(numeric, token);
    }

    /** {@inheritDoc} */
    @Override
    public void onPingFailed(final Parser tParser) {
        checkParser(tParser);
        owner.onPingFailed();
    }

    /** {@inheritDoc} */
    @Override
    public void onPingSent(final Parser tParser) {
        checkParser(tParser);

        ActionManager.processEvent(CoreActionType.SERVER_PINGSENT, null, owner);
    }

    /** {@inheritDoc} */
    @Override
    public void onPingSuccess(final Parser tParser) {
        checkParser(tParser);

        ActionManager.processEvent(CoreActionType.SERVER_GOTPING, null, owner,
                Long.valueOf(owner.getParser().getServerLatency()));
    }

    /** {@inheritDoc} */
    @Override
    public void onAwayState(final Parser tParser, final boolean currentState,
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
    public void onConnectError(final Parser tParser, final ParserError errorInfo) {
        checkParser(tParser);
        owner.onConnectError(errorInfo);
    }

    /** {@inheritDoc} */
    @Override
    public void onNickInUse(final Parser tParser, final String nickname) {
        owner.onNickInUse(nickname);
        checkParser(tParser);
    }

    /** {@inheritDoc} */
    @Override
    public void onPost005(final Parser tParser) {
        checkParser(tParser);
        owner.onPost005();
    }

    /** {@inheritDoc} */
    @Override
    public void onNoticeAuth(final Parser tParser, final String sData) {
        checkParser(tParser);

        owner.doNotification("authNotice", CoreActionType.SERVER_AUTHNOTICE, sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnknownNotice(final Parser tParser, final String sMessage,
            final String sTarget, final String sHost) {
        checkParser(tParser);

        owner.doNotification("unknownNotice", CoreActionType.SERVER_UNKNOWNNOTICE,
                sHost, sTarget, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onUserModeChanged(final Parser tParser,
            final ClientInfo cClient, final String sSetBy, final String sModes) {
        checkParser(tParser);

        owner.doNotification("userModeChanged", CoreActionType.SERVER_USERMODES,
                owner.getParser().getClient(sSetBy), sModes);
    }

    /** {@inheritDoc} */
    @Override
    public void onUserModeDiscovered(final Parser tParser, final ClientInfo cClient,
             final String sModes) {
        checkParser(tParser);

        owner.doNotification("userModeDiscovered", CoreActionType.SERVER_USERMODES,
                cClient, sModes);
    }

    /** {@inheritDoc} */
    @Override
    public void onInvite(final Parser tParser, final String userHost,
            final String channel) {
        checkParser(tParser);

        owner.addInvite(new Invite(owner, channel, userHost));
        owner.doNotification("inviteReceived",
                CoreActionType.SERVER_INVITERECEIVED,
                owner.getParser().getClient(userHost), channel);
    }

    /** {@inheritDoc} */
    @Override
    public void onWallop(final Parser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("wallop", CoreActionType.SERVER_WALLOPS,
                owner.getParser().getClient(sHost), sMessage);

    }

    /** {@inheritDoc} */
    @Override
    public void onWalluser(final Parser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("walluser", CoreActionType.SERVER_WALLUSERS,
                owner.getParser().getClient(sHost), sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onWallDesync(final Parser tParser, final String sMessage,
            final String sHost) {
        checkParser(tParser);

        owner.doNotification("walldesync", CoreActionType.SERVER_WALLDESYNC,
                owner.getParser().getClient(sHost), sMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onNickChanged(final Parser tParser, final ClientInfo cClient,
            final String sOldNick) {
        checkParser(tParser);

        if (cClient.equals(owner.getParser().getLocalClient())) {
            owner.doNotification("selfNickChange", CoreActionType.SERVER_NICKCHANGE,
                    sOldNick, cClient.getNickname());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onServerError(final Parser tParser, final String sMessage) {
        checkParser(tParser);

        owner.doNotification("serverError", CoreActionType.SERVER_ERROR, sMessage);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkParser(final Parser parser) {
        super.checkParser(parser);
        
        if (owner.getState() != ServerState.CONNECTED
                && owner.getState() != ServerState.CONNECTING
                && owner.getState() != ServerState.DISCONNECTING) {
            throw new IllegalArgumentException("Event called from a parser (#"
                    + owner.getStatus().getParserID(parser) + ") that " +
                    "shouldn't be in use.\nState history:\n"
                    + owner.getStatus().getTransitionHistory());
        }
    }

}
