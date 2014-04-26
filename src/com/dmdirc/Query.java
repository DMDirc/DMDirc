/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.QueryCommandParser;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.QueryActionEvent;
import com.dmdirc.events.QueryClosedEvent;
import com.dmdirc.events.QueryMessageEvent;
import com.dmdirc.events.QueryNickchangeEvent;
import com.dmdirc.events.QueryQuitEvent;
import com.dmdirc.events.QuerySelfActionEvent;
import com.dmdirc.events.QuerySelfMessageEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.PrivateChat;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.CallbackNotFoundException;
import com.dmdirc.parser.common.CompositionState;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.CompositionStateChangeListener;
import com.dmdirc.parser.interfaces.callbacks.NickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateActionListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
import com.dmdirc.parser.interfaces.callbacks.QuitListener;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The Query class represents the client's view of a query with another user. It handles callbacks
 * for query events from the parser, maintains the corresponding QueryWindow, and handles user input
 * for the query.
 */
@Factory(inject = true, providers = true, singleton = true)
public class Query extends MessageTarget implements PrivateActionListener,
        PrivateMessageListener, NickChangeListener, QuitListener,
        CompositionStateChangeListener, PrivateChat {

    /** The Server this Query is on. */
    private final Server server;
    /** The full host of the client associated with this query. */
    private String host;
    /** The nickname of the client associated with this query. */
    private String nickname;

    /**
     * Creates a new instance of Query.
     *
     * @param newHost             host of the remove client
     * @param newServer           The server object that this Query belongs to
     * @param tabCompleterFactory The factory to use to create tab completers.
     * @param commandController   The controller to load commands from.
     * @param messageSinkManager  The sink manager to use to despatch messages.
     * @param urlBuilder          The URL builder to use when finding icons.
     */
    public Query(
            @Unbound final Server newServer,
            @Unbound final String newHost,
            final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController,
            final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder) {
        super(newServer, "query", newServer.parseHostmask(newHost)[0],
                newServer.parseHostmask(newHost)[0],
                newServer.getConfigManager(),
                new QueryCommandParser(newServer, commandController),
                tabCompleterFactory.getTabCompleter(newServer.getTabCompleter(),
                        newServer.getConfigManager(),
                        CommandType.TYPE_QUERY, CommandType.TYPE_CHAT),
                messageSinkManager,
                urlBuilder,
                newServer.getEventBus(),
                Arrays.asList(
                        WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier()));

        this.server = newServer;
        this.host = newHost;
        this.nickname = server.parseHostmask(host)[0];

        if (!server.getState().isDisconnected()) {
            reregister();
        }

        updateTitle();
    }

    @Override
    public void sendLine(final String line) {
        sendLine(line, getNickname());
    }

    @Override
    public void sendLine(final String line, final String target) {
        if (server.getState() != ServerState.CONNECTED) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        for (String part : splitLine(line)) {
            if (!part.isEmpty()) {
                server.getParser().sendMessage(target, part);

                doNotification("querySelfMessage", server.getParser().getLocalClient(), part);
                final DisplayableEvent event = new QuerySelfMessageEvent(this, server.getParser().
                        getLocalClient(), part);
                event.setDisplayFormat("querySelfMessage");
                getEventBus().post(event);
            }
        }
    }

    @Override
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        if (arg instanceof ClientInfo) {
            final ClientInfo clientInfo = (ClientInfo) arg;
            args.add(clientInfo.getNickname());
            args.add(clientInfo.getUsername());
            args.add(clientInfo.getHostname());
            return true;
        } else {
            return super.processNotificationArg(arg, args);
        }
    }

    @Override
    public int getMaxLineLength() {
        return server.getState() == ServerState.CONNECTED ? server.getParser()
                .getMaxLength("PRIVMSG", host) : -1;
    }

    @Override
    public void sendAction(final String action) {
        if (server.getState() != ServerState.CONNECTED) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        final ClientInfo client = server.getParser().getLocalClient();
        final int maxLineLength = server.getParser().getMaxLength("PRIVMSG", host);

        if (maxLineLength >= action.length() + 2) {
            server.getParser().sendAction(getNickname(), action);

            doNotification("querySelfAction", client, action);
            final DisplayableEvent event = new QuerySelfActionEvent(this, client, action);
            event.setDisplayFormat("querySelfAction");
            getEventBus().post(event);
        } else {
            addLine("actionTooLong", action.length());
        }
    }

    @Override
    public void onPrivateMessage(final Parser parser, final Date date,
            final String message, final String host) {
        final String[] parts = server.parseHostmask(host);

        final StringBuffer buff = new StringBuffer("queryMessage");

        final DisplayableEvent event = new QueryMessageEvent(this, parser.getClient(host), message);
        event.setDisplayFormat(buff.toString());
        getEventBus().post(event);

        addLine(buff, parts[0], parts[1], parts[2], message);
    }

    @Override
    public void onPrivateAction(final Parser parser, final Date date,
            final String message, final String host) {
        final String[] parts = server.parseHostmask(host);

        final StringBuffer buff = new StringBuffer("queryAction");

        final DisplayableEvent event = new QueryActionEvent(this, parser.getClient(host), message);
        event.setDisplayFormat(buff.toString());
        getEventBus().post(event);

        addLine(buff, parts[0], parts[1], parts[2], message);
    }

    /**
     * Updates the QueryWindow's title.
     */
    private void updateTitle() {
        setTitle(getNickname());
    }

    /**
     * Reregisters query callbacks. Called when reconnecting to the server.
     */
    public void reregister() {
        final CallbackManager callbackManager = server.getParser().getCallbackManager();
        final String nick = getNickname();

        try {
            callbackManager.addCallback(PrivateActionListener.class, this, nick);
            callbackManager.addCallback(PrivateMessageListener.class, this, nick);
            callbackManager.addCallback(CompositionStateChangeListener.class, this, nick);
            callbackManager.addCallback(QuitListener.class, this);
            callbackManager.addCallback(NickChangeListener.class, this);
        } catch (CallbackNotFoundException ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to get query events", ex);
        }
    }

    @Override
    public void onNickChanged(final Parser parser, final Date date,
            final ClientInfo client, final String oldNick) {
        if (oldNick.equals(getNickname())) {
            final CallbackManager callbackManager = server.getParser().getCallbackManager();

            callbackManager.delCallback(PrivateActionListener.class, this);
            callbackManager.delCallback(PrivateMessageListener.class, this);
            callbackManager.delCallback(CompositionStateChangeListener.class, this);

            try {
                callbackManager.addCallback(PrivateActionListener.class, this, client.getNickname());
                callbackManager.
                        addCallback(PrivateMessageListener.class, this, client.getNickname());
                callbackManager.addCallback(CompositionStateChangeListener.class, this, client.
                        getNickname());
            } catch (CallbackNotFoundException ex) {
                Logger.appError(ErrorLevel.HIGH, "Unable to get query events", ex);
            }

            final StringBuffer format = new StringBuffer("queryNickChanged");

            final DisplayableEvent event = new QueryNickchangeEvent(this, oldNick);
            event.setDisplayFormat("queryNickChanged");
            getEventBus().post(event);

            server.updateQuery(this, oldNick, client.getNickname());

            addLine(format, oldNick, client.getUsername(),
                    client.getHostname(), client.getNickname());
            host = client.getNickname() + "!" + client.getUsername() + "@" + client.getHostname();
            nickname = client.getNickname();
            updateTitle();

            setName(client.getNickname());
        }
    }

    @Override
    public void onQuit(final Parser parser, final Date date,
            final ClientInfo client, final String reason) {
        if (client.getNickname().equals(getNickname())) {
            final StringBuffer format = new StringBuffer(reason.isEmpty()
                    ? "queryQuit" : "queryQuitReason");

            final DisplayableEvent event = new QueryQuitEvent(this, reason);
            event.setDisplayFormat(format.toString());
            getEventBus().post(event);

            addLine(format, client.getNickname(),
                    client.getUsername(), client.getHostname(), reason);
        }
    }

    @Override
    public void onCompositionStateCanged(final Parser parser, final Date date,
            final CompositionState state, final String host) {
        if (state == CompositionState.TYPING) {
            addComponent(WindowComponent.TYPING_INDICATOR.getIdentifier());
        } else {
            removeComponent(WindowComponent.TYPING_INDICATOR.getIdentifier());
        }
    }

    @Override
    public Connection getConnection() {
        return server;
    }

    @Override
    public void close() {
        super.close();

        // Remove any callbacks or listeners
        if (server != null && server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(this);
        }

        // Trigger action for the window closing
        getEventBus().post(new QueryClosedEvent(this));

        // Inform any parents that the window is closing
        if (server != null) {
            server.delQuery(this);
        }
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setCompositionState(final CompositionState state) {
        getConnection().getParser().setCompositionState(host, state);
    }

}
