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

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelAction;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelGotNames;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelJoin;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelKick;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelMessage;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelPart;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelQuit;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelTopic;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;
import uk.org.ownage.dmdirc.ui.ChannelFrame;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.Formatter;
import uk.org.ownage.dmdirc.ui.messages.Styliser;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelFrame, and handles user input to a ChannelFrame
 * @author chris
 */
public class Channel implements IChannelMessage, IChannelGotNames, IChannelTopic,
        IChannelJoin, IChannelPart, IChannelKick, IChannelQuit, IChannelAction,
        InternalFrameListener, FrameContainer {
    
    /** The parser's pChannel class */
    private ChannelInfo channelInfo;
    
    /** The server this channel is on */
    private Server server;
    
    /** The ChannelFrame used for this channel */
    private ChannelFrame frame;
    
    /**
     * The tabcompleter used for this channel
     */
    private TabCompleter tabCompleter;
    
    /**
     * Creates a new instance of Channel
     * @param server The server object that this channel belongs to
     * @param channelInfo The parser's channel object that corresponds to this channel
     */
    public Channel(Server server, ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
        this.server = server;
        
        tabCompleter = new TabCompleter(server.getTabCompleter());
        
        frame = new ChannelFrame(this);
        MainFrame.getMainFrame().addChild(frame);
        frame.addInternalFrameListener(this);
        frame.setTabCompleter(tabCompleter);
        frame.open();
        
        try {
            server.getParser().getCallbackManager().addCallback("OnChannelGotNames", this, channelInfo.getName());
            server.getParser().getCallbackManager().addCallback("OnChannelTopic", this, channelInfo.getName());
            server.getParser().getCallbackManager().addCallback("OnChannelMessage", this, channelInfo.getName());
            server.getParser().getCallbackManager().addCallback("OnChannelJoin", this, channelInfo.getName());
            server.getParser().getCallbackManager().addCallback("OnChannelPart", this, channelInfo.getName());
            server.getParser().getCallbackManager().addCallback("OnChannelQuit", this, channelInfo.getName());
            server.getParser().getCallbackManager().addCallback("OnChannelKick", this, channelInfo.getName());
            server.getParser().getCallbackManager().addCallback("OnChannelAction", this, channelInfo.getName());
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        
        updateTitle();
        selfJoin();
    }
    
    /**
     * Sends the specified line as a message to the channel that this object
     * represents
     * @param line The message to send
     */
    public void sendLine(String line) {
        channelInfo.sendMessage(line);
        
        ClientInfo me = server.getParser().getMyself();
        String modes = channelInfo.getUser(me).getImportantModePrefix();
        
        frame.addLine("channelSelfMessage", modes, me.getNickname(), line);
    }
    
    /**
     * Sends the specified string as an action (CTCP) to the channel that this object
     * represents
     * @param action The action to send
     */
    public void sendAction(String action) {
        channelInfo.sendAction(action);
        
        ClientInfo me = server.getParser().getMyself();
        String modes = channelInfo.getUser(me).getImportantModePrefix();
        
        frame.addLine("channelSelfAction", modes, me.getNickname(), action);
    }
    
    /**
     * Returns the server object that this channel belongs to
     * @return The server object
     */
    public Server getServer() {
        return server;
    }
    
    /**
     * Returns the parser's ChannelInfo object that this object is associated with
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
    public void setChannelInfo(ChannelInfo newChannelInfo) {
        channelInfo = newChannelInfo;
    }
    
    /**
     * Called when we join this channel. Just needs to output a message.
     */
    public void selfJoin() {
        ClientInfo me = server.getParser().getMyself();
        frame.addLine("channelSelfJoin", me.getNickname(), channelInfo.getName());
    }
    
    /**
     * Updates the title of the channel frame, and of the main frame if appropriate.
     */
    private void updateTitle() {
        String title = Styliser.stipControlCodes(channelInfo.getName()+" - "+channelInfo.getTopic());
        
        frame.setTitle(title);
        
        if (frame.isMaximum() && MainFrame.getMainFrame().getActiveFrame().equals(frame)) {
            MainFrame.getMainFrame().setTitle("DMDirc - "+title);
        }
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
    public void part(String reason) {
        ClientInfo me = server.getParser().getMyself();
        String modes = channelInfo.getUser(me).getImportantModePrefix();
        
        server.getParser().partChannel(channelInfo.getName(), reason);
        
        if (reason.equals("")) {
            frame.addLine("channelSelfPart", modes, me.getNickname(), channelInfo.getName());
        } else {
            frame.addLine("channelSelfPart", modes, me.getNickname(), channelInfo.getName(), reason);
        }
    }
    
    /**
     * Closes the channel window. Frees all references related to the channel.
     */
    public void close() {
        server.getParser().getCallbackManager().delCallback("OnChannelMessage", this);
        server.getParser().getCallbackManager().delCallback("OnChannelTopic", this);
        server.getParser().getCallbackManager().delCallback("OnChannelGotNames", this);
        server.getParser().getCallbackManager().delCallback("OnChannelJoin", this);
        server.getParser().getCallbackManager().delCallback("OnChannelPart", this);
        server.getParser().getCallbackManager().delCallback("OnChannelQuit", this);
        server.getParser().getCallbackManager().delCallback("OnChannelKick", this);
        server.getParser().getCallbackManager().delCallback("OnChannelAction", this);
        
        server.delChannel(channelInfo.getName());
        
        frame.setVisible(false);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        server = null;
    }
    
    /**
     * Determines if the specified frame is owned by this object
     */
    public boolean ownsFrame(JInternalFrame target) {
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
    public void onChannelMessage(IRCParser tParser, ChannelInfo cChannel,
            ChannelClientInfo cChannelClient, String sMessage, String sHost) {
        String source = getNick(cChannelClient, sHost);
        String modes = getModes(cChannelClient, sHost);
        frame.addLine("channelMessage", modes, source, sMessage);
    }
    
    /**
     * Called when the parser receives a NAMES reply from the server. This means that
     * the nicklist in the ChannelFrame needs to be updated.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     */
    public void onChannelGotNames(IRCParser tParser, ChannelInfo cChannel) {
        frame.updateNames(channelInfo.getChannelClients());
        
        ArrayList<String> names = new ArrayList<String>();
        for (ChannelClientInfo channelClient : cChannel.getChannelClients()) {
            names.add(channelClient.getNickname());
        }
        tabCompleter.replaceEntries(names);
    }
    
    /**
     * Called when the channel topic is changed. Changes the title of the channel
     * frame, and also of the main frame if the channel is maximised and active.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param bIsJoinTopic Whether this is the topic received when we joined the
     * channel or not
     */
    public void onChannelTopic(IRCParser tParser, ChannelInfo cChannel, boolean bIsJoinTopic) {
        if (bIsJoinTopic) {
            frame.addLine("channelJoinTopic", cChannel.getTopic(), cChannel.getName());
            frame.addLine("channelJoinTopicSetBy", cChannel.getTopicUser(),
                    1000*cChannel.getTopicTime(), cChannel.getName());
        } else {
            ChannelClientInfo user = cChannel.getUser(cChannel.getTopicUser());
            String nick = getNick(user, cChannel.getTopicUser());
            String modes = getModes(user, cChannel.getTopicUser());
            String topic = cChannel.getTopic();
            frame.addLine("channelTopicChange", modes, nick, topic, cChannel.getName());
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
    public void onChannelJoin(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient) {
        frame.addLine("* "+cChannelClient.getNickname()+" has joined the channel");
        frame.addName(cChannelClient);
        tabCompleter.addEntry(cChannelClient.getNickname());
        // TODO: Use formatter
    }
    
    /**
     * Called when a client parts the channel. Removes the client from the listbox
     * in the channel frame.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient The client that just parted
     * @param sReason The reason specified when the client parted
     */
    public void onChannelPart(IRCParser tParser, ChannelInfo cChannel,
            ChannelClientInfo cChannelClient, String sReason) {
        String nick = cChannelClient.getNickname();
        String modes = cChannelClient.getImportantModePrefix();
        
        if (sReason.equals("")) {
            frame.addLine("channelPart", modes, nick, cChannel.getName());
        } else {
            frame.addLine("channelPartReason", modes, nick, cChannel.getName(),
                    sReason);
        }
        
        tabCompleter.removeEntry(cChannelClient.getNickname());
        frame.removeName(cChannelClient);
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
    public void onChannelKick(IRCParser tParser, ChannelInfo cChannel,
            ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient,
            String sReason, String sKickedByHost) {
        String kicker = getNick(cKickedByClient, sKickedByHost);
        String kickermodes = getModes(cKickedByClient, sKickedByHost);
        String victim = cKickedClient.getNickname();
        String victimmodes = cKickedClient.getImportantModePrefix();
        
        if (sReason.equals("")) {
            frame.addLine("channelKick", kickermodes, kicker, victimmodes,
                    victim, cChannel.getName());
        } else {
            frame.addLine("channelKickReason", kickermodes, kicker, victimmodes,
                    victim, cChannel.getName(), sReason);
        }
        
        tabCompleter.removeEntry(cKickedClient.getNickname());
        frame.removeName(cKickedClient);
    }
    
    /**
     * Called when a client that was present on this channel has disconnected
     * from the IRC server (or been netsplit)
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient A reference to the client that has quit
     * @param sReason The reason specified in the client's quit message
     */
    public void onChannelQuit(IRCParser tParser, ChannelInfo cChannel,
            ChannelClientInfo cChannelClient, String sReason) {
        String source = cChannelClient.getNickname();
        String modes = cChannelClient.getImportantModePrefix();
        if (sReason.equals("")) {
            frame.addLine("channelQuit", modes, source);
        } else {
            frame.addLine("channelQuitReason", modes, source, sReason);
        }
        frame.removeName(cChannelClient);
    }
    
    /**
     * Called when an action is sent to the channel.
     * @param tParser A reference to the IRC Parser for this server
     * @param cChannel A reference to the ChannelInfo object for this channel
     * @param cChannelClient A reference to the client that sent the action
     * @param sMessage The text of the action
     * @param sHost The host of the performer (lest it wasn't an actual client)
     */
    public void onChannelAction(IRCParser tParser, ChannelInfo cChannel,
            ChannelClientInfo cChannelClient, String sMessage, String sHost) {
        String source = getNick(cChannelClient, sHost);
        String modes = getModes(cChannelClient, sHost);
        frame.addLine("channelAction", modes, source, sMessage);
    }
    
    /**
     * Returns a string containing the most important mode for the specified client
     * @param channelClient The channel client to check
     * @param host The hostname to check if the channel client doesn't exist
     * @return A string containing the most important mode, or an empty string
     * if there are no (known) modes.
     */
    private String getModes(ChannelClientInfo channelClient, String host) {
        if (channelClient == null) {
            return "";
        } else {
            return channelClient.getImportantModePrefix();
        }
    }
    
    /**
     * Returns a string containing the nickname, or other appropriate portion
     * of the host for displaying (e.g. server name)
     * @param channelClient The channel client to check
     * @param host The hostname to check if the channel client doesn't exist
     * @return A string containing a displayable name
     */
    private String getNick(ChannelClientInfo channelClient, String host) {
        if (channelClient == null) {
            return ClientInfo.parseHost(host);
        } else {
            return channelClient.getNickname();
        }
    }
    
    /**
     * Called when the channel frame is opened. Checks config settings to
     * determine if the window should be maximised
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameOpened(InternalFrameEvent internalFrameEvent) {
        Boolean pref = Boolean.parseBoolean(Config.getOption("ui","maximisewindows"));
        if (pref.equals(Boolean.TRUE) || MainFrame.getMainFrame().getMaximised()) {
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
    public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
        part(Config.getOption("general","partmessage"));
        close();
    }
    
    /**
     * Called when the channel frame is actually closed. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameClosed(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the channel frame is iconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameIconified(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the channel frame is deiconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeiconified(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the channel frame is activated. Maximises the frame if it
     * needs to be
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameActivated(InternalFrameEvent internalFrameEvent) {
        if (MainFrame.getMainFrame().getMaximised()) {
            try {
                frame.setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.error(ErrorLevel.WARNING, ex);
            }
        }
    }
    
    /**
     * Called when the channel frame is deactivated. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeactivated(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Returns this channel's name
     * @return A string representation of this channel (i.e., its name)
     */
    public String toString() {
        return channelInfo.getName();
    }
    
    /**
     * Requests that this object's frame be activated
     */
    public void activateFrame() {
        MainFrame.getMainFrame().setActiveFrame(frame);
    }    

    /**
     * Adds a line of text to the main text area of the channel frame
     * @param line The line to add
     */
    public void addLine(String line) {
        frame.addLine(line);
    }

    /**
     * Retrieves the icon used by the channel frame
     * @return The channel frame's icon
     */
    public ImageIcon getIcon() {
        return MainFrame.getMainFrame().getIcon();
    }
    
}
