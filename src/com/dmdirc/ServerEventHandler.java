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

import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.QuerySelfActionEvent;
import com.dmdirc.events.QuerySelfMessageEvent;
import com.dmdirc.events.ServerAuthnoticeEvent;
import com.dmdirc.events.ServerAwayEvent;
import com.dmdirc.events.ServerBackEvent;
import com.dmdirc.events.ServerCtcpEvent;
import com.dmdirc.events.ServerCtcprEvent;
import com.dmdirc.events.ServerErrorEvent;
import com.dmdirc.events.ServerGotpingEvent;
import com.dmdirc.events.ServerInvitereceivedEvent;
import com.dmdirc.events.ServerMotdendEvent;
import com.dmdirc.events.ServerMotdlineEvent;
import com.dmdirc.events.ServerMotdstartEvent;
import com.dmdirc.events.ServerNickchangeEvent;
import com.dmdirc.events.ServerNoticeEvent;
import com.dmdirc.events.ServerPingsentEvent;
import com.dmdirc.events.ServerServernoticeEvent;
import com.dmdirc.events.ServerUnknownactionEvent;
import com.dmdirc.events.ServerUnknownmessageEvent;
import com.dmdirc.events.ServerUnknownnoticeEvent;
import com.dmdirc.events.ServerUsermodesEvent;
import com.dmdirc.events.ServerWalldesyncEvent;
import com.dmdirc.events.ServerWallopsEvent;
import com.dmdirc.events.ServerWallusersEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.logger.ErrorLevel;
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
import com.dmdirc.util.EventUtils;

import java.util.Date;

