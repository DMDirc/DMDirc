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
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.INickChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import uk.org.ownage.dmdirc.ui.ServerFrame;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelFrame, and handles user input to a ChannelFrame
 * @author chris
 */
public class Query implements IPrivateAction, IPrivateMessage, INickChanged,
        InternalFrameListener {
    
    /** The server this channel is on */
    private Server server;
    
    /** The ServerFrame used for this channel */
    private ServerFrame frame;
    
    private ClientInfo client;
    
    /**
     * Creates a new instance of Channel
     * @param server The server object that this channel belongs to
     * @param channelInfo The parser's channel object that corresponds to this channel
     */
    public Query(Server server, ClientInfo client) {
        this.server = server;
        this.client = client;
        
        //frame = new ServerFrame(this);
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
    
    public void sendLine(String line) {
        server.getParser().sendMessage(client.getNickname(), line);
        frame.addLine("> "+line);
    }
    
    public void sendAction(String action) {
        server.getParser().sendAction(client.getNickname(), action);
        frame.addLine("*> "+action);
    }
    
    public void onPrivateMessage(IRCParser parser, ClientInfo client, String message, String host) {
        frame.addLine("<"+client.getNickname()+"> "+message);
    }
    
    public void onPrivateAction(IRCParser parser, ClientInfo client, String message, String host) {
        frame.addLine("* "+client.toString()+" "+message);
    }
    
    private void updateTitle() {
        String title = client.getNickname();
        
        frame.setTitle(title);
        
        if (frame.isMaximum() && MainFrame.getMainFrame().getActiveFrame().equals(frame)) {
            MainFrame.getMainFrame().setTitle("DMDirc - "+title);
        }
    }
    
    public void onNickChanged(IRCParser tParser, ClientInfo cClient, String sOldNick) {
        server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
        server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
        try {
            server.getParser().getCallbackManager().addCallback("onPrivateAction", this, client.getNickname());
            server.getParser().getCallbackManager().addCallback("onPrivateMessage", this, client.getNickname());
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
        updateTitle();
    }
    
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
    
    public void internalFrameOpened(InternalFrameEvent internalFrameEvent) {
    }
    
    public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
        close();
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
