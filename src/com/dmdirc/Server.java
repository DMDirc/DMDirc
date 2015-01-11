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

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.ServerConnectErrorEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerConnectingEvent;
import com.dmdirc.events.ServerDisconnectedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChatManager;
import com.dmdirc.interfaces.InviteManager;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.parser.common.DefaultStringConverter;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.parser.common.ParserError;
import com.dmdirc.parser.common.ThreadedParser;
import com.dmdirc.parser.interfaces.EncodingParser;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.ProtocolDescription;
import com.dmdirc.parser.interfaces.SecureParser;
import com.dmdirc.parser.interfaces.StringConverter;
import com.dmdirc.tls.CertificateManager;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.HighlightManager;
import com.dmdirc.ui.messages.sink.MessageSinkManager;

import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Server class represents the client's view of a server. It maintains a list of all channels,
 * queries, etc, and handles parser callbacks pertaining to the server.
 */
public class Server extends FrameContainer implements Connection {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    /** The name of the general domain. */
    private static final String DOMAIN_GENERAL = "general";

    /** Manager of group chats. */
    private final GroupChatManagerImpl groupChatManager;
    /** Manager of invites. */
    private final InviteManager inviteManager;
    /** Open query windows on the server. */
    private final Map<String, Query> queries = new ConcurrentSkipListMap<>();
    /** The user manager to retrieve users from. */
    private final UserManager userManager;

    /** The Parser instance handling this server. */
    @Nonnull
    private Optional<Parser> parser = Optional.empty();
    /** The Parser instance that used to be handling this server. */
    @Nonnull
    private Optional<Parser> oldParser = Optional.empty();
    /** The parser-supplied protocol description object. */
    @Nonnull
    private Optional<ProtocolDescription> protocolDescription = Optional.empty();

    /**
     * Object used to synchronise access to parser. This object should be locked by anything
     * requiring that the parser reference remains the same for a duration of time, or by anything
     * which is updating the parser reference.
     *
     * If used in conjunction with myStateLock, the parserLock must always be locked INSIDE the
     * myStateLock to prevent deadlocks.
     */
    private final ReadWriteLock parserLock = new ReentrantReadWriteLock();

    /** Object used to synchronise access to myState. */
    private final Object myStateLock = new Object();
    /** The current state of this server. */
    private final ServerStatus myState = new ServerStatus(this, myStateLock);

    /** The address of the server we're connecting to. */
    @Nonnull
    private URI address;
    /** The profile we're using. */
    @Nonnull
    private Profile profile;

    /** Our reason for being away, if any. */
    private Optional<String> awayMessage;
    /** Our event handler. */
    private final ServerEventHandler eventHandler;
    /** Our ignore list. */
    private final IgnoreList ignoreList = new IgnoreList();
    /** Our string convertor. */
    private StringConverter converter = new DefaultStringConverter();
    /** ParserFactory we use for creating parsers. */
    private final ParserFactory parserFactory;
    /** Factory to use to create new identities. */
    private final IdentityFactory identityFactory;
    /** The migrator to use to change our config provider. */
    private final ConfigProviderMigrator configMigrator;
    /** Factory to use for creating queries. */
    private final QueryFactory queryFactory;
    /** The config provider to write user settings to. */
    private final ConfigProvider userSettings;
    /** Executor service to use to schedule repeated events. */
    private final ScheduledExecutorService executorService;
    /** The message encoder factory to create a message encoder with. */
    private final MessageEncoderFactory messageEncoderFactory;
    /** The manager to use for highlighting. */
    private final HighlightManager highlightManager;
    /** Listener to use for config changes. */
    private final ConfigChangeListener configListener = (domain, key) -> updateTitle();
    /** The future used when a reconnect timer is scheduled. */
    private ScheduledFuture<?> reconnectTimerFuture;

