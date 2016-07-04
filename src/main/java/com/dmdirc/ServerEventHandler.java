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
import com.dmdirc.events.UserInfoResponseEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.events.AuthNoticeEvent;
import com.dmdirc.parser.events.AwayStateEvent;
import com.dmdirc.parser.events.ChannelSelfJoinEvent;
import com.dmdirc.parser.events.ConnectErrorEvent;
import com.dmdirc.parser.events.ErrorInfoEvent;
import com.dmdirc.parser.events.InviteEvent;
import com.dmdirc.parser.events.MOTDEndEvent;
import com.dmdirc.parser.events.MOTDLineEvent;
import com.dmdirc.parser.events.MOTDStartEvent;
import com.dmdirc.parser.events.NickChangeEvent;
import com.dmdirc.parser.events.NickInUseEvent;
import com.dmdirc.parser.events.NumericEvent;
import com.dmdirc.parser.events.ParserErrorEvent;
import com.dmdirc.parser.events.PingFailureEvent;
import com.dmdirc.parser.events.PingSentEvent;
import com.dmdirc.parser.events.PingSuccessEvent;
import com.dmdirc.parser.events.PrivateActionEvent;
import com.dmdirc.parser.events.PrivateCTCPEvent;
import com.dmdirc.parser.events.PrivateCTCPReplyEvent;
import com.dmdirc.parser.events.PrivateMessageEvent;
import com.dmdirc.parser.events.PrivateNoticeEvent;
import com.dmdirc.parser.events.ServerReadyEvent;
import com.dmdirc.parser.events.SocketCloseEvent;
import com.dmdirc.parser.events.UnknownActionEvent;
import com.dmdirc.parser.events.UnknownMessageEvent;
import com.dmdirc.parser.events.UnknownNoticeEvent;
import com.dmdirc.parser.events.UserInfoEvent;
import com.dmdirc.parser.events.UserModeChangeEvent;
import com.dmdirc.parser.events.UserModeDiscoveryEvent;
import com.dmdirc.parser.events.WallDesyncEvent;
import com.dmdirc.parser.events.WallopEvent;
import com.dmdirc.parser.events.WalluserEvent;
import com.dmdirc.ui.StatusMessage;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import net.engio.mbassy.listener.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.APP_ERROR;
import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Handles parser events for a Server object.
 */
