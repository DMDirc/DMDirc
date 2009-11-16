/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.RollingList;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelWindow, and handles user input for the channel.
 *
 * @author chris
 */
public class Channel extends MessageTarget implements ConfigChangeListener,
        Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** The parser's pChannel class. */
    private transient ChannelInfo channelInfo;

    /** The server this channel is on. */
    private Server server;

    /** The ChannelWindow used for this channel. */
    private ChannelWindow window;

    /** The tabcompleter used for this channel. */
    private final TabCompleter tabCompleter;

    /** A list of previous topics we've seen. */
    private final RollingList<Topic> topics;

    /** Our event handler. */
    private final ChannelEventHandler eventHandler;

    /** Whether we're in this channel or not. */
    private boolean onChannel;

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
     */
    public Channel(final Server newServer, final ChannelInfo newChannelInfo) {
        super("channel", newChannelInfo.getName(),
                new ConfigManager(newServer.getIrcd(), newServer.getNetwork(),
                newServer.getName(), newChannelInfo.getName()));

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
                CommandManager.getCommandNames(CommandType.TYPE_CHANNEL));
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                CommandManager.getCommandNames(CommandType.TYPE_CHAT));

        window = Main.getUI().getChannel(Channel.this);
        WindowManager.addWindow(server.getFrame(), window);
        window.getInputHandler().setTabCompleter(tabCompleter);

        eventHandler = new ChannelEventHandler(this);

        registerCallbacks();

        ActionManager.processEvent(CoreActionType.CHANNEL_OPENED, null, this);

        updateTitle();
        selfJoin();
    }

    /**
     * Registers callbacks with the parser for this channel.
     */
    private void registerCallbacks() {
        eventHandler.registerCallbacks();
        getConfigManager().migrate(server.getIrcd(), server.getNetwork(),
                server.getName(), channelInfo.getName());
    }

    /**
     * Shows this channel's window.
     */
    public void show() {
        window.open();
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
        final String[] details = getDetails(channelInfo.getChannelClient(me), showColours);

        for (String part : splitLine(window.getTranscoder().encode(line))) {
            if (!part.isEmpty()) {
                final StringBuffer buff = new StringBuffer("channelSelfMessage");

                ActionManager.processEvent(CoreActionType.CHANNEL_SELF_MESSAGE, buff,
                        this, channelInfo.getChannelClient(me), part);

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
        final String[] details = getDetails(channelInfo.getChannelClient(me), showColours);

        if (server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName())
                <= action.length()) {
            addLine("actionTooLong", action.length());
        } else {
            final StringBuffer buff = new StringBuffer("channelSelfAction");

            ActionManager.processEvent(CoreActionType.CHANNEL_SELF_ACTION, buff,
                    this, channelInfo.getChannelClient(me), action);

            addLine(buff, details[0], details[1], details[2], details[3],
                    window.getTranscoder().encode(action), channelInfo);

            channelInfo.sendAction(window.getTranscoder().encode(action));
        }
    }

    /**
     * Returns the server object that this channel belongs to.
     *
     * @return The server object
     */
    @Override
    public Server getServer() {
        return server;
    }

    /**
     * Returns the parser's ChannelInfo object that this object is associated
     * with.
     *
     * @return The ChannelInfo object associated with this object
     */
    public ChannelInfo getChannelInfo() {
        return channelInfo;
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
     * Returns the internal window belonging to this object.
     *
     * @return This object's internal window
     */
    @Override
    public InputWindow getFrame() {
        return window;
    }

    /**
     * Returns the tab completer for this channel.
     *
     * @return This channel's tab completer
     */
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    /**
     * Called when we join this channel. Just needs to output a message.
     */
    public void selfJoin() {
        onChannel = true;

        final ClientInfo me = server.getParser().getLocalClient();
        addLine("channelSelfJoin", "", me.getNickname(), me.getUsername(),
                me.getHostname(), channelInfo.getName());

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
            temp = temp + " - " + Styliser.stipControlCodes(channelInfo.getTopic());
        }

        window.setTitle(temp);
    }

    /**
     * Joins the specified channel. This only makes sense if used after a call
     * to part().
     */
    public void join() {
        server.getParser().joinChannel(channelInfo.getName());
        activateFrame();

        setIcon("channel");
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
     * Resets the window state after the client has left a channel.
     */
    public void resetWindow() {
        onChannel = false;

        setIcon("channel-inactive");

        window.updateNames(new ArrayList<ChannelClientInfo>());
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 1: Make the window non-visible
        window.setVisible(false);

        // 2: Remove any callbacks or listeners
        eventHandler.unregisterCallbacks();

        if (server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(eventHandler);
        }

        // 3: Trigger any actions neccessary
        if (onChannel) {
            part(getConfigManager().getOption("general", "partmessage"));
        }

        // 4: Trigger action for the window closing
        ActionManager.processEvent(CoreActionType.CHANNEL_CLOSED, null, this);

        // 5: Inform any parents that the window is closing
        server.delChannel(channelInfo.getName());

        // 6: Remove the window from the window manager
        WindowManager.removeWindow(window);

        // 7: Remove any references to the window and parents
        window = null; // NOPMD
        server = null; // NOPMD
    }

    /**
     * Called every {general.whotime} seconds to check if the channel needs
     * to send a who request.
     */
    public void checkWho() {
        if (onChannel && sendWho) {
            channelInfo.sendWho();
        }
    }

    /**
     * Adds a ChannelClient to this Channel.
     *
     * @param client The client to be added
     */
    public void addClient(final ChannelClientInfo client) {
        window.addName(client);
        tabCompleter.addEntry(TabCompletionType.CHANNEL_NICK, client.getClient().getNickname());
    }

    /**
     * Removes the specified ChannelClient from this channel.
     *
     * @param client The client to be removed
     */
    public void removeClient(final ChannelClientInfo client) {
        window.removeName(client);
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
        window.updateNames(clients);

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
        if (window != null && onChannel) {
            window.updateNames();
        }
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

    /**
     * Adds the specified topic to this channel's topic list.
     *
     * @param topic The topic to be added.
     */
    public void addTopic(final Topic topic) {
        topics.add(topic);
        updateTitle();
    }

    /**
     * Retrieve the topics that have been seen on this channel.
     *
     * @return A list of topics that have been seen on this channel, including
     * the current one.
     */
    public List<Topic> getTopics() {
        return topics.getList();
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
     * @param showColours Whether or not to show colours
     * @return A string[] containing displayable components
     */
    private String[] getDetails(final ChannelClientInfo client,
            final boolean showColours) {
        if (client == null) {
            // WTF?
            throw new UnsupportedOperationException("getDetails called with"
                     + " null ChannelClientInfo");
        }

        final String[] res = new String[4];
        res[0] = getModes(client);
        res[1] = Styliser.CODE_NICKNAME + client.getClient().getNickname() + Styliser.CODE_NICKNAME;
        res[2] = client.getClient().getUsername();
        res[3] = client.getClient().getHostname();

        if (showColours) {
            final Map map = client.getMap();
            String prefix = null;
            Color colour;

            if (map.containsKey(ChannelClientProperty.TEXT_FOREGROUND)) {
                colour = (Color) map.get(ChannelClientProperty.TEXT_FOREGROUND);
                prefix = Styliser.CODE_HEXCOLOUR + ColourManager.getHex(colour);
                if (map.containsKey(ChannelClientProperty.TEXT_BACKGROUND)) {
                    colour = (Color) map.get(ChannelClientProperty.TEXT_BACKGROUND);
                    prefix = "," + ColourManager.getHex(colour);
                }
            }

            if (prefix != null) {
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
            args.addAll(Arrays.asList(getDetails(clientInfo, showColours)));

            return true;
        } else if (arg instanceof Topic) {
            // Format topics

            args.add("");
            args.addAll(Arrays.asList(server.getParser().parseHostmask(((Topic) arg).getClient())));
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
}