    /**
     * Creates a new server which will connect to the specified URL with the specified profile.
     */
    public Server(
            final ConfigProviderMigrator configMigrator,
            final CommandParser commandParser,
            final ParserFactory parserFactory,
            final TabCompleterFactory tabCompleterFactory,
            final IdentityFactory identityFactory,
            final MessageSinkManager messageSinkManager,
            final QueryFactory queryFactory,
            final DMDircMBassador eventBus,
            final MessageEncoderFactory messageEncoderFactory,
            final ConfigProvider userSettings,
            final GroupChatManagerImplFactory groupChatManagerFactory,
            final ScheduledExecutorService executorService,
            @Nonnull final URI uri,
            @Nonnull final Profile profile,
            final BackBufferFactory backBufferFactory,
            final UserManager userManager) {
        super(null, "server-disconnected",
                getHost(uri),
                getHost(uri),
                configMigrator.getConfigProvider(),
                backBufferFactory,
                commandParser,
                tabCompleterFactory.getTabCompleter(configMigrator.getConfigProvider(),
                        CommandType.TYPE_SERVER, CommandType.TYPE_GLOBAL),
                messageSinkManager,
                eventBus,
                Arrays.asList(
                        WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier(),
                        WindowComponent.CERTIFICATE_VIEWER.getIdentifier()));

        this.parserFactory = parserFactory;
        this.identityFactory = identityFactory;
        this.configMigrator = configMigrator;
        this.queryFactory = queryFactory;
        this.executorService = executorService;
        this.userSettings = userSettings;
        this.messageEncoderFactory = messageEncoderFactory;
        this.userManager = userManager;
        this.groupChatManager = groupChatManagerFactory.create(this, executorService);
        this.inviteManager = new InviteManagerImpl(this);

        awayMessage = Optional.empty();
        eventHandler = new ServerEventHandler(this, groupChatManager, eventBus);

        this.address = uri;
        this.profile = profile;
        setConnectionDetails(uri, profile);

        updateIcon();

        getConfigManager().addChangeListener("formatter", "serverName", configListener);
        getConfigManager().addChangeListener("formatter", "serverTitle", configListener);

        initBackBuffer();
        this.highlightManager = new HighlightManager(getConfigManager(),
                new ColourManager(getConfigManager(), getEventBus()));
        highlightManager.init();
        getEventBus().subscribe(highlightManager);
    }

    /**
     * Updates the connection details for this server. If the specified URI does not define a port,
     * the default port from the protocol description will be used.
     *
     * @param uri     The new URI that this server should connect to
     * @param profile The profile that this server should use
     */
    private void setConnectionDetails(final URI uri, final Profile profile) {
        this.address = checkNotNull(uri);
        this.protocolDescription = Optional.ofNullable(parserFactory.getDescription(uri));
        this.profile = profile;

        if (uri.getPort() == -1) {
            protocolDescription.ifPresent(pd -> {
                try {
                    this.address = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                            pd.getDefaultPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
                } catch (URISyntaxException ex) {
                    getEventBus().publish(
                            new AppErrorEvent(ErrorLevel.MEDIUM, ex, "Unable to construct URI",
                                    ""));
                }
            });
        }
    }

    @Override
    public void connect() {
        connect(address, profile);
    }

