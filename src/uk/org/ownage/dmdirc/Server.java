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

import java.util.Hashtable;
import javax.swing.event.InternalFrameEvent;
import uk.org.ownage.dmdirc.commandparser.ServerCommandParser;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ParserError;
import uk.org.ownage.dmdirc.parser.ServerInfo;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;
import java.util.Vector;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.logger.Logger;
import javax.swing.event.InternalFrameListener;

/**
 * The Server class represents the client's view of a server. It maintains
 * a list of all channels, queries, etc, and handles parser callbacks pertaining
 * to the server
 * @author chris
 */
public class Server implements IRCParser.IChannelSelfJoin, IRCParser.IErrorInfo,
        InternalFrameListener {
    
    /**
     * Open channels that currently exist on the server
     */
    private Hashtable<String,Channel> channels  = new Hashtable<String,Channel>();
    
    /**
     * The ServerFrame corresponding to this server
     */
    private ServerFrame frame;
    
    /**
     * The IRC Parser instance handling this server
     */
    private IRCParser parser;
    
    private Raw raw;
    
    /**
     * Creates a new instance of Server
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     */
    public Server(String server, int port, String password) {
        
        ServerManager.getServerManager().registerServer(this);
                
        frame = new ServerFrame(this);
        frame.setTitle(server+":"+port);
        frame.addInternalFrameListener(this);
        
        MainFrame.getMainFrame().addChild(frame);
        
        frame.addLine("Connecting to "+server+":"+port);
        
        parser = new IRCParser(new ServerInfo(server, port, password));
        
        parser.addChannelSelfJoin(this);
        parser.addErrorInfo(this);
        
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
        parser.delChannelSelfJoin(this);
        parser.delErrorInfo(this);
        // Unregister frame callbacks
        frame.removeInternalFrameListener(this);
        // Disconnect from the server
        disconnect(reason);
        // Unregister ourselves with the server manager
        ServerManager.getServerManager().unregisterServer(this);
        // Close all channel windows
        closeChannels();
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
        for (Channel channel : channels.values()) {
            channel.close();
        }
    }
    
    public void delChannel(Channel chan) {
        channels.remove(chan);
    }
    
    private void addChannel(ChannelInfo chan) {
        channels.put(chan.getName(), new Channel(this, chan));
    }
    
    public void onChannelSelfJoin(IRCParser tParser, ChannelInfo cChannel) {
        addChannel(cChannel);
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

    public void internalFrameOpened(InternalFrameEvent internalFrameEvent) {
    }

    public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
        close(Config.getOption("general","quitmessage"));
    }

    public void internalFrameClosed(InternalFrameEvent internalFrameEvent) {
    }

    public void internalFrameIconified(InternalFrameEvent internalFrameEvent) {
    }

    public void internalFrameDeiconified(InternalFrameEvent internalFrameEvent) {
    }

    public void internalFrameActivated(InternalFrameEvent internalFrameEvent) {
    }

    public void internalFrameDeactivated(InternalFrameEvent internalFrameEvent) {
    }
    
}
