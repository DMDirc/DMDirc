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
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.QuerySelfActionEvent;
import com.dmdirc.events.QuerySelfMessageEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.ParserError;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.AuthNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.AwayStateListener;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ConnectErrorListener;
import com.dmdirc.parser.interfaces.callbacks.ErrorInfoListener;
import com.dmdirc.parser.interfaces.callbacks.InviteListener;
import com.dmdirc.parser.interfaces.callbacks.MotdEndListener;
import com.dmdirc.parser.interfaces.callbacks.MotdLineListener;
import com.dmdirc.parser.interfaces.callbacks.MotdStartListener;
import com.dmdirc.parser.interfaces.callbacks.NickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.NickInUseListener;
import com.dmdirc.parser.interfaces.callbacks.NumericListener;
import com.dmdirc.parser.interfaces.callbacks.PingFailureListener;
import com.dmdirc.parser.interfaces.callbacks.PingSentListener;
import com.dmdirc.parser.interfaces.callbacks.PingSuccessListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateActionListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateCtcpReplyListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ServerErrorListener;
import com.dmdirc.parser.interfaces.callbacks.ServerNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownActionListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownMessageListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.UserModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.UserModeDiscoveryListener;
import com.dmdirc.parser.interfaces.callbacks.WallDesyncListener;
import com.dmdirc.parser.interfaces.callbacks.WallopListener;
import com.dmdirc.parser.interfaces.callbacks.WalluserListener;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    /** Event bus to post events to. */
    private final EventBus eventBus;

    /**
     * Creates a new instance of ServerEventHandler.
     *
     * @param owner    The Server instance that we're handling events for
     * @param eventBus The event bus to post events to
     */
    public ServerEventHandler(final Server owner, final EventBus eventBus) {
        this.owner = owner;
        this.eventBus = eventBus;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends CallbackInterface> void addCallback(
            final CallbackManager cbm, final Class<T> type) {
        cbm.addCallback(type, (T) this);
    }

    @Override
    protected Connection getConnection() {
        return owner;
    }

    @Override
    public void onChannelSelfJoin(final Parser parser, final Date date, final ChannelInfo channel) {
        checkParser(parser);
        owner.addChannel(channel);
    }

    @Override
    public void onPrivateMessage(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        if (!owner.hasQuery(host)) {
            owner.getQuery(host).onPrivateMessage(parser, date, message, host);
        }
    }

    @Override
    public void onPrivateAction(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        if (!owner.hasQuery(host)) {
            owner.getQuery(host).onPrivateAction(parser, date, message, host);
        }
    }

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

    @Override
    public void onPrivateCTCP(final Parser parser, final Date date, final String type,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification("privateCTCP", owner.getParser().getClient(host), type, message);
        if (triggerAction("privateCTCP", CoreActionType.SERVER_CTCP, owner.getParser().getClient(
                host), type, message)) {
            owner.sendCTCPReply(owner.parseHostmask(host)[0], type, message);
        }
    }

    @Override
    public void onPrivateCTCPReply(final Parser parser, final Date date, final String type,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification("privateCTCPreply", owner.getParser().getClient(host), type, message);
        triggerAction("privateCTCPreply", CoreActionType.SERVER_CTCPR, owner.getParser().getClient(
                host), type, message);
    }

    @Override
    public void onSocketClosed(final Parser parser, final Date date) {
        if (owner.getParser() == parser) {
            owner.onSocketClosed();
        }
    }

    @Override
    public void onPrivateNotice(final Parser parser, final Date date,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification("privateNotice", owner.getParser().getClient(host), message);
        triggerAction("privateNotice", CoreActionType.SERVER_NOTICE, owner.getParser().getClient(
                host), message);
    }

    @Override
    public void onServerNotice(final Parser parser, final Date date,
            final String message, final String host) {
        checkParser(parser);

        owner.doNotification("serverNotice", owner.getParser().getClient(host), message);
        triggerAction("serverNotice", CoreActionType.SERVER_SERVERNOTICE, owner.getParser().
                getClient(host), message);
    }

    @Override
    public void onMOTDStart(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        owner.doNotification("motdStart", data);
        triggerAction("motdStart", CoreActionType.SERVER_MOTDSTART, data);
    }

    @Override
    public void onMOTDLine(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        owner.doNotification("motdLine", data);
        triggerAction("motdLine", CoreActionType.SERVER_MOTDLINE, data);
    }

    @Override
    public void onMOTDEnd(final Parser parser, final Date date,
            final boolean noMOTD, final String data) {
        checkParser(parser);

        owner.doNotification("motdEnd", data);
        triggerAction("motdEnd", CoreActionType.SERVER_MOTDEND, data);
    }

    @Override
    public void onNumeric(final Parser parser, final Date date, final int numeric,
            final String[] token) {
        checkParser(parser);
        owner.onNumeric(numeric, token);
    }

    @Override
    public void onPingFailed(final Parser parser, final Date date) {
        checkParser(parser);
        owner.onPingFailed();
    }

    @Override
    public void onPingSent(final Parser parser, final Date date) {
        checkParser(parser);

        ActionManager.getActionManager().triggerEvent(
                CoreActionType.SERVER_PINGSENT, null, owner);
    }

    @Override
    public void onPingSuccess(final Parser parser, final Date date) {
        checkParser(parser);

        ActionManager.getActionManager().triggerEvent(
                CoreActionType.SERVER_GOTPING, null, owner,
                Long.valueOf(owner.getParser().getServerLatency()));
    }

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
            owner.doNotification("away", reason);
            triggerAction("away", CoreActionType.SERVER_AWAY, reason);
        } else {
            owner.doNotification("back");
            triggerAction("back", CoreActionType.SERVER_BACK);
        }
    }

    @Override
    public void onConnectError(final Parser parser, final Date date, final ParserError errorInfo) {
        checkParser(parser);
        owner.onConnectError(errorInfo);
    }

    @Override
    public void onNickInUse(final Parser parser, final Date date, final String nickname) {
        owner.onNickInUse(nickname);
        checkParser(parser);
    }

    @Override
    public void onServerReady(final Parser parser, final Date date) {
        checkParser(parser);
        owner.onPost005();
    }

    @Override
    public void onNoticeAuth(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        owner.doNotification("authNotice", data);
        triggerAction("authNotice", CoreActionType.SERVER_AUTHNOTICE, data);
    }

    @Override
    public void onUnknownNotice(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        owner.doNotification("unknownNotice", host, target, message);
        triggerAction("unknownNotice", CoreActionType.SERVER_UNKNOWNNOTICE, host, target, message);
    }

    @Override
    public void onUnknownMessage(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        if (parser.getLocalClient().equals(parser.getClient(host))) {
            // Local client
            owner.getQuery(target).doNotification("querySelfExternalMessage",
                    parser.getLocalClient(), message);
            final DisplayableEvent event = new QuerySelfMessageEvent(owner.getQuery(target),
                    parser.getLocalClient(), message);
            event.setDisplayFormat("querySelfExternalMessage");
            eventBus.post(event);
        } else {
            owner.doNotification("unknownMessage", host, target, message);
            triggerAction("unknownMessage", CoreActionType.SERVER_UNKNOWNNOTICE, host, target,
                    message);
        }
    }

    @Override
    public void onUnknownAction(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        if (parser.getLocalClient().equals(parser.getClient(host))) {
            // Local client
            owner.getQuery(target).doNotification("querySelfExternalAction",
                    parser.getLocalClient(), message);
            final DisplayableEvent event = new QuerySelfActionEvent(owner.getQuery(target),
                    parser.getLocalClient(), message);
            event.setDisplayFormat("querySelfExternalAction");
            eventBus.post(event);
        } else {
            owner.doNotification("unknownAction", host, target, message);
            triggerAction("unknownAction", CoreActionType.SERVER_UNKNOWNACTION, host, target,
                    message);
        }
    }

    @Override
    public void onUserModeChanged(final Parser parser, final Date date,
            final ClientInfo client, final String host, final String modes) {
        checkParser(parser);

        owner.doNotification("userModeChanged", owner.getParser().getClient(host), modes);
        triggerAction("userModeChanged", CoreActionType.SERVER_USERMODES, owner.getParser().
                getClient(host), modes);
    }

    @Override
    public void onUserModeDiscovered(final Parser parser, final Date date,
            final ClientInfo client, final String modes) {
        checkParser(parser);

        owner.doNotification(modes.isEmpty() || "+".equals(modes)
                ? "userNoModes" : "userModeDiscovered", client, modes);
        triggerAction(modes.isEmpty() || "+".equals(modes) ? "userNoModes" : "userModeDiscovered",
                CoreActionType.SERVER_USERMODES, client, modes);
    }

    @Override
    public void onInvite(final Parser parser, final Date date, final String userHost,
            final String channel) {
        checkParser(parser);

        owner.addInvite(new Invite(owner, channel, userHost));
        owner.doNotification("inviteReceived",
                owner.getParser().getClient(userHost), channel);
        triggerAction("inviteReceived", CoreActionType.SERVER_INVITERECEIVED, owner.getParser().
                getClient(userHost), channel);
    }

    @Override
    public void onWallop(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification("wallop", owner.getParser().getClient(host), message);
        triggerAction("wallop", CoreActionType.SERVER_WALLOPS, owner.getParser().getClient(host),
                message);

    }

    @Override
    public void onWalluser(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification("walluser", owner.getParser().getClient(host), message);
        triggerAction("walluser", CoreActionType.SERVER_WALLUSERS, owner.getParser().getClient(host),
                message);
    }

    @Override
    public void onWallDesync(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        owner.doNotification("walldesync", owner.getParser().getClient(host), message);
        triggerAction("walldesync", CoreActionType.SERVER_WALLDESYNC, owner.getParser().getClient(
                host), message);
    }

    @Override
    public void onNickChanged(final Parser parser, final Date date, final ClientInfo client,
            final String oldNick) {
        checkParser(parser);

        if (client.equals(owner.getParser().getLocalClient())) {
            owner.doNotification("selfNickChange", oldNick, client.getNickname());
            triggerAction("selfNickChange", CoreActionType.SERVER_NICKCHANGE, oldNick, client.
                    getNickname());
            owner.updateTitle();
        }
    }

    @Override
    public void onServerError(final Parser parser, final Date date, final String message) {
        checkParser(parser);

        owner.doNotification("serverError", message);
        triggerAction("serverError", CoreActionType.SERVER_ERROR, message);
    }

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

    private boolean triggerAction(final String messageType, final ActionType actionType,
            final Object... args) {
        final StringBuffer buffer = new StringBuffer(messageType);
        final List<Object> actionArgs = new ArrayList<>();
        actionArgs.add(owner);
        actionArgs.addAll(Arrays.asList(args));
        return ActionManager.getActionManager().triggerEvent(actionType, buffer, actionArgs.
                toArray());
    }

}