    @Override
    @Precondition({
        "The current parser is null or not connected",
        "The specified profile is not null"
    })
    @SuppressWarnings("fallthrough")
    public void connect(final URI address, final Profile profile) {
        checkNotNull(address);
        checkNotNull(profile);

        synchronized (myStateLock) {
            LOG.info("Connecting to {}, current state is {}", address, myState.getState());

            switch (myState.getState()) {
                case RECONNECT_WAIT:
                    LOG.debug("Cancelling reconnection timer");
                    if (reconnectTimerFuture != null) {
                        reconnectTimerFuture.cancel(false);
                    }
                    break;
                case CLOSING:
                    // Ignore the connection attempt
                    return;
                case CONNECTED:
                case CONNECTING:
                    disconnect(getConfigManager().getOption(DOMAIN_GENERAL, "quitmessage"));
                case DISCONNECTING:
                    while (!myState.getState().isDisconnected()) {
                        try {
                            myStateLock.wait();
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                    break;
                default:
                    // Do nothing
                    break;
            }

            final URI connectAddress;
            final Parser newParser;

            try {
                parserLock.writeLock().lock();
                if (parser.isPresent()) {
                    throw new IllegalArgumentException("Connection attempt while parser "
                            + "is still connected.\n\nMy state:" + getState());
                }

                configMigrator.migrate(address.getScheme(), "", "", address.getHost());

                setConnectionDetails(address, profile);

                updateTitle();
                updateIcon();

                parser = Optional.ofNullable(buildParser());

                if (!parser.isPresent()) {
                    addLine("serverUnknownProtocol", address.getScheme());
                    return;
                }

                newParser = parser.get();
                connectAddress = newParser.getURI();
            } finally {
                parserLock.writeLock().unlock();
            }

            addLine("serverConnecting", connectAddress.getHost(), connectAddress.getPort());

            myState.transition(ServerState.CONNECTING);

            doCallbacks();

            updateAwayState(Optional.empty());
            inviteManager.removeInvites();

            newParser.connect();
            if (newParser instanceof ThreadedParser) {
                ((ThreadedParser) newParser).getControlThread()
                        .setName("Parser - " + connectAddress.getHost());
            }
        }

        getEventBus().publish(new ServerConnectingEvent(this));
    }

    @Override
    public void reconnect(final String reason) {
        synchronized (myStateLock) {
            if (myState.getState() == ServerState.CLOSING) {
                return;
            }

            disconnect(reason);

            connect(address, profile);
        }
    }

    @Override
    public void reconnect() {
        reconnect(getConfigManager().getOption(DOMAIN_GENERAL, "reconnectmessage"));
    }

    @Override
    public void disconnect() {
        disconnect(getConfigManager().getOption(DOMAIN_GENERAL, "quitmessage"));
    }

    @Override
    public void disconnect(final String reason) {
        synchronized (myStateLock) {
            LOG.info("Disconnecting. Current state: {}", myState.getState());

            switch (myState.getState()) {
                case CLOSING:
                case DISCONNECTING:
                case DISCONNECTED:
                case TRANSIENTLY_DISCONNECTED:
                    return;
                case RECONNECT_WAIT:
                    LOG.debug("Cancelling reconnection timer");
                    if (reconnectTimerFuture != null) {
                        reconnectTimerFuture.cancel(false);
                    }
                    break;
                default:
                    break;
            }

            groupChatManager.handleDisconnect();

            try {
                parserLock.readLock().lock();
                if (parser.isPresent()) {
                    myState.transition(ServerState.DISCONNECTING);

                    inviteManager.removeInvites();
                    updateIcon();

                    parser.get().disconnect(reason);
                } else {
                    myState.transition(ServerState.DISCONNECTED);
                }
            } finally {
                parserLock.readLock().unlock();
            }

            if (getConfigManager().getOptionBool(DOMAIN_GENERAL, "closequeriesonquit")) {
                closeQueries();
            }
        }
    }

    /**
     * Schedules a reconnect attempt to be performed after a user-defined delay.
     */
    @Precondition("The server state is transiently disconnected")
    private void doDelayedReconnect() {
        synchronized (myStateLock) {
            LOG.info("Performing delayed reconnect. State: {}", myState.getState());

            if (myState.getState() != ServerState.TRANSIENTLY_DISCONNECTED) {
                throw new IllegalStateException("doDelayedReconnect when not "
                        + "transiently disconnected\n\nState: " + myState);
            }

            final int delay = Math.max(1000,
                    getConfigManager().getOptionInt(DOMAIN_GENERAL, "reconnectdelay"));

            handleNotification("connectRetry", getAddress(), delay / 1000);

            reconnectTimerFuture = executorService.schedule(() -> {
                synchronized (myStateLock) {
                    LOG.debug("Reconnect task executing, state: {}", myState.getState());
                    if (myState.getState() == ServerState.RECONNECT_WAIT) {
                        myState.transition(ServerState.TRANSIENTLY_DISCONNECTED);
                        reconnect();
                    }
                }
            }, delay, TimeUnit.MILLISECONDS);

            LOG.info("Scheduling reconnect task for delay of {}", delay);

            myState.transition(ServerState.RECONNECT_WAIT);
            updateIcon();
        }
    }

    @Override
    public boolean hasQuery(final String host) {
        return queries.containsKey(converter.toLowerCase(getUser(host).getNickname()));
    }

    @Override
    public Optional<User> getLocalUser() {
        return parser.map(Parser::getLocalClient)
                .map(client -> userManager.getUserFromClientInfo(client, this));
    }

    @Override
    public User getUser(final String details) {
        return parser.map(p -> p.getClient(details))
                .map(client -> userManager.getUserFromClientInfo(client, this)).get();
    }

    @Override
    public Query getQuery(final String host) {
        return getQuery(host, false);
    }

    @Override
    public Query getQuery(final String host, final boolean focus) {
        synchronized (myStateLock) {
            if (myState.getState() == ServerState.CLOSING) {
                // Can't open queries while the server is closing
                return null;
            }
        }

        final String nick = getUser(host).getNickname();
        final String lnick = converter.toLowerCase(nick);

        if (!queries.containsKey(lnick)) {
            final Query newQuery = queryFactory.getQuery(this, getUser(host));
            if (!getState().isDisconnected()) {
                newQuery.reregister();
            }
            getTabCompleter().addEntry(TabCompletionType.QUERY_NICK, nick);
            queries.put(lnick, newQuery);
        }

        return queries.get(lnick);
    }

    @Override
    public void updateQuery(final Query query, final String oldNick, final String newNick) {
        getTabCompleter().removeEntry(TabCompletionType.QUERY_NICK, oldNick);
        getTabCompleter().addEntry(TabCompletionType.QUERY_NICK, newNick);

        queries.put(converter.toLowerCase(newNick), query);
        queries.remove(converter.toLowerCase(oldNick));
    }

    @Override
    public Collection<Query> getQueries() {
        return Collections.unmodifiableCollection(queries.values());
    }

    @Override
    public void delQuery(final Query query) {
        getTabCompleter().removeEntry(TabCompletionType.QUERY_NICK, query.getNickname());
        queries.remove(converter.toLowerCase(query.getNickname()));
    }

    /**
     * Closes all open query windows associated with this server.
     */
    private void closeQueries() {
        new ArrayList<>(queries.values()).forEach(Query::close);
    }

    /**
     * Retrieves the host component of the specified URI, or throws a relevant exception if this is
     * not possible.
     *
     * @param uri The URI to be processed
     *
     * @return The URI's host component, as returned by {@link URI#getHost()}.
     *
     * @throws NullPointerException     If <code>uri</code> is null
     * @throws IllegalArgumentException If the specified URI has no host
     * @since 0.6.4
     */
    private static String getHost(final URI uri) {
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("URIs must have hosts");
        }

        return uri.getHost();
    }

    /**
     * Builds an appropriately configured {@link Parser} for this server.
     *
     * @return A configured parser.
     */
    @Nullable
    private Parser buildParser() {
        final Parser myParser = parserFactory.getParser(profile, address, getConfigManager())
                .orElse(null);

        if (myParser != null) {
            myParser.setIgnoreList(ignoreList);
        }

        if (myParser instanceof SecureParser) {
            final CertificateManager certificateManager =
                    new CertificateManager(this, address.getHost(), getConfigManager(),
                            userSettings, getEventBus());
            final SecureParser secureParser = (SecureParser) myParser;
            secureParser.setTrustManagers(certificateManager);
            secureParser.setKeyManagers(certificateManager.getKeyManager());
        }

        if (myParser instanceof EncodingParser) {
            final EncodingParser encodingParser = (EncodingParser) myParser;
            encodingParser.setEncoder(messageEncoderFactory.getMessageEncoder(this, myParser));
        }

        return myParser;
    }

    @Override
    public boolean compareURI(final URI uri) {
        return parser.map(p -> p.compareURI(uri)).orElse(
                oldParser.map(op -> op.compareURI(uri)).orElse(false));
    }

    /**
     * Updates this server's icon.
     */
    private void updateIcon() {
        final String icon = myState.getState() == ServerState.CONNECTED
                ? protocolDescription.get().isSecure(address)
                ? "secure-server" : "server" : "server-disconnected";
        setIcon(icon);
    }

    /**
     * Registers callbacks.
     */
    private void doCallbacks() {
        eventHandler.registerCallbacks();
        queries.values().forEach(Query::reregister);
    }

    @Override
    public void sendLine(final String line) {
        synchronized (myStateLock) {
            try {
                parserLock.readLock().lock();
                parser.ifPresent(p -> {
                    if (!line.isEmpty() && myState.getState() == ServerState.CONNECTED) {
                        p.sendRawMessage(line);
                    }
                });
            } finally {
                parserLock.readLock().unlock();
            }
        }
    }

    @Override
    public void sendMessage(final String target, final String message) {
        parser.ifPresent(p -> {
            if (!message.isEmpty()) {
                p.sendMessage(target, message);
            }
        });
    }

    @Override
    public int getMaxLineLength() {
        return withParserReadLock(Parser::getMaxLength, -1);
    }

    @Override
    @Nonnull
    public Optional<Parser> getParser() {
        return parser;
    }

    @Nonnull
    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public String getAddress() {
        return withParserReadLock(Parser::getServerName, address.getHost());
    }

    @Override
    public String getNetwork() {
        try {
            parserLock.readLock().lock();
            return parser.map(p -> p.getNetworkName().isEmpty()
                            ? getNetworkFromServerName(p.getServerName()) : p.getNetworkName())
                    .orElseThrow(() -> new IllegalStateException(
                            "getNetwork called when " + "parser is null (state: " + getState() +
                                    ')'));
        } finally {
            parserLock.readLock().unlock();
        }
    }

    @Override
    public boolean isNetwork(final String target) {
        synchronized (myStateLock) {
            return withParserReadLock(p -> getNetwork().equalsIgnoreCase(target), false);
        }
    }

    /**
     * Calculates a network name from the specified server name. This method implements parts 2-4 of
     * the procedure documented at getNetwork().
     *
     * @param serverName The server name to parse
     *
     * @return A network name for the specified server
     */
    protected static String getNetworkFromServerName(final String serverName) {
        final String[] parts = serverName.split("\\.");
        final String[] tlds = {"biz", "com", "info", "net", "org"};
        boolean isTLD = false;

        for (String tld : tlds) {
            if (serverName.endsWith('.' + tld)) {
                isTLD = true;
                break;
            }
        }

        if (isTLD && parts.length > 2) {
            return parts[parts.length - 2] + '.' + parts[parts.length - 1];
        } else if (parts.length > 2) {
            final StringBuilder network = new StringBuilder();

            for (int i = 1; i < parts.length; i++) {
                if (network.length() > 0) {
                    network.append('.');
                }

                network.append(parts[i]);
            }

            return network.toString();
        } else {
            return serverName;
        }
    }

    @Override
    public String getIrcd() {
        return parser.get().getServerSoftwareType();
    }

    @Override
    public String getProtocol() {
        return address.getScheme();
    }

    @Override
    public boolean isAway() {
        return awayMessage.isPresent();
    }

    @Override
    public String getAwayMessage() {
        return awayMessage.orElse(null);
    }

    @Override
    public ServerState getState() {
        return myState.getState();
    }

    @Override
    public ServerStatus getStatus() {
        return myState;
    }

    @Override
    public void close() {
        synchronized (myStateLock) {
            eventHandler.unregisterCallbacks();
            getConfigManager().removeListener(configListener);
            highlightManager.stop();
            getEventBus().unsubscribe(highlightManager);
            executorService.shutdown();

            disconnect();

            myState.transition(ServerState.CLOSING);
        }

        groupChatManager.closeAll();
        closeQueries();
        inviteManager.removeInvites();

        super.close();
    }

    @Override
    public FrameContainer getWindowModel() {
        return this;
    }

    @Override
    public void addLineToAll(final String messageType, final Date date,
            final Object... args) {
        groupChatManager.addLineToAll(messageType, date, args);

        for (Query query : queries.values()) {
            query.addLine(messageType, date, args);
        }

        addLine(messageType, date, args);
    }

    @Override
    public void sendCTCPReply(final String source, final String type, final String args) {
        if ("VERSION".equalsIgnoreCase(type)) {
            parser.get().sendCTCPReply(source, "VERSION",
                    "DMDirc " + getConfigManager().getOption("version", "version") +
                            " - https://www.dmdirc.com/");
        } else if ("PING".equalsIgnoreCase(type)) {
            parser.get().sendCTCPReply(source, "PING", args);
        } else if ("CLIENTINFO".equalsIgnoreCase(type)) {
            parser.get().sendCTCPReply(source, "CLIENTINFO", "VERSION PING CLIENTINFO");
        }
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.of(this);
    }

    @Override
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        if (arg instanceof User) {
            final User clientInfo = (User) arg;
            args.add(clientInfo.getNickname());
            args.add(clientInfo.getUsername());
            args.add(clientInfo.getHostname());
            return true;
        } else {
            return super.processNotificationArg(arg, args);
        }
    }

