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
import com.dmdirc.parser.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.callbacks.interfaces.INickChanged;
import com.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import com.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import com.dmdirc.parser.callbacks.interfaces.IQuit;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.QueryWindow;

import java.io.Serializable;

/**
 * The Query class represents the client's view of a query with another user.
 * It handles callbacks for query events from the parser, maintains the
 * corresponding QueryWindow, and handles user input for the query.
 * @author chris
 */
public final class Query extends MessageTarget implements
        IPrivateAction, IPrivateMessage, INickChanged, IQuit, Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The Server this Query is on. */
    private Server server;
    
    /** The QueryWindow used for this Query. */
    private QueryWindow window;
    
    /** The full host of the client associated with this Query. */
    private String host;
    
    /** The tab completer for the query window. */
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
        
        icon = IconManager.getIconManager().getIcon("query");
        
        window = Main.getUI().getQuery(this);
        Main.getUI().getFrameManager().addWindow(server, this);
        
        ActionManager.processEvent(CoreActionType.QUERY_OPENED, null, this);
        
        window.setFrameIcon(icon);
        
        if (!server.getConfigManager().getOptionBool("general", "hidequeries", false)) {
            window.open();
        }
        
        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(CommandManager.getQueryCommandNames());
        tabCompleter.addEntries(CommandManager.getChatCommandNames());
        window.getInputHandler().setTabCompleter(tabCompleter);
        
        reregister();
        
        updateTitle();
    }
    
    /**
     * Shows this query's window.
     */
    public void show() {
        window.open();
    }
    
    /** {@inheritDoc} */
    public InputWindow getFrame() {
        return window;
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
            server.getParser().sendMessage(ClientInfo.parseHost(host), window.getTranscoder().encode(line));
            
            final StringBuffer buff = new StringBuffer("querySelfMessage");
            
            ActionManager.processEvent(CoreActionType.QUERY_SELF_MESSAGE, buff, this, line);
            
            addLine(buff, client.getNickname(), client.getIdent(),
                    client.getHost(), window.getTranscoder().encode(line));
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
            server.getParser().sendAction(ClientInfo.parseHost(host), window.getTranscoder().encode(action));
            
            final StringBuffer buff = new StringBuffer("querySelfAction");
            
            ActionManager.processEvent(CoreActionType.QUERY_SELF_ACTION, buff, this, action);
            
            addLine(buff, client.getNickname(), client.getIdent(),
                    client.getHost(), window.getTranscoder().encode(action));
        } else {
            addLine("actionTooLong", action.length());
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
        
        addLine(buff, parts[0], parts[1], parts[2], message);
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
        
        addLine(buff, parts[0], parts[1], parts[2], message);
    }
    
    /**
     * Updates the QueryWindow's title.
     */
    private void updateTitle() {
        final String title = ClientInfo.parseHost(host);
        
        window.setTitle(title);
        
        if (window.isMaximum() && window.equals(Main.getUI().getMainWindow().getActiveFrame())) {
            Main.getUI().getMainWindow().setTitle(Main.getUI().getMainWindow().getTitlePrefix() + " - " + title);
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
            callbackManager.addCallback("onQuit", this);
            callbackManager.addCallback("onNickChanged", this);
        } catch (CallbackNotFoundException ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to get query events", ex);
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
            } catch (CallbackNotFoundException ex) {
                Logger.appError(ErrorLevel.HIGH, "Unable to get query events", ex);
            }
            
            final StringBuffer format = new StringBuffer("queryNickChanged");
            
            ActionManager.processEvent(CoreActionType.QUERY_NICKCHANGE, format, this, sOldNick);
            
            addLine(format, sOldNick, cClient.getIdent(),
                    cClient.getHost(), cClient.getNickname());
            host = cClient.getNickname() + "!" + cClient.getIdent() + "@" + cClient.getHost();
            updateTitle();
        }
    }
    
    /** {@inheritDoc} */
    public void onQuit(final IRCParser tParser, final ClientInfo cClient,
            final String sReason) {
        if (cClient.getNickname().equals(ClientInfo.parseHost(host))) {
            final StringBuffer format = new StringBuffer(sReason.isEmpty() ?
                "queryQuit" : "queryQuitReason");
            
            ActionManager.processEvent(CoreActionType.QUERY_QUIT, format, this, sReason);
            
            addLine(format, cClient.getNickname(),
                    cClient.getIdent(), cClient.getHost(), sReason);
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
     * Closes the query and associated window.
     */
    public void close() {
        close(true);
    }
    
    /**
     * Closes the query and associated window.
     *
     * @param shouldRemove Whether or not we should remove the window from the server.
     */
    public void close(final boolean shouldRemove) {
        server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
        server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
        server.getParser().getCallbackManager().delCallback("onNickChanged", this);
        server.getParser().getCallbackManager().delCallback("onQuit", this);
        
        ActionManager.processEvent(CoreActionType.QUERY_CLOSED, null, this);
        
        window.setVisible(false);
        
        if (shouldRemove) {
            server.delQuery(host);
        }
        
        Main.getUI().getMainWindow().delChild(window);
        
        window = null;
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
        if (window == null) {
            return;
        }
        
        if (!window.isVisible()) {
            show();
        }
        
        Main.getUI().getMainWindow().setActiveFrame(window);
    }
    
}
