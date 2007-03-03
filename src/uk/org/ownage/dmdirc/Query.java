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

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import uk.org.ownage.dmdirc.commandparser.QueryCommandParser;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.INickChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import uk.org.ownage.dmdirc.ui.QueryFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;

/**
 * The Query class represents the client's view of a query with another user.
 * It handles callbacks for query events from the parser, maintains the 
 * corresponding ServerFrame, and handles user input to a ServerFrame
 * @author chris
 */
public class Query implements IPrivateAction, IPrivateMessage, INickChanged,
        InternalFrameListener {
    
    /**
     * The Server this Query is on
     */
    private Server server;
    
    /**
     * The ServerFrame used for this Query
     */
    private QueryFrame frame;
    
    /**
     * The Client associated with this Query
     */
    private ClientInfo client;
    
    /**
     * Creates a new instance of Query
     * @param client Client user being queried
     * @param server The server object that this Query belongs to
     */
    public Query(Server server, ClientInfo client) {
        this.server = server;
        this.client = client;
        
        frame = new QueryFrame(new QueryCommandParser(this.server, this));
        MainFrame.getMainFrame().addChild(frame);
        frame.addInternalFrameListener(this);
        
        try {
            server.getParser().getCallbackManager().addCallback("onPrivateAction", this, client.getNickname());
            server.getParser().getCallbackManager().addCallback("onPrivateMessage", this, client.getNickname());
            server.getParser().getCallbackManager().addCallback("onNickChanged", this);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        
        updateTitle();
    }
    
    /**
     * Sends a private message to the remote user
     * @param line message text to send
     */
    public void sendLine(String line) {
        server.getParser().sendMessage(client.getNickname(), line);
        frame.addLine("> "+line);
    }
    
    /**
     * Sends a private action to the remote user
     * @param action action text to send
     */
    public void sendAction(String action) {
        server.getParser().sendAction(client.getNickname(), action);
        frame.addLine("*> "+action);
    }
    
    /**
     * Handles a private message event from the parser
     * @param parser Parser receiving the event
     * @param client remote client
     * @param message message received
     * @param host remote user host
     */
    public void onPrivateMessage(IRCParser parser, ClientInfo client, String message, String host) {
        frame.addLine("<"+client.getNickname()+"> "+message);
    }
    
    /**
     * Handles a private action event from the parser
     * @param parser Parser receiving the event
     * @param client remote client
     * @param message message received
     * @param host remote host
     */
    public void onPrivateAction(IRCParser parser, ClientInfo client, String message, String host) {
        frame.addLine("* "+client.toString()+" "+message);
    }
    
    /**
     * Updates the QueryFrame title
     */
    private void updateTitle() {
        String title = client.getNickname();
        
        frame.setTitle(title);
        
        if (frame.isMaximum() && MainFrame.getMainFrame().getActiveFrame().equals(frame)) {
            MainFrame.getMainFrame().setTitle("DMDirc - "+title);
        }
    }
    
    /**
     * Handles nick change events from the parser
     * @param parser Parser receiving the event
     * @param client remote client changing nick
     * @param oldNick clients old nickname
     */
    public void onNickChanged(IRCParser parser, ClientInfo client, String oldNick) {
        server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
        server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
        try {
            server.getParser().getCallbackManager().addCallback("onPrivateAction", this, client.getNickname());
            server.getParser().getCallbackManager().addCallback("onPrivateMessage", this, client.getNickname());
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        frame.addLine("* "+oldNick+" is now known as "+client.toString());
        updateTitle();
    }
    
    /**
     * 
     * @return 
     */
    public Server getServer() {
        return server;
    }
    
    public void close() {
        server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
        server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
        server.getParser().getCallbackManager().delCallback("onNickChanged", this);
        
        frame.setVisible(false);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        server = null;
    }
    
    /**
     * 
     * @param internalFrameEvent 
     */
    public void internalFrameOpened(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * 
     * @param internalFrameEvent 
     */
    public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
        close();
    }
    
    /**
     * 
     * @param internalFrameEvent 
     */
    public void internalFrameClosed(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * 
     * @param internalFrameEvent 
     */
    public void internalFrameIconified(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * 
     * @param internalFrameEvent 
     */
    public void internalFrameDeiconified(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * 
     * @param internalFrameEvent 
     */
    public void internalFrameActivated(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * 
     * @param internalFrameEvent 
     */
    public void internalFrameDeactivated(InternalFrameEvent internalFrameEvent) {
    }
}