    @Override
    public void updateTitle() {
        synchronized (myStateLock) {
            if (myState.getState() == ServerState.CLOSING) {
                return;
            }

            try {
                parserLock.readLock().lock();
                final Object[] arguments = {
                    address.getHost(), parser.map(Parser::getServerName).orElse("Unknown"),
                    address.getPort(), parser.map(p -> getNetwork()).orElse("Unknown"),
                    getLocalUser().map(User::getNickname).orElse("Unknown")
                };

                setName(Formatter.formatMessage(getConfigManager(),
                        "serverName", arguments));
                setTitle(Formatter.formatMessage(getConfigManager(),
                        "serverTitle", arguments));
            } finally {
                parserLock.readLock().unlock();
            }
        }
    }

    /**
     * Called when the socket has been closed.
     */
    public void onSocketClosed() {
        LOG.info("Received socket closed event, state: {}", myState.getState());

        if (Thread.holdsLock(myStateLock)) {
            LOG.info("State lock contended: rerunning on a new thread");

            executorService.schedule(this::onSocketClosed, 0, TimeUnit.SECONDS);
            return;
        }

        handleNotification("socketClosed", getAddress());

        getEventBus().publish(new ServerDisconnectedEvent(this));

        eventHandler.unregisterCallbacks();

        synchronized (myStateLock) {
            if (myState.getState() == ServerState.CLOSING
                    || myState.getState() == ServerState.DISCONNECTED) {
                // This has been triggered via .disconnect()
                return;
            }

            if (myState.getState() == ServerState.DISCONNECTING) {
                myState.transition(ServerState.DISCONNECTED);
            } else {
                myState.transition(ServerState.TRANSIENTLY_DISCONNECTED);
            }

            groupChatManager.handleSocketClosed();

            try {
                parserLock.writeLock().lock();
                oldParser = parser;
                parser = Optional.empty();
            } finally {
                parserLock.writeLock().unlock();
            }

            updateIcon();

            if (getConfigManager().getOptionBool(DOMAIN_GENERAL, "closequeriesondisconnect")) {
                closeQueries();
            }

            inviteManager.removeInvites();
            updateAwayState(Optional.empty());

            if (getConfigManager().getOptionBool(DOMAIN_GENERAL,
                    "reconnectondisconnect")
                    && myState.getState() == ServerState.TRANSIENTLY_DISCONNECTED) {
                doDelayedReconnect();
            }
        }
    }

