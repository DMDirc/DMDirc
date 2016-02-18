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
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.events.QueryActionEvent;
import com.dmdirc.events.QueryClosedEvent;
import com.dmdirc.events.QueryMessageEvent;
import com.dmdirc.events.QueryNickChangeEvent;
import com.dmdirc.events.QueryQuitEvent;
import com.dmdirc.events.QuerySelfActionEvent;
import com.dmdirc.events.QuerySelfMessageEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.PrivateChat;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.CompositionState;
import com.dmdirc.parser.events.CompositionStateChangeEvent;
import com.dmdirc.parser.events.NickChangeEvent;
import com.dmdirc.parser.events.PrivateActionEvent;
import com.dmdirc.parser.events.PrivateMessageEvent;
import com.dmdirc.parser.events.QuitEvent;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Optional;

import net.engio.mbassy.listener.Handler;

/**
 * The Query class represents the client's view of a query with another user. It handles callbacks
 * for query events from the parser, maintains the corresponding QueryWindow, and handles user input
 * for the query.
 */
public class Query extends FrameContainer implements PrivateChat {

    /** The connection this Query is on. */
    private final Connection connection;
    /** The user associated with this query. */
    private final User user;

    public Query(
            final Connection connection,
            final User user,
            final TabCompleterFactory tabCompleterFactory,
            final BackBufferFactory backBufferFactory) {
        super("query",
                user.getNickname(),
                user.getNickname(),
                connection.getWindowModel().getConfigManager(),
                backBufferFactory,
                tabCompleterFactory.getTabCompleter(
                        connection.getWindowModel().getInputModel().get().getTabCompleter(),
                        connection.getWindowModel().getConfigManager(),
                        CommandType.TYPE_QUERY, CommandType.TYPE_CHAT),
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
    public int getMaxLineLength() {
        // TODO: The parser layer should abstract this
        return connection.getState() == ServerState.CONNECTED ? connection.getParser().get()
                .getMaxLength("PRIVMSG", user.getNickname()
                        + '!' + user.getUsername().orElse("")
                        + '@' + user.getHostname().orElse("")) : -1;
    }

    @Override
    public void sendAction(final String action) {
        if (connection.getState() != ServerState.CONNECTED) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        final int maxLineLength = getInputModel().get().getMaxLineLength();

        if (maxLineLength >= action.length() + 2) {
            connection.getParser().get().sendAction(getNickname(), action);
            getEventBus().publishAsync(
                    new QuerySelfActionEvent(this, connection.getLocalUser().get(), action));
        } else {
            getEventBus().publishAsync(
                    new CommandErrorEvent(this, "Warning: action too long to be sent"));
        }
    }

    @Handler
    public void onPrivateMessage(final PrivateMessageEvent event) {
        if (!checkQuery(event.getHost())) {
            return;
        }
        getEventBus().publishAsync(
                new QueryMessageEvent(this, connection.getUser(event.getHost()), event.getMessage()));
    }

    @Handler
    public void onPrivateAction(final PrivateActionEvent event) {
        if (!checkQuery(event.getHost())) {
            return;
        }
        getEventBus().publishAsync(
                new QueryActionEvent(this, connection.getUser(event.getHost()), event.getMessage()));
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
        callbackManager.subscribe(this);
    }

    @Handler
    public void onNickChanged(final NickChangeEvent event) {
        if (!checkQuery(event.getClient().getHostname())) {
            return;
        }
        final ClientInfo client = event.getClient();
        final String oldNick = event.getOldNick();
        if (client.getNickname().equals(getNickname())) {
            connection.updateQuery(this, oldNick, client.getNickname());

            getEventBus().publish(new QueryNickChangeEvent(this, oldNick, client.getNickname()));
            updateTitle();

            setName(client.getNickname());
        }
    }

    @Handler
    public void onQuit(final QuitEvent event) {
        if (!checkQuery(event.getClient().getHostname())) {
            return;
        }
        if (event.getClient().getNickname().equals(getNickname())) {
            getEventBus().publish(new QueryQuitEvent(this, event.getReason()));
        }
    }

    @Handler
    public void onCompositionStateChanged(final CompositionStateChangeEvent event) {
        if (!checkQuery(event.getHost())) {
            return;
        }
        if (event.getState() == CompositionState.TYPING) {
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
        connection.getParser().map(Parser::getCallbackManager).ifPresent(cm -> cm.unsubscribe(this));


        // Trigger action for the window closing
        getEventBus().publishAsync(new QueryClosedEvent(this));

        // Inform any parents that the window is closing
        connection.delQuery(this);
    }

    @Override
    public String getNickname() {
        return user.getNickname();
    }

    @Override
    public void setCompositionState(final CompositionState state) {
        connection.getParser().get().setCompositionState(user.getNickname(), state);
    }

    @Override
    public WindowModel getWindowModel() {
        return this;
    }

    @Override
    public User getUser() {
        return user;
    }

    private boolean checkQuery(final String host) {
        return connection.getUser(host).getNickname().equals(user.getNickname());
    }

}
