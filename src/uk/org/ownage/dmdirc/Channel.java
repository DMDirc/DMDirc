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

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

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
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelAction;
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
        IChannelUserModeChanged, InternalFrameListener, FrameContainer {
    
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
            Logger.error(ErrorLevel.FATAL, ex);
        } catch (InterruptedException ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        
        try {
            final CallbackManager callbackManager = server.getParser().getCallbackManager();
            final String channel = channelInfo.getName();
            
            callbackManager.addCallback("OnChannelGotNames", this, channel);
            callbackManager.addCallback("OnChannelTopic", this, channel);
            callbackManager.addCallback("OnChannelMessage", this, channel);
            callbackManager.addCallback("OnChannelJoin", this, channel);
            callbackManager.addCallback("OnChannelPart", this, channel);
            callbackManager.addCallback("OnChannelQuit", this, channel);
            callbackManager.addCallback("OnChannelKick", this, channel);
            callbackManager.addCallback("OnChannelAction", this, channel);
            callbackManager.addCallback("OnChannelNickChanged", this, channel);
            callbackManager.addCallback("OnChannelModeChanged", this, channel);
            callbackManager.addCallback("OnChannelUserModeChanged", this, channel);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        
        updateTitle();
        selfJoin();
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
        channelInfo.sendMessage(line);
        
        final ClientInfo me = server.getParser().getMyself();
        final String modes = channelInfo.getUser(me).getImportantModePrefix();
        
        frame.addLine("channelSelfMessage", modes, me.getNickname(), me.getIdent(),
                me.getHost(), line, channelInfo);
        sendNotification();
    }
    
    /**
     * Sends the specified string as an action (CTCP) to the channel that this object
     * represents.
     * @param action The action to send
     */
    public void sendAction(final String action) {
        channelInfo.sendAction(action);
        
        final ClientInfo me = server.getParser().getMyself();
        final String modes = channelInfo.getUser(me).getImportantModePrefix();
        
        frame.addLine("channelSelfAction", modes, me.getNickname(), me.getIdent(),
                me.getHost(), action, channelInfo);
        sendNotification();
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
    }
    
    /**
     * Returns the internal frame belonging to this object.
     * @return This object's internal frame
     */
    public CommandWindow getFrame() {
        return frame;
    }
    
    /**
     * Called when we join this channel. Just needs to output a message.
     */
    public void selfJoin() {
        final ClientInfo me = server.getParser().getMyself();
        frame.addLine("channelSelfJoin", "", me.getNickname(), me.getIdent(),
                me.getHost(), channelInfo.getName());
        sendNotification();
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
    private void resetWindow() {
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
        
        callbackManager.delCallback("OnChannelMessage", this);
        callbackManager.delCallback("OnChannelTopic", this);
        callbackManager.delCallback("OnChannelGotNames", this);
        callbackManager.delCallback("OnChannelJoin", this);
        callbackManager.delCallback("OnChannelPart", this);
        callbackManager.delCallback("OnChannelQuit", this);
        callbackManager.delCallback("OnChannelKick", this);
        callbackManager.delCallback("OnChannelAction", this);
        callbackManager.delCallback("OnChannelNickChanged", this);
        callbackManager.delCallback("OnChannelModeChanged", this);
        callbackManager.delCallback("OnChannelUserModeChanged", this);
        
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
        if (cChannelClient != null && cChannelClient.getClient().equals(tParser.getMyself())) {
            type = "channelSelfExternalMessage";
        }
        
        final String[] parts = getDetails(cChannelClient, sHost);
        final String modes = cChannelClient.getImportantModePrefix();
        
        frame.addLine(type, modes, parts[0], parts[1], parts[2], sMessage, 
                cChannel);
        
        sendNotification();
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
        final String[] parts = getDetails(cChannelClient, sHost);
        final String modes = getModes(cChannelClient);
        String type = "channelAction";
        if (parts[0].equals(tParser.getMyself().getNickname())) {
            type = "channelSelfExternalAction";
        }
        frame.addLine(type, modes, parts[0], parts[1], parts[2], sMessage, cChannel);
        sendNotification();
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
            names.add(channelClient.getNickname());
        }
        tabCompleter.replaceEntries(names);
        tabCompleter.addEntries(CommandManager.getChannelCommandNames());
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
            frame.addLine("channelJoinTopic", cChannel.getTopic(),
                    cChannel.getTopicUser(), 1000 * cChannel.getTopicTime(), cChannel);
        } else {
            final ChannelClientInfo user = cChannel.getUser(cChannel.getTopicUser());
            final String[] parts = ClientInfo.parseHostFull(cChannel.getTopicUser());
            final String modes = getModes(user);
            final String topic = cChannel.getTopic();
            frame.addLine("channelTopicChange", modes, parts[0], parts[1], parts[2], topic, cChannel);
        }
        sendNotification();
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
        
        frame.addLine("channelJoin", "", client.getNickname(), client.getIdent(),
                client.getHost(), cChannel);
        
        frame.addName(cChannelClient);
        tabCompleter.addEntry(cChannelClient.getNickname());
        sendNotification();
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
        
        if (nick.equals(tParser.getMyself().getNickname())) {
            if (sReason.length() == 0) {
                frame.addLine("channelSelfPart", modes, nick, ident, host, cChannel);
            } else {
                frame.addLine("channelSelfPartReason", modes, nick, ident, host, cChannel, sReason);
            }
            resetWindow();
        } else {
            if (sReason.length() == 0) {
                frame.addLine("channelPart", modes, nick, ident, host, cChannel);
            } else {
                frame.addLine("channelPartReason", modes, nick, ident, host, sReason, cChannel);
            }
        }
        
        frame.removeName(cChannelClient);
        
        tabCompleter.removeEntry(cChannelClient.getNickname());
        sendNotification();
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
        final String[] kicker = getDetails(cKickedByClient, sKickedByHost);
        final String kickermodes = getModes(cKickedByClient);
        final String victim = cKickedClient.getNickname();
        final String victimmodes = cKickedClient.getImportantModePrefix();
        final String victimident = cKickedClient.getClient().getIdent();
        final String victimhost = cKickedClient.getClient().getHost();
        
        if (sReason.length() == 0) {
            frame.addLine("channelKick", kickermodes, kicker[0], kicker[1], kicker[2], victimmodes,
                    victim, victimident, victimhost, cChannel.getName());
        } else {
            frame.addLine("channelKickReason", kickermodes, kicker[0], kicker[1], kicker[2], victimmodes,
                    victim, victimident, victimhost, sReason, cChannel.getName());
        }
        
        frame.removeName(cKickedClient);
        
        tabCompleter.removeEntry(cKickedClient.getNickname());
        
        if (cKickedClient.getClient().equals(tParser.getMyself())) {
            resetWindow();
        }
        
        sendNotification();
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
        if (sReason.length() == 0) {
            frame.addLine("channelQuit", modes, source, client.getIdent(),
                    client.getHost(), cChannel);
        } else {
            frame.addLine("channelQuitReason", modes, source, client.getIdent(),
                    client.getHost(), sReason, cChannel);
        }
        
        frame.removeName(cChannelClient);
        
        sendNotification();
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
        frame.addLine(type, modes, sOldNick, ident, host, nick, cChannel);
        sendNotification();
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
            frame.addLine("channelModeDiscovered", sModes, cChannel.getName());
        } else {
            final String modes = getModes(cChannelClient);
            final String[] details = getDetails(cChannelClient, sHost);
            final String myNick = tParser.getMyself().getNickname();
            String type = "channelModeChange";
            if (cChannelClient != null && myNick.equals(cChannelClient.getNickname())) {
                type = "channelSelfModeChange";
            }
            frame.addLine(type,  modes, details[0], details[1],
                    details[2], sModes, cChannel.getName());
        }
        
        frame.updateNames();
        sendNotification();
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
        
        if (Boolean.parseBoolean(configManager.getOption("channel", "splitusermodes"))) {
            final String sourceModes = getModes(cSetByClient);
            final String[] sourceHost = getDetails(cSetByClient, sHost);
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
                    targetHost, sMode, cChannel);
        }
        
    }
    
    /**
     * Returns a string containing the most important mode for the specified client.
     * @param channelClient The channel client to check.
     * @return A string containing the most important mode, or an empty string
     * if there are no (known) modes.
     */
    private String getModes(final ChannelClientInfo channelClient) {
        if (channelClient == null) {
            return "";
        } else {
            return channelClient.getImportantModePrefix();
        }
    }
    
    /**
     * Returns a string[] containing the nickname/ident/host of the client, or
     * server, where applicable.
     * @param channelClient The channel client to check
     * @param host The hostname to check if the channel client doesn't exist
     * @return A string[] containing displayable components
     */
    private String[] getDetails(final ChannelClientInfo channelClient, final String host) {
        if (channelClient == null) {
            return ClientInfo.parseHostFull(host);
        } else {
            final String[] res = new String[3];
            res[0] = channelClient.getNickname();
            res[1] = channelClient.getClient().getIdent();
            res[2] = channelClient.getClient().getHost();
            return res;
        }
    }
    
    /**
     * Called when the channel frame is opened. Checks config settings to
     * determine if the window should be maximised.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
        final Boolean pref = Boolean.parseBoolean(configManager.getOption("ui", "maximisewindows"));
        if (pref || MainFrame.getMainFrame().getMaximised()) {
            try {
                frame.setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.error(ErrorLevel.WARNING, ex);
            }
        }
    }
    
    /**
     * Called when the channel frame is being closed. Has the parser part the
     * channel, and frees all resources associated with the channel.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
        part(configManager.getOption("general", "partmessage"));
        close();
    }
    
    /**
     * Called when the channel frame is actually closed. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the channel frame is iconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the channel frame is deiconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
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
                Logger.error(ErrorLevel.WARNING, ex);
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame.getMainFrame().setActiveFrame(frame);
            }
        });
    }
    
    /**
     * Adds a line of text to the main text area of the channel frame.
     * @param line The line to add
     */
    public void addLine(final String line) {
        frame.addLine(line);
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
        final JInternalFrame activeFrame = MainFrame.getMainFrame().getActiveFrame();
        if (activeFrame != null && !activeFrame.equals(frame)) {
            final Color colour = ColourManager.getColour(4);
            MainFrame.getMainFrame().getFrameManager().showNotification(this, colour);
        }
    }
    
    /**
     * Clears any outstanding notifications this frame has set.
     */
    private void clearNotification() {
        MainFrame.getMainFrame().getFrameManager().clearNotification(this);
    }
    
}
