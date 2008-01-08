/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.RollingList;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelWindow, and handles user input for the channel.
 *
 * @author chris
 */
public final class Channel extends MessageTarget
        implements ConfigChangeListener, Serializable {
    
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
    
    /** The config manager for this channel. */
    private final ConfigManager configManager;
    
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
     * @param newServer The server object that this channel belongs to
     * @param newChannelInfo The parser's channel object that corresponds to
     * this channel
     */
    public Channel(final Server newServer, final ChannelInfo newChannelInfo) {
        super();
        
        channelInfo = newChannelInfo;
        server = newServer;
        
        configManager = new ConfigManager(server.getIrcd(), server.getNetwork(),
                server.getName(), channelInfo.getName());
                
        configManager.addChangeListener("channel", this);
        configManager.addChangeListener("ui", "shownickcoloursintext", this);
        
        topics = new RollingList<Topic>(configManager.getOptionInt("channel",
                "topichistorysize", 10));
        
        sendWho = configManager.getOptionBool("channel", "sendwho", false);
        showModePrefix = configManager.getOptionBool("channel", "showmodeprefix", false);
        showColours = configManager.getOptionBool("ui", "shownickcoloursintext", false);
        
        icon = IconManager.getIconManager().getIcon("channel");
        
        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(CommandManager.getCommandNames(CommandType.TYPE_CHANNEL));
        tabCompleter.addEntries(CommandManager.getCommandNames(CommandType.TYPE_CHAT));
        
        window = Main.getUI().getChannel(Channel.this);
        WindowManager.addWindow(server.getFrame(), window);
        window.setFrameIcon(icon);
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
        configManager.migrate(server.getIrcd(), server.getNetwork(),
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
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /** {@inheritDoc} */
    @Override
    public void sendLine(final String line) {
        if (server.getParser().getChannelInfo(channelInfo.getName()) == null) {
            // We're not in the channel
            return;
        } else if (line.indexOf('\n') > -1) {
            for (String part : line.split("\n")) {
                sendLine(part);
            }
            
            return;
        }
        
        final ClientInfo me = server.getParser().getMyself();
        final String modes = getModes(channelInfo.getUser(me));
        final String[] details = getDetails(channelInfo.getUser(me), showColours);
        
        if (line.length() <= getMaxLineLength()) {
            final StringBuffer buff = new StringBuffer("channelSelfMessage");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_SELF_MESSAGE, buff,
                    this, channelInfo.getUser(me), line);
            
            addLine(buff, modes, details[0], details[1], details[2],
                    window.getTranscoder().encode(line), channelInfo);
            
            channelInfo.sendMessage(window.getTranscoder().encode(line));
        } else {
            sendLine(line.substring(0, getMaxLineLength()));
            sendLine(line.substring(getMaxLineLength()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public void sendAction(final String action) {
        if (server.getParser().getChannelInfo(channelInfo.getName()) == null) {
            // We're not in the channel
            return;
        }
        
        final ClientInfo me = server.getParser().getMyself();
        final String modes = channelInfo.getUser(me).getImportantModePrefix();
        
        if (server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName()) <= action.length()) {
            addLine("actionTooLong", action.length());
        } else {
            final StringBuffer buff = new StringBuffer("channelSelfAction");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_SELF_ACTION, buff,
                    this, channelInfo.getUser(me), action);
            
            addLine(buff, modes, me.getNickname(), me.getIdent(),
                    me.getHost(), window.getTranscoder().encode(action), channelInfo);
            
            channelInfo.sendAction(window.getTranscoder().encode(action));
        }
    }
    
    /**
     * Returns the server object that this channel belongs to.
     * @return The server object
     */
    @Override
    public Server getServer() {
        return server;
    }
    
    /**
     * Returns the parser's ChannelInfo object that this object is associated with.
     * @return The ChannelInfo object associated with this object
     */
    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }
    
    /**
     * Sets this object's ChannelInfo reference to the one supplied. This only needs
     * to be done if the channel window (and hence this channel object) has stayed
     * open while the user has been out of the channel.
     * @param newChannelInfo The new ChannelInfo object
     */
    public void setChannelInfo(final ChannelInfo newChannelInfo) {
        channelInfo = newChannelInfo;
        registerCallbacks();
    }
    
    /**
     * Returns the internal window belonging to this object.
     * @return This object's internal window
     */
    @Override
    public InputWindow getFrame() {
        return window;
    }
    
    /**
     * Returns the tab completer for this channel.
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
        
        final ClientInfo me = server.getParser().getMyself();
        addLine("channelSelfJoin", "", me.getNickname(), me.getIdent(),
                me.getHost(), channelInfo.getName());
        
        icon = IconManager.getIconManager().getIcon("channel");
        iconUpdated(icon);
        
        server.removeInvites(channelInfo.getName());
    }
    
    /**
     * Updates the title of the channel window, and of the main window if appropriate.
     */
    private void updateTitle() {
        String temp = Styliser.stipControlCodes(channelInfo.getName());
        
        if (!channelInfo.getTopic().isEmpty()) {
            temp = temp + " - " + Styliser.stipControlCodes(channelInfo.getTopic());
        }
        
        window.setTitle(temp);
        
        if (window.isMaximum() && window.equals(Main.getUI().getMainWindow().getActiveFrame())) {
            Main.getUI().getMainWindow().setTitle(
                    Main.getUI().getMainWindow().getTitlePrefix() + " - " + temp);
        }
    }
    
    /**
     * Joins the specified channel. This only makes sense if used after a call to
     * part().
     */
    public void join() {
        server.getParser().joinChannel(channelInfo.getName());
        activateFrame();
        
        icon = IconManager.getIconManager().getIcon("channel");
        iconUpdated(icon);        
    }
    
    /**
     * Parts this channel with the specified message. Parting does NOT close the
     * channel window.
     * @param reason The reason for parting the channel
     */
    public void part(final String reason) {
        server.getParser().partChannel(channelInfo.getName(), reason);
        resetWindow();
    }
    
    /**
     * Resets the window state after the client has left a channel.
     */
    public void resetWindow() {
        onChannel = false;
        
        icon = IconManager.getIconManager().getIcon("channel-inactive");
        iconUpdated(icon);
        
        window.updateNames(new ArrayList<ChannelClientInfo>());
    }
    
    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 1: Make the window non-visible
        window.setVisible(false);
        
        // 2: Remove any callbacks or listeners
        eventHandler.unregisterCallbacks();
        
        // 3: Trigger any actions neccessary
        part(configManager.getOption("general", "partmessage"));
        
        // 4: Trigger action for the window closing
        ActionManager.processEvent(CoreActionType.CHANNEL_CLOSED, null, this);
        
        // 5: Inform any parents that the window is closing
        server.delChannel(channelInfo.getName());
        
        // 6: Remove the window from the window manager
        WindowManager.removeWindow(window);
        if (server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(eventHandler);
        }
        
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
            server.getParser().sendLine("WHO :" + channelInfo.getName());
        }
    }
    
    /**
     * Adds a ChannelClient to this Channel.
     * 
     * @param client The client to be added
     */
    public void addClient(final ChannelClientInfo client) {
        window.addName(client);
        tabCompleter.addEntry(client.getNickname());   
    }
    
    /**
     * Removes the specified ChannelClient from this channel.
     * 
     * @param client The client to be removed
     */
    public void removeClient(final ChannelClientInfo client) {
        window.removeName(client);
        tabCompleter.removeEntry(client.getNickname());
        
        if (client.getClient().equals(server.getParser().getMyself())) {
            resetWindow();
        }
    }
    
    /**
     * Renames a client that is in this channel.
     * 
     * @param oldName The old nickname of the client
     * @param newName The new nickname of the client
     */
    public void renameClient(final String oldName, final String newName) {
        tabCompleter.removeEntry(oldName);
        tabCompleter.addEntry(newName);        
        refreshClients();
    }
    
    /**
     * Refreshes the list of clients stored by this channel. Should be called
     * when (visible) user modes or nicknames change.
     */
    public void refreshClients() {
        window.updateNames();
    }
   
    public void onChannelGotNames() {
        
        window.updateNames(channelInfo.getChannelClients());
        
        final ArrayList<String> names = new ArrayList<String>();
        
        for (ChannelClientInfo channelClient : channelInfo.getChannelClients()) {
            names.add(channelClient.getNickname());
        }
        
        tabCompleter.replaceEntries(names);
        tabCompleter.addEntries(CommandManager.getCommandNames(CommandType.TYPE_CHANNEL));
        tabCompleter.addEntries(CommandManager.getCommandNames(CommandType.TYPE_CHAT));
        
        ActionManager.processEvent(CoreActionType.CHANNEL_GOTNAMES, null, this);
    }
    
    public void onChannelTopic(final boolean bIsJoinTopic) {
        if (bIsJoinTopic) {
            final StringBuffer buff = new StringBuffer("channelJoinTopic");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_GOTTOPIC, buff, this);
            
            addLine(buff, channelInfo.getTopic(), channelInfo.getTopicUser(),
                    1000 * channelInfo.getTopicTime(), channelInfo);
        } else {
            final ChannelClientInfo user = channelInfo.getUser(channelInfo.getTopicUser());
            final String[] parts = ClientInfo.parseHostFull(channelInfo.getTopicUser());
            final String modes = getModes(user);
            final String topic = channelInfo.getTopic();
            
            final StringBuffer buff = new StringBuffer("channelTopicChange");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_TOPICCHANGE, buff, this, user, topic);
            
            addLine(buff, modes, parts[0], parts[1], parts[2], channelInfo, topic);
        }
        
        topics.add(new Topic(channelInfo.getTopic(), 
                channelInfo.getTopicUser(), channelInfo.getTopicTime()));
        
        updateTitle();
    }
    
    public void onChannelModeChanged(
            final ChannelClientInfo cChannelClient, final String sHost, final String sModes) {
        if (sHost.isEmpty()) {
            final StringBuffer buff = new StringBuffer(21);
            
            if (sModes.length() <= 1) {
                buff.append("channelNoModes");
            } else {
                buff.append("channelModeDiscovered");
            }
            
            ActionManager.processEvent(CoreActionType.CHANNEL_MODECHANGE, buff, this, cChannelClient, sModes);
            
            addLine(buff, sModes, channelInfo.getName());
        } else {
            final String modes = getModes(cChannelClient);
            final String[] details = getDetails(cChannelClient, showColours);
            final String myNick = server.getParser().getMyself().getNickname();
            
            String type = "channelModeChange";
            if (myNick.equals(cChannelClient.getNickname())) {
                type = "channelSelfModeChange";
            }
            
            final StringBuffer buff = new StringBuffer(type);
            
            ActionManager.processEvent(CoreActionType.CHANNEL_MODECHANGE, buff, this, cChannelClient, sModes);
            
            addLine(type,  modes, details[0], details[1],
                    details[2], channelInfo.getName(), sModes);
        }
        
        window.updateNames();
    }
    
    public void onChannelUserModeChanged(
            final ChannelClientInfo cChangedClient,
            final ChannelClientInfo cSetByClient, final String sMode) {
        
        if (configManager.getOptionBool("channel", "splitusermodes", false)) {
            final String sourceModes = getModes(cSetByClient);
            final String[] sourceHost = getDetails(cSetByClient, showColours);
            final String targetModes = cChangedClient.getImportantModePrefix();
            final String targetNick = cChangedClient.getClient().getNickname();
            final String targetIdent = cChangedClient.getClient().getIdent();
            final String targetHost = cChangedClient.getClient().getHost();
            
            String format = "channelUserMode_" + sMode;
            if (!Formatter.hasFormat(format)) {
                format = "channelUserMode_default";
            }
            
            addLine(format, sourceModes, sourceHost[0], sourceHost[1],
                    sourceHost[2], targetModes, targetNick, targetIdent,
                    targetHost, channelInfo, sMode);
        }
        
        ActionManager.processEvent(CoreActionType.CHANNEL_USERMODECHANGE, null,
                this, cSetByClient, cChangedClient, sMode);
    }
    
    public void onAwayStateOther(final ClientInfo client, final boolean state) {
        final ChannelClientInfo channelClient = channelInfo.getUser(client);
        
        if (channelClient != null) {
            if (state) {
                ActionManager.processEvent(CoreActionType.CHANNEL_USERAWAY,
                        null, this, channelClient);
            } else {
                ActionManager.processEvent(CoreActionType.CHANNEL_USERBACK,
                        null, this, channelClient);
            }
        }
    }
    
    /**
     * Returns a string containing the most important mode for the specified client.
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
     * Retrieve the topics that have been seen on this channel.
     * 
     * @return A list of topics that have been seen on this channel, including
     * the current one.
     */
    public List<Topic> getTopics() {
        return topics.getList();
    }
    
    /**
     * Returns this channel's name.
     * 
     * @return A string representation of this channel (i.e., its name)
     */
    @Override
    public String toString() {
        return channelInfo.getName();
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("sendwho".equals(key)) {
            sendWho = configManager.getOptionBool("channel", "sendwho", false);
        } else if ("showmodeprefix".equals(key)) {
            showModePrefix = configManager.getOptionBool("channel", "showmodeprefix", false);
        } else if ("shownickcoloursintext".equals(key)) {
            showColours = configManager.getOptionBool("ui", "shownickcoloursintext", false);
        }
    }
    
    /**
     * Returns a string[] containing the nickname/ident/host of a channel client.
     * 
     * @param client The channel client to check
     * @param showColours Whether or not to show colours
     * @return A string[] containing displayable components
     */
    private static String[] getDetails(final ChannelClientInfo client, final boolean showColours) {
        if (client == null) {
            // WTF?
            throw new UnsupportedOperationException("getDetails called with" +
                    "null ChannelClientInfo");
        }
        
        final String[] res = new String[3];
        res[0] = Styliser.CODE_NICKNAME + client.getNickname() + Styliser.CODE_NICKNAME;
        res[1] = client.getClient().getIdent();
        res[2] = client.getClient().getHost();
        
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
                res[0] = prefix + res[0] + Styliser.CODE_HEXCOLOUR;
            }
        }
        
        return res;
    }    
    
    /** {@inheritDoc} */
    @Override
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        if (arg instanceof ClientInfo) {
            final ClientInfo clientInfo = (ClientInfo) arg;
            args.add(clientInfo.getNickname());
            args.add(clientInfo.getIdent());
            args.add(clientInfo.getHost());
            return true;
        } else if (arg instanceof ChannelClientInfo) {
            final ChannelClientInfo clientInfo = (ChannelClientInfo) arg;
            args.add(getModes(clientInfo));
            args.addAll(Arrays.asList(getDetails(clientInfo, showColours)));
            return true;            
        } else {
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
        server.getParser().sendLine("TOPIC " + channelInfo.getName() + " :" + topic);
    }
}
