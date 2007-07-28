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
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ChannelInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.callbacks.CallbackManager;
import com.dmdirc.parser.callbacks.CallbackNotFound;
import com.dmdirc.parser.callbacks.interfaces.IAwayStateOther;
import com.dmdirc.parser.callbacks.interfaces.IChannelAction;
import com.dmdirc.parser.callbacks.interfaces.IChannelCTCP;
import com.dmdirc.parser.callbacks.interfaces.IChannelGotNames;
import com.dmdirc.parser.callbacks.interfaces.IChannelJoin;
import com.dmdirc.parser.callbacks.interfaces.IChannelKick;
import com.dmdirc.parser.callbacks.interfaces.IChannelMessage;
import com.dmdirc.parser.callbacks.interfaces.IChannelModeChanged;
import com.dmdirc.parser.callbacks.interfaces.IChannelNickChanged;
import com.dmdirc.parser.callbacks.interfaces.IChannelPart;
import com.dmdirc.parser.callbacks.interfaces.IChannelQuit;
import com.dmdirc.parser.callbacks.interfaces.IChannelTopic;
import com.dmdirc.parser.callbacks.interfaces.IChannelUserModeChanged;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.Styliser;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.SwingUtilities;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelFrame, and handles user input to a ChannelFrame.
 *
 * @author chris
 */
