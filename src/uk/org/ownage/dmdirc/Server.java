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
import java.util.Hashtable;
import javax.swing.event.InternalFrameEvent;
import uk.org.ownage.dmdirc.commandparser.ServerCommandParser;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.parser.MyInfo;
import uk.org.ownage.dmdirc.parser.ParserError;
import uk.org.ownage.dmdirc.parser.ServerInfo;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.logger.Logger;
import javax.swing.event.InternalFrameListener;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelSelfJoin;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IErrorInfo;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;

/**
 * The Server class represents the client's view of a server. It maintains
 * a list of all channels, queries, etc, and handles parser callbacks pertaining
 * to the server
 * @author chris
 */
public class Server implements IChannelSelfJoin, IPrivateMessage, IPrivateAction, IErrorInfo, InternalFrameListener {
    
    /**
     * Open channels that currently exist on the server
     */
    private Hashtable<String,Channel> channels  = new Hashtable<String,Channel>();
    
    /**
     * Open query windows on the server
     */
    private Hashtable<String,Query> queries = new Hashtable<String,Query>();
    
    /**
     * The ServerFrame corresponding to this server
     */
    private ServerFrame frame;
    
    /**
     * The IRC Parser instance handling this server
     */
    private IRCParser parser;
    
    private Raw raw;
    
    private boolean closing = false;
    
    /**
     * Creates a new instance of Server
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     */
    public Server(String server, int port, String password) {
        
        ServerManager.getServerManager().registerServer(this);
        
        frame = new ServerFrame(new ServerCommandParser(this));
        frame.setTitle(server+":"+port);
        frame.addInternalFrameListener(this);
        
        MainFrame.getMainFrame().addChild(frame);
        
        frame.open();
        
        frame.addLine("Connecting to "+server+":"+port);
        
        MyInfo myInfo = new MyInfo();
        myInfo.sNickname = Config.getOption("general","defaultnick");
        myInfo.sAltNickname = Config.getOption("general","alternatenick");
        
        parser = new IRCParser(myInfo, new ServerInfo(server, port, password));
        
        try {
            parser.getCallbackManager().addCallback("OnChannelSelfJoin", this);
            parser.getCallbackManager().addCallback("OnErrorInfo", this);
            parser.getCallbackManager().addCallback("OnPrivateMessage", this);
            parser.getCallbackManager().addCallback("OnPrivateAction", this);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        
        raw = new Raw(this);
        
        try {
            Thread thread = new Thread(parser);
            thread.start();
        } catch (Exception ex) {
            frame.addLine("ERROR: "+ex.getMessage());
        }
    }
    
    /**
     * Retrieves the parser used for this connection
     * @return IRCParser this connection's parser
     */
    public IRCParser getParser() {
        return parser;
    }
    
    public void addLine(String line) {
        frame.addLine(line);
    }
    
    public void close(String reason) {
        // Unregister parser callbacks
        parser.getCallbackManager().delCallback("OnChannelSelfJoin", this);
        parser.getCallbackManager().delCallback("OnErrorInfo", this);
        parser.getCallbackManager().delCallback("OnPrivateMessage", this);
        parser.getCallbackManager().delCallback("OnPrivateAction", this);
        // Unregister frame callbacks
        frame.removeInternalFrameListener(this);
        // Disconnect from the server
        disconnect(reason);
        // Unregister ourselves with the server manager
        ServerManager.getServerManager().unregisterServer(this);
        // Close all channel windows
        closeChannels();
        //Close all query windows
        // Close the raw window
        raw.close();
        // Close our own window
        frame.setVisible(false);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        // Ditch the parser
        parser = null;
    }
    
    public void disconnect(String reason) {
        parser.quit(reason);
    }
    
    private void closeChannels() {
        closing = true;
        for (Channel channel : channels.values()) {
            channel.close();
        }
        closing = false;
    }
    
    private void closeQueries() {
        closing = true;
        for (Query query: queries.values()) {
            query.close();
        }
        closing = false;
    }
    
    public void delChannel(String chan) {
        if (!closing) {
            channels.remove(chan);
        }
    }
    
    private void addChannel(ChannelInfo chan) {
        channels.put(chan.getName(), new Channel(this, chan));
    }
    
    public void onChannelSelfJoin(IRCParser tParser, ChannelInfo cChannel) {
        if (channels.containsKey(cChannel.getName())) {
            channels.get(cChannel.getName()).setChannelInfo(cChannel);
        } else {
            addChannel(cChannel);
        }
    }
    
    private void addQuery(String host) {
        queries.put(ClientInfo.parseHost(host), new Query(this, host));
    }
    
    public void delQuery(String host) {
        if (!closing) {
            queries.remove(ClientInfo.parseHost(host));
        }
    }
    
    public void onPrivateMessage(IRCParser parser, String message, String host) {
        if (!queries.containsKey(ClientInfo.parseHost(host))) {
            addQuery(host);
        }
    }
    
    public void onPrivateAction(IRCParser parser, String message, String host) {
        if (!queries.containsKey(ClientInfo.parseHost(host))) {
            addQuery(host);
        }
    }
    
    public void onErrorInfo(IRCParser tParser, ParserError errorInfo) {
        ErrorLevel errorLevel;
        if (errorInfo.isFatal()) {
            errorLevel = ErrorLevel.FATAL;
        } else if (errorInfo.isError()) {
            errorLevel = ErrorLevel.ERROR;
        } else if (errorInfo.isWarning()) {
            errorLevel = ErrorLevel.WARNING;
        } else {
            Logger.error(ErrorLevel.WARNING, "Unknown error level for parser error: "+errorInfo.getData());
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
     * Called when the server frame is being closed. Has the parser quit
     * the server, close all channels, and free all resources
     * @param internalFrameEvent The event that triggered this callback
     */    
    public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
        close(Config.getOption("general","quitmessage"));
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
     * Called when the channel frame is activated. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameActivated(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Called when the channel frame is deactivated. Not implemented.
     * @param internalFrameEvent The event that triggered this callback
     */
    public void internalFrameDeactivated(InternalFrameEvent internalFrameEvent) {
    }
}
