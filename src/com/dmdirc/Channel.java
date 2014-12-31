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
import com.dmdirc.commandparser.parsers.ChannelCommandParser;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.ChannelClosedEvent;
import com.dmdirc.events.ChannelSelfActionEvent;
import com.dmdirc.events.ChannelSelfMessageEvent;
import com.dmdirc.events.ChannelTopicChangeEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.NickListClientAddedEvent;
import com.dmdirc.events.NickListClientRemovedEvent;
import com.dmdirc.events.NickListClientsChangedEvent;
import com.dmdirc.events.NickListUpdatedEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.EventUtils;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.collections.RollingList;
import com.dmdirc.util.colours.Colour;
import com.dmdirc.util.colours.ColourUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Channel class represents the client's view of the channel. It handles callbacks for channel
 * events from the parser, maintains the corresponding ChannelWindow, and handles user input for the
 * channel.
 */
public class Channel extends MessageTarget implements GroupChat {

    /** List of registered listeners. */
    private final ListenerList listenerList = new ListenerList();
    /** The parser's pChannel class. */
    private ChannelInfo channelInfo;
    /** The server this channel is on. */
    private final Server server;
    /** A list of previous topics we've seen. */
    private final RollingList<Topic> topics;
    /** Our event handler. */
    private final ChannelEventHandler eventHandler;
    /** The migrator to use to migrate our config provider. */
    private final ConfigProviderMigrator configMigrator;
    /** Manager used to retrieve {@link GroupChatUser}s */
    private final GroupChatUserManager groupChatUserManager;
    /** Whether we're in this channel or not. */
    private boolean isOnChannel;
    /** Whether we should send WHO requests for this channel. */
    @ConfigBinding(domain = "channel", key = "sendwho")
    private volatile boolean sendWho;
    /** Whether we should show mode prefixes in text. */
    @ConfigBinding(domain = "channel", key = "showmodeprefix")
    private volatile boolean showModePrefix;
    /** Whether we should show colours in nicks. */
    @ConfigBinding(domain = "ui", key = "shownickcoloursintext")
    private volatile boolean showColours;

    /**
     * Creates a new instance of Channel.
     *
     * @param newServer           The server object that this channel belongs to
     * @param newChannelInfo      The parser's channel object that corresponds to this channel
     * @param configMigrator      The config migrator which provides the config for this channel.
     * @param tabCompleterFactory The factory to use to create tab completers.
     * @param commandController   The controller to load commands from.
     * @param messageSinkManager  The sink manager to use to despatch messages.
     * @param urlBuilder          The URL builder to use when finding icons.
     * @param eventBus            The bus to despatch events onto.
     */
    public Channel(
            final Server newServer,
            final ChannelInfo newChannelInfo,
            final ConfigProviderMigrator configMigrator,
            final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController,
            final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder,
            final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory,
            final GroupChatUserManager groupChatUserManager) {
        super(newServer, "channel-inactive", newChannelInfo.getName(),
                Styliser.stipControlCodes(newChannelInfo.getName()),
                configMigrator.getConfigProvider(),
                backBufferFactory,
                new ChannelCommandParser(newServer, commandController, eventBus),
                tabCompleterFactory.getTabCompleter(newServer.getTabCompleter(),
                        configMigrator.getConfigProvider(), CommandType.TYPE_CHANNEL,
                        CommandType.TYPE_CHAT),
                messageSinkManager,
                urlBuilder,
                newServer.getEventBus(),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier(),
                        WindowComponent.TOPICBAR.getIdentifier(),
                        WindowComponent.USERLIST.getIdentifier()));

        this.configMigrator = configMigrator;
        this.channelInfo = newChannelInfo;
        this.server = newServer;
        this.groupChatUserManager = groupChatUserManager;

        getConfigManager().getBinder().bind(this, Channel.class);

        topics = new RollingList<>(getConfigManager().getOptionInt("channel", "topichistorysize"));

        eventHandler = new ChannelEventHandler(this, getEventBus(), groupChatUserManager);