public final class Channel extends MessageTarget implements
        IChannelMessage, IChannelGotNames, IChannelTopic, IChannelJoin,
        IChannelPart, IChannelKick, IChannelQuit, IChannelAction,
        IChannelNickChanged, IChannelModeChanged, IChannelUserModeChanged,
        IChannelCTCP, IAwayStateOther, Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * The callbacks that should be registered for channel instances.
     */
    private static final String[] CALLBACKS = {
        "OnChannelMessage", "OnChannelGotNames", "OnChannelTopic",
        "OnChannelJoin", "OnChannelPart", "OnChannelKick", "OnChannelQuit",
        "OnChannelAction", "OnChannelNickChanged", "OnChannelModeChanged",
        "OnChannelUserModeChanged", "OnChannelCTCP",
    };
    
    /** The parser's pChannel class. */
    private transient ChannelInfo channelInfo;
    
    /** The server this channel is on. */
    private Server server;
    
    /** The ChannelWindow used for this channel. */
    private ChannelWindow frame;
    
    /** The tabcompleter used for this channel. */
    private final TabCompleter tabCompleter;
    
    /** The config manager for this channel. */
    private final ConfigManager configManager;
    
    /** Whether we're in this channel or not. */
    private boolean onChannel;
    
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
        
        icon = IconManager.getIconManager().getIcon("channel");
        
        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(CommandManager.getChannelCommandNames());
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    frame = Main.getUI().getChannel(Channel.this);
                    frame.setFrameIcon(icon);
                    frame.getInputHandler().setTabCompleter(tabCompleter);
                }
            });
        } catch (InvocationTargetException ex) {
            Logger.appError(ErrorLevel.FATAL, "Unable to load channel", ex.getCause());
        } catch (InterruptedException ex) {
            Logger.appError(ErrorLevel.FATAL, "Unable to load channel", ex);
        }
        
        registerCallbacks();
        
        ActionManager.processEvent(CoreActionType.CHANNEL_OPENED, null, this);
        
        updateTitle();
        selfJoin();
    }
    
    /**
     * Registers callbacks with the parser for this channel.
     */
    private void registerCallbacks() {
        try {
            final CallbackManager callbackManager = server.getParser().getCallbackManager();
            final String channel = channelInfo.getName();
            
            for (String callback : CALLBACKS) {
                callbackManager.addCallback(callback, this, channel);
            }
        } catch (CallbackNotFound ex) {
            Logger.appError(ErrorLevel.FATAL, "Unable to load channel", ex);
        }
    }
    
    /**
     * Shows this channel's frame.
     */
    public void show() {
        frame.open();
    }
    
    /** {@inheritDoc} */
    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /** {@inheritDoc} */
    public void sendLine(final String line) {
        final ClientInfo me = server.getParser().getMyself();
        final String modes = getModes(channelInfo.getUser(me));
        final String[] details = getDetails(channelInfo.getUser(me));
        
        if (line.length() <= getMaxLineLength()) {
            final StringBuffer buff = new StringBuffer("channelSelfMessage");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_SELF_MESSAGE, buff,
                    this, channelInfo.getUser(me), line);
            
            frame.addLine(buff, modes, details[0], details[1], details[2],
                    line, channelInfo);
            
            channelInfo.sendMessage(line);
        } else {
            sendLine(line.substring(0, getMaxLineLength()));
            sendLine(line.substring(getMaxLineLength()));
        }
    }
    
    /** {@inheritDoc} */
    public int getMaxLineLength() {
        return server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName());
    }
    
    /** {@inheritDoc} */
    public void sendAction(final String action) {
        final ClientInfo me = server.getParser().getMyself();
        final String modes = channelInfo.getUser(me).getImportantModePrefix();
        
        if (server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName()) <= action.length()) {
            frame.addLine("actionTooLong", action.length());
        } else {
            final StringBuffer buff = new StringBuffer("channelSelfAction");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_SELF_ACTION, buff,
                    this, channelInfo.getUser(me), action);
            
            frame.addLine(buff, modes, me.getNickname(), me.getIdent(),
                    me.getHost(), action, channelInfo);
            
            channelInfo.sendAction(action);
        }
    }
    
    /**
     * Returns the server object that this channel belongs to.
     * @return The server object
     */
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
     * Returns the internal frame belonging to this object.
     * @return This object's internal frame
     */
    public InputWindow getFrame() {
        return frame;
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
        frame.addLine("channelSelfJoin", "", me.getNickname(), me.getIdent(),
                me.getHost(), channelInfo.getName());
    }
    
    /**
     * Updates the title of the channel frame, and of the main frame if appropriate.
     */
    private void updateTitle() {
        String temp = Styliser.stipControlCodes(channelInfo.getName());
        
        if (channelInfo.getTopic().length() > 0) {
            temp = temp + " - " + Styliser.stipControlCodes(channelInfo.getTopic());
        }
        
        // Needs to be final for AIC
        final String title = temp;
        
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                frame.setTitle(title);
                
                if (frame.isMaximum() && frame.equals(Main.getUI().getMainWindow().getActiveFrame())) {
                    Main.getUI().getMainWindow().setTitle(Main.getUI().getMainWindow().getTitlePrefix() + " - " + title);
                }
            }
        });
    }
    
    /**
     * Joins the specified channel. This only makes sense if used after a call to
     * part().
     */
    public void join() {
        server.getParser().joinChannel(channelInfo.getName());
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
        
        frame.updateNames(new ArrayList<ChannelClientInfo>());
    }
    
    /**
     * Parts the channel and then closes the frame.
     */
    public void close() {
        part(configManager.getOption("general", "partmessage"));
        closeWindow();
    }
    
    /**
     * Closes the window without parting the channel.
     */
    public void closeWindow() {
        final CallbackManager callbackManager = server.getParser().getCallbackManager();
        
        for (String callback : CALLBACKS) {
            callbackManager.delCallback(callback, this);
        }
        
        ActionManager.processEvent(CoreActionType.CHANNEL_CLOSED, null, this);
        
        server.delChannel(channelInfo.getName());
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(false);
                Main.getUI().getMainWindow().delChild(frame);
                frame = null;
                server = null;
            }
        });
    }
    
    /**
     * Called every {general.whotime} seconds to check if the channel needs
     * to send a who request.
     */
    public void checkWho() {
        if (onChannel && configManager.getOptionBool("channel", "sendwho")) {
            server.getParser().sendLine("WHO :" + channelInfo.getName());
        }
    }
    
    /** {@inheritDoc} */
    public void onChannelMessage(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
        
        String type = "channelMessage";
        if (cChannelClient.getClient().equals(tParser.getMyself())) {
            type = "channelSelfExternalMessage";
        }
        
        final String[] parts = getDetails(cChannelClient);
        final String modes = getModes(cChannelClient);
        
        final StringBuffer buff = new StringBuffer(type);
        
        ActionManager.processEvent(CoreActionType.CHANNEL_MESSAGE, buff, this, cChannelClient, sMessage);
        
        frame.addLine(buff, modes, parts[0], parts[1], parts[2], sMessage, cChannel);
    }
    
    /** {@inheritDoc} */
    public void onChannelAction(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
        final String[] parts = getDetails(cChannelClient);
        final String modes = getModes(cChannelClient);
        String type = "channelAction";
        if (parts[0].equals(tParser.getMyself().getNickname())) {
            type = "channelSelfExternalAction";
        }
        
        final StringBuffer buff = new StringBuffer(type);
        
        ActionManager.processEvent(CoreActionType.CHANNEL_ACTION, buff, this, cChannelClient, sMessage);
        
        frame.addLine(buff, modes, parts[0], parts[1], parts[2], sMessage, cChannel);
    }
    
    /** {@inheritDoc} */
    public void onChannelGotNames(final IRCParser tParser, final ChannelInfo cChannel) {
        
        frame.updateNames(channelInfo.getChannelClients());
        
        final ArrayList<String> names = new ArrayList<String>();
        
        for (ChannelClientInfo channelClient : cChannel.getChannelClients()) {
            names.add(channelClient.getNickname());
        }
        
        tabCompleter.replaceEntries(names);
        tabCompleter.addEntries(CommandManager.getChannelCommandNames());
        
        ActionManager.processEvent(CoreActionType.CHANNEL_GOTNAMES, null, this);
    }
    
    /** {@inheritDoc} */
    public void onChannelTopic(final IRCParser tParser, final ChannelInfo cChannel,
            final boolean bIsJoinTopic) {
        if (bIsJoinTopic) {
            final StringBuffer buff = new StringBuffer("channelJoinTopic");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_GOTTOPIC, buff, this);
            
            frame.addLine(buff, cChannel.getTopic(), cChannel.getTopicUser(),
                    1000 * cChannel.getTopicTime(), cChannel);
        } else {
            final ChannelClientInfo user = cChannel.getUser(cChannel.getTopicUser());
            final String[] parts = ClientInfo.parseHostFull(cChannel.getTopicUser());
            final String modes = getModes(user);
            final String topic = cChannel.getTopic();
            
            final StringBuffer buff = new StringBuffer("channelTopicChange");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_TOPICCHANGE, buff, this, user, topic);
            
            frame.addLine(buff, modes, parts[0], parts[1], parts[2], cChannel, topic);
        }
        updateTitle();
    }
    
    /** {@inheritDoc} */
    public void onChannelJoin(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient) {
        final ClientInfo client = cChannelClient.getClient();
        
        final StringBuffer buff = new StringBuffer("channelJoin");
        
        ActionManager.processEvent(CoreActionType.CHANNEL_JOIN, buff, this, cChannelClient);
        
        frame.addLine(buff, "", client.getNickname(), client.getIdent(),
                client.getHost(), cChannel);
        
        frame.addName(cChannelClient);
        tabCompleter.addEntry(cChannelClient.getNickname());
    }
    
    /** {@inheritDoc} */
    public void onChannelPart(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sReason) {
        final ClientInfo client = cChannelClient.getClient();
        final String nick = cChannelClient.getNickname();
        final String ident = client.getIdent();
        final String host = client.getHost();
        final String modes = cChannelClient.getImportantModePrefix();
        
        String type = "";
        
        if (nick.equals(tParser.getMyself().getNickname())) {
            if (sReason.length() == 0) {
                type = "channelSelfPart";
            } else {
                type = "channelSelfPartReason";
            }
            resetWindow();
        } else {
            if (sReason.length() == 0) {
                type = "channelPart";
            } else {
                type = "channelPartReason";
            }
        }
        
        final StringBuffer buff = new StringBuffer(type);
        
        ActionManager.processEvent(CoreActionType.CHANNEL_PART, buff, this, cChannelClient, sReason);
        
        frame.addLine(buff, modes, nick, ident, host, cChannel, sReason);
        
        frame.removeName(cChannelClient);
        
        tabCompleter.removeEntry(cChannelClient.getNickname());
    }
    
    /** {@inheritDoc} */
    public void onChannelKick(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cKickedClient, final ChannelClientInfo cKickedByClient,
            final String sReason, final String sKickedByHost) {
        final String[] kicker = getDetails(cKickedByClient);
        final String kickermodes = getModes(cKickedByClient);
        final String victim = cKickedClient.getNickname();
        final String victimmodes = cKickedClient.getImportantModePrefix();
        final String victimident = cKickedClient.getClient().getIdent();
        final String victimhost = cKickedClient.getClient().getHost();
        
        String type = "";
        
        if (sReason.length() == 0) {
            type = "channelKick";
        } else {
            type = "channelKickReason";
        }
        
        final StringBuffer buff = new StringBuffer(type);
        
        ActionManager.processEvent(CoreActionType.CHANNEL_KICK, buff, this,
                cKickedByClient, cKickedClient, sReason);
        
        frame.addLine(buff, kickermodes, kicker[0], kicker[1], kicker[2], victimmodes,
                victim, victimident, victimhost, cChannel.getName(), sReason);
        
        frame.removeName(cKickedClient);
        
        tabCompleter.removeEntry(cKickedClient.getNickname());
        
        if (cKickedClient.getClient().equals(tParser.getMyself())) {
            resetWindow();
        }
    }
    
    /** {@inheritDoc} */
    public void onChannelQuit(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sReason) {
        final ClientInfo client = cChannelClient.getClient();
        final String source = cChannelClient.getNickname();
        final String modes = cChannelClient.getImportantModePrefix();
        
        String type = "";
        
        if (sReason.length() == 0) {
            type = "channelQuit";
        } else {
            type = "channelQuitReason";
        }
        
        final StringBuffer buff = new StringBuffer(type);
        
        ActionManager.processEvent(CoreActionType.CHANNEL_QUIT, buff, this, cChannelClient, sReason);
        
        frame.addLine(buff, modes, source, client.getIdent(),
                client.getHost(), cChannel, sReason);
        
        frame.removeName(cChannelClient);
        tabCompleter.removeEntry(cChannelClient.getNickname());
    }
    
    /** {@inheritDoc} */
    public void onChannelNickChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sOldNick) {
        final String modes = cChannelClient.getImportantModePrefix();
        final String nick = cChannelClient.getNickname();
        final String ident = cChannelClient.getClient().getIdent();
        final String host = cChannelClient.getClient().getHost();
        String type = "channelNickChange";
        if (nick.equals(tParser.getMyself().getNickname())) {
            type = "channelSelfNickChange";
        }
        tabCompleter.removeEntry(sOldNick);
        tabCompleter.addEntry(nick);
        
        final StringBuffer buff = new StringBuffer(type);
        
        ActionManager.processEvent(CoreActionType.CHANNEL_NICKCHANGE, buff, this, cChannelClient, sOldNick);
        
        frame.addLine(buff, modes, sOldNick, ident, host, cChannel, nick);
        frame.updateNames();
    }
    
    /** {@inheritDoc} */
    public void onChannelModeChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sHost, final String sModes) {
        if (sHost.length() == 0) {
            final StringBuffer buff = new StringBuffer();
            
            if (sModes.length() <= 1) {
                buff.append("channelNoModes");
            } else {
                buff.append("channelModeDiscovered");
            }
            
            ActionManager.processEvent(CoreActionType.CHANNEL_MODECHANGE, buff, this, cChannelClient, sModes);
            
            frame.addLine(buff, sModes, cChannel.getName());
        } else {
            final String modes = getModes(cChannelClient);
            final String[] details = getDetails(cChannelClient);
            final String myNick = tParser.getMyself().getNickname();
            
            String type = "channelModeChange";
            if (myNick.equals(cChannelClient.getNickname())) {
                type = "channelSelfModeChange";
            }
            
            final StringBuffer buff = new StringBuffer(type);
            
            ActionManager.processEvent(CoreActionType.CHANNEL_MODECHANGE, buff, this, cChannelClient, sModes);
            
            frame.addLine(type,  modes, details[0], details[1],
                    details[2], cChannel.getName(), sModes);
        }
        
        frame.updateNames();
    }
    
    /** {@inheritDoc} */
    public void onChannelUserModeChanged(final IRCParser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChangedClient,
            final ChannelClientInfo cSetByClient, final String sHost,
            final String sMode) {
        
        if (configManager.getOptionBool("channel", "splitusermodes")) {
            final String sourceModes = getModes(cSetByClient);
            final String[] sourceHost = getDetails(cSetByClient);
            final String targetModes = cChangedClient.getImportantModePrefix();
            final String targetNick = cChangedClient.getClient().getNickname();
            final String targetIdent = cChangedClient.getClient().getIdent();
            final String targetHost = cChangedClient.getClient().getHost();
            
            String format = "channelUserMode_" + sMode;
            if (!Formatter.hasFormat(format)) {
                format = "channelUserMode_default";
            }
            
            frame.addLine(format, sourceModes, sourceHost[0], sourceHost[1],
                    sourceHost[2], targetModes, targetNick, targetIdent,
                    targetHost, cChannel, sMode);
        }
        
        ActionManager.processEvent(CoreActionType.CHANNEL_USERMODECHANGE, null,
                this, cSetByClient, cChangedClient, sMode);
    }
    
    /** {@inheritDoc} */
    public void onChannelCTCP(final IRCParser tParser,
            final ChannelInfo cChannel, final ChannelClientInfo cChannelClient,
            final String sType, final String sMessage, final String sHost) {
        
        final String modes = getModes(cChannelClient);
        final String[] source = getDetails(cChannelClient);
        
        frame.addLine("channelCTCP", modes, source[0], source[1], source[2],
                sType, sMessage, cChannel);
        
        server.sendCTCPReply(source[0], sType, sMessage);
        
        // TODO: Action hook
    }
    
    /** {@inheritDoc} */
    public void onAwayStateOther(final IRCParser tParser,
            final ClientInfo client, final boolean state) {
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
     * @param channelClient The channel client to check.
     * @return A string containing the most important mode, or an empty string
     * if there are no (known) modes.
     */
    private String getModes(final ChannelClientInfo channelClient) {
        if (channelClient == null || !configManager.getOptionBool("channel", "showmodeprefix")) {
            return "";
        } else {
            return channelClient.getImportantModePrefix();
        }
    }
    
    /**
     * Returns a string[] containing the nickname/ident/host of the client.
     * @param client The channel client to check
     * @return A string[] containing displayable components
     */
    private String[] getDetails(final ChannelClientInfo client) {
        final String[] res = new String[3];
        res[0] = client.getNickname();
        res[1] = client.getClient().getIdent();
        res[2] = client.getClient().getHost();
        
        if (configManager.getOptionBool("ui", "shownickcoloursintext")) {
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
    
    /**
     * Returns this channel's name.
     * @return A string representation of this channel (i.e., its name)
     */
    public String toString() {
        return channelInfo.getName();
    }
}
