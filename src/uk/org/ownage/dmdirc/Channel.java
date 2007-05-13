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

package uk.org.ownage.dmdirc;

import java.awt.Color;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import uk.org.ownage.dmdirc.actions.ActionManager;
import uk.org.ownage.dmdirc.actions.CoreActionType;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.identities.ConfigManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackManager;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IAwayStateOther;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelAction;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelCTCP;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelGotNames;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelJoin;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelKick;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelMessage;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelNickChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelPart;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelQuit;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelTopic;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelUserModeChanged;
import uk.org.ownage.dmdirc.ui.ChannelFrame;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;
import uk.org.ownage.dmdirc.ui.messages.Formatter;
import uk.org.ownage.dmdirc.ui.messages.Styliser;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelFrame, and handles user input to a ChannelFrame.
 * @author chris
 */
public final class Channel implements IChannelMessage, IChannelGotNames,
        IChannelTopic, IChannelJoin, IChannelPart, IChannelKick, IChannelQuit,
        IChannelAction, IChannelNickChanged, IChannelModeChanged,
        IChannelUserModeChanged, IChannelCTCP, IAwayStateOther,
        InternalFrameListener, FrameContainer {
    
    /** The callbacks that should be registered for channel instances. */
    private final static String[] callbacks = {
        "OnChannelMessage", "OnChannelGotNames", "OnChannelTopic",
        "OnChannelJoin", "OnChannelPart", "OnChannelKick", "OnChannelQuit",
        "OnChannelAction", "OnChannelNickChanged", "OnChannelModeChanged",
        "OnChannelUserModeChanged", "OnChannelCTCP"
    };
    
    /** The parser's pChannel class. */
    private ChannelInfo channelInfo;
    
    /** The server this channel is on. */
    private Server server;
    
    /** The ChannelFrame used for this channel. */
    private ChannelFrame frame;
    
    /** The tabcompleter used for this channel. */
    private final TabCompleter tabCompleter;
    
    /** The icon being used for this channel. */
    private final ImageIcon imageIcon;
    
    /** The config manager for this channel. */
    private final ConfigManager configManager;
    
    /** The colour of this channel's notifications. */
    private Color notification = Color.BLACK;
    
    /** Whether we're in this channel or not. */
    private boolean onChannel;
    
    /**
     * Creates a new instance of Channel.
     * @param newServer The server object that this channel belongs to
     * @param newChannelInfo The parser's channel object that corresponds to
     * this channel
     */
    public Channel(final Server newServer, final ChannelInfo newChannelInfo) {
        channelInfo = newChannelInfo;
        server = newServer;
        
        configManager = new ConfigManager(server.getIrcd(), server.getNetwork(),
                server.getName(), channelInfo.getName());
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        final URL imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/channel.png");
        imageIcon = new ImageIcon(imageURL);
        
        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(CommandManager.getChannelCommandNames());
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    frame = new ChannelFrame(Channel.this);
                    MainFrame.getMainFrame().addChild(frame);
                    frame.addInternalFrameListener(Channel.this);
                    frame.setFrameIcon(imageIcon);
                    frame.setTabCompleter(tabCompleter);
                }
            });
        } catch (InvocationTargetException ex) {
            Logger.error(ErrorLevel.FATAL, "Unable to load channel", ex.getCause());
        } catch (InterruptedException ex) {
            Logger.error(ErrorLevel.FATAL, "Unable to load channel", ex);
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
            
            for (String callback : callbacks) {
                callbackManager.addCallback(callback, this, channel);
            }
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, "Unable to load channel", ex);
        }
    }
    
    /**
     * Shows this channel's frame.
     */
    public void show() {
        frame.open();
    }
    
    /**
     * Retrieves this channel's config manager.
     * @return This channel's configManager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Sends the specified line as a message to the channel that this object
     * represents.
     * @param line The message to send
     */
    public void sendLine(final String line) {
        final ClientInfo me = server.getParser().getMyself();
        final String modes = channelInfo.getUser(me).getImportantModePrefix();
        final int maxLineLength = server.getParser().getMaxLength("PRIVMSG", getChannelInfo().getName());
        
        if (maxLineLength >= line.length()) {
            final StringBuffer buff = new StringBuffer("channelSelfMessage");
            
            ActionManager.processEvent(CoreActionType.CHANNEL_SELF_MESSAGE, buff,
                    this, channelInfo.getUser(me), line);
            
            frame.addLine(buff, modes, me.getNickname(), me.getIdent(),
                    me.getHost(), line, channelInfo);
            channelInfo.sendMessage(line);
        } else {
            sendLine(line.substring(0, maxLineLength));
            sendLine(line.substring(maxLineLength));
        }
    }
    
    /**
     * Sends the specified string as an action (CTCP) to the channel that this object
     * represents.
     * @param action The action to send
     */
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
    public CommandWindow getFrame() {
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
        final String title = Styliser.stipControlCodes(channelInfo.getName()
        + " - " + channelInfo.getTopic());
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setTitle(title);
                
                if (frame.isMaximum() && frame.equals(MainFrame.getMainFrame().getActiveFrame())) {
                    MainFrame.getMainFrame().setTitle(MainFrame.getMainFrame().getTitlePrefix() + " - " + title);
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
        
        for (String callback : callbacks) {
            callbackManager.delCallback(callback, this);
        }
        
        ActionManager.processEvent(CoreActionType.CHANNEL_CLOSED, null, this);
        
        server.delChannel(channelInfo.getName());
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(false);
                MainFrame.getMainFrame().delChild(frame);
                frame = null;
                server = null;
            }
        });
    }
        
    /**
     * Determines if the specified frame is owned by this object.
     *
     * @param target JInternalFrame to check ownership of
     * @return boolean whether this object owns the specified frame
     */
    public boolean ownsFrame(final JInternalFrame target) {
        return frame.equals(target);
    }
    
    /**
     * Ensures that a channel client's map is set up correctly.
     * @param target The ChannelClientInfo to check
     */
    private void mapClient(final ChannelClientInfo target) {
        if (target.getMap() == null) {
            target.setMap(new HashMap<ChannelClientProperty, Object>());
        }
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
    
    /**
     * Called whenever a message is sent to this channel. NB that the ChannelClient
     * passed may be null if the message was not sent by a client on the channel
     * (i.e., it was sent by a server, or a client outside of the channel). In these
     * cases the full host is used instead.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient A reference to the ChannelClient object that sent the message
     * @param sMessage The message that was sent
     * @param sHost The full host of the sender.
     */
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
    
    /**
     * Called when an action is sent to the channel.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient A reference to the client that sent the action
     * @param sMessage The text of the action
     * @param sHost The host of the performer (lest it wasn't an actual client)
     */
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
    
    /**
     * Called when the parser receives a NAMES reply from the server. This means that
     * the nicklist in the ChannelFrame needs to be updated.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     */
    public void onChannelGotNames(final IRCParser tParser, final ChannelInfo cChannel) {
        
        frame.updateNames(channelInfo.getChannelClients());
        
        final ArrayList<String> names = new ArrayList<String>();
        for (ChannelClientInfo channelClient : cChannel.getChannelClients()) {
            mapClient(channelClient);
            names.add(channelClient.getNickname());
        }
        tabCompleter.replaceEntries(names);
        tabCompleter.addEntries(CommandManager.getChannelCommandNames());
        
        ActionManager.processEvent(CoreActionType.CHANNEL_GOTNAMES, null, this);
    }
    
    /**
     * Called when the channel topic is changed. Changes the title of the channel
     * frame, and also of the main frame if the channel is maximised and active.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param bIsJoinTopic Whether this is the topic received when we joined the
     * channel or not
     */
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
    
    /**
     * Called when a new client joins the channel. Adds the client to the listbox
     * in the channel frame.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient The client that has just joined
     */
    public void onChannelJoin(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient) {
        final ClientInfo client = cChannelClient.getClient();
        
        final StringBuffer buff = new StringBuffer("channelJoin");
        
        mapClient(cChannelClient);
        
        ActionManager.processEvent(CoreActionType.CHANNEL_JOIN, buff, this, cChannelClient);
        
        frame.addLine(buff, "", client.getNickname(), client.getIdent(),
                client.getHost(), cChannel);
        
        frame.addName(cChannelClient);
        tabCompleter.addEntry(cChannelClient.getNickname());
    }
    
    /**
     * Called when a client parts the channel. Removes the client from the listbox
     * in the channel frame.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient The client that just parted
     * @param sReason The reason specified when the client parted
     */
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
    
    /**
     * Called when a client is kicked from the channel. The victim is removed
     * from the channelframe's listbox.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cKickedClient A reference to the client that was kicked
     * @param cKickedByClient A reference to the client that did the kicking
     * @param sReason The reason specified in the kick message
     * @param sKickedByHost The host of the kicker (in case it wasn't an actual client)
     */
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
    
    /**
     * Called when a client that was present on this channel has disconnected
     * from the IRC server (or been netsplit).
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient A reference to the client that has quit
     * @param sReason The reason specified in the client's quit message
     */
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
    
    /**
     * Called when someone on the channel changes their nickname.
     * @param tParser A reference to the IRC parser for this server
     * @param cChannel A reference to the CHannelInfo object for this channel
     * @param cChannelClient The client that changed nickname
     * @param sOldNick The old nickname of the client
     */
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
    }
    
    /**
     * Called when modes are changed on the channel.
     * @param tParser A reference to the IRC parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient The client that set the modes
     * @param sHost the host of the client that set the modes
     * @param sModes the modes that were set
     */
    public void onChannelModeChanged(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sHost, final String sModes) {
        if (sHost.length() == 0) {
            final StringBuffer buff = new StringBuffer("channelModeDiscovered");
            
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
    
    /**
     * Called when a mode has been changed on a user. Used for custom mode
     * formats.
     * @param tParser A reference to the IRC parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChangedClient The client whose mode was changed
     * @param cSetByClient The client who made the change
     * @param sHost The hostname of the setter
     * @param sMode The mode that has been changed
     */
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
        
        // TODO: Action hook
    }
    
    /**
     * Handles a channel CTCP.
     * @param tParser The IRC parser for this server
     * @param cChannel This channel's ChannelInfo object
     * @param cChannelClient The source of the CTCP
     * @param sType The CTCP type
     * @param sMessage The CTCP contents (if any)
     * @param sHost The full host of the source
     */
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
            
            if (map.containsKey(ChannelClientProperty.COLOUR_FOREGROUND)) {
                colour = (Color) map.get(ChannelClientProperty.COLOUR_FOREGROUND);
                prefix = Styliser.CODE_HEXCOLOUR + ColourManager.getHex(colour);
                if (map.containsKey(ChannelClientProperty.COLOUR_BACKGROUND)) {
                    colour = (Color) map.get(ChannelClientProperty.COLOUR_BACKGROUND);
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
     * Called when the channel frame is opened. Checks config settings to
     * determine if the window should be maximised.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
        final boolean pref = configManager.getOptionBool("ui", "maximisewindows");
        if (pref || MainFrame.getMainFrame().getMaximised()) {
            try {
                frame.setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to maximise channel", ex);
            }
        }
    }
    
    /**
     * Called when the channel frame is being closed. Has the parser part the
     * channel, and frees all resources associated with the channel.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
        close();
    }
    
    /**
     * Called when the channel frame is actually closed. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Called when the channel frame is iconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Called when the channel frame is deiconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Called when the channel frame is activated. Maximises the frame if it
     * needs to be.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
        if (MainFrame.getMainFrame().getMaximised()) {
            try {
                frame.setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to maximise channel", ex);
            }
        }
        MainFrame.getMainFrame().getFrameManager().setSelected(this);
        server.setActiveFrame(this);
        clearNotification();
    }
    
    /**
     * Called when the channel frame is deactivated. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Returns this channel's name.
     * @return A string representation of this channel (i.e., its name)
     */
    public String toString() {
        return channelInfo.getName();
    }
    
    /**
     * Requests that this object's frame be activated.
     */
    public void activateFrame() {
        MainFrame.getMainFrame().setActiveFrame(frame);
    }
        
    /**
     * Formats the specified arguments using the supplied message type, and
     * outputs to the main text area.
     * @param messageType the message type to use
     * @param args the arguments to pass
     */
    public void addLine(final String messageType, final Object... args) {
        frame.addLine(messageType, args);
    }
    
    /**
     * Retrieves the icon used by the channel frame.
     * @return The channel frame's icon
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }
    
    /**
     * Sends a notification to the frame manager if this frame isn't active.
     */
    public void sendNotification() {
        sendNotification(Color.RED);
    }
    
    /**
     * Sends a notification to the frame manager if this fame isn't active.
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Color colour) {
        final JInternalFrame activeFrame = MainFrame.getMainFrame().getActiveFrame();
        if (activeFrame != null && !activeFrame.equals(frame)) {
            MainFrame.getMainFrame().getFrameManager().showNotification(this, colour);
            notification = colour;
        }
    }
    
    /**
     * Clears any outstanding notifications this frame has set.
     */
    private void clearNotification() {
        MainFrame.getMainFrame().getFrameManager().clearNotification(this);
        notification = Color.BLACK;
    }
    
    /**
     * Retrieves the current notification colour of this channel.
     * @return This channel's notification colour
     */
    public Color getNotification() {
        return notification;
    }
}
