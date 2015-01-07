/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.QueryActionEvent;
import com.dmdirc.events.QueryClosedEvent;
import com.dmdirc.events.QueryMessageEvent;
import com.dmdirc.events.QueryNickChangeEvent;
import com.dmdirc.events.QueryQuitEvent;
import com.dmdirc.events.QuerySelfActionEvent;
import com.dmdirc.events.QuerySelfMessageEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.PrivateChat;
import com.dmdirc.interfaces.User;
import com.dmdirc.logger.ErrorLevel;
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
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.EventUtils;

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The Query class represents the client's view of a query with another user. It handles callbacks
 * for query events from the parser, maintains the corresponding QueryWindow, and handles user input
 * for the query.
 */
public class Query extends FrameContainer implements PrivateActionListener,
        PrivateMessageListener, NickChangeListener, QuitListener,
        CompositionStateChangeListener, PrivateChat {

    /** The connection this Query is on. */
    private final Connection connection;
    /** The user associated with this query. */
    private final User user;

    public Query(
            final Connection connection,
            final User user,
            final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController,
            final MessageSinkManager messageSinkManager,
            final BackBufferFactory backBufferFactory) {
        super(connection.getWindowModel(), "query",
                user.getNickname(),
                user.getNickname(),
                connection.getWindowModel().getConfigManager(),
                backBufferFactory,
                new QueryCommandParser(connection.getWindowModel(), commandController,
                        connection.getWindowModel().getEventBus()),
                tabCompleterFactory.getTabCompleter(connection.getWindowModel().getTabCompleter(),
                        connection.getWindowModel().getConfigManager(),
                        CommandType.TYPE_QUERY, CommandType.TYPE_CHAT),
                messageSinkManager,
                connection.getWindowModel().getEventBus(),
                Arrays.asList(
                        WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier()));

        this.connection = connection;
        this.user = user;
        updateTitle();
        initBackBuffer();
    }

    @Override
    public void sendLine(final String line) {
        sendLine(line, getNickname());
    }

    @Override
    public void sendLine(final String line, final String target) {
        if (connection.getState() != ServerState.CONNECTED) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        splitLine(line).stream().filter(part -> !part.isEmpty()).forEach(part -> {
            connection.getParser().get().sendMessage(target, part);
            getEventBus().publishAsync(new QuerySelfMessageEvent(this,
                    connection.getLocalUser().get(), part));
        });
    }

    @Override
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        if (arg instanceof User) {
            final User clientInfo = (User) arg;
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
        return connection.getState() == ServerState.CONNECTED ? connection.getParser().get()
                .getMaxLength("PRIVMSG", getHost()) : -1;
    }

    @Override
    public void sendAction(final String action) {
        if (connection.getState() != ServerState.CONNECTED) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        final int maxLineLength = connection.getParser().get().getMaxLength("PRIVMSG", getHost());

        if (maxLineLength >= action.length() + 2) {
            connection.getParser().get().sendAction(getNickname(), action);
            getEventBus().publishAsync(
                    new QuerySelfActionEvent(this, connection.getLocalUser().get(), action));
        } else {
            addLine("actionTooLong", action.length());
        }
    }

    @Override
    public void onPrivateMessage(final Parser parser, final Date date,
            final String message, final String host) {
        getEventBus().publishAsync(
                new QueryMessageEvent(this, connection.getUser(host), message));
    }

    @Override
    public void onPrivateAction(final Parser parser, final Date date,
            final String message, final String host) {
        getEventBus().publishAsync(
                new QueryActionEvent(this, connection.getUser(host), message));
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
        final CallbackManager callbackManager = connection.getParser().get().getCallbackManager();
        final String nick = getNickname();

        try {
            callbackManager.addCallback(PrivateActionListener.class, this, nick);
            callbackManager.addCallback(PrivateMessageListener.class, this, nick);
            callbackManager.addCallback(CompositionStateChangeListener.class, this, nick);
            callbackManager.addCallback(QuitListener.class, this);
            callbackManager.addCallback(NickChangeListener.class, this);
        } catch (CallbackNotFoundException ex) {
            getEventBus().publishAsync(new AppErrorEvent(ErrorLevel.HIGH, ex,
                    "Unable to get query events", ex.getMessage()));
        }
    }

    @Override
    public void onNickChanged(final Parser parser, final Date date,
            final ClientInfo client, final String oldNick) {
        if (oldNick.equals(getNickname())) {
            final CallbackManager callbackManager = connection.getParser().get().getCallbackManager();

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
                getEventBus().publishAsync(
                        new AppErrorEvent(ErrorLevel.HIGH, ex, "Unable to get query events",
                                ex.getMessage()));
            }

            final String format = EventUtils.postDisplayable(getEventBus(),
                    new QueryNickChangeEvent(this, oldNick),
                    "queryNickChanged");

            connection.updateQuery(this, oldNick, client.getNickname());

            addLine(format, oldNick, client.getUsername(),
                    client.getHostname(), client.getNickname());
            user.setNickname(client.getNickname());
            updateTitle();

            setName(client.getNickname());
        }
    }

    @Override
    public void onQuit(final Parser parser, final Date date,
            final ClientInfo client, final String reason) {
        if (client.getNickname().equals(getNickname())) {
            final String format = EventUtils.postDisplayable(getEventBus(),
                    new QueryQuitEvent(this, reason),
                    reason.isEmpty() ? "queryQuit" : "queryQuitReason");
            addLine(format, client.getNickname(), client.getUsername(), client.getHostname(),
                    reason);
        }
    }

    @Override
    public void onCompositionStateChanged(final Parser parser, final Date date,
            final CompositionState state, final String host) {
        if (state == CompositionState.TYPING) {
            addComponent(WindowComponent.TYPING_INDICATOR.getIdentifier());
        } else {
            removeComponent(WindowComponent.TYPING_INDICATOR.getIdentifier());
        }
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.of(connection);
    }

    @Override
    public void close() {
        super.close();

        // Remove any callbacks or listeners
        connection.getParser().map(Parser::getCallbackManager).ifPresent(cm -> cm.delAllCallback(this));

        // Trigger action for the window closing
        getEventBus().publishAsync(new QueryClosedEvent(this));

        // Inform any parents that the window is closing
        connection.delQuery(this);
    }

    @Override
    public String getHost() {
        // TODO: Icky, IRC specific. Kill with fire.
        return user.getNickname() + '!' + user.getUsername() + '@' + user.getHostname();
    }

    @Override
    public String getNickname() {
        return user.getNickname();
    }

    @Override
    public void setCompositionState(final CompositionState state) {
        connection.getParser().get().setCompositionState(getHost(), state);
    }

    @Override
    public FrameContainer getWindowModel() {
        return this;
    }

    @Override
    public User getUser() {
        return user;
    }

}