        registerCallbacks();
        updateTitle();
        selfJoin();
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    @Override
    public boolean isOnChannel() {
        return isOnChannel;
    }

    /**
     * Registers callbacks with the parser for this channel.
     */
    private void registerCallbacks() {
        eventHandler.registerCallbacks();
        configMigrator.migrate(server.getProtocol(), server.getIrcd(),
                server.getNetwork(), server.getAddress(), channelInfo.getName());
    }

    @Override
    public void sendLine(final String line) {
        if (server.getState() != ServerState.CONNECTED
                || server.getParser().get().getChannel(channelInfo.getName()) == null) {
            // We're not in the channel/connected to the server
            return;
        }

        final GroupChatUser me = getUser(server.getLocalUser()).get();
        final String[] details = getDetails(me);

        splitLine(line).stream().filter(part -> !part.isEmpty()).forEach(part -> {
            final ChannelSelfMessageEvent event =
                    new ChannelSelfMessageEvent(this, me, part);
            final String format =
                    EventUtils.postDisplayable(getEventBus(), event, "channelSelfMessage");
            addLine(format, details[0], details[1], details[2], details[3], part, channelInfo);

            channelInfo.sendMessage(part);
        });
    }

    @Override
    public int getMaxLineLength() {
        return server.getState() == ServerState.CONNECTED
                ? server.getParser().get().getMaxLength("PRIVMSG", getChannelInfo().getName())
                : -1;
    }

    @Override
    public void sendAction(final String action) {
        if (server.getState() != ServerState.CONNECTED
                || server.getParser().get().getChannel(channelInfo.getName()) == null) {
            // We're not on the server/channel
            return;
        }


        final GroupChatUser me = getUser(server.getLocalUser()).get();
        final String[] details = getDetails(me);

        if (server.getParser().get().getMaxLength("PRIVMSG", getChannelInfo().getName())
                <= action.length()) {
            addLine("actionTooLong", action.length());
        } else {
            final ChannelSelfActionEvent event = new ChannelSelfActionEvent(this, me, action);
            final String format = EventUtils.postDisplayable(getEventBus(), event,
                    "channelSelfAction");
            addLine(format, details[0], details[1], details[2], details[3], action, channelInfo);

            channelInfo.sendAction(action);
        }
    }

    /**
     * Sets this object's ChannelInfo reference to the one supplied. This only needs to be done if
     * the channel window (and hence this channel object) has stayed open while the user has been
     * out of the channel.
     *
     * @param newChannelInfo The new ChannelInfo object
     */
    public void setChannelInfo(final ChannelInfo newChannelInfo) {
        channelInfo = newChannelInfo;
        registerCallbacks();
    }

    /**
     * Called when we join this channel. Just needs to output a message.
     */
    public void selfJoin() {
        isOnChannel = true;

        final User me = server.getLocalUser();
        addLine("channelSelfJoin", "", me.getNickname(), me.getUsername(),
                me.getHostname(), channelInfo.getName());

        checkWho();
        setIcon("channel");

        server.removeInvites(channelInfo.getName());
    }

    /**
     * Updates the title of the channel window, and of the main window if appropriate.
     */
    private void updateTitle() {
        String temp = Styliser.stipControlCodes(channelInfo.getName());

        if (!channelInfo.getTopic().isEmpty()) {
            temp += " - " + Styliser.stipControlCodes(channelInfo.getTopic());
        }

        setTitle(temp);
    }

    @Override
    public void join() {
        server.getParser().get().joinChannel(channelInfo.getName());
    }

    @Override
    public void part(final String reason) {
        channelInfo.part(reason);

        resetWindow();
    }

    @Override
    public void retrieveListModes() {
        channelInfo.requestListModes();
    }

    /**
     * Resets the window state after the client has left a channel.
     */
    public void resetWindow() {
        isOnChannel = false;

        setIcon("channel-inactive");

        getEventBus().publishAsync(new NickListClientsChangedEvent(this,
                Collections.<GroupChatUser>emptyList()));
    }