    /**
     * Called when an error was encountered while connecting.
     *
     * @param errorInfo The parser's error information
     */
    @Precondition("The current server state is CONNECTING")
    public void onConnectError(final ParserError errorInfo) {
        synchronized (myStateLock) {
            LOG.info("Received connect error event, state: {}; error: {}", myState.getState(),
                    errorInfo);

            if (myState.getState() == ServerState.CLOSING
                    || myState.getState() == ServerState.DISCONNECTING) {
                // Do nothing
                return;
            } else if (myState.getState() != ServerState.CONNECTING) {
                // Shouldn't happen
                throw new IllegalStateException("Connect error when not "
                        + "connecting\n\n" + getStatus().getTransitionHistory());
            }

            myState.transition(ServerState.TRANSIENTLY_DISCONNECTED);

            try {
                parserLock.writeLock().lock();
                oldParser = parser;
                parser = Optional.empty();
            } finally {
                parserLock.writeLock().unlock();
            }

            updateIcon();

            final String description;

            if (errorInfo.getException() == null) {
                description = errorInfo.getData();
            } else {
                final Exception exception = errorInfo.getException();

                if (exception instanceof UnknownHostException) {
                    description = "Unknown host (unable to resolve)";
                } else if (exception instanceof NoRouteToHostException) {
                    description = "No route to host";
                } else if (exception instanceof SocketTimeoutException) {
                    description = "Connection attempt timed out";
                } else if (exception instanceof SocketException
                        || exception instanceof SSLException) {
                    description = exception.getMessage();
                } else {
                    getEventBus().publish(new AppErrorEvent(ErrorLevel.LOW,
                            new IllegalArgumentException(exception),
                            "Unknown socket error: " + exception.getClass().getCanonicalName(),
                            ""));
                    description = "Unknown error: " + exception.getMessage();
                }
            }

            getEventBus().publish(new ServerConnectErrorEvent(this, description));

            handleNotification("connectError", getAddress(), description);

            if (getConfigManager().getOptionBool(DOMAIN_GENERAL, "reconnectonconnectfailure")) {
                doDelayedReconnect();
            }
        }
    }

