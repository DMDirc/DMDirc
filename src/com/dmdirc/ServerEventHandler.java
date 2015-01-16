/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.events.ServerAuthNoticeEvent;
import com.dmdirc.events.ServerAwayEvent;
import com.dmdirc.events.ServerBackEvent;
import com.dmdirc.events.ServerCtcpEvent;
import com.dmdirc.events.ServerCtcpReplyEvent;
import com.dmdirc.events.ServerErrorEvent;
import com.dmdirc.events.ServerGotPingEvent;
import com.dmdirc.events.ServerInviteReceivedEvent;
import com.dmdirc.events.ServerMotdEndEvent;
import com.dmdirc.events.ServerMotdLineEvent;
import com.dmdirc.events.ServerMotdStartEvent;
import com.dmdirc.events.ServerNickChangeEvent;
import com.dmdirc.events.ServerNoPingEvent;
import com.dmdirc.events.ServerNoticeEvent;
import com.dmdirc.events.ServerNumericEvent;
import com.dmdirc.events.ServerPingSentEvent;
import com.dmdirc.events.ServerServerNoticeEvent;
import com.dmdirc.events.ServerStonedEvent;
import com.dmdirc.events.ServerUnknownActionEvent;
import com.dmdirc.events.ServerUnknownMessageEvent;
import com.dmdirc.events.ServerUnknownNoticeEvent;
import com.dmdirc.events.ServerUserModesEvent;
import com.dmdirc.events.ServerWalldesyncEvent;
import com.dmdirc.events.ServerWallopsEvent;
import com.dmdirc.events.ServerWallusersEvent;
import com.dmdirc.events.StatusBarMessageEvent;
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
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.util.EventUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    private static final Logger LOG = LoggerFactory.getLogger(ServerEventHandler.class);

    /** The server instance that owns this event handler. */
    private final Server owner;
    /** Group chat manager to poke for channel events. */
    private final GroupChatManagerImpl groupChatManager;
    /** Event bus to post events to. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of ServerEventHandler.
     *
     * @param owner    The Server instance that we're handling events for
     * @param eventBus The event bus to post events to
     */
    public ServerEventHandler(final Server owner, final GroupChatManagerImpl groupChatManager,
            final DMDircMBassador eventBus) {
        super(eventBus);
        this.owner = owner;
        this.groupChatManager = groupChatManager;
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
        groupChatManager.addChannel(channel);
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

        final ServerCtcpEvent event = new ServerCtcpEvent(owner, owner.getUser(host),
                type, message);
        eventBus.publish(event);
        if (!event.isHandled()) {
            owner.sendCTCPReply(owner.getUser(host).getNickname(), type, message);
        }
    }

    @Override
    public void onPrivateCTCPReply(final Parser parser, final Date date, final String type,
            final String message, final String host) {
        checkParser(parser);

        eventBus.publish(new ServerCtcpReplyEvent(owner, owner.getUser(host), type, message));
    }

    @Override
    public void onSocketClosed(final Parser parser, final Date date) {
        if (owner.getParser().orElse(null) == parser) {
            owner.onSocketClosed();
        }
    }

    @Override
    public void onPrivateNotice(final Parser parser, final Date date,
            final String message, final String host) {
        checkParser(parser);

        final ServerNoticeEvent event = new ServerNoticeEvent(owner, owner.getLocalUser().get(),
                message);
        final String format = EventUtils.postDisplayable(eventBus, event, "privateNotice");
        owner.doNotification(format, owner.getUser(host), message);
    }

    @Override
    public void onServerNotice(final Parser parser, final Date date,
            final String message, final String host) {
        checkParser(parser);

        final ServerServerNoticeEvent event = new ServerServerNoticeEvent(owner,
                owner.getLocalUser().get(), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "serverNotice");
        owner.doNotification(format, owner.getUser(host), message);
    }

    @Override
    public void onMOTDStart(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        final ServerMotdStartEvent event = new ServerMotdStartEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "motdStart");
        owner.doNotification(format, data);
    }

    @Override
    public void onMOTDLine(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        final ServerMotdLineEvent event = new ServerMotdLineEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "motdLine");
        owner.doNotification(format, data);
    }

    @Override
    public void onMOTDEnd(final Parser parser, final Date date,
            final boolean noMOTD, final String data) {
        checkParser(parser);

        final ServerMotdEndEvent event = new ServerMotdEndEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "motdEnd");
        owner.doNotification(format, data);
    }

    @Override
    public void onNumeric(final Parser parser, final Date date, final int numeric,
            final String[] token) {
        checkParser(parser);

        String snumeric = String.valueOf(numeric);

        if (numeric < 10) {
            snumeric = "00" + snumeric;
        } else if (numeric < 100) {
            snumeric = '0' + snumeric;
        }

        final String sansIrcd = "numeric_" + snumeric;
        String target = "";

        if (owner.getConfigManager().hasOptionString("formatter", sansIrcd)) {
            target = sansIrcd;
        } else if (owner.getConfigManager().hasOptionString("formatter", "numeric_unknown")) {
            target = "numeric_unknown";
        }

        final ServerNumericEvent event = new ServerNumericEvent(owner, numeric, token);
        final String format = EventUtils.postDisplayable(eventBus, event, target);
        owner.handleNotification(format, (Object[]) token);
    }

    @Override
    public void onPingFailed(final Parser parser, final Date date) {
        checkParser(parser);

        eventBus.publishAsync(new StatusBarMessageEvent(new StatusMessage(
                "No ping reply from " + owner.getName() + " for over " +
                        (int) Math.floor(parser.getPingTime() / 1000.0) + " seconds.",
                owner.getConfigManager())));

        eventBus.publishAsync(new ServerNoPingEvent(owner, parser.getPingTime()));

        if (parser.getPingTime()
                >= owner.getConfigManager().getOptionInt("server", "pingtimeout")) {
            LOG.warn("Server appears to be stoned, reconnecting");
            eventBus.publishAsync(new ServerStonedEvent(owner));
            owner.reconnect();
        }
    }

    @Override
    public void onPingSent(final Parser parser, final Date date) {
        checkParser(parser);

        eventBus.publishAsync(new ServerPingSentEvent(owner));
    }

    @Override
    public void onPingSuccess(final Parser parser, final Date date) {
        checkParser(parser);

        eventBus.publishAsync(new ServerGotPingEvent(owner,
                owner.getParser().get().getServerLatency()));
    }

    @Override
    public void onAwayState(final Parser parser, final Date date, final AwayState oldState,
            final AwayState currentState, final String reason) {
        checkParser(parser);

        owner.updateAwayState(currentState == AwayState.AWAY ? Optional.of(reason) : Optional.empty());

        if (currentState == AwayState.AWAY) {
            final ServerAwayEvent event = new ServerAwayEvent(owner, reason);
            final String format = EventUtils.postDisplayable(eventBus, event, "away");
            owner.doNotification(format, reason);
        } else if (oldState != AwayState.UNKNOWN) {
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

        final String lastNick = parser.getLocalClient().getNickname();

        // If our last nick is still valid, ignore the in use message
        if (!parser.getStringConverter().equalsIgnoreCase(lastNick, nickname)) {
            return;
        }

        String newNick = lastNick + new Random().nextInt(10);

        final List<String> alts = owner.getProfile().getNicknames();
        int offset = 0;

        // Loop so we can check case sensitivity
        for (String alt : alts) {
            offset++;
            if (parser.getStringConverter().equalsIgnoreCase(alt, lastNick)) {
                break;
            }
        }

        if (offset < alts.size() && !alts.get(offset).isEmpty()) {
            newNick = alts.get(offset);
        }

        parser.getLocalClient().setNickname(newNick);
    }

    @Override
    public void onServerReady(final Parser parser, final Date date) {
        checkParser(parser);
        owner.onPost005();
    }

    @Override
    public void onNoticeAuth(final Parser parser, final Date date, final String data) {
        checkParser(parser);

        final ServerAuthNoticeEvent event = new ServerAuthNoticeEvent(owner, data);
        final String format = EventUtils.postDisplayable(eventBus, event, "authNotice");
        owner.doNotification(format, data);
    }

    @Override
    public void onUnknownNotice(final Parser parser, final Date date, final String message,
            final String target, final String host) {
        checkParser(parser);

        final ServerUnknownNoticeEvent
                event = new ServerUnknownNoticeEvent(owner, host, target, message);
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
                    owner.getLocalUser().get(), message);
            final String format = EventUtils.postDisplayable(eventBus, event,
                    "querySelfExternalMessage");
            owner.getQuery(target).doNotification(format, owner.getLocalUser(), message);
        } else {
            final ServerUnknownMessageEvent event
                    = new ServerUnknownMessageEvent(owner, host, target, message);
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
                    owner.getLocalUser().get(), message);
            final String format = EventUtils.postDisplayable(eventBus, event,
                    "querySelfExternalAction");
            owner.getQuery(target).doNotification(format, owner.getLocalUser(), message);
        } else {
            final ServerUnknownActionEvent event
                    = new ServerUnknownActionEvent(owner, host, target, message);
            final String format = EventUtils.postDisplayable(eventBus, event, "unknownAction");
            owner.doNotification(format, host, target, message);
        }
    }

    @Override
    public void onUserModeChanged(final Parser parser, final Date date,
            final ClientInfo client, final String host, final String modes) {
        checkParser(parser);

        final ServerUserModesEvent event = new ServerUserModesEvent(owner,
                owner.getUser(client.getHostname()), modes);
        final String format = EventUtils.postDisplayable(eventBus, event, "userModeChanged");
        owner.doNotification(format, owner.getUser(client.getHostname()), modes);
    }

    @Override
    public void onUserModeDiscovered(final Parser parser, final Date date,
            final ClientInfo client, final String modes) {
        checkParser(parser);

        final ServerUserModesEvent event = new ServerUserModesEvent(owner,
                owner.getUser(client.getHostname()), modes);
        final String format = EventUtils.postDisplayable(eventBus, event,
                modes.isEmpty() || "+".equals(modes) ? "userNoModes" : "userModeDiscovered");
        owner.doNotification(format, owner.getUser(client.getHostname()), modes);
    }

    @Override
    public void onInvite(final Parser parser, final Date date, final String userHost,
            final String channel) {
        checkParser(parser);

        final Invite invite = new Invite(owner.getInviteManager(), channel, owner.getUser(userHost));
        owner.getInviteManager().addInvite(invite);
        final ServerInviteReceivedEvent event = new ServerInviteReceivedEvent(owner,
                owner.getUser(userHost), channel, invite);
        final String format = EventUtils.postDisplayable(eventBus, event, "inviteReceived");
        owner.doNotification(format, owner.getUser(userHost), channel);
    }

    @Override
    public void onWallop(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        final ServerWallopsEvent event = new ServerWallopsEvent(owner,
                owner.getUser(host), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "wallop");
        owner.doNotification(format, owner.getUser(host), message);

    }

    @Override
    public void onWalluser(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        final ServerWallusersEvent event = new ServerWallusersEvent(owner,
                owner.getLocalUser().get(), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "walluser");
        owner.doNotification(format, owner.getUser(host), message);
    }

    @Override
    public void onWallDesync(final Parser parser, final Date date, final String message,
            final String host) {
        checkParser(parser);

        final ServerWalldesyncEvent event = new ServerWalldesyncEvent(owner,
                owner.getLocalUser().get(), message);
        final String format = EventUtils.postDisplayable(eventBus, event, "walldesync");
        owner.doNotification(format, owner.getUser(host), message);
    }

    @Override
    public void onNickChanged(final Parser parser, final Date date, final ClientInfo client,
            final String oldNick) {
        checkParser(parser);

        if (client.equals(owner.getParser().get().getLocalClient())) {
            final ServerNickChangeEvent event = new ServerNickChangeEvent(owner, oldNick,
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
