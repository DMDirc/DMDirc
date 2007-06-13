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
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.callbacks.CallbackManager;
import com.dmdirc.parser.callbacks.CallbackNotFound;
import com.dmdirc.parser.callbacks.interfaces.INickChanged;
import com.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import com.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.QueryFrame;
import com.dmdirc.ui.input.TabCompleter;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * The Query class represents the client's view of a query with another user.
 * It handles callbacks for query events from the parser, maintains the
 * corresponding ServerFrame, and handles user input to a ServerFrame.
 * @author chris
 */
public final class Query extends WritableFrameContainer implements
        IPrivateAction, IPrivateMessage, INickChanged {
    
    /** The Server this Query is on. */
    private Server server;
    
    /** The QueryFrame used for this Query. */
    private QueryFrame frame;
    
    /** The full host of the client associated with this Query. */
    private String host;
    
    /** The tab completer for the query frame. */
    private final TabCompleter tabCompleter;
    
    /**
     * Creates a new instance of Query.
     *
     * @param newHost host of the remove client
     * @param newServer The server object that this Query belongs to
     */
    public Query(final Server newServer, final String newHost) {
        super();
        
        this.server = newServer;
        this.host = newHost;
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        final URL imageURL = cldr.getResource("com/dmdirc/res/query.png");
        imageIcon = new ImageIcon(imageURL);
        
        frame = new QueryFrame(this);
        
        ActionManager.processEvent(CoreActionType.QUERY_OPENED, null, this);
        
        MainFrame.getMainFrame().addChild(frame);
        frame.addInternalFrameListener(this);
        frame.setFrameIcon(imageIcon);
        
        if (!Config.getOptionBool("general", "hidequeries")) {
            frame.open();
        }
        
        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(CommandManager.getQueryCommandNames());
        frame.getInputHandler().setTabCompleter(tabCompleter);
        
        reregister();
        
        updateTitle();
    }
    
    /**
     * Shows this query's frame.
     */
    public void show() {
        frame.open();
    }
    
    /**
     * Returns the internal frame belonging to this object.
     *
     * @return This object's internal frame
     */
    public InputWindow getFrame() {
        return frame;
    }
    
    /**
     * Returns the tab completer for this query.
     *
     * @return This query's tab completer
     */
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }
    
    /** {@inheritDoc} */
    public void sendLine(final String line) {
        final ClientInfo client = server.getParser().getMyself();
        
        if (line.length() <= getMaxLineLength()) {
            server.getParser().sendMessage(ClientInfo.parseHost(host), line);
            
            final StringBuffer buff = new StringBuffer("querySelfMessage");
            
            ActionManager.processEvent(CoreActionType.QUERY_SELF_MESSAGE, buff, this, line);
            
            frame.addLine(buff, client.getNickname(), client.getIdent(), client.getHost(), line);
        } else {
            sendLine(line.substring(0, getMaxLineLength()));
            sendLine(line.substring(getMaxLineLength()));
        }
    }
    
    /** {@inheritDoc} */
    public int getMaxLineLength() {
        return server.getParser().getMaxLength("PRIVMSG", host);
    }
    
    /**
     * Sends a private action to the remote user.
     *
     * @param action action text to send
     */
    public void sendAction(final String action) {
        final ClientInfo client = server.getParser().getMyself();
        final int maxLineLength = server.getParser().getMaxLength("PRIVMSG", host);
        
        if (maxLineLength >= action.length() + 2) {
            server.getParser().sendAction(ClientInfo.parseHost(host), action);
            
            final StringBuffer buff = new StringBuffer("querySelfAction");
            
            ActionManager.processEvent(CoreActionType.QUERY_SELF_ACTION, buff, this, action);
            
            frame.addLine(buff, client.getNickname(), client.getIdent(), client.getHost(), action);
        } else {
            frame.addLine("actionTooLong", action.length());
        }
    }
    
    /**
     * Handles a private message event from the parser.
     *
     * @param parser Parser receiving the event
     * @param message message received
     * @param remoteHost remote user host
     */
    public void onPrivateMessage(final IRCParser parser, final String message,
            final String remoteHost) {
        final String[] parts = ClientInfo.parseHostFull(remoteHost);
        
        final StringBuffer buff = new StringBuffer("queryMessage");
        
        ActionManager.processEvent(CoreActionType.QUERY_MESSAGE, buff, this, message);
        
        frame.addLine(buff, parts[0], parts[1], parts[2], message);
    }
    
    /**
     * Handles a private action event from the parser.
     *
     * @param parser Parser receiving the event
     * @param message message received
     * @param remoteHost remote host
     */
    public void onPrivateAction(final IRCParser parser, final String message,
            final String remoteHost) {
        final String[] parts = ClientInfo.parseHostFull(host);
        
        final StringBuffer buff = new StringBuffer("queryAction");
        
        ActionManager.processEvent(CoreActionType.QUERY_ACTION, buff, this, message);
        
        frame.addLine(buff, parts[0], parts[1], parts[2], message);
    }
    
    /**
     * Updates the QueryFrame title.
     */
    private void updateTitle() {
        final String title = ClientInfo.parseHost(host);
        
        frame.setTitle(title);
        
        if (frame.isMaximum() && frame.equals(MainFrame.getMainFrame().getActiveFrame())) {
            MainFrame.getMainFrame().setTitle(MainFrame.getMainFrame().getTitlePrefix() + " - " + title);
        }
    }
    
    /**
     * Reregisters query callbacks. Called when reconnecting to the server.
     */
    public void reregister() {
        final CallbackManager callbackManager = server.getParser().getCallbackManager();
        
        try {
            callbackManager.addCallback("onPrivateAction", this, ClientInfo.parseHost(host));
            callbackManager.addCallback("onPrivateMessage", this, ClientInfo.parseHost(host));
            callbackManager.addCallback("onNickChanged", this);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to get query events", ex);
        }
    }
    
    /** {@inheritDoc} */
    public void onNickChanged(final IRCParser tParser, final ClientInfo cClient,
            final String sOldNick) {
        if (sOldNick.equals(ClientInfo.parseHost(host))) {
            final CallbackManager callbackManager = server.getParser().getCallbackManager();
            
            callbackManager.delCallback("onPrivateAction", this);
            callbackManager.delCallback("onPrivateMessage", this);
            
            try {
                callbackManager.addCallback("onPrivateAction", this, cClient.getNickname());
                callbackManager.addCallback("onPrivateMessage", this, cClient.getNickname());
            } catch (CallbackNotFound ex) {
                Logger.error(ErrorLevel.ERROR, "Unable to get query events", ex);
            }
            
            // TODO: Action hook!
            frame.addLine("queryNickChanged", sOldNick, cClient.getIdent(),
                    cClient.getHost(), cClient.getNickname());
            host = cClient.getNickname() + "!" + cClient.getIdent() + "@" + cClient.getHost();
            updateTitle();
        }
    }
    
    /**
     * Returns the Server assocaited with this query.
     *
     * @return asscoaited Server
     */
    public Server getServer() {
        return server;
    }
    
    /**
     * Closes the query and associated frame.
     */
    public void close() {
        server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
        server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
        server.getParser().getCallbackManager().delCallback("onNickChanged", this);
        
        ActionManager.processEvent(CoreActionType.QUERY_CLOSED, null, this);
        
        frame.setVisible(false);
        server.delQuery(host);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        server = null;
    }
    
    /**
     * Returns this query's name.
     *
     * @return A string representation of this query (i.e., the user's name)
     */
    public String toString() {
        return ClientInfo.parseHost(host);
    }
    
    /**
     * Returns the host that this query is with.
     *
     * @return The full host that this query is with
     */
    public String getHost() {
        return host;
    }
    
    /** {@inheritDoc} */
    @Override
    public void activateFrame() {
        if (!frame.isVisible()) {
            show();
        }
        
        MainFrame.getMainFrame().setActiveFrame(frame);
    }
}
