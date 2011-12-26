/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.ChannelCommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.NicklistListener;
import com.dmdirc.interfaces.TopicChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.collections.RollingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.ListenerSupport;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelWindow, and handles user input for the channel.
 */
@ListenerSupport({TopicChangeListener.class, NicklistListener.class})
public class Channel extends MessageTarget implements ConfigChangeListener {

    /** The parser's pChannel class. */
    @Getter
    private ChannelInfo channelInfo;

    /** The server this channel is on. */
    @Getter
    private Server server;

    /** The tabcompleter used for this channel. */
    @Getter
    private final TabCompleter tabCompleter;

    /** A list of previous topics we've seen. */
    private final RollingList<Topic> topics;

    /** Our event handler. */
    private final ChannelEventHandler eventHandler;

    /** Whether we're in this channel or not. */
    @Getter
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
     * @param newServer The server object that this channel belongs to
     * @param newChannelInfo The parser's channel object that corresponds to
     * this channel
     * @param focus Whether or not to focus this channel
     */
    public Channel(final Server newServer, final ChannelInfo newChannelInfo,
            final boolean focus) {
        super("channel-inactive", newChannelInfo.getName(),
                Styliser.stipControlCodes(newChannelInfo.getName()),
                new ConfigManager(newServer.getProtocol(), newServer.getIrcd(),
                newServer.getNetwork(), newServer.getAddress(), newChannelInfo.getName()),
                new ChannelCommandParser(newServer),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier(),
                WindowComponent.TOPICBAR.getIdentifier(),
                WindowComponent.USERLIST.getIdentifier()));

        channelInfo = newChannelInfo;
        server = newServer;

        getConfigManager().addChangeListener("channel", this);
        getConfigManager().addChangeListener("ui", "shownickcoloursintext", this);

        topics = new RollingList<Topic>(getConfigManager().getOptionInt("channel",
                "topichistorysize"));