import javax.annotation.Nonnull;


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
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of ServerEventHandler.
     *
     * @param owner    The Server instance that we're handling events for
     * @param eventBus The event bus to post events to
     */
    public ServerEventHandler(final Server owner, final DMDircMBassador eventBus) {
        this.owner = owner;
        this.eventBus = eventBus;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends CallbackInterface> void addCallback(
            final CallbackManager cbm, final Class<T> type) {
        cbm.addCallback(type, (T) this);
    }

    @Nonnull
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
            eventBus.publishAsync(new UserErrorEvent(errorLevel, ex, errorInfo.getData(), ""));
        } else {
            eventBus.publishAsync(new AppErrorEvent(errorLevel, ex, errorInfo.getData(), ""));
        }
    }

    @Override
    public void onPrivateCTCP(final Parser parser, final Date date, final String type,
            final String message, final String host) {
        checkParser(parser);

        final ServerCtcpEvent event = new ServerCtcpEvent(owner, owner.getParser().getClient(host),
                type, message);
        final String format = EventUtils.postDisplayable(eventBus, event, "privateCTCP");
        owner.doNotification(format, owner.getParser().getClient(host), type, message);
        if (!event.isHandled()) {
            owner.sendCTCPReply(owner.parseHostmask(host)[0], type, message);
        }
    }

    @Override
    public void onPrivateCTCPReply(final Parser parser, final Date date, final String type,
            final String message, final String host) {
        checkParser(parser);

        final ServerCtcprEvent event
                = new ServerCtcprEvent(owner, owner.getParser().getClient(host), type, message);
        final String format = EventUtils.postDisplayable(eventBus, event, "privateCTCPreply");
        owner.doNotification(format, owner.getParser().getClient(host), type, message);
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

        final ServerNoticeEvent event = new ServerNoticeEvent(owner, owner.getParser().
                getClient(host), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "privateNotice");
        owner.doNotification(format, owner.getParser().getClient(host), message);
    }

    @Override
    public void onServerNotice(final Parser parser, final Date date,
            final String message, final String host) {
        checkParser(parser);

        final ServerServernoticeEvent event = new ServerServernoticeEvent(owner, owner.getParser().
                getClient(host), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "serverNotice");
        owner.doNotification(format, owner.getParser().getClient(host), message);
    }

    @Override
    public void onMOTDStart(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        final ServerMotdstartEvent event = new ServerMotdstartEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "motdStart");
        owner.doNotification(format, data);
    }

    @Override
    public void onMOTDLine(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        final ServerMotdlineEvent event = new ServerMotdlineEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "motdLine");
        owner.doNotification(format, data);
    }

    @Override
    public void onMOTDEnd(final Parser parser, final Date date,
            final boolean noMOTD, final String data) {
        checkParser(parser);

        final ServerMotdendEvent event = new ServerMotdendEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "motdEnd");
        owner.doNotification(format, data);
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

        eventBus.publishAsync(new ServerPingsentEvent(owner));
    }

    @Override
    public void onPingSuccess(final Parser parser, final Date date) {
        checkParser(parser);

        eventBus.publishAsync(new ServerGotpingEvent(owner, owner.getParser().getServerLatency()));
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
            final ServerAwayEvent event = new ServerAwayEvent(owner, reason);
            final String format = EventUtils.postDisplayable(eventBus, event, "away");
            owner.doNotification(format, reason);
        } else {
            final ServerBackEvent event = new ServerBackEvent(owner);
            final String format = EventUtils.postDisplayable(eventBus, event, "back");
            owner.doNotification(format);
        }
    }

    @Override
    public void onConnectError(final Parser parser, final Date date, final ParserError errorInfo) {
        checkParser(parser);
        owner.onConnectError(errorInfo);
    }

    @Override
    public void onNickInUse(final Parser parser, final Date date, final String nickname) {
        checkParser(parser);
        owner.onNickInUse(nickname);
    }

    @Override
    public void onServerReady(final Parser parser, final Date date) {
        checkParser(parser);
        owner.onPost005();
    }

    @Override
    public void onNoticeAuth(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        final ServerAuthnoticeEvent event = new ServerAuthnoticeEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "authNotice");
        owner.doNotification(format, data);
    }

    @Override
    public void onUnknownNotice(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        final ServerUnknownnoticeEvent event = new ServerUnknownnoticeEvent(owner, host, target, message);
        final String format = EventUtils.postDisplayable(eventBus, event, "unknownNotice");
        owner.doNotification(format, host, target, message);
    }

    @Override
    public void onUnknownMessage(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        if (parser.getLocalClient().equals(parser.getClient(host))) {
            // Local client
            final QuerySelfMessageEvent event = new QuerySelfMessageEvent(owner.getQuery(target),
                    parser.getLocalClient(), message);
            final String format = EventUtils.postDisplayable(eventBus, event,
                    "querySelfExternalMessage");
            owner.getQuery(target).doNotification(format, parser.getLocalClient(), message);
        } else {
            final ServerUnknownmessageEvent event
                    = new ServerUnknownmessageEvent(owner, host, target, message);
            final String format = EventUtils.postDisplayable(eventBus, event, "unknownMessage");
            owner.doNotification(format, host, target, message);
        }
    }

    @Override
    public void onUnknownAction(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        if (parser.getLocalClient().equals(parser.getClient(host))) {
            // Local client
            final QuerySelfActionEvent event = new QuerySelfActionEvent(owner.getQuery(target),
                    parser.getLocalClient(), message);
            final String format = EventUtils.postDisplayable(eventBus, event,
                    "querySelfExternalAction");
            owner.getQuery(target).doNotification(format, parser.getLocalClient(), message);
        } else {
            final ServerUnknownactionEvent event
                    = new ServerUnknownactionEvent(owner, host, target, message);
            final String format = EventUtils.postDisplayable(eventBus, event, "unknownAction");
            owner.doNotification(format, host, target, message);
        }
    }

    @Override
    public void onUserModeChanged(final Parser parser, final Date date,
            final ClientInfo client, final String host, final String modes) {
        checkParser(parser);

        final ServerUsermodesEvent event = new ServerUsermodesEvent(owner,
                owner.getParser().getClient(host), modes);
        final String format = EventUtils.postDisplayable(eventBus, event, "userModeChanged");
        owner.doNotification(format, owner.getParser().getClient(host), modes);
    }

    @Override
    public void onUserModeDiscovered(final Parser parser, final Date date,
            final ClientInfo client, final String modes) {
        checkParser(parser);

        final ServerUsermodesEvent event = new ServerUsermodesEvent(owner, client, modes);
        final String format = EventUtils.postDisplayable(eventBus, event, modes.isEmpty()
                || "+".equals(modes) ? "userNoModes" : "userModeDiscovered");
        owner.doNotification(format, client, modes);
    }

    @Override
    public void onInvite(final Parser parser, final Date date, final String userHost,
            final String channel) {
        checkParser(parser);

        owner.addInvite(new Invite(owner, channel, userHost));
        final ServerInvitereceivedEvent event = new ServerInvitereceivedEvent(owner, owner.getParser().
                getClient(userHost), channel);
        final String format = EventUtils.postDisplayable(eventBus, event, "inviteReceived");
        owner.doNotification(format, owner.getParser().getClient(userHost), channel);
    }

    @Override
    public void onWallop(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        final ServerWallopsEvent event = new ServerWallopsEvent(owner,
                owner.getParser().getClient(host), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "wallop");
        owner.doNotification(format, owner.getParser().getClient(host), message);

    }

    @Override
    public void onWalluser(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        final ServerWallusersEvent event = new ServerWallusersEvent(owner,
                owner.getParser().getClient(host), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "walluser");
        owner.doNotification(format, owner.getParser().getClient(host), message);
    }

    @Override
    public void onWallDesync(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        final ServerWalldesyncEvent event = new ServerWalldesyncEvent(owner,
                owner.getParser().getClient(host), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "walldesync");
        owner.doNotification(format, owner.getParser().getClient(host), message);
    }

    @Override
    public void onNickChanged(final Parser parser, final Date date, final ClientInfo client,
            final String oldNick) {
        checkParser(parser);

        if (client.equals(owner.getParser().getLocalClient())) {
            final ServerNickchangeEvent event = new ServerNickchangeEvent(owner, oldNick,
                    client.getNickname());
            final String format = EventUtils.postDisplayable(eventBus, event, "selfNickChange");
            owner.doNotification(format, oldNick, client.getNickname());
            owner.updateTitle();
        }
    }

    @Override
    public void onServerError(final Parser parser, final Date date, final String message) {
        checkParser(parser);

        final ServerErrorEvent event = new ServerErrorEvent(owner, message);
        final String format = EventUtils.postDisplayable(eventBus, event, "serverError");
        owner.doNotification(format, message);
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

}
