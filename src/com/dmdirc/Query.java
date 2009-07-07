/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.irc.callbacks.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.callbacks.NickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateActionListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
import com.dmdirc.parser.interfaces.callbacks.QuitListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.QueryWindow;

import java.awt.Toolkit;
import java.io.Serializable;

/**
 * The Query class represents the client's view of a query with another user.
 * It handles callbacks for query events from the parser, maintains the
 * corresponding QueryWindow, and handles user input for the query.
 * @author chris
 */
public final class Query extends MessageTarget implements
        PrivateActionListener, PrivateMessageListener, NickChangeListener, QuitListener, Serializable {

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
        super("query", newServer.getParser().parseHostmask(newHost)[0],
                newServer.getConfigManager());

        this.server = newServer;
        this.host = newHost;

        window = Main.getUI().getQuery(this);
        WindowManager.addWindow(server.getFrame(), window);

        ActionManager.processEvent(CoreActionType.QUERY_OPENED, null, this);

        if (!server.getConfigManager().getOptionBool("general", "hidequeries")) {
            window.open();
        }

        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                CommandManager.getCommandNames(CommandType.TYPE_QUERY));
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                CommandManager.getCommandNames(CommandType.TYPE_CHAT));
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
    @Override
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
    @Override
    public void sendLine(final String line) {
        if (server.getState() != ServerState.CONNECTED) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        final ClientInfo client = server.getParser().getLocalClient();

        for (String part : splitLine(window.getTranscoder().encode(line))) {
            if (!part.isEmpty()) {
                server.getParser().sendMessage(getNickname(), part);

                final StringBuffer buff = new StringBuffer("querySelfMessage");

                ActionManager.processEvent(CoreActionType.QUERY_SELF_MESSAGE, buff, this, part);

                addLine(buff, client.getNickname(), client.getUsername(),
                        client.getHostname(), part);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return server.getState() == ServerState.CONNECTED ? server.getParser()
                .getMaxLength("PRIVMSG", host) : -1;
    }

    /**
     * Sends a private action to the remote user.
     *
     * @param action action text to send
     */
    @Override
    public void sendAction(final String action) {
        if (server.getState() != ServerState.CONNECTED) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        final ClientInfo client = server.getParser().getLocalClient();
        final int maxLineLength = server.getParser().getMaxLength("PRIVMSG", host);

        if (maxLineLength >= action.length() + 2) {
            server.getParser().sendAction(getNickname(),
                    window.getTranscoder().encode(action));

            final StringBuffer buff = new StringBuffer("querySelfAction");

            ActionManager.processEvent(CoreActionType.QUERY_SELF_ACTION, buff, this, action);

            addLine(buff, client.getNickname(), client.getUsername(),
                    client.getHostname(), window.getTranscoder().encode(action));
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
    @Override
    public void onPrivateMessage(final Parser parser, final String message,
            final String remoteHost) {
        final String[] parts = parser.parseHostmask(host);

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
    @Override
    public void onPrivateAction(final Parser parser, final String message,
            final String remoteHost) {
        final String[] parts = parser.parseHostmask(host);

        final StringBuffer buff = new StringBuffer("queryAction");

        ActionManager.processEvent(CoreActionType.QUERY_ACTION, buff, this, message);

        addLine(buff, parts[0], parts[1], parts[2], message);
    }

    /**
     * Updates the QueryWindow's title.
     */
    private void updateTitle() {
        window.setTitle(getNickname());
    }

    /**
     * Reregisters query callbacks. Called when reconnecting to the server.
     */
    public void reregister() {
        final CallbackManager<?> callbackManager = server.getParser().getCallbackManager();
        final String nick = getNickname();

        try {
            callbackManager.addCallback(PrivateActionListener.class, this, nick);
            callbackManager.addCallback(PrivateMessageListener.class, this, nick);
            callbackManager.addCallback(QuitListener.class, this);
            callbackManager.addCallback(NickChangeListener.class, this);
        } catch (CallbackNotFoundException ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to get query events", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onNickChanged(final Parser tParser, final ClientInfo cClient,
            final String sOldNick) {
        if (sOldNick.equals(getNickname())) {
            final CallbackManager<?> callbackManager = server.getParser().getCallbackManager();

            callbackManager.delCallback(PrivateActionListener.class, this);
            callbackManager.delCallback(PrivateMessageListener.class, this);

            try {
                callbackManager.addCallback(PrivateActionListener.class, this, cClient.getNickname());
                callbackManager.addCallback(PrivateMessageListener.class, this, cClient.getNickname());
            } catch (CallbackNotFoundException ex) {
                Logger.appError(ErrorLevel.HIGH, "Unable to get query events", ex);
            }

            final StringBuffer format = new StringBuffer("queryNickChanged");

            ActionManager.processEvent(CoreActionType.QUERY_NICKCHANGE, format, this, sOldNick);

            server.getTabCompleter().removeEntry(TabCompletionType.QUERY_NICK, sOldNick);
            server.getTabCompleter().addEntry(TabCompletionType.QUERY_NICK, cClient.getNickname());

            addLine(format, sOldNick, cClient.getUsername(),
                    cClient.getHostname(), cClient.getNickname());
            host = cClient.getNickname() + "!" + cClient.getUsername() + "@" + cClient.getHostname();
            updateTitle();

            setName(cClient.getNickname());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onQuit(final Parser tParser, final ClientInfo cClient,
            final String sReason) {
        if (cClient.getNickname().equals(getNickname())) {
            final StringBuffer format = new StringBuffer(sReason.isEmpty()
                ? "queryQuit" : "queryQuitReason");

            ActionManager.processEvent(CoreActionType.QUERY_QUIT, format, this, sReason);

            addLine(format, cClient.getNickname(),
                    cClient.getUsername(), cClient.getHostname(), sReason);
        }
    }

    /**
     * Returns the Server associated with this query.
     *
     * @return associated Server
     */
    @Override
    public Server getServer() {
        return server;
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 1: Make the window non-visible
        window.setVisible(false);

        // 2: Remove any callbacks or listeners
        if (server != null && server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(this);
        }

        // 3: Trigger any actions neccessary

        // 4: Trigger action for the window closing
        ActionManager.processEvent(CoreActionType.QUERY_CLOSED, null, this);

        // 5: Inform any parents that the window is closing
        if (server != null) {
            server.delQuery(this);
        }

        // 6: Remove the window from the window manager
        WindowManager.removeWindow(window);

        // 7: Remove any references to the window and parents
        window = null;
        server = null;
    }

    /**
     * Returns the host that this query is with.
     *
     * @return The full host that this query is with
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the current nickname of the user that this query is with.
     *
     * @return The nickname of this query's user
     */
    public String getNickname() {
        return server.getParser().parseHostmask(host)[0];
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

        super.activateFrame();
    }

}