    @Override
    public void close() {
        super.close();

        // Remove any callbacks or listeners
        eventHandler.unregisterCallbacks();
        getConfigManager().getBinder().unbind(this);

        server.getParser().map(Parser::getCallbackManager)
                .ifPresent(cm -> cm.delAllCallback(eventHandler));

        // Trigger any actions neccessary
        if (isOnChannel && server.getState() != ServerState.CLOSING) {
            part(getConfigManager().getOption("general", "partmessage"));
        }

        // Trigger action for the window closing
        getEventBus().publish(new ChannelClosedEvent(this));

        // Inform any parents that the window is closing
        server.delChannel(channelInfo.getName());
    }

    /**
     * Called every {general.whotime} seconds to check if the channel needs to send a who request.
     */
    public void checkWho() {
        if (isOnChannel && sendWho) {
            channelInfo.sendWho();
        }
    }

    /**
     * Adds a ChannelClient to this Channel.
     *
     * @param client The client to be added
     */
    public void addClient(final GroupChatUser client) {
        getEventBus().publishAsync(new NickListClientAddedEvent(this, client));

        getTabCompleter().addEntry(TabCompletionType.CHANNEL_NICK, client.getNickname());
    }

    /**
     * Removes the specified ChannelClient from this channel.
     *
     * @param client The client to be removed
     */
    public void removeClient(final GroupChatUser client) {
        getEventBus().publishAsync(new NickListClientRemovedEvent(this, client));

        getTabCompleter().removeEntry(TabCompletionType.CHANNEL_NICK,
                client.getNickname());

        if (client.getUser().equals(server.getLocalUser())) {
            resetWindow();
        }
    }

    /**
     * Replaces the list of known clients on this channel with the specified one.
     *
     * @param clients The list of clients to use
     */
    public void setClients(final Collection<GroupChatUser> clients) {
        getEventBus().publishAsync(new NickListClientsChangedEvent(this, clients));

        getTabCompleter().clear(TabCompletionType.CHANNEL_NICK);

        getTabCompleter().addEntries(TabCompletionType.CHANNEL_NICK,
            clients.stream().map(GroupChatUser::getNickname).collect(Collectors.toList()));
    }

    /**
     * Renames a client that is in this channel.
     *
     * @param oldName The old nickname of the client
     * @param newName The new nickname of the client
     */
    public void renameClient(final String oldName, final String newName) {
        getTabCompleter().removeEntry(TabCompletionType.CHANNEL_NICK, oldName);
        getTabCompleter().addEntry(TabCompletionType.CHANNEL_NICK, newName);
        refreshClients();
    }

    /**
     * Refreshes the list of clients stored by this channel. Should be called when (visible) user
     * modes or nicknames change.
     */
    public void refreshClients() {
        if (!isOnChannel) {
            return;
        }

        getEventBus().publishAsync(new NickListUpdatedEvent(this));
    }

    /**
     * Returns a string containing the most important mode for the specified client.
     *
     * @param user The channel client to check.
     *
     * @return A string containing the most important mode, or an empty string if there are no
     *         (known) modes.
     */
    private String getModes(final GroupChatUser user) {
        if (user == null || !showModePrefix) {
            return "";
        } else {
            return user.getImportantMode();
        }
    }

    /**
     * Returns a string[] containing the nickname/ident/host of a channel client.
     *
     *
     *
     * @param client The channel client to check
     *
     * @return  A string[] containing displayable components
     *          0 - mode
     *          1 - nickname
     *          2 - ident
     *          3 - hostname
     */
    private String[] getDetails(final GroupChatUser client) {
        if (client == null) {
            // WTF?
            throw new UnsupportedOperationException("getDetails called with"
                    + " null ChannelClientInfo");
        }

        final String[] res = {
            getModes(client),
            Styliser.CODE_NICKNAME + client.getNickname() + Styliser.CODE_NICKNAME,
            client.getUsername().orElse(""),
            client.getHostname().orElse(""),};

        if (showColours) {
            final Optional<Colour> foreground
                    = client.getDisplayProperty(DisplayProperty.FOREGROUND_COLOUR);
            final Optional<Colour> background
                    = client.getDisplayProperty(DisplayProperty.BACKGROUND_COLOUR);
            if (foreground.isPresent()) {
                String prefix = Styliser.CODE_HEXCOLOUR + ColourUtils.getHex(foreground.get());
                if (background.isPresent()) {
                    prefix += ',' + ColourUtils.getHex(background.get());
                }
                res[1] = prefix + res[1] + Styliser.CODE_HEXCOLOUR;
            }
        }

        return res;
    }

