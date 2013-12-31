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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.ParserError;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.*; //NOPMD

import java.util.Date;

/**
 * Handles parser events for a Server object.
 */
public class ServerEventHandler extends EventHandler implements
        ChannelSelfJoinListener, PrivateMessageListener, PrivateActionListener,
        ErrorInfoListener, PrivateCtcpListener, PrivateCtcpReplyListener,
        SocketCloseListener, PrivateNoticeListener, MotdStartListener,
        MotdLineListener, MotdEndListener, NumericListener, PingFailureListener,
        PingSuccessListener, AwayStateListener, ConnectErrorListener,
        NickInUseListener, AuthNoticeListener, UnknownNoticeListener,
        UserModeChangeListener, InviteListener, WallopListener,
        WalluserListener, WallDesyncListener, NickChangeListener,
        ServerErrorListener, PingSentListener, UserModeDiscoveryListener,
        ServerNoticeListener, UnknownMessageListener, UnknownActionListener,
        ServerReadyListener {

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
            final CallbackManager cbm, final Class<T> type) {
        cbm.addCallback(type, (T) this);
    }

    /** {@inheritDoc} */
    @Override
    protected Connection getConnection() {
        return owner;
    }

    /** {@inheritDoc} */
    @Override
    public void onChannelSelfJoin(final Parser parser, final Date date, final ChannelInfo channel) {
        checkParser(parser);
        owner.addChannel(channel);
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateMessage(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        if (!owner.hasQuery(host)) {
            owner.getQuery(host).onPrivateMessage(parser, date, message, host);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateAction(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        if (!owner.hasQuery(host)) {
            owner.getQuery(host).onPrivateAction(parser, date, message, host);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onErrorInfo(final Parser parser, final Date date, final ParserError errorInfo) {
        final ErrorLevel errorLevel = ErrorLevel.UNKNOWN;

        final StringBuilder errorString = new StringBuilder();
        errorString.append("Parser exception.\n\n");

        errorString.append("\tLast line:\t");
        errorString.append(errorInfo.getLastLine()).append('\n');

        errorString.append("\tServer:\t");
        errorString.append(owner.getAddress()).append('\n');

        errorString.append("\tAdditional Information:\n");
        for (final String line : parser.getServerInformationLines()) {
            errorString.append("\t\t").append(line).append('\n');
        }

        final Exception ex = errorInfo.isException() ? errorInfo.getException()
                : new Exception(errorString.toString()); // NOPMD

        if (errorInfo.isUserError()) {
            Logger.userError(errorLevel, errorInfo.getData(), ex);
        } else {
            Logger.appError(errorLevel, errorInfo.getData(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateCTCP(final Parser parser, final Date date, final String type,
            final String message, final String host) {
        checkParser(parser);

        if (owner.doNotification("privateCTCP", CoreActionType.SERVER_CTCP,
                owner.getParser().getClient(host), type, message)) {
            owner.sendCTCPReply(owner.parseHostmask(host)[0], type, message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateCTCPReply(final Parser parser, final Date date, final String type,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification("privateCTCPreply", CoreActionType.SERVER_CTCPR,
                owner.getParser().getClient(host), type, message);
    }

    /** {@inheritDoc} */
    @Override
    public void onSocketClosed(final Parser parser, final Date date) {
        if (owner.getParser() == parser) {
            owner.onSocketClosed();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrivateNotice(final Parser parser, final Date date,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification("privateNotice", CoreActionType.SERVER_NOTICE,
                owner.getParser().getClient(host), message);
    }

    /** {@inheritDoc} */
    @Override
    public void onServerNotice(final Parser parser, final Date date,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification("serverNotice", CoreActionType.SERVER_SERVERNOTICE,
                owner.getParser().getClient(host), message);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDStart(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        owner.doNotification("motdStart", CoreActionType.SERVER_MOTDSTART, data);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDLine(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        owner.doNotification("motdLine", CoreActionType.SERVER_MOTDLINE, data);
    }

    /** {@inheritDoc} */
    @Override
    public void onMOTDEnd(final Parser parser, final Date date,
            final boolean noMOTD, final String data) {
        checkParser(parser);

        owner.doNotification("motdEnd", CoreActionType.SERVER_MOTDEND, data);
    }

    /** {@inheritDoc} */
    @Override
    public void onNumeric(final Parser parser, final Date date, final int numeric,
            final String[] token) {
        checkParser(parser);
        owner.onNumeric(numeric, token);
    }

    /** {@inheritDoc} */
    @Override
    public void onPingFailed(final Parser parser, final Date date) {
        checkParser(parser);
        owner.onPingFailed();
    }

    /** {@inheritDoc} */
    @Override
    public void onPingSent(final Parser parser, final Date date) {
        checkParser(parser);

        ActionManager.getActionManager().triggerEvent(
                CoreActionType.SERVER_PINGSENT, null, owner);
    }

    /** {@inheritDoc} */
    @Override
    public void onPingSuccess(final Parser parser, final Date date) {
        checkParser(parser);

        ActionManager.getActionManager().triggerEvent(
                CoreActionType.SERVER_GOTPING, null, owner,
                Long.valueOf(owner.getParser().getServerLatency()));
    }

    /** {@inheritDoc} */
    @Override
    public void onAwayState(final Parser parser, final Date date, final AwayState oldState,
            final AwayState currentState, final String reason) {
        checkParser(parser);

        owner.updateAwayState(currentState == AwayState.AWAY ? reason : null);

        if (oldState == AwayState.UNKNOWN) {
            // Ignore discovered self away states
            return;
        }

        if (currentState == AwayState.AWAY) {
            owner.doNotification("away", CoreActionType.SERVER_AWAY, reason);
        } else {
            owner.doNotification("back", CoreActionType.SERVER_BACK);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onConnectError(final Parser parser, final Date date, final ParserError errorInfo) {
        checkParser(parser);
        owner.onConnectError(errorInfo);
    }

    /** {@inheritDoc} */
    @Override
    public void onNickInUse(final Parser parser, final Date date, final String nickname) {
        owner.onNickInUse(nickname);
        checkParser(parser);
    }

    /** {@inheritDoc} */
    @Override
    public void onServerReady(final Parser parser, final Date date) {
        checkParser(parser);
        owner.onPost005();
    }

    /** {@inheritDoc} */
    @Override
    public void onNoticeAuth(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        owner.doNotification("authNotice", CoreActionType.SERVER_AUTHNOTICE, data);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnknownNotice(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        owner.doNotification("unknownNotice", CoreActionType.SERVER_UNKNOWNNOTICE,
                host, target, message);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnknownMessage(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        if (parser.getLocalClient().equals(parser.getClient(host))) {
            // Local client
            owner.getQuery(target).doNotification("querySelfExternalMessage",
                    CoreActionType.QUERY_SELF_MESSAGE, parser.getLocalClient(), message);
        } else {
            owner.doNotification("unknownMessage", CoreActionType.SERVER_UNKNOWNNOTICE,
                    host, target, message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onUnknownAction(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        if (parser.getLocalClient().equals(parser.getClient(host))) {
            // Local client
            owner.getQuery(target).doNotification("querySelfExternalAction",
                    CoreActionType.QUERY_SELF_ACTION, parser.getLocalClient(), message);
        } else {
            owner.doNotification("unknownAction", CoreActionType.SERVER_UNKNOWNACTION,
                    host, target, message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onUserModeChanged(final Parser parser, final Date date,
            final ClientInfo client, final String host, final String modes) {
        checkParser(parser);

        owner.doNotification("userModeChanged", CoreActionType.SERVER_USERMODES,
                owner.getParser().getClient(host), modes);
    }

    /** {@inheritDoc} */
    @Override
    public void onUserModeDiscovered(final Parser parser, final Date date,
            final ClientInfo client, final String modes) {
        checkParser(parser);

        owner.doNotification(modes.isEmpty() || "+".equals(modes)
                ? "userNoModes" : "userModeDiscovered",
                CoreActionType.SERVER_USERMODES, client, modes);
    }

    /** {@inheritDoc} */
    @Override
    public void onInvite(final Parser parser, final Date date, final String userHost,
            final String channel) {
        checkParser(parser);

        owner.addInvite(new Invite(owner, channel, userHost));
        owner.doNotification("inviteReceived",
                CoreActionType.SERVER_INVITERECEIVED,
                owner.getParser().getClient(userHost), channel);
    }

    /** {@inheritDoc} */
    @Override
    public void onWallop(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification("wallop", CoreActionType.SERVER_WALLOPS,
                owner.getParser().getClient(host), message);

    }

    /** {@inheritDoc} */
    @Override
    public void onWalluser(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification("walluser", CoreActionType.SERVER_WALLUSERS,
                owner.getParser().getClient(host), message);
    }

    /** {@inheritDoc} */
    @Override
    public void onWallDesync(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification("walldesync", CoreActionType.SERVER_WALLDESYNC,
                owner.getParser().getClient(host), message);
    }

    /** {@inheritDoc} */
    @Override
    public void onNickChanged(final Parser parser, final Date date, final ClientInfo client,
            final String oldNick) {
        checkParser(parser);

        if (client.equals(owner.getParser().getLocalClient())) {
            owner.doNotification("selfNickChange", CoreActionType.SERVER_NICKCHANGE,
                    oldNick, client.getNickname());
            owner.updateTitle();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onServerError(final Parser parser, final Date date, final String message) {
        checkParser(parser);

        owner.doNotification("serverError", CoreActionType.SERVER_ERROR, message);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkParser(final Parser parser) {
        super.checkParser(parser);

        if (owner.getState() != ServerState.CONNECTED
                && owner.getState() != ServerState.CONNECTING
                && owner.getState() != ServerState.DISCONNECTING) {
            throw new IllegalArgumentException("Event called from a parser (#"
                    + owner.getStatus().getParserID(parser) + ") that "
                    + "shouldn't be in use.\nState history:\n"
                    + owner.getStatus().getTransitionHistory());
        }
    }

}
