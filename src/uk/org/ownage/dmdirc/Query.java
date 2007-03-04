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
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import uk.org.ownage.dmdirc.commandparser.QueryCommandParser;
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
import uk.org.ownage.dmdirc.ui.QueryFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;

/**
 * The Query class represents the client's view of a query with another user.
 * It handles callbacks for query events from the parser, maintains the
 * corresponding ServerFrame, and handles user input to a ServerFrame
 * @author chris
 */
public class Query implements IPrivateAction, IPrivateMessage, INickChanged,
        InternalFrameListener, FrameContainer {
    
    /**
     * The Server this Query is on
     */
    private Server server;
    
    /**
     * The ServerFrame used for this Query
     */
    private QueryFrame frame;
    
    /**
     * The full host of the client associated with this Query
     */
    private String host;
    
    /**
     * Creates a new instance of Query
     * @param host host of the remove client
     * @param server The server object that this Query belongs to
     */
    public Query(Server server, String host) {
        this.server = server;
        this.host = host;
        
        frame = new QueryFrame(new QueryCommandParser(this.server, this));
        MainFrame.getMainFrame().addChild(frame);
        frame.addInternalFrameListener(this);
        frame.setTabCompleter(server.getTabCompleter());
        frame.open();
        
        try {
            server.getParser().getCallbackManager().addCallback("onPrivateAction", this, ClientInfo.parseHost(host));
            server.getParser().getCallbackManager().addCallback("onPrivateMessage", this, ClientInfo.parseHost(host));
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
        server.getParser().sendMessage(ClientInfo.parseHost(host), line);
        frame.addLine("querySelfMessage", server.getParser().getMyself().getNickname(), line);
    }
    
    /**
     * Sends a private action to the remote user
     * @param action action text to send
     */
    public void sendAction(String action) {
        server.getParser().sendAction(ClientInfo.parseHost(host), action);
        frame.addLine("querySelfAction", server.getParser().getMyself().getNickname(), action);
    }
    
    /**
     * Handles a private message event from the parser
     * @param parser Parser receiving the event
     * @param message message received
     * @param host remote user host
     */
    public void onPrivateMessage(IRCParser parser, String message, String host) {
        frame.addLine("queryMessage", ClientInfo.parseHost(host), message);
    }
    
    /**
     * Handles a private action event from the parser
     * @param parser Parser receiving the event
     * @param message message received
     * @param host remote host
     */
    public void onPrivateAction(IRCParser parser, String message, String host) {
        frame.addLine("queryAction", ClientInfo.parseHost(host), message);
    }
    
    /**
     * Updates the QueryFrame title
     */
    private void updateTitle() {
        String title = ClientInfo.parseHost(host);
        
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
     * Returns the Server assocaited with this query
     * @return asscoaited Server
     */
    public Server getServer() {
        return server;
    }
    
    /**
     * Determines if the specified frame is owned by this object
     */
    public boolean ownsFrame(JInternalFrame target) {
        return frame.equals(target);
    }
    
    /**
     * Closes the query and associated frame
     */
    public void close() {
        server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
        server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
        server.getParser().getCallbackManager().delCallback("onNickChanged", this);
        
        frame.setVisible(false);
        server.delQuery(host);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        server = null;
    }
    
    /**
     * Invoked when a internal frame has been opened.
     * @param internalFrameEvent frame opened event
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
     * Invoked when an internal frame is in the process of being closed.
     * @param internalFrameEvent frame closing event
     */
    public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
        close();
    }
    
    /**
     * Invoked when an internal frame has been closed.
     * @param internalFrameEvent frame closed event
     */
    public void internalFrameClosed(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Invoked when an internal frame is iconified.
     * @param internalFrameEvent frame iconified event
     */
    public void internalFrameIconified(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Invoked when an internal frame is de-iconified.
     * @param internalFrameEvent frame deiconified event
     */
    public void internalFrameDeiconified(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Invoked when an internal frame is activated.
     * @param internalFrameEvent frame activation event
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
     * Invoked when an internal frame is de-activated.
     * @param internalFrameEvent frame deactivation event
     */
    public void internalFrameDeactivated(InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Returns this query's name
     * @return A string representation of this query (i.e., the user's name)
     */
    public String toString() {
        return ClientInfo.parseHost(host);
    }    
    
    /**
     * Requests that this object's frame be activated
     */
    public void activateFrame() {
        MainFrame.getMainFrame().setActiveFrame(frame);
    }    

    /**
     * Adds a line of text to the main text area of the query frame
     * @param line The line to add
     */
    public void addLine(String line) {
        frame.addLine(line);
    }

    /**
     * Retrieves the icon used by the query frame
     * @return The query frame's icon
     */
    public ImageIcon getIcon() {
        return MainFrame.getMainFrame().getIcon();
    }
}