    /**
     * Called after the parser receives the 005 headers from the server.
     */
    @Precondition("State is CONNECTING")
    public void onPost005() {
        synchronized (myStateLock) {
            if (myState.getState() != ServerState.CONNECTING) {
                // Shouldn't happen
                throw new IllegalStateException("Received onPost005 while not "
                        + "connecting\n\n" + myState.getTransitionHistory());
            }

            myState.transition(ServerState.CONNECTED);

            configMigrator.migrate(address.getScheme(),
                    parser.get().getServerSoftwareType(), getNetwork(), parser.get().getServerName());

            updateIcon();
            updateTitle();
            updateIgnoreList();

            converter = parser.get().getStringConverter();
            groupChatManager.handleConnected();
        }

        getEventBus().publish(new ServerConnectedEvent(this));
    }

    @Override
    public IgnoreList getIgnoreList() {
        return ignoreList;
    }

    @Override
    public void updateIgnoreList() {
        ignoreList.clear();
        ignoreList.addAll(getConfigManager().getOptionList("network", "ignorelist"));
    }

    @Override
    public void saveIgnoreList() {
        getNetworkIdentity().setOption("network", "ignorelist", ignoreList.getRegexList());
    }

    @Override
    public ConfigProvider getServerIdentity() {
        return identityFactory.createServerConfig(parser.get().getServerName());
    }