public class ServerEventHandler extends EventHandler {

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
        this.owner = owner;
        this.groupChatManager = groupChatManager;
        this.eventBus = eventBus;
    }

    @Nonnull
    @Override
    protected Connection getConnection() {
        return owner;
    }

    @Handler
    public void onChannelSelfJoin(final ChannelSelfJoinEvent event) {
        groupChatManager.addChannel(event.getChannel());
    }

    @Handler
    public void onPrivateMessage(final PrivateMessageEvent event) {
        if (!owner.hasQuery(event.getHost())) {
            owner.getQuery(event.getHost()).onPrivateMessage(event);
        }
    }

    @Handler
    public void onPrivateAction(final PrivateActionEvent event) {
        if (!owner.hasQuery(event.getHost())) {
            owner.getQuery(event.getHost()).onPrivateAction(event);
        }
    }

    @Handler
    public void onWhoisEvent(final UserInfoEvent event) {
        eventBus.publishAsync(new UserInfoResponseEvent(
                event.getDate(), owner, owner.getUser(event.getClient().getNickname()),
                event.getInfo()));
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Handler
    public void onErrorInfo(final ParserErrorEvent event) {
        LOG.warn(APP_ERROR, "Error in parser", event.getThrowable());
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Handler
    public void onErrorInfo(final ErrorInfoEvent event) {
        final StringBuilder errorString = new StringBuilder();
        errorString.append("Parser exception.\n\n");

        errorString.append("\tLast line:\t");
        errorString.append(event.getErrorInfo().getLastLine()).append('\n');

        errorString.append("\tServer:\t");
        errorString.append(owner.getAddress()).append('\n');

        errorString.append("\tAdditional Information:\n");
        for (final String line : event.getParser().getServerInformationLines()) {
            errorString.append("\t\t").append(line).append('\n');
        }

        final Exception ex = event.getErrorInfo().isException() ? event.getErrorInfo().getException()
                : new Exception(errorString.toString()); // NOPMD

        LOG.warn(event.getErrorInfo().isUserError() ? USER_ERROR : APP_ERROR,
                event.getErrorInfo().getData(), ex);
    }

    @Handler
    public void onPrivateCTCP(final PrivateCTCPEvent event) {
        final ServerCtcpEvent coreEvent = new ServerCtcpEvent(owner, owner.getUser(event.getHost()),
                event.getType(), event.getMessage());
        eventBus.publish(coreEvent);
        if (!coreEvent.isHandled()) {
            owner.sendCTCPReply(owner.getUser(event.getHost()).getNickname(), event.getType(),
                    event.getMessage());
        }
    }

    @Handler
    public void onPrivateCTCPReply(final PrivateCTCPReplyEvent event) {
        eventBus.publish(new ServerCtcpReplyEvent(owner, owner.getUser(event.getHost()),
                event.getType(), event.getMessage()));
    }

    @Handler
    public void onSocketClosed(final SocketCloseEvent event) {
        if (owner.getParser().orElse(null) == event.getParser()) {
            owner.onSocketClosed();
        }
    }

    @Handler
    public void onPrivateNotice(final PrivateNoticeEvent event) {
        eventBus.publishAsync(new ServerNoticeEvent(owner, owner.getUser(event.getHost()),
                event.getMessage()));
    }

    @Handler
    public void onServerNotice(final com.dmdirc.parser.events.ServerNoticeEvent event) {
        eventBus.publishAsync(
                new ServerServerNoticeEvent(owner, owner.getLocalUser().get(), event.getMessage()));
    }

    @Handler
    public void onMOTDStart(final MOTDStartEvent event) {
        eventBus.publishAsync(new ServerMotdStartEvent(owner, event.getData()));
    }

    @Handler
    public void onMOTDLine(final MOTDLineEvent event) {
        eventBus.publishAsync(new ServerMotdLineEvent(owner, event.getData()));
    }

    @Handler
    public void onMOTDEnd(final MOTDEndEvent event) {
        eventBus.publishAsync(new ServerMotdEndEvent(owner, event.getData()));
    }

    @Handler
    public void onNumeric(final NumericEvent event) {
        eventBus.publishAsync(new ServerNumericEvent(owner, event.getNumeric(), event.getToken()));
    }

    @Handler
    public void onPingFailed(final PingFailureEvent event) {
        eventBus.publishAsync(new StatusBarMessageEvent(new StatusMessage(
                "No ping reply from " + owner.getWindowModel().getName() + " for over " +
                        (int) Math.floor(event.getParser().getPingTime() / 1000.0) + " seconds.",
                owner.getWindowModel().getConfigManager())));

        eventBus.publishAsync(new ServerNoPingEvent(owner, event.getParser().getPingTime()));

        if (event.getParser().getPingTime()
                >= owner.getWindowModel().getConfigManager().getOptionInt("server", "pingtimeout")) {
            LOG.warn("Server appears to be stoned, reconnecting");
            eventBus.publishAsync(new ServerStonedEvent(owner));
            owner.reconnect();
        }
    }

    @Handler
    public void onPingSent(final PingSentEvent event) {
        eventBus.publishAsync(new ServerPingSentEvent(owner));
    }

    @Handler
    public void onPingSuccess(final PingSuccessEvent event) {
        eventBus.publishAsync(new ServerGotPingEvent(owner,
                owner.getParser().get().getServerLatency()));
    }

    @Handler
    public void onAwayState(final AwayStateEvent event) {
        owner.updateAwayState(event.getNewState() == AwayState.AWAY ?
                Optional.of(event.getReason()) : Optional.empty());

        if (event.getNewState() == AwayState.AWAY) {
            eventBus.publishAsync(new ServerAwayEvent(owner, event.getReason()));
        } else if (event.getOldState() != AwayState.UNKNOWN) {
            eventBus.publishAsync(new ServerBackEvent(owner));
        }
    }

    @Handler
    public void onConnectError(final ConnectErrorEvent event) {
        owner.onConnectError(event.getErrorInfo());
    }

    @Handler
    public void onNickInUse(final NickInUseEvent event) {
        final String lastNick = event.getParser().getLocalClient().getNickname();

        // If our last nick is still valid, ignore the in use message
        if (!event.getParser().getStringConverter().equalsIgnoreCase(lastNick, event.getNickname())) {
            return;
        }

        String newNick = lastNick + new Random().nextInt(10);

        final List<String> alts = owner.getProfile().getNicknames();
        int offset = 0;

        // Loop so we can check case sensitivity
        for (String alt : alts) {
            offset++;
            if (event.getParser().getStringConverter().equalsIgnoreCase(alt, lastNick)) {
                break;
            }
        }

        if (offset < alts.size() && !alts.get(offset).isEmpty()) {
            newNick = alts.get(offset);
        }

        event.getParser().getLocalClient().setNickname(newNick);
    }

    @Handler
    public void onServerReady(final ServerReadyEvent event) {
        owner.onPost005();
    }

    @Handler
    public void onNoticeAuth(final AuthNoticeEvent event) {
         eventBus.publishAsync(new ServerAuthNoticeEvent(owner, event.getMessage()));
    }

    @Handler
    public void onUnknownNotice(final UnknownNoticeEvent event) {
        eventBus.publishAsync(new ServerUnknownNoticeEvent(
                owner, event.getHost(), event.getTarget(),event.getMessage()));
    }

    @Handler
    public void onUnknownMessage(final UnknownMessageEvent event) {
        if (event.getParser().getLocalClient().equals(event.getParser().getClient(event.getHost()))) {
            // Local client
            eventBus.publishAsync(
                    new QuerySelfMessageEvent(owner.getQuery(event.getTarget()), owner.getLocalUser().get(),
                            event.getMessage()));
        } else {
            eventBus.publishAsync(new ServerUnknownMessageEvent(
                    owner, event.getHost(), event.getTarget(), event.getMessage()));
        }
    }

    @Handler
    public void onUnknownAction(final UnknownActionEvent event) {
        if (event.getParser().getLocalClient().equals(event.getParser().getClient(event.getHost()))) {
            // Local client
            eventBus.publishAsync(
                    new QuerySelfActionEvent(owner.getQuery(event.getTarget()), owner.getLocalUser().get(),
                            event.getMessage()));
        } else {
            eventBus.publishAsync(new ServerUnknownActionEvent(
                    owner, event.getHost(), event.getTarget(), event.getMessage()));
        }
    }

    @Handler
    public void onUserModeChanged(final UserModeChangeEvent event) {
        eventBus.publishAsync(new ServerUserModesEvent(
                owner, owner.getUser(event.getClient().getHostname()), event.getModes()));
    }

    @Handler
    public void onUserModeDiscovered(final UserModeDiscoveryEvent event) {
        eventBus.publishAsync(new ServerUserModesEvent(
                owner, owner.getUser(event.getClient().getHostname()), event.getModes()));
    }

    @Handler
    public void onInvite(final InviteEvent event) {
        final Invite invite = new Invite(owner.getInviteManager(), event.getChannel(),
                owner.getUser(event.getUserHost()));
        owner.getInviteManager().addInvite(invite);
        eventBus.publishAsync(new ServerInviteReceivedEvent(owner,
                owner.getUser(event.getUserHost()), event.getChannel(), invite));
    }

    @Handler
    public void onWallop(final WallopEvent event) {
        eventBus.publishAsync(new ServerWallopsEvent(owner,
                owner.getUser(event.getHost()), event.getMessage()));
    }

    @Handler
    public void onWalluser(final WalluserEvent event) {
        eventBus.publishAsync(new ServerWallusersEvent(owner,
                owner.getLocalUser().get(), event.getMessage()));
    }

    @Handler
    public void onWallDesync(final WallDesyncEvent event) {
        eventBus.publishAsync(new ServerWalldesyncEvent(owner,
                owner.getLocalUser().get(), event.getMessage()));
    }

    @Handler
    public void onNickChanged(final NickChangeEvent event) {
        if (event.getClient().equals(owner.getParser().get().getLocalClient())) {
            eventBus.publishAsync(new ServerNickChangeEvent(owner,
                    event.getOldNick(), event.getClient().getNickname()));
            owner.updateTitle();
        }

        owner.handleNickChange(event.getClient(), event.getOldNick());
    }

    @Handler
    public void onServerError(final com.dmdirc.parser.events.ServerErrorEvent event) {
        eventBus.publishAsync(new ServerErrorEvent(owner, event.getMessage()));
    }

}
