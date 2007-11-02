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
import com.dmdirc.actions.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.interfaces.InviteListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.MyInfo;
import com.dmdirc.parser.ParserError;
import com.dmdirc.parser.ServerInfo;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.Formatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Server class represents the client's view of a server. It maintains
 * a list of all channels, queries, etc, and handles parser callbacks pertaining
 * to the server.
 *
 * @author chris
 */
public final class Server extends WritableFrameContainer implements Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** The name of the general domain. */
    private static final String DOMAIN_GENERAL = "general".intern();
    /** The name of the profile domain. */
    private static final String DOMAIN_PROFILE = "profile".intern();
    /** The name of the server domain. */
    private static final String DOMAIN_SERVER = "server".intern();

    /** The name of the server notification target. */
    private static final String NOTIFICATION_SERVER = "server".intern();

    /** Open channels that currently exist on the server. */
    private final Map<String, Channel> channels  = new Hashtable<String, Channel>();
    /** Open query windows on the server. */
    private final Map<String, Query> queries = new Hashtable<String, Query>();

    /** The IRC Parser instance handling this server. */
    private transient IRCParser parser;
    /** The raw frame used for this server instance. */
    private Raw raw;
    /** The ServerWindow corresponding to this server. */
    private ServerWindow window;

    /** The details of the server we're connecting to. */
    private ServerInfo serverInfo;

    /** The profile we're using. */
    private transient Identity profile;

    /** The current state of this server. */
    private ServerState myState = ServerState.DISCONNECTED;
    /** The timer we're using to delay reconnects. */
    private Timer reconnectTimer;

    /** Channels we're meant to auto-join. */
    private final List<String> autochannels;

    /** The tabcompleter used for this server. */
    private final TabCompleter tabCompleter = new TabCompleter();
    /** The last activated internal frame for this server. */
    private FrameContainer activeFrame = this;
    /** The config manager for this server. */
    private ConfigManager configManager;

    /** Our reason for being away, if any. */
    private String awayMessage = "";

    /** Our event handler. */
    private final ServerEventHandler eventHandler = new ServerEventHandler(this);

    /** A list of outstanding invites. */
    private final List<Invite> invites = new ArrayList<Invite>();

    /**
     * Creates a new instance of Server.
     *
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     * @param ssl Whether to use SSL or not
     * @param profile The profile to use
     */
    public Server(final String server, final int port, final String password,
            final boolean ssl, final Identity profile) {
        this(server, port, password, ssl, profile, new ArrayList<String>());
    }

    /**
     * Creates a new instance of Server.
     *
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     * @param ssl Whether to use SSL or not
     * @param profile The profile to use
     * @param autochannels A list of channels to auto-join when we connect
     */
    public Server(final String server, final int port, final String password,
            final boolean ssl, final Identity profile, final List<String> autochannels) {
        super();

        serverInfo = new ServerInfo(server, port, password);
        serverInfo.setSSL(ssl);

        ServerManager.getServerManager().registerServer(this);

        configManager = new ConfigManager("", "", server);

        window = Main.getUI().getServer(this);

        WindowManager.addWindow(window);

        window.setTitle(server + ":" + port);

        tabCompleter.addEntries(AliasWrapper.getAliasWrapper().getAliases());
        window.getInputHandler().setTabCompleter(tabCompleter);

        updateIcon();

        window.open();

        tabCompleter.addEntries(CommandManager.getServerCommandNames());
        tabCompleter.addEntries(CommandManager.getGlobalCommandNames());

        this.autochannels = autochannels;

        new Timer("Server Who Timer").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Channel channel : channels.values()) {
                    channel.checkWho();
                }
            }
        }, 0, configManager.getOptionInt(DOMAIN_GENERAL, "whotime", 60000));
        
        if (configManager.getOptionBool(DOMAIN_GENERAL, "showrawwindow", false)) {
            addRaw();
        }        

        connect(server, port, password, ssl, profile);
    }

    /**
     * Connects to a new server with the specified details.
     *
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     * @param ssl Whether to use SSL or not
     * @param profile The profile to use
     */
    @Precondition({
        "The IRC Parser is null or not connected",
        "The specified profile is not null"
    })
    public void connect(final String server, final int port, final String password,
            final boolean ssl, final Identity profile) {
        assert(profile != null);
        
        synchronized(myState) {
            switch (myState) {
            case RECONNECT_WAIT:
                reconnectTimer.cancel();
                break;
            case CLOSING:
                Logger.appError(ErrorLevel.MEDIUM,
                        "Connect attempt while not expecting one",
                        new UnsupportedOperationException("Current state: " + myState));
                return;
            case CONNECTED:
            case CONNECTING:
                disconnect(configManager.getOption(DOMAIN_GENERAL, "quitmessage"));
                break;
            default:
                // Do nothing
                break;
            }

            myState = ServerState.CONNECTING;
        }

        ActionManager.processEvent(CoreActionType.SERVER_CONNECTING, null, this);

        assert(parser == null || parser.getSocketState() != IRCParser.STATE_OPEN);

        serverInfo = new ServerInfo(server, port, password);
        serverInfo.setSSL(ssl);

        this.profile = profile;

        configManager = new ConfigManager("", "", server);

        updateIcon();

        addLine("serverConnecting", server, port);

        final MyInfo myInfo = getMyInfo();

        parser = new IRCParser(myInfo, serverInfo);
        parser.setRemoveAfterCallback(true);
        parser.setCreateFake(true);
        parser.setAddLastLine(true);

        if (configManager.hasOption(DOMAIN_GENERAL, "bindip")) {
            parser.setBindIP(configManager.getOption(DOMAIN_GENERAL, "bindip"));
        }

        doCallbacks();

        awayMessage = "";
        invites.clear();
        window.setAwayIndicator(false);

        try {
            new Thread(parser, "IRC Parser thread").start();
        } catch (IllegalThreadStateException ex) {
            Logger.appError(ErrorLevel.FATAL, "Unable to start IRC Parser", ex);
        }

        updateIgnoreList();
    }

    /**
     * Updates this server's icon.
     */
    private void updateIcon() {
        icon = IconManager.getIconManager().getIcon(
                myState == ServerState.CONNECTED ?
                    serverInfo.getSSL() ? "secure-server" : "server" : "server-disconnected");
        if (window != null) {
            window.setFrameIcon(icon);

            iconUpdated(icon);
        }
    }

    /**
     * Retrieves the MyInfo object used for the IRC Parser.
     *
     * @return The MyInfo object for our profile
     */
    @Precondition("The current profile is not null")
    private MyInfo getMyInfo() {
        assert(profile != null);
        
        final MyInfo myInfo = new MyInfo();
        myInfo.setNickname(profile.getOption(DOMAIN_PROFILE, "nickname"));
        myInfo.setRealname(profile.getOption(DOMAIN_PROFILE, "realname"));

        if (profile.hasOption(DOMAIN_PROFILE, "ident")) {
            myInfo.setUsername(profile.getOption(DOMAIN_PROFILE, "ident"));
        }

        return myInfo;
    }

    /**
     * Registers callbacks.
     */
    private void doCallbacks() {
        if (raw != null) {
            raw.registerCallbacks();
        }

        eventHandler.registerCallbacks();

        for (Query query : queries.values()) {
            query.reregister();
        }
    }
    
    /**
     * Joins the specified channel.
     * 
     * @param channel The channel to be joined
     */
    @Precondition("This server is connected")
    public void join(final String channel) {
        assert(myState == ServerState.CONNECTED);
        
        if (hasChannel(channel)) {
            getChannel(channel).join();
        } else {
            parser.joinChannel(channel);
        }
    }

    /**
     * Reconnects to the IRC server with a specified reason.
     *
     * @param reason The quit reason to send
     */
    public void reconnect(final String reason) {
        synchronized(myState) {
            if (myState == ServerState.CLOSING) {
                return;
            }
        }

        disconnect(reason);
        connect(serverInfo.getHost(), serverInfo.getPort(),
                serverInfo.getPassword(), serverInfo.getSSL(), profile);
    }

    /**
     * Reconnects to the IRC server.
     */
    public void reconnect() {
        reconnect(configManager.getOption(DOMAIN_GENERAL, "reconnectmessage"));
    }

    /** {@inheritDoc} */
    @Override
    public void sendLine(final String line) {
        synchronized(myState) {
            if (parser != null && myState == ServerState.CONNECTED) {
                parser.sendLine(window.getTranscoder().encode(line));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return IRCParser.MAX_LINELENGTH;
    }

    /**
     * Updates the ignore list for this server.
     */
    public void updateIgnoreList() {
        if (parser == null || parser.getIgnoreList() == null) {
            return;
        }

        parser.getIgnoreList().clear();

        if (configManager.hasOption("network", "ignorelist")) {
            for (String line : configManager.getOptionList("network", "ignorelist")) {
                parser.getIgnoreList().add(line);
            }
        }
    }

    /**
     * Determines whether the server knows of the specified channel.
     *
     * @param channel The channel to be checked
     * @return True iff the channel is known, false otherwise
     */
    public boolean hasChannel(final String channel) {
        return parser != null && channels.containsKey(parser.toLowerCase(channel));
    }

    /**
     * Retrieves the specified channel belonging to this server.
     *
     * @param channel The channel to be retrieved
     * @return The appropriate channel object
     */
    public Channel getChannel(final String channel) {
        return channels.get(parser.toLowerCase(channel));
    }

    /**
     * Retrieves a list of channel names belonging to this server.
     *
     * @return list of channel names belonging to this server
     */
    public List<String> getChannels() {
        final ArrayList<String> res = new ArrayList<String>();

        for (String channel : channels.keySet()) {
            res.add(channel);
        }

        return res;
    }

    /**
     * Determines whether the server knows of the specified query.
     *
     * @param query The query to be checked
     * @return True iff the query is known, false otherwise
     */
    public boolean hasQuery(final String query) {
        return queries.containsKey(parser.toLowerCase(query));
    }

    /**
     * Retrieves the specified query belonging to this server.
     *
     * @param query The query to be retrieved
     * @return The appropriate query object
     */
    public Query getQuery(final String query) {
        return queries.get(parser.toLowerCase(query));
    }

    /**
     * Retrieves a list of queries belonging to this server.
     *
     * @return list of queries belonging to this server
     */
    public List<String> getQueries() {
        final ArrayList<String> res = new ArrayList<String>();

        for (String query : queries.keySet()) {
            res.add(query);
        }

        return res;
    }

    /**
     * Adds a raw window to this server.
     */
    public void addRaw() {
        raw = new Raw(this);
        
        if (parser != null) {
            raw.registerCallbacks();
        }
    }

    /**
     * Retrieves the raw window associated with this server.
     *
     * @return The raw window associated with this server.
     */
    public Raw getRaw() {
        return raw;
    }

    /**
     * Retrieves the parser used for this connection.
     *
     * @return IRCParser this connection's parser
     */
    public IRCParser getParser() {
        return parser;
    }

    /**
     * Retrieves the profile that's in use for this server.
     *
     * @return The profile in use by this server
     */
    public Identity getProfile() {
        return profile;
    }

    /**
     * Retrieves the name of this server.
     *
     * @return The name of this server
     */
    public String getName() {
        return serverInfo.getHost();
    }

    /**
     * Retrieves the name of this server's network. The network name is
     * determined using the following rules:
     *
     *  1. If the server includes its network name in the 005 information, we
     *     use that
     *  2. If the server's name ends in biz, com, info, net or org, we use the
     *     second level domain (e.g., foo.com)
     *  3. If the server's name contains more than two dots, we drop everything
     *     up to and including the first part, and use the remainder
     *  4. In all other cases, we use the full server name
     *
     * @return The name of this server's network
     */
    public String getNetwork() {
        if (parser == null) {
            return "";
        } else if (parser.getNetworkName().isEmpty()) {
            return getNetworkFromServerName(parser.getServerName());
        } else {
            return parser.getNetworkName();
        }
    }

    /**
     * Caclaultes a network name from the specified server name. This method
     * implements parts 2-4 of the procedure documented at getNetwork().
     *
     * @param serverName The server name to parse
     * @return A network name for the specified server
     */
    protected static String getNetworkFromServerName(final String serverName) {
        final String[] parts = serverName.split("\\.");
        final String[] tlds = {"biz", "com", "info", "net", "org"};
        boolean isTLD = false;

        for (String tld : tlds) {
            if (serverName.endsWith("." + tld)) {
                isTLD = true;
            }
        }

        if (isTLD && parts.length > 2) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
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

    /**
     * Retrieves the name of this server's IRCd.
     *
     * @return The name of this server's IRCd
     */
    public String getIrcd() {
        return parser.getIRCD(true);
    }

    /**
     * Returns the current away status.
     *
     * @return True if the client is marked as away, false otherwise
     */
    public boolean isAway() {
        return !awayMessage.isEmpty();
    }

    /**
     * Gets the current away message.
     *
     * @return Null if the client isn't away, or a textual away message if it is
     */
    public String getAwayMessage() {
        return awayMessage;
    }

    /**
     * Returns the tab completer for this connection.
     *
     * @return The tab completer for this server
     */
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    /** {@inheritDoc} */
    @Override
    public InputWindow getFrame() {
        return window;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Retrieves the current state for this server.
     *
     * @return This server's state
     */
    public ServerState getState() {
        return myState;
    }

    /**
     * Closes this server connection and associated windows.
     *
     * @param reason reason for closing
     */
    public void close(final String reason) {
        if (parser != null) {
            // Unregister parser callbacks
            parser.getCallbackManager().delAllCallback(eventHandler);
        }

        // Disconnect from the server
        disconnect(reason);

        myState = ServerState.CLOSING;

        // Close all channel windows
        closeChannels();
        // Close all query windows
        closeQueries();
        // Close the raw window
        if (raw != null) {
            raw.close();
        }
        // Unregister ourselves with the server manager
        ServerManager.getServerManager().unregisterServer(this);

        if (window != null) {
            window.setVisible(false);
            Main.getUI().getMainWindow().delChild(window);
            window = null; //NOPMD
        }

        // Ditch the parser
        parser = null; //NOPMD
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        close(configManager.getOption(DOMAIN_GENERAL, "quitmessage"));
    }

    /**
     * Disconnects from the server.
     *
     * @param reason disconnect reason
     */
    public void disconnect(final String reason) {
        synchronized(myState) {
            switch (myState) {
            case CLOSING:
            case DISCONNECTED:
            case TRANSIENTLY_DISCONNECTED:
                return;
            case RECONNECT_WAIT:
                reconnectTimer.cancel();
                break;
            default:
                break;
            }

            myState = ServerState.DISCONNECTED;
        }
        
        updateIcon();

        if (parser != null && parser.getSocketState() == IRCParser.STATE_OPEN) {
            parser.disconnect(reason);

            if (configManager.getOptionBool(DOMAIN_GENERAL, "closechannelsonquit", false)) {
                closeChannels();
            } else {
                clearChannels();
            }

            if (configManager.getOptionBool(DOMAIN_GENERAL, "closequeriesonquit", false)) {
                closeQueries();
            }
        }
    }

    /**
     * Closes all open channel windows associated with this server.
     */
    private void closeChannels() {
        for (Channel channel : channels.values()) {
            WindowManager.removeWindow(channel.getFrame());
            channel.closeWindow(false);
        }

        channels.clear();
    }

    /**
     * Clears the nicklist of all open channels.
     */
    private void clearChannels() {
        for (Channel channel : channels.values()) {
            channel.resetWindow();
        }
    }

    /**
     * Closes all open query windows associated with this server.
     */
    private void closeQueries() {
        for (Query query : queries.values()) {
            WindowManager.removeWindow(query.getFrame());
            query.close(false);
        }

        queries.clear();
    }

    /**
     * Removes our reference to the raw object (presumably after it has been
     * closed).
     */
    public void delRaw() {
        WindowManager.removeWindow(raw.getFrame());
        raw = null; //NOPMD
    }

    /**
     * Removes a specific channel and window from this server.
     *
     * @param chan channel to remove
     */
    public void delChannel(final String chan) {
        tabCompleter.removeEntry(chan);
        WindowManager.removeWindow(
                channels.get(parser.toLowerCase(chan)).getFrame());
        channels.remove(parser.toLowerCase(chan));
    }

    /**
     * Adds a specific channel and window to this server.
     *
     * @param chan channel to add
     */
    public void addChannel(final ChannelInfo chan) {
        if (hasChannel(chan.getName())) {
            getChannel(chan.getName()).setChannelInfo(chan);
            getChannel(chan.getName()).selfJoin();
        } else {
            final Channel newChan = new Channel(this, chan);

            tabCompleter.addEntry(chan.getName());
            channels.put(parser.toLowerCase(chan.getName()), newChan);
            newChan.show();
        }
    }

    /**
     * Adds a query to this server.
     *
     * @param host host of the remote client being queried
     */
    public void addQuery(final String host) {
        if (!queries.containsKey(parser.toLowerCase(ClientInfo.parseHost(host)))) {
            final Query newQuery = new Query(this, host);

            tabCompleter.addEntry(ClientInfo.parseHost(host));
            queries.put(parser.toLowerCase(ClientInfo.parseHost(host)), newQuery);
        }
    }

    /**
     * Deletes a query from this server.
     *
     * @param host host of the remote client being queried
     */
    public void delQuery(final String host) {
        tabCompleter.removeEntry(ClientInfo.parseHost(host));
        WindowManager.removeWindow(
                queries.get(parser.toLowerCase(ClientInfo.parseHost(host))).getFrame());
        queries.remove(parser.toLowerCase(ClientInfo.parseHost(host)));
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownsFrame(final Window target) {
        // Check if it's our server frame
        if (window != null && window.equals(target)) { return true; }
        // Check if it's the raw frame
        if (raw != null && raw.ownsFrame(target)) { return true; }
        // Check if it's a channel frame
        for (Channel channel : channels.values()) {
            if (channel.ownsFrame(target)) { return true; }
        }
        // Check if it's a query frame
        for (Query query : queries.values()) {
            if (query.ownsFrame(target)) { return true; }
        }
        return false;
    }

    /**
     * Sets the specified frame as the most-recently activated.
     *
     * @param source The frame that was activated
     */
    public void setActiveFrame(final FrameContainer source) {
        activeFrame = source;
    }

    /**
     * Passes the arguments to the most recently activated frame for this
     * server. If the frame isn't know, or isn't visible, use this frame
     * instead.
     *
     * @param messageType The type of message to send
     * @param args The arguments for the message
     */
    public void addLineToActive(final String messageType, final Object... args) {
        if (activeFrame == null || !activeFrame.getFrame().isVisible()) {
            activeFrame = this;
        }

        activeFrame.getFrame().addLine(messageType, args);
    }

    /**
     * Passes the arguments to all frames for this server.
     *
     * @param messageType The type of message to send
     * @param args The arguments of the message
     */
    public void addLineToAll(final String messageType, final Object... args) {
        for (Channel channel : channels.values()) {
            channel.getFrame().addLine(messageType, args);
        }

        for (Query query : queries.values()) {
            query.getFrame().addLine(messageType, args);
        }

        addLine(messageType, args);
    }

    /**
     * Processes and displays a notification.
     *
     * @param messageType The name of the formatter to be used for the message
     * @param actionType The action type to be used
     * @param args The arguments for the message
     */
    public void doNotification(final String messageType,
            final ActionType actionType, final Object... args) {
        final List<Object> messageArgs = new ArrayList<Object>();
        final List<Object> actionArgs = new ArrayList<Object>();
        final StringBuffer buffer = new StringBuffer(messageType);

        actionArgs.add(this);

        for (Object arg : args) {
            actionArgs.add(arg);

            if (arg instanceof ClientInfo) {
                final ClientInfo clientInfo = (ClientInfo) arg;
                messageArgs.add(clientInfo.getNickname());
                messageArgs.add(clientInfo.getIdent());
                messageArgs.add(clientInfo.getHost());
            } else {
                messageArgs.add(arg);
            }
        }

        ActionManager.processEvent(actionType, buffer, actionArgs.toArray());

        handleNotification(messageType, messageArgs.toArray());
    }

    /**
     * Handles general server notifications (i.e., ones note tied to a
     * specific window). The user can select where the notifications should
     * go in their config.
     *
     * @param messageType The type of message that is being sent
     * @param args The arguments for the message
     */
    public void handleNotification(final String messageType, final Object... args) {
        String target = configManager.getOption("notifications", messageType,
                NOTIFICATION_SERVER);

        if (target.startsWith("group:")) {
            target = configManager.getOption("notifications", target.substring(6),
                    NOTIFICATION_SERVER);
        }

        if (NOTIFICATION_SERVER.equals(target)) {
            addLine(messageType, args);
        } else if ("all".equals(target)) {
            addLineToAll(messageType, args);
        } else if ("active".equals(target)) {
            addLineToActive(messageType, args);
        } else if (target.startsWith("window:")) {
            final String windowName = target.substring(7);

            Window targetWindow = WindowManager.findCustomWindow(getFrame(), windowName);

            if (targetWindow == null) {
                targetWindow = new CustomWindow(windowName, windowName, getFrame()).getFrame();
            }

            targetWindow.addLine(messageType, args);
        } else if (target.startsWith("lastcommand:")) {
            final Object[] escapedargs = new Object[args.length];

            for (int i = 0; i < args.length; i++) {
                escapedargs[i] = "\\Q" + args[i] + "\\E";
            }

            final String command = String.format(target.substring(12), escapedargs);

            WritableFrameContainer best = this;
            long besttime = 0;

            final List<WritableFrameContainer> containers
                    = new ArrayList<WritableFrameContainer>();
            containers.addAll(channels.values());
            containers.addAll(queries.values());

            if (raw != null) {
                containers.add(raw);
            }

            for (WritableFrameContainer container: containers) {
                final long time
                        = container.getFrame().getCommandParser().getCommandTime(command);
                if (time > besttime) {
                    besttime = time;
                    best = container;
                }
            }

            best.addLine(messageType, args);
        } else if (target.startsWith("channel:")) {
           final String channel = String.format(target.substring(8), args);

           if (hasChannel(channel)) {
               getChannel(channel).addLine(messageType, args);
           } else {
               addLine(messageType, args);
               Logger.userError(ErrorLevel.LOW,
                       "Invalid notification target for type " + messageType
                       + ": channel " + channel + " doesn't exist");
           }
        } else if (!"none".equals(target)) {
            Logger.userError(ErrorLevel.MEDIUM,
                    "Invalid notification target for type " + messageType + ": " + target);
        }
    }

    /**
     * Replies to an incoming CTCP message.
     *
     * @param source The source of the message
     * @param type The CTCP type
     * @param args The CTCP arguments
     */
    public void sendCTCPReply(final String source, final String type, final String args) {
        if (type.equalsIgnoreCase("VERSION")) {
            parser.sendCTCPReply(source, "VERSION", "DMDirc " + Main.VERSION
                    + " - http://www.dmdirc.com/");
        } else if (type.equalsIgnoreCase("PING")) {
            parser.sendCTCPReply(source, "PING", args);
        } else if (type.equalsIgnoreCase("CLIENTINFO")) {
            parser.sendCTCPReply(source, "CLIENTINFO", "VERSION PING CLIENTINFO");
        }
    }

    /**
     * Called when the server says that the nickname we're trying to use is
     * already in use.
     *
     * @param nickname The nickname that we were trying to use
     */
    public void onNickInUse(final String nickname) {
        final String lastNick = parser.getMyNickname();

        // If our last nick is still valid, ignore the in use message
        if (!parser.equalsIgnoreCase(lastNick, nickname)) {
            return;
        }

        String newNick = lastNick + (int) (Math.random() * 10);

        if (profile.hasOption(DOMAIN_PROFILE, "altnicks")) {
            final String[] alts = profile.getOption(DOMAIN_PROFILE, "altnicks").split("\n");
            int offset = 0;

            if (!parser.equalsIgnoreCase(lastNick,
                    profile.getOption(DOMAIN_PROFILE, "nickname"))) {
                for (String alt : alts) {
                    offset++;
                    if (parser.equalsIgnoreCase(alt, lastNick)) {
                        break;
                    }
                }
            }

            if (offset < alts.length && !alts[offset].isEmpty()) {
                newNick = alts[offset];
            }
        }

        parser.setNickname(newNick);
    }

    /**
     * Called when the server sends a numeric event.
     *
     * @param numeric The numeric code for the event
     * @param tokens The (tokenised) arguments of the event
     */
    public void onNumeric(final int numeric, final String[] tokens) {
        String snumeric = String.valueOf(numeric);

        if (numeric < 10) {
            snumeric = "00" + snumeric;
        } else if (numeric < 100) {
            snumeric = "0" + snumeric;
        }

        final String withIrcd = "numeric_" + parser.getIRCD(true) + "_" + snumeric;
        final String sansIrcd = "numeric_" + snumeric;
        String target = null;

        if (Formatter.hasFormat(withIrcd)) {
            target = withIrcd;
        } else if (Formatter.hasFormat(sansIrcd)) {
            target = sansIrcd;
        } else if (Formatter.hasFormat("numeric_unknown")) {
            target = "numeric_unknown";
        }

        if (target != null) {
            handleNotification(target, (Object[]) tokens);
        }

        ActionManager.processEvent(CoreActionType.SERVER_NUMERIC, null, this,
                Integer.valueOf(numeric), tokens);
    }

    /**
     * Called when our away state changes.
     *
     * @param currentState The new aray state
     * @param reason Our away reason, if applicable
     */
    public void onAwayState(final boolean currentState, final String reason) {
        if (currentState) {
            awayMessage = reason;

            ActionManager.processEvent(CoreActionType.SERVER_AWAY, null, this, awayMessage);
        } else {
            awayMessage = "";

            ActionManager.processEvent(CoreActionType.SERVER_BACK, null, this);
        }

        window.setAwayIndicator(isAway());
    }

    /**
     * Called when the socket has been closed.
     */
    public void onSocketClosed() {
        handleNotification("socketClosed", getName());

        synchronized(myState) {
            if (myState == ServerState.CLOSING || myState == ServerState.DISCONNECTED) {
                // This has been triggered via .disconect()
                return;
            }

            myState = ServerState.TRANSIENTLY_DISCONNECTED;
        }
        
        updateIcon();

        if (configManager.getOptionBool(DOMAIN_GENERAL, "closechannelsondisconnect", false)) {
            closeChannels();
        } else {
            clearChannels();
        }

        if (configManager.getOptionBool(DOMAIN_GENERAL, "closequeriesondisconnect", false)) {
            closeQueries();
        }

        if (configManager.getOptionBool(DOMAIN_GENERAL, "reconnectondisconnect", false)) {
            doDelayedReconnect();
        }
    }

    /**
     * Called when an error was encountered while connecting.
     *
     * @param errorInfo The parser's error information
     */
    @Precondition("The current server state is CONNECTING")
    public void onConnectError(final ParserError errorInfo) {
        synchronized(myState) {
            assert(myState == ServerState.CONNECTING);

            myState = ServerState.TRANSIENTLY_DISCONNECTED;
        }
        
        updateIcon();

        String description;

        if (errorInfo.getException() == null) {
            description = errorInfo.getData();
        } else {
            final Exception exception = errorInfo.getException();

            if (exception instanceof java.net.UnknownHostException) {
                description = "Unknown host (unable to resolve)";
            } else if (exception instanceof java.net.NoRouteToHostException) {
                description = "No route to host";
            } else if (exception instanceof java.net.SocketException) {
                description = exception.getMessage();
            } else {
                Logger.appError(ErrorLevel.LOW, "Unknown socket error", exception);
                description = "Unknown error: " + exception.getMessage();
            }
        }

        ActionManager.processEvent(CoreActionType.SERVER_CONNECTERROR, null,
                this, description);

        handleNotification("connectError", getName(), description);

        if (configManager.getOptionBool(DOMAIN_GENERAL, "reconnectonconnectfailure", false)) {
            doDelayedReconnect();
        }
    }

    /**
     * Schedules a reconnect attempt to be performed after a user-defiend delay.
     */
    private void doDelayedReconnect() {
        final int delay = Math.max(1,
                configManager.getOptionInt(DOMAIN_GENERAL, "reconnectdelay", 5));

        handleNotification("connectRetry", getName(), delay);

        reconnectTimer = new Timer("Server Reconnect Timer");
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized(myState) {
                    if (myState == ServerState.RECONNECT_WAIT) {
                        myState = ServerState.TRANSIENTLY_DISCONNECTED;
                        reconnect();
                    }
                }
            }
        }, delay * 1000);

        myState = ServerState.RECONNECT_WAIT;
        updateIcon();
    }

    /**
     * Called when we fail to receive a ping reply within a set period of time.
     */
    public void onPingFailed() {
        Main.getUI().getStatusBar().setMessage("No ping reply from "
                + getName() + " for over "
                + ((int) (Math.floor(parser.getPingTime(false) / 1000.0)))
                + " seconds.", null, 10);

        ActionManager.processEvent(CoreActionType.SERVER_NOPING, null, this,
                Long.valueOf(parser.getPingTime(false)));

        if (parser.getPingTime(false) >=
                configManager.getOptionInt(DOMAIN_SERVER, "pingtimeout", 60000)) {
            handleNotification("stonedServer", getName());
            reconnect();
        }
    }

    /**
     * Called after the parser receives the 005 headers from the server.
     */
    public void onPost005() {
        synchronized(myState) {
            myState = ServerState.CONNECTED;
        }
        updateIcon();

        configManager = new ConfigManager(parser.getIRCD(true), getNetwork(), getName());
        updateIgnoreList();

        ActionManager.processEvent(CoreActionType.SERVER_CONNECTED, null, this);

        if (configManager.hasOption(DOMAIN_GENERAL, "rejoinchannels")) {
            for (Channel chan : channels.values()) {
                chan.join();
            }
        }

        for (String channel : autochannels) {
            parser.joinChannel(channel);
        }

        // Check we have mode aliases
        final String modes = parser.getBoolChanModes() + parser.getListChanModes()
                + parser.getSetOnlyChanModes() + parser.getSetUnsetChanModes();

        for (int i = 0; i < modes.length(); i++) {
            final char mode = modes.charAt(i);
            if (!configManager.hasOption(DOMAIN_SERVER, "mode" + mode)) {
                Logger.appError(ErrorLevel.LOW, "No mode alias for mode +" + mode,
                        new Exception("No mode alias for mode +" + mode + "\n" // NOPMD
                        + "Network: " + parser.getNetworkName() + "\n"
                        + "IRCd: " + parser.getIRCD(false)
                        + " (" + parser.getIRCD(true) + ")\n\n"));
            }
        }
    }

    /**
     * Adds an invite listener to this server.
     *
     * @param listener The listener to be added
     */
    public void addInviteListener(final InviteListener listener) {
        listeners.add(InviteListener.class, listener);
    }

    /**
     * Removes an invite listener from this server.
     *
     * @param listener The listener to be removed
     */
    public void removeInviteListener(final InviteListener listener) {
        listeners.remove(InviteListener.class, listener);
    }

    /**
     * Adds an invite to this server, and fires the appropriate listeners.
     *
     * @param invite The invite to be added
     */
    public void addInvite(final Invite invite) {
        invites.add(invite);

        for (InviteListener listener : listeners.get(InviteListener.class)) {
            listener.inviteReceived(this, invite);
        }
    }

    /**
     * Removes an invite from this server, and fires the appropriate listeners.
     *
     * @param invite The invite to be removed
     */
    public void removeInvite(final Invite invite) {
        invites.remove(invite);

        for (InviteListener listener : listeners.get(InviteListener.class)) {
            listener.inviteExpired(this, invite);
        }
    }

    /**
     * Returns this server's name.
     *
     * @return A string representation of this server (i.e., its name)
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns the server instance associated with this frame.
     *
     * @return the associated server connection
     */
    @Override
    public Server getServer() {
        return this;
    }
}