        sendWho = getConfigManager().getOptionBool("channel", "sendwho");
        showModePrefix = getConfigManager().getOptionBool("channel", "showmodeprefix");
        showColours = getConfigManager().getOptionBool("ui", "shownickcoloursintext");

        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                CommandManager.getCommandManager().getCommandNames(CommandType.TYPE_CHANNEL));
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                CommandManager.getCommandManager().getCommandNames(CommandType.TYPE_CHAT));

        WindowManager.getWindowManager().addWindow(server, this, focus);

        eventHandler = new ChannelEventHandler(this);

        registerCallbacks();

        ActionManager.getActionManager().triggerEvent(
                CoreActionType.CHANNEL_OPENED, null, this);

        updateTitle();
        selfJoin();
    }

    /**
     * Registers callbacks with the parser for this channel.
     */
    private void registerCallbacks() {
        eventHandler.registerCallbacks();
        getConfigManager().migrate(server.getProtocol(), server.getIrcd(),
                server.getNetwork(), server.getAddress(), channelInfo.getName());
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return server.getState() == ServerState.CONNECTED
                ? server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName())
                : -1;
    }

    /** {@inheritDoc} */
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
     * Sets this object's ChannelInfo reference to the one supplied. This only
     * needs to be done if the channel window (and hence this channel object)
     * has stayed open while the user has been out of the channel.
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
     * Updates the title of the channel window, and of the main window if
     * appropriate.
     */
    private void updateTitle() {
        String temp = Styliser.stipControlCodes(channelInfo.getName());

        if (!channelInfo.getTopic().isEmpty()) {
            temp += " - " + Styliser.stipControlCodes(channelInfo.getTopic());
        }

        setTitle(temp);
    }

    /**
     * Joins the specified channel. This only makes sense if used after a call
     * to part().
     */
    public void join() {
        server.getParser().joinChannel(channelInfo.getName());
    }

    /**
     * Parts this channel with the specified message. Parting does NOT close the
     * channel window.
     *
     * @param reason The reason for parting the channel
     */
    public void part(final String reason) {
        channelInfo.part(reason);

        resetWindow();
    }

    /**
     * Requests all available list modes for this channel.
     *
     * @since 0.6.3
     */
    public void retrieveListModes() {
        channelInfo.requestListModes();
    }

    /**
     * Resets the window state after the client has left a channel.
     */
    public void resetWindow() {
        isOnChannel = false;

        setIcon("channel-inactive");

        fireClientListUpdated(new ArrayList<ChannelClientInfo>());
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 2: Remove any callbacks or listeners
        eventHandler.unregisterCallbacks();

        if (server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(eventHandler);
        }

        // 3: Trigger any actions neccessary
        if (isOnChannel) {
            part(getConfigManager().getOption("general", "partmessage"));
        }

        // 4: Trigger action for the window closing
        ActionManager.getActionManager().triggerEvent(
                CoreActionType.CHANNEL_CLOSED, null, this);

        // 5: Inform any parents that the window is closing
        server.delChannel(channelInfo.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
        synchronized (this) {
            server = null; // NOPMD
        }
    }

    /**
     * Called every {general.whotime} seconds to check if the channel needs
     * to send a who request.
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
        fireClientAdded(client);

        tabCompleter.addEntry(TabCompletionType.CHANNEL_NICK, client.getClient().getNickname());
    }

    /**
     * Removes the specified ChannelClient from this channel.
     *
     * @param client The client to be removed
     */
    public void removeClient(final ChannelClientInfo client) {
        fireClientRemoved(client);

        tabCompleter.removeEntry(TabCompletionType.CHANNEL_NICK, client.getClient().getNickname());

        if (client.getClient().equals(server.getParser().getLocalClient())) {
            resetWindow();
        }
    }

    /**
     * Replaces the list of known clients on this channel with the specified
     * one.
     *
     * @param clients The list of clients to use
     */
    public void setClients(final Collection<ChannelClientInfo> clients) {
        fireClientListUpdated(clients);

        tabCompleter.clear(TabCompletionType.CHANNEL_NICK);

        for (ChannelClientInfo client : clients) {
            tabCompleter.addEntry(TabCompletionType.CHANNEL_NICK, client.getClient().getNickname());
        }
    }

    /**
     * Renames a client that is in this channel.
     *
     * @param oldName The old nickname of the client
     * @param newName The new nickname of the client
     */
    public void renameClient(final String oldName, final String newName) {
        tabCompleter.removeEntry(TabCompletionType.CHANNEL_NICK, oldName);
        tabCompleter.addEntry(TabCompletionType.CHANNEL_NICK, newName);
        refreshClients();
    }

    /**
     * Refreshes the list of clients stored by this channel. Should be called
     * when (visible) user modes or nicknames change.
     */
    public void refreshClients() {
        if (!isOnChannel) {
            return;
        }

        fireClientListUpdated();
    }

    /**
     * Returns a string containing the most important mode for the specified
     * client.
     *
     * @param channelClient The channel client to check.
     * @return A string containing the most important mode, or an empty string
     * if there are no (known) modes.
     */
    private String getModes(final ChannelClientInfo channelClient) {
        if (channelClient == null || !showModePrefix) {
            return "";
        } else {
            return channelClient.getImportantModePrefix();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("sendwho".equals(key)) {
            sendWho = getConfigManager().getOptionBool("channel", "sendwho");
        } else if ("showmodeprefix".equals(key)) {
            showModePrefix = getConfigManager().getOptionBool("channel", "showmodeprefix");
        } else if ("shownickcoloursintext".equals(key)) {
            showColours = getConfigManager().getOptionBool("ui", "shownickcoloursintext");
        }
    }

    /**
     * Returns a string[] containing the nickname/ident/host of a channel
     * client.
     *
     * @param client The channel client to check
     * @return A string[] containing displayable components
     */
    private String[] getDetails(final ChannelClientInfo client) {
        if (client == null) {
            // WTF?
            throw new UnsupportedOperationException("getDetails called with"
                     + " null ChannelClientInfo");
        }

        final String[] res = new String[] {
            getModes(client),
            Styliser.CODE_NICKNAME + client.getClient().getNickname() + Styliser.CODE_NICKNAME,
            client.getClient().getUsername(),
            client.getClient().getHostname(),
        };

        if (showColours) {
            final Map<?, ?> map = client.getMap();

            if (map.containsKey(ChannelClientProperty.TEXT_FOREGROUND)) {
                String prefix;

                if (map.containsKey(ChannelClientProperty.TEXT_BACKGROUND)) {
                    prefix = "," + ColourManager.getHex((Colour)
                            map.get(ChannelClientProperty.TEXT_BACKGROUND));
                } else {
                    prefix = Styliser.CODE_HEXCOLOUR + ColourManager.getHex((Colour)
                            map.get(ChannelClientProperty.TEXT_FOREGROUND));
                }

                res[1] = prefix + res[1] + Styliser.CODE_HEXCOLOUR;
            }
        }

        return res;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

            /** {@inheritDoc} */
            @Override
            public void run() {
                fireTopicChanged(Channel.this, topic);
            }
        }, "Topic change listener runner").start();
    }

    /**
     * Retrieve the topics that have been seen on this channel.
     *
     * @return A list of topics that have been seen on this channel, including
     * the current one.
     */
    public List<Topic> getTopics() {
        synchronized (topics) {
            return new ArrayList<Topic>(topics.getList());
        }
    }

    /**
     * Returns the current topic for this channel.
     *
     * @return Current channel topic
     */
    public Topic getCurrentTopic() {
        synchronized (topics) {
            if (topics.getList().isEmpty()) {
                return null;
            } else {
                return topics.get(topics.getList().size() - 1);
            }
        }
    }

    // ------------------------------------------ PARSER METHOD DELEGATION -----

    /**
     * Attempts to set the topic of this channel.
     *
     * @param topic The new topic to be used. An empty string will clear the
     * current topic
     */
    public void setTopic(final String topic) {
        channelInfo.setTopic(topic);
    }

    /**
     * Retrieves the maximum length that a topic on this channel can be.
     *
     * @return The maximum length that this channel's topic may be
     * @since 0.6.3
     */
    public int getMaxTopicLength() {
        return server.getParser().getMaxTopicLength();
    }
}
