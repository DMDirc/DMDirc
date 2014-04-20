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
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.ChannelCommandParser;
import com.dmdirc.events.ChannelClosedEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.NicklistListener;
import com.dmdirc.interfaces.TopicChangeListener;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.collections.RollingList;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * The Channel class represents the client's view of the channel. It handles callbacks for channel
 * events from the parser, maintains the corresponding ChannelWindow, and handles user input for the
 * channel.
 */
@Factory(inject = true, providers = true, singleton = true)
public class Channel extends MessageTarget implements ConfigChangeListener, GroupChat {

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
    /** Whether we're in this channel or not. */
    private boolean isOnChannel;
    /** Whether we should send WHO requests for this channel. */
    private volatile boolean sendWho;
    /** Whether we should show mode prefixes in text. */
    private volatile boolean showModePrefix;
    /** Whether we should show colours in nicks. */
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
            @Unbound final Server newServer,
            @Unbound final ChannelInfo newChannelInfo,
            @Unbound final ConfigProviderMigrator configMigrator,
            final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController,
            final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder,
            final EventBus eventBus) {
        super(newServer, "channel-inactive", newChannelInfo.getName(),
                Styliser.stipControlCodes(newChannelInfo.getName()),
                configMigrator.getConfigProvider(),
                new ChannelCommandParser(newServer, commandController),
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

        getConfigManager().addChangeListener("channel", this);
        getConfigManager().addChangeListener("ui", "shownickcoloursintext", this);

        topics = new RollingList<>(getConfigManager().getOptionInt("channel",
                "topichistorysize"));

        sendWho = getConfigManager().getOptionBool("channel", "sendwho");
        showModePrefix = getConfigManager().getOptionBool("channel", "showmodeprefix");
        showColours = getConfigManager().getOptionBool("ui", "shownickcoloursintext");

        eventHandler = new ChannelEventHandler(this, eventBus);

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
                || server.getParser().getChannel(channelInfo.getName()) == null) {
            // We're not in the channel/connected to the server
            return;
        }

        final ClientInfo me = server.getParser().getLocalClient();
        final String[] details = getDetails(channelInfo.getChannelClient(me));

        for (String part : splitLine(line)) {
            if (!part.isEmpty()) {
                final StringBuffer buff = new StringBuffer("channelSelfMessage");

                ActionManager.getActionManager().triggerEvent(
                        CoreActionType.CHANNEL_SELF_MESSAGE, buff, this,
                        channelInfo.getChannelClient(me), part);

                addLine(buff, details[0], details[1], details[2], details[3],
                        part, channelInfo);

                channelInfo.sendMessage(part);
            }
        }
    }

    @Override
    public int getMaxLineLength() {
        return server.getState() == ServerState.CONNECTED
                ? server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName())
                : -1;
    }

    @Override
    public void sendAction(final String action) {
        if (server.getState() != ServerState.CONNECTED
                || server.getParser().getChannel(channelInfo.getName()) == null) {
            // We're not on the server/channel
            return;
        }

        final ClientInfo me = server.getParser().getLocalClient();
        final String[] details = getDetails(channelInfo.getChannelClient(me));

        if (server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName())
                <= action.length()) {
            addLine("actionTooLong", action.length());
        } else {
            final StringBuffer buff = new StringBuffer("channelSelfAction");

            ActionManager.getActionManager().triggerEvent(
                    CoreActionType.CHANNEL_SELF_ACTION, buff, this,
                    channelInfo.getChannelClient(me), action);

            addLine(buff, details[0], details[1], details[2], details[3],
                    action, channelInfo);

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

        final ClientInfo me = server.getParser().getLocalClient();
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
        server.getParser().joinChannel(channelInfo.getName());
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

        listenerList.getCallable(NicklistListener.class)
                .clientListUpdated(Collections.<ChannelClientInfo>emptyList());
    }

    @Override
    public void close() {
        super.close();

        // Remove any callbacks or listeners
        eventHandler.unregisterCallbacks();

        if (server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(eventHandler);
        }

        // Trigger any actions neccessary
        if (isOnChannel) {
            part(getConfigManager().getOption("general", "partmessage"));
        }

        // Trigger action for the window closing
        getEventBus().post(new ChannelClosedEvent(this));

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
    public void addClient(final ChannelClientInfo client) {
        listenerList.getCallable(NicklistListener.class).clientAdded(client);

        getTabCompleter().addEntry(TabCompletionType.CHANNEL_NICK,
                client.getClient().getNickname());
    }

    /**
     * Removes the specified ChannelClient from this channel.
     *
     * @param client The client to be removed
     */
    public void removeClient(final ChannelClientInfo client) {
        listenerList.getCallable(NicklistListener.class).clientRemoved(client);

        getTabCompleter().removeEntry(TabCompletionType.CHANNEL_NICK,
                client.getClient().getNickname());

        if (client.getClient().equals(server.getParser().getLocalClient())) {
            resetWindow();
        }
    }

    /**
     * Replaces the list of known clients on this channel with the specified one.
     *
     * @param clients The list of clients to use
     */
    public void setClients(final Collection<ChannelClientInfo> clients) {
        listenerList.getCallable(NicklistListener.class).clientListUpdated(clients);

        getTabCompleter().clear(TabCompletionType.CHANNEL_NICK);

        for (ChannelClientInfo client : clients) {
            getTabCompleter().addEntry(TabCompletionType.CHANNEL_NICK,
                    client.getClient().getNickname());
        }
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

        listenerList.getCallable(NicklistListener.class).clientListUpdated();
    }

    /**
     * Returns a string containing the most important mode for the specified client.
     *
     * @param channelClient The channel client to check.
     *
     * @return A string containing the most important mode, or an empty string if there are no
     *         (known) modes.
     */
    private String getModes(final ChannelClientInfo channelClient) {
        if (channelClient == null || !showModePrefix) {
            return "";
        } else {
            return channelClient.getImportantModePrefix();
        }
    }

    @Override
    public void configChanged(final String domain, final String key) {
        switch (key) {
            case "sendwho":
                sendWho = getConfigManager().getOptionBool("channel", "sendwho");
                break;
            case "showmodeprefix":
                showModePrefix = getConfigManager().getOptionBool("channel", "showmodeprefix");
                break;
            case "shownickcoloursintext":
                showColours = getConfigManager().getOptionBool("ui", "shownickcoloursintext");
                break;
        }
    }

    /**
     * Returns a string[] containing the nickname/ident/host of a channel client.
     *
     * @param client The channel client to check
     *
     * @return A string[] containing displayable components
     */
    private String[] getDetails(final ChannelClientInfo client) {
        if (client == null) {
            // WTF?
            throw new UnsupportedOperationException("getDetails called with"
                    + " null ChannelClientInfo");
        }

        final String[] res = new String[]{
            getModes(client),
            Styliser.CODE_NICKNAME + client.getClient().getNickname() + Styliser.CODE_NICKNAME,
            client.getClient().getUsername(),
            client.getClient().getHostname(),};

        if (showColours) {
            final Map<?, ?> map = client.getMap();

            if (map.containsKey(ChannelClientProperty.TEXT_FOREGROUND)) {
                String prefix;

                if (map.containsKey(ChannelClientProperty.TEXT_BACKGROUND)) {
                    prefix = "," + ColourManager.getHex((Colour) map.get(
                            ChannelClientProperty.TEXT_BACKGROUND));
                } else {
                    prefix = Styliser.CODE_HEXCOLOUR + ColourManager.getHex((Colour) map.get(
                            ChannelClientProperty.TEXT_FOREGROUND));
                }

                res[1] = prefix + res[1] + Styliser.CODE_HEXCOLOUR;
            }
        }

        return res;
    }

    @Override
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        if (arg instanceof ClientInfo) {
            // Format ClientInfos

            final ClientInfo clientInfo = (ClientInfo) arg;
            args.add(clientInfo.getNickname());
            args.add(clientInfo.getUsername());
            args.add(clientInfo.getHostname());

            return true;
        } else if (arg instanceof ChannelClientInfo) {
            // Format ChannelClientInfos

            final ChannelClientInfo clientInfo = (ChannelClientInfo) arg;
            args.addAll(Arrays.asList(getDetails(clientInfo)));

            return true;
        } else if (arg instanceof Topic) {
            // Format topics

            args.add("");
            args.addAll(Arrays.asList(server.parseHostmask(((Topic) arg).getClient())));
            args.add(((Topic) arg).getTopic());
            args.add(((Topic) arg).getTime() * 1000);

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

        new Thread(new Runnable() {

            @Override
            public void run() {
                listenerList.getCallable(TopicChangeListener.class)
                        .topicChanged(Channel.this, topic);
            }
        }, "Topic change listener runner").start();
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
                return Optional.absent();
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
        return server.getParser().getMaxTopicLength();
    }

    @Override
    public void addNicklistListener(final NicklistListener listener) {
        listenerList.add(NicklistListener.class, listener);
    }

    @Override
    public void removeNicklistListener(final NicklistListener listener) {
        listenerList.remove(NicklistListener.class, listener);
    }

    @Override
    public void addTopicChangeListener(final TopicChangeListener listener) {
        listenerList.add(TopicChangeListener.class, listener);
    }

    @Override
    public void removeTopicChangeListener(final TopicChangeListener listener) {
        listenerList.remove(TopicChangeListener.class, listener);
    }

    @Override
    @Nonnull
    public Connection getConnection() {
        return server;
    }

}