    @Override
    public ConfigProvider getNetworkIdentity() {
        return identityFactory.createNetworkConfig(getNetwork());
    }

    @Override
    public void updateAwayState(final Optional<String> message) {
        checkNotNull(message);
        if (awayMessage.equals(message)) {
            return;
        }

        awayMessage = message;
    }

    @Override
    public String getUserModes() {
        return getParser().map(Parser::getChannelUserModes).orElse("");
    }

    @Override
    public String getBooleanModes() {
        return getParser().map(Parser::getBooleanChannelModes).orElse("");
    }

    @Override
    public String getListModes() {
        return getParser().map(Parser::getListChannelModes).orElse("");
    }

    @Override
    public String getParameterModes() {
        return getParser().map(Parser::getParameterChannelModes).orElse("");
    }

    @Override
    public String getDoubleParameterModes() {
        return getParser().map(Parser::getDoubleParameterChannelModes).orElse("");
    }

    @Override
    public int getMaxListModes(final char mode) {
        return getParser().map(p -> p.getMaxListModes(mode)).orElse(-1);
    }

    @Override
    public GroupChatManager getGroupChatManager() {
        return groupChatManager;
    }

    @Override
    public InviteManager getInviteManager() {
        return inviteManager;
    }

    @Override
    public void setNickname(final String nickname) {
        parser.map(Parser::getLocalClient).ifPresent(c -> c.setNickname(nickname));
    }

    /**
     * Utility method to get a result from the parser while holding the {@link #parserLock}
     * read lock.
     *
     * @param func The function to use to retrieve information from the parser.
     * @param orElse The value to return if the parser is otherwise not present.
     * @param <T> The type of result returned.
     * @return The value returned by {@code func}, if the parser is present, otherwise the
     * {@code orElse} value.
     */
    private <T> T withParserReadLock(
            final Function<? super Parser, ? extends T> func,
            final T orElse) {
        try {
            parserLock.readLock().lock();
            return parser.map(func).orElse(orElse);
        } finally {
            parserLock.readLock().unlock();
        }
    }

}