    @Override
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        if (arg instanceof User) {
            final User clientInfo = (User) arg;
            args.add(clientInfo.getNickname());
            args.add(clientInfo.getUsername());
            args.add(clientInfo.getHostname());

            return true;
        } else if (arg instanceof ClientInfo) {
            final ClientInfo clientInfo = (ClientInfo) arg;
            args.add(clientInfo.getNickname());
            args.add(clientInfo.getUsername());
            args.add(clientInfo.getHostname());

            return true;
        } else if (arg instanceof GroupChatUser) {
            final GroupChatUser clientInfo = (GroupChatUser) arg;

            args.addAll(Arrays.asList(getDetails(clientInfo)));

            return true;
        } else if (arg instanceof ChannelClientInfo) {
            final GroupChatUser clientInfo = groupChatUserManager.getUserFromClient(
                    (ChannelClientInfo) arg, this);

            args.addAll(Arrays.asList(getDetails(clientInfo)));

            return true;
        } else if (arg instanceof Topic) {
            // Format topics
            final Topic topic = (Topic) arg;
            args.add("");
            args.add(topic.getClient().map(GroupChatUser::getNickname).orElse("Unknown"));
            args.add(topic.getClient().flatMap(GroupChatUser::getUsername).orElse(""));
            args.add(topic.getClient().flatMap(GroupChatUser::getHostname).orElse(""));
            args.add(topic.getTopic());
            args.add(topic.getTime() * 1000);

            return true;
        } else {
            // Everything else - default formatting

            return super.processNotificationArg(arg, args);
        }
    }

    @Override
    protected void modifyNotificationArgs(final List<Object> actionArgs,
            final List<Object> messageArgs) {
        messageArgs.add(channelInfo.getName());
    }

    // ---------------------------------------------------- TOPIC HANDLING -----
    /**
     * Adds the specified topic to this channel's topic list.
     *
     * @param topic The topic to be added.
     */
    public void addTopic(final Topic topic) {
        synchronized (topics) {
            topics.add(topic);
        }
        updateTitle();

        getEventBus().publishAsync(new ChannelTopicChangeEvent(this, topic));
    }

    @Override
    public List<Topic> getTopics() {
        synchronized (topics) {
            return new ArrayList<>(topics.getList());
        }
    }

    @Override
    public Optional<Topic> getCurrentTopic() {
        synchronized (topics) {
            if (topics.getList().isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(topics.get(topics.getList().size() - 1));
            }
        }
    }

    // ------------------------------------------ PARSER METHOD DELEGATION -----
    @Override
    public void setTopic(final String topic) {
        channelInfo.setTopic(topic);
    }

    @Override
    public int getMaxTopicLength() {
        return server.getParser().get().getMaxTopicLength();
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.of(server);
    }

    @Override
    public Optional<GroupChatUser> getUser(final User user) {
        final ChannelClientInfo ci = channelInfo.getChannelClient(((Client) user).getClientInfo());
        if (ci == null) {
            return Optional.empty();
        }
        return Optional.of(groupChatUserManager.getUserFromClient(ci, user, this));
    }

    @Override
    public Collection<GroupChatUser> getUsers() {
        return channelInfo.getChannelClients().stream()
                .map(client -> groupChatUserManager.getUserFromClient(client, this))
                .collect(Collectors.toList());
    }

    @Override
    public FrameContainer getWindowModel() {
        return this;
    }

    @Override
    public void kick(final GroupChatUser user, final Optional<String> reason) {
        ((ChannelClient) user).getClientInfo().kick(
                reason.orElse(getConfigManager().getOption("general", "kickmessage")));
    }

}
