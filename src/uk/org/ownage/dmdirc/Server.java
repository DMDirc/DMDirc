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
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommandParser;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.parser.MyInfo;
import uk.org.ownage.dmdirc.parser.ParserError;
import uk.org.ownage.dmdirc.parser.ServerInfo;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelSelfJoin;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IErrorInfo;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateCTCP;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateCTCPReply;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateNotice;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.ISocketClosed;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;

/**
 * The Server class represents the client's view of a server. It maintains
 * a list of all channels, queries, etc, and handles parser callbacks pertaining
 * to the server.
 * @author chris
 */
public class Server implements IChannelSelfJoin, IPrivateMessage, IPrivateAction,
        IErrorInfo, IPrivateCTCP, IPrivateCTCPReply, InternalFrameListener,
        ISocketClosed, IPrivateNotice, FrameContainer {
    
    /**
     * Open channels that currently exist on the server.
     */
    private Hashtable<String, Channel> channels  = new Hashtable<String, Channel>();
    
    /**
     * Open query windows on the server.
     */
    private Hashtable<String, Query> queries = new Hashtable<String, Query>();
    
    /**
     * The ServerFrame corresponding to this server.
     */
    private ServerFrame frame;
    
    /**
     * The IRC Parser instance handling this server.
     */
    private IRCParser parser;
    
    /**
     * The raw frame used for this server instance.
     */
    private Raw raw;
    
    /**
     * The name of the server we're connecting to.
     */
    private String serverName;
    
    /**
     * Used to indicate that this server is in the process of closing all of its
     * windows, and thus requests for individual ones to be closed should be
     * ignored.
     */
    private boolean closing;
    
    /**
     * The tabcompleter used for this server.
     */
    private TabCompleter tabCompleter = new TabCompleter();
    
    /**
     * The icon being used for this server.
     */
    private ImageIcon imageIcon;
    
    /**
     * The last activated internal frame for this server.
     */
    private CommandWindow activeFrame;
    
    /**
     * Creates a new instance of Server.
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     * @param ssl Whether to use SSL or not
     */
    public Server(final String server, final int port, final String password,
            final boolean ssl) {
        
        serverName = server;
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        URL imageURL;
        if (ssl) {
            imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/secure-server.png");
        } else {
            imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/server.png");
        }
        imageIcon = new ImageIcon(imageURL);
        
        ServerManager.getServerManager().registerServer(this);
        
        frame = new ServerFrame(new ServerCommandParser(Server.this));
        frame.setTitle(server + ":" + port);
        frame.setTabCompleter(tabCompleter);
        frame.addInternalFrameListener(Server.this);
        frame.setFrameIcon(imageIcon);
        MainFrame.getMainFrame().addChild(frame);
        
        activeFrame = frame;
        
        frame.open();
        
        tabCompleter.addEntries(CommandManager.getServerCommandNames());
        
        // TODO: Use formatter
        frame.addLine("Connecting to " + server + ":" + port);
        sendNotification();
        
        final MyInfo myInfo = new MyInfo();
        myInfo.sNickname = Config.getOption("general", "defaultnick");
        myInfo.sAltNickname = Config.getOption("general", "alternatenick");
        
        final ServerInfo serverInfo = new ServerInfo(server, port, password);
        serverInfo.bSSL = ssl;
        parser = new IRCParser(myInfo, serverInfo);
        
        try {
            parser.getCallbackManager().addCallback("OnChannelSelfJoin", this);
            parser.getCallbackManager().addCallback("OnErrorInfo", this);
            parser.getCallbackManager().addCallback("OnPrivateMessage", this);
            parser.getCallbackManager().addCallback("OnPrivateAction", this);
            parser.getCallbackManager().addCallback("OnPrivateNotice", this);
            parser.getCallbackManager().addCallback("OnPrivateCTCP", this);
            parser.getCallbackManager().addCallback("OnPrivateCTCPReply", this);
            parser.getCallbackManager().addCallback("OnSocketClosed", this);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        
        raw = new Raw(this);
        MainFrame.getMainFrame().getFrameManager().addRaw(this, raw);
        
        try {
            new Thread(parser).start();
        } catch (Exception ex) {
            frame.addLine("ERROR: " + ex.getMessage());
        }
    }
    
    /**
     * Connects to a new server with the specified details.
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     * @param ssl Whether to use SSL or not
     */
    public void connect(final String server, final int port, final String password,
            final boolean ssl) {
        // TODO: Abstract the functionality of this and the constructor
        if (parser != null && parser.getSocketState() == parser.stateOpen) {
            disconnect(Config.getOption("general", "quitmessage"));
            closeChannels();
            closeQueries();
        }
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        URL imageURL;
        if (ssl) {
            imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/secure-server.png");
        } else {
            imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/server.png");
        }
        imageIcon = new ImageIcon(imageURL);
        
        // TODO: Use formatter
        frame.addLine("Connecting to " + server + ":" + port);
        sendNotification();
        
        final MyInfo myInfo = new MyInfo();
        myInfo.sNickname = Config.getOption("general", "defaultnick");
        myInfo.sAltNickname = Config.getOption("general", "alternatenick");
        
        final ServerInfo serverInfo = new ServerInfo(server, port, password);
        serverInfo.bSSL = ssl;
        parser = new IRCParser(myInfo, serverInfo);
        
        try {
            parser.getCallbackManager().addCallback("OnChannelSelfJoin", this);
            parser.getCallbackManager().addCallback("OnErrorInfo", this);
            parser.getCallbackManager().addCallback("OnPrivateMessage", this);
            parser.getCallbackManager().addCallback("OnPrivateAction", this);
            parser.getCallbackManager().addCallback("OnPrivateCTCP", this);
            parser.getCallbackManager().addCallback("OnPrivateNotice", this);
            parser.getCallbackManager().addCallback("OnPrivateCTCPReply", this);
            parser.getCallbackManager().addCallback("OnSocketClosed", this);
            
            parser.getCallbackManager().addCallback("OnDataIn", raw);
            parser.getCallbackManager().addCallback("OnDataOut", raw);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        
        try {
            new Thread(parser).start();
        } catch (Exception ex) {
            frame.addLine("ERROR: " + ex.getMessage());
        }
    }
    
    /**
     * Retrieves the parser used for this connection.
     * @return IRCParser this connection's parser
     */
    public final IRCParser getParser() {
        return parser;
    }
    
    /**
     * Returns the tab completer for this connection.
     * @return The tab completer for this server
     */
    public final TabCompleter getTabCompleter() {
        return tabCompleter;
    }
    
    /**
     * Adds a line to the server window.
     * @param line line to be added
     */
    public final void addLine(final String line) {
        frame.addLine(line);
    }
    
    /**
     * closes this server connection and associated windows.
     * @param reason reason for closing
     */
    public void close(final String reason) {
        // Unregister parser callbacks
        parser.getCallbackManager().delCallback("OnChannelSelfJoin", this);
        parser.getCallbackManager().delCallback("OnErrorInfo", this);
        parser.getCallbackManager().delCallback("OnPrivateMessage", this);
        parser.getCallbackManager().delCallback("OnPrivateAction", this);
        parser.getCallbackManager().delCallback("OnPrivateCTCP", this);
        parser.getCallbackManager().delCallback("OnPrivateNotice", this);
        parser.getCallbackManager().delCallback("OnPrivateCTCPReply", this);
        parser.getCallbackManager().delCallback("OnSocketClosed", this);
        // Unregister frame callbacks
        frame.removeInternalFrameListener(this);
        // Disconnect from the server
        disconnect(reason);
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Close our own window
                frame.setVisible(false);
                MainFrame.getMainFrame().delChild(frame);
                frame = null;
            }
        });
        // Ditch the parser
        parser = null;
    }
    
    /**
     * Closes this server connection and associated reason, with the default
     * quit message.
     */
    public void close() {
        close(Config.getOption("general", "quitmessage"));
    }
    
    /**
     * Disconnects from the server.
     * @param reason disconnect reason
     */
    public void disconnect(final String reason) {
        parser.quit(reason);
    }
    
    /**
     * closes all open channel windows associated with this server.
     */
    private void closeChannels() {
        closing = true;
        for (Channel channel : channels.values()) {
            channel.close();
        }
        channels.clear();
        closing = false;
    }
    
    /**
     * closes all open query windows associated with this server.
     */
    private void closeQueries() {
        closing = true;
        for (Query query : queries.values()) {
            query.close();
        }
        queries.clear();
        closing = false;
    }
    
    /**
     * Removes our reference to the raw object (presumably after it has been
     * closed).
     */
    public void delRaw() {
        MainFrame.getMainFrame().getFrameManager().delRaw(this, raw);
        raw = null;
    }
    
    /**
     * Removes a specific channel and window from this server.
     * @param chan channel to remove
     */
    public void delChannel(final String chan) {
        tabCompleter.removeEntry(chan);
        MainFrame.getMainFrame().getFrameManager().delChannel(this, channels.get(chan));
        if (!closing) {
            channels.remove(chan);
        }
    }
    
    /**
     * Adds a specific channel and window to this server.
     * @param chan channel to add
     */
    private void addChannel(final ChannelInfo chan) {
        final Channel newChan = new Channel(this, chan);
        
        tabCompleter.addEntry(chan.getName());
        channels.put(chan.getName(), newChan);
        MainFrame.getMainFrame().getFrameManager().addChannel(this, newChan);
    }
    
    /**
     * Adds a query query to this server.
     * @param host host of the remote client being queried
     */
    private void addQuery(final String host) {
        final Query newQuery = new Query(this, host);
        
        tabCompleter.addEntry(ClientInfo.parseHost(host));
        queries.put(ClientInfo.parseHost(host), newQuery);
        MainFrame.getMainFrame().getFrameManager().addQuery(this, newQuery);
    }
    
    /**
     * Deletes a query from this server.
     * @param host host of the remote client being queried
     */
    public void delQuery(final String host) {
        tabCompleter.removeEntry(ClientInfo.parseHost(host));
        MainFrame.getMainFrame().getFrameManager().delQuery(this, queries.get(ClientInfo.parseHost(host)));
        if (!closing) {
            queries.remove(ClientInfo.parseHost(host));
        }
    }
    
    /**
     * Determines if the specified frame is owned by this server.
     * @param target internalframe to be checked for ownership
     * @return boolean ownership status
     */
    public boolean ownsFrame(final JInternalFrame target) {
        // Check if it's our server frame
        if (frame.equals(target)) { return true; }
        // Check if it's the raw frame
        if (raw.ownsFrame(target)) { return true; }
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
     * @param source The frame that was activated
     */
    public void setActiveFrame(final CommandWindow source) {
        activeFrame = source;
    }
    
    /**
     * Passes the arguments to the most recently activated frame for this
     * server. If the frame isn't know, or isn't visible, use this frame
     * instead.
     * @param messageType The type of message to send
     * @param args The arguments for the message
     */
    public void addLineToActive(final String messageType, final Object... args) {
        if (activeFrame == null || !activeFrame.isVisible()) {
            activeFrame = frame;
        }
        
        frame.addLine(messageType, args);
    }
    
    /**
     * Event when client joins a channel, creates new channel object and opens
     * a channel window.
     * @param tParser parser instance triggering event
     * @param cChannel Channel being joined
     */
    public void onChannelSelfJoin(final IRCParser tParser, final ChannelInfo cChannel) {
        if (channels.containsKey(cChannel.getName())) {
            channels.get(cChannel.getName()).setChannelInfo(cChannel);
            channels.get(cChannel.getName()).selfJoin();
        } else {
            addChannel(cChannel);
        }
    }
    
    /**
     * Private message event, creates a new query object and opens a new query
     * window if one doesnt exist.
     * @param tParser parser instance triggering event
     * @param message private message being received
     * @param host host of the remote client
     */
    public void onPrivateMessage(final IRCParser tParser, final String message,
            final String host) {
        if (!queries.containsKey(ClientInfo.parseHost(host))) {
            addQuery(host);
        }
    }
    
    /**
     * Private action event, creates a new query object and opens a new query
     * window if one doesnt exist.
     * @param action action text being received
     * @param tParser parser instance triggering event
     * @param host host of remote client
     */
    public void onPrivateAction(final IRCParser tParser, final String action, final String host) {
        if (!queries.containsKey(ClientInfo.parseHost(host))) {
            addQuery(host);
        }
    }
    
    /**
     * Called when we receive a CTCP request.
     * @param tParser The associated IRC parser
     * @param sType The type of the CTCP
     * @param sMessage The contents of the CTCP
     * @param sHost The source host
     */
    public void onPrivateCTCP(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        final String[] parts = ClientInfo.parseHostFull(sHost);
        // TODO: Obey general.sendinfomessagestoactive
        frame.addLine("privateCTCP", parts[0], parts[1], parts[2], sType, sMessage);
        sendNotification();
    }
    
    /**
     * Called when we receive a CTCP reply.
     * @param tParser The associated IRC parser
     * @param sType The type of the CTCPR
     * @param sMessage The contents of the CTCPR
     * @param sHost The source host
     */
    public void onPrivateCTCPReply(final IRCParser tParser, final String sType,
            final String sMessage, final String sHost) {
        final String[] parts = ClientInfo.parseHostFull(sHost);
        // TODO: Obey general.sendinfomessagestoactive
        frame.addLine("privateCTCPreply", parts[0], parts[1], parts[2], sType, sMessage);
        sendNotification();
    }
    
    /**
     * Called when we receive a notice.
     * @param tParser The associated IRC parser
     * @param sMessage The notice content
     * @param sHost The source of the message
     */
    public void onPrivateNotice(final IRCParser tParser, final String sMessage, 
            final String sHost) {
        final String[] parts = ClientInfo.parseHostFull(sHost);
        // TODO: Obey general.sendinfomessagestoactive
        frame.addLine("privateNotice", parts[0], parts[1], parts[2], sMessage);
        sendNotification();
    }
    
    /**
     * Called when the IRC socket is closed for any reason.
     * @param tParser The IRC parser for this server
     */
    public void onSocketClosed(final IRCParser tParser) {
        frame.addLine("socketClosed", serverName);
        sendNotification();
        if (Boolean.parseBoolean(Config.getOption("general", "globaldisconnectmessage"))) {
            for (Channel channel : channels.values()) {
                channel.addLine("socketClosed", serverName);
            }
            for (Query query : queries.values()) {
                query.addLine("socketClosed", serverName);
            }
        }
    }
    
    /**
     * Parses the parser error and notifies the Logger.
     * @param tParser parser instance triggering event
     * @param errorInfo Parser error object
     */
    public void onErrorInfo(final IRCParser tParser, final ParserError errorInfo) {
        ErrorLevel errorLevel;
        if (errorInfo.isFatal()) {
            errorLevel = ErrorLevel.FATAL;
        } else if (errorInfo.isError()) {
            errorLevel = ErrorLevel.ERROR;
        } else if (errorInfo.isWarning()) {
            errorLevel = ErrorLevel.WARNING;
        } else {
            Logger.error(ErrorLevel.WARNING,
                    "Unknown error level for parser error: " + errorInfo.getData());
            return;
        }
        
        if (errorInfo.isException()) {
            Logger.error(errorLevel, errorInfo.getException());
        } else {
            Logger.error(errorLevel, errorInfo.getData());
        }
    }
    
    /**
     * Called when the server frame is opened. Checks config settings to
     * determine if the window should be maximised.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
        final Boolean pref = Boolean.parseBoolean(Config.getOption("ui", "maximisewindows"));
        if (pref.equals(Boolean.TRUE) || MainFrame.getMainFrame().getMaximised()) {
            try {
                frame.setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.error(ErrorLevel.WARNING, ex);
            }
        }
    }
    
    /**
     * Called when the server frame is being closed. Has the parser quit
     * the server, close all channels, and free all resources.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
        close(Config.getOption("general", "quitmessage"));
    }
    
    /**
     * Called when the server frame is actually closed. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the server frame is iconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the server frame is deiconified. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the server frame is activated. Maximises the frame if it
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
        setActiveFrame(frame);
        clearNotification();
    }
    
    /**
     * Called when the server frame is deactivated. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Returns this server's name.
     * @return A string representation of this server (i.e., its name)
     */
    public String toString() {
        return serverName;
    }
    
    /**
     * Requests that this object's frame be activated.
     */
    public void activateFrame() {
        MainFrame.getMainFrame().setActiveFrame(frame);
    }
    
    /**
     * Returns the server frame's icon.
     * @return The server frame's icon
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }
    
    /**
     * Sends a notification to the frame manager if this frame isn't active.
     */
    private void sendNotification() {
        if (!MainFrame.getMainFrame().getActiveFrame().equals(frame)) {
            final Color c = ColourManager.getColour(4);
            MainFrame.getMainFrame().getFrameManager().showNotification(this, c);
        }
    }
    
    /**
     * Clears any outstanding notifications this frame has set.
     */
    private void clearNotification() {
        MainFrame.getMainFrame().getFrameManager().clearNotification(this);
    }
    
}
