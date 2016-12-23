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

import com.dmdirc.events.ChannelClosedEvent;
import com.dmdirc.events.ChannelSelfActionEvent;
import com.dmdirc.events.ChannelSelfJoinEvent;
import com.dmdirc.events.ChannelSelfMessageEvent;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.events.NickListClientAddedEvent;
import com.dmdirc.events.NickListClientRemovedEvent;
import com.dmdirc.events.NickListClientsChangedEvent;
import com.dmdirc.events.NickListUpdatedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.parser.common.ChannelListModeItem;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.StyledMessageUtils;
import com.google.common.collect.EvictingQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * The Channel class represents the client's view of the channel. It handles callbacks for channel
 * events from the parser, maintains the corresponding ChannelWindow, and handles user input for the
 * channel.
 */
public class Channel extends FrameContainer implements GroupChat {

    /** The parser's pChannel class. */
    private ChannelInfo channelInfo;
    /** The connection this channel is on. */
    private final Connection connection;
    /** A list of previous topics we've seen. */
    private final Queue<Topic> topics;
    /** Our event handler. */
    private final ChannelEventHandler eventHandler;
    /** The migrator to use to migrate our config provider. */
    private final ConfigProviderMigrator configMigrator;
    /** Manager used to retrieve {@link GroupChatUser}s */
    private final GroupChatUserManager groupChatUserManager;
    /** Whether we're currently in this channel or not. */
    private boolean isOnChannel;
    /** Reason for leaving the channel. */
    private PartReason partReason;

    /**
     * Creates a new instance of Channel.
     *
     * @param connection          The connection object that this channel belongs to
     * @param newChannelInfo      The parser's channel object that corresponds to this channel
     * @param configMigrator      The config migrator which provides the config for this channel.
     */
    public Channel(
            final Connection connection,
            final ChannelInfo newChannelInfo,
            final ConfigProviderMigrator configMigrator,
            final BackBufferFactory backBufferFactory,
            final GroupChatUserManager groupChatUserManager) {
        super("channel-inactive",
                newChannelInfo.getName(),
                new StyledMessageUtils().stripControlCodes(newChannelInfo.getName()), // TODO: Inject this
                configMigrator.getConfigProvider(),
                backBufferFactory,
                connection.getWindowModel().getEventBus(),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier(),
                        WindowComponent.TOPICBAR.getIdentifier(),
                        WindowComponent.USERLIST.getIdentifier()));

        this.configMigrator = configMigrator;
        this.channelInfo = newChannelInfo;
        this.connection = connection;
        this.groupChatUserManager = groupChatUserManager;

        topics = EvictingQueue.create(
                getConfigManager().getOptionInt("channel", "topichistorysize"));

        eventHandler = new ChannelEventHandler(this, getEventBus(), groupChatUserManager);

        initBackBuffer();
        registerCallbacks();
        updateTitle();
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    @Override
    public boolean isOnChannel() {
        return isOnChannel;
    }

    @Override
    public PartReason getPartReason() {
        return partReason;
    }

    /**
     * Registers callbacks with the parser for this channel.
     */
    private void registerCallbacks() {
        eventHandler.registerCallbacks();
        configMigrator.migrate(connection.getProtocol(), connection.getIrcd(), connection.getNetwork(),
                connection.getAddress(), channelInfo.getName());
    }

    @Override
    public void sendLine(final String line) {
        if (connection.getState() != ServerState.CONNECTED
                || connection.getParser().get().getChannel(channelInfo.getName()) == null) {
            // We're not in the channel/connected to the server
            return;
        }

        final GroupChatUser me = getUser(connection.getLocalUser().get()).get();
        getInputModel().get()
                .splitLine(line)
                .stream()
                .filter(part -> !part.isEmpty())
                .forEach(part -> {
                    getEventBus().publishAsync(new ChannelSelfMessageEvent(this, me, part));
                    channelInfo.sendMessage(part);
                });
    }

    @Override
    public int getMaxLineLength() {
        return connection.getState() == ServerState.CONNECTED
                ? connection.getParser().get().getMaxLength("PRIVMSG", getChannelInfo().getName())
                : -1;
    }

    @Override
    public void sendAction(final String action) {
        if (connection.getState() != ServerState.CONNECTED
                || connection.getParser().get().getChannel(channelInfo.getName()) == null) {
            // We're not on the server/channel
            return;
        }

        if (connection.getParser().get().getMaxLength("PRIVMSG", getChannelInfo().getName())
                <= action.length()) {
            getEventBus().publishAsync(new CommandErrorEvent(this,
                    "Warning: action too long to be sent"));
        } else {
            final GroupChatUser me = getUser(connection.getLocalUser().get()).get();
            getEventBus().publishAsync(new ChannelSelfActionEvent(this, me, action));
            channelInfo.sendAction(action);
        }
    }

    /**
     * Sets this object's ChannelInfo reference to the one supplied. This only needs to be done if
     * the channel window (and hence this channel object) has stayed open while the user has been
     * out of the channel.
     *
     * @param newChannelInfo The new ChannelInfo object
     */
    public void setChannelInfo(final ChannelInfo newChannelInfo) {
        channelInfo = newChannelInfo;
        registerCallbacks();
    }

    /**
     * Called when we join this channel. Just needs to output a message.
     */
    public void selfJoin() {
        isOnChannel = true;

        final User me = connection.getLocalUser().get();
        getEventBus().publishAsync(new ChannelSelfJoinEvent(this, me));

        setIcon("channel");

        connection.getInviteManager().removeInvites(channelInfo.getName());
    }

    /**
     * Updates the title of the channel window, and of the main window if appropriate.
     */
    private void updateTitle() {
        final StyledMessageUtils styleUtils = new StyledMessageUtils();

        String temp = styleUtils.stripControlCodes(channelInfo.getName());
        if (!channelInfo.getTopic().isEmpty()) {
            temp += " - " + styleUtils.stripControlCodes(channelInfo.getTopic());
        }
        setTitle(temp);
    }

    @Override
    public void join() {
        connection.getParser().get().joinChannel(channelInfo.getName());
    }

    @Override
    public void part(final String reason) {
        channelInfo.part(reason);

        resetWindow(PartReason.LOCAL_PART);
    }

    @Override
    public void retrieveListModes() {
        channelInfo.requestListModes();
    }

    /**
     * Resets the window state after the client has left a channel.
     */
    public void resetWindow(final PartReason reason) {
        if (isOnChannel) {
            partReason = reason;
            isOnChannel = false;
        }

        setIcon("channel-inactive");

        // Needs to be published synchronously so that nicklists are cleared before the parser
        // is disconnected (which happens synchronously after this method returns).
        getEventBus().publish(
                new NickListClientsChangedEvent(this, Collections.emptyList()));
    }

    @Override
    public void close() {
        super.close();

        // Remove any callbacks or listeners
        eventHandler.unregisterCallbacks();

        connection.getParser().map(Parser::getCallbackManager)
                .ifPresent(cm -> cm.unsubscribe(eventHandler));

        // Trigger any actions neccessary
        if (isOnChannel && connection.getState() != ServerState.CLOSING) {
            part(getConfigManager().getOption("general", "partmessage"));
        }

        // Trigger action for the window closing
        getEventBus().publish(new ChannelClosedEvent(this));
    }

    /**
     * Adds a ChannelClient to this Channel.
     *
     * @param client The client to be added
     */
    public void addClient(final GroupChatUser client) {
        getEventBus().publishAsync(new NickListClientAddedEvent(this, client));

        getInputModel().get().getTabCompleter().addEntry(
                TabCompletionType.CHANNEL_NICK, client.getNickname());
    }

    /**
     * Removes the specified ChannelClient from this channel.
     *
     * @param client The client to be removed
     */
    public void removeClient(final GroupChatUser client) {
        getEventBus().publishAsync(new NickListClientRemovedEvent(this, client));

        getInputModel().get().getTabCompleter().removeEntry(
                TabCompletionType.CHANNEL_NICK, client.getNickname());

        if (client.getUser().equals(connection.getLocalUser().orElse(null))) {
            resetWindow(PartReason.REMOTE_PART);
        }
    }

    /**
     * Replaces the list of known clients on this channel with the specified one.
     *
     * @param clients The list of clients to use
     */
    public void setClients(final Collection<GroupChatUser> clients) {
        getEventBus().publishAsync(new NickListClientsChangedEvent(this, clients));

        getInputModel().get().getTabCompleter().clear(TabCompletionType.CHANNEL_NICK);

        getInputModel().get().getTabCompleter().addEntries(
                TabCompletionType.CHANNEL_NICK,
                clients.stream().map(GroupChatUser::getNickname).collect(Collectors.toList()));
    }

    /**
     * Renames a client that is in this channel.
     *
     * @param oldName The old nickname of the client
     * @param newName The new nickname of the client
     */
    public void renameClient(final String oldName, final String newName) {
        getInputModel().get().getTabCompleter().removeEntry(
                TabCompletionType.CHANNEL_NICK, oldName);
        getInputModel().get().getTabCompleter().addEntry(
                TabCompletionType.CHANNEL_NICK, newName);
        refreshClients();
    }

    @Override
    public void refreshClients() {
        if (!isOnChannel) {
            return;
        }

        getEventBus().publishAsync(new NickListUpdatedEvent(this));
    }

    // ---------------------------------------------------- TOPIC HANDLING -----
    /**
     * Adds the specified topic to this channel's topic list.
     *
     * @param topic The topic to be added.
     */
    public void addTopic(final Topic topic) {
        synchronized (topics) {
            topics.add(topic);
        }
        updateTitle();
    }

    @Override
    public List<Topic> getTopics() {
        synchronized (topics) {
            return new ArrayList<>(topics);
        }
    }

    @Override
    public Optional<Topic> getCurrentTopic() {
        synchronized (topics) {
            if (topics.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(getTopics().get(topics.size() - 1));
            }
        }
    }

    // ------------------------------------------ PARSER METHOD DELEGATION -----
    @Override
    public void setTopic(final String topic) {
        channelInfo.setTopic(topic);
    }

    @Override
    public int getMaxTopicLength() {
        return connection.getParser().get().getMaxTopicLength();
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.of(connection);
    }

    @Override
    public Optional<GroupChatUser> getUser(final User user) {
        final ChannelClientInfo ci = channelInfo.getChannelClient(((Client) user).getClientInfo());
        if (ci == null) {
            return Optional.empty();
        }
        return Optional.of(groupChatUserManager.getUserFromClient(ci, user, this));
    }

    @Override
    public Collection<GroupChatUser> getUsers() {
        return channelInfo.getChannelClients().stream()
                .map(client -> groupChatUserManager.getUserFromClient(client, this))
                .collect(Collectors.toList());
    }

    @Override
    public WindowModel getWindowModel() {
        return this;
    }

    @Override
    public void kick(final GroupChatUser user, final Optional<String> reason) {
        ((ChannelClient) user).getClientInfo().kick(
                reason.orElse(getConfigManager().getOption("general", "kickmessage")));
    }

    @Override
    public Collection<ChannelListModeItem> getListModeItems(final char mode) {
        return channelInfo.getListMode(mode);
    }

    @Override
    public void setMode(final char mode, @Nullable final String value) {
        channelInfo.alterMode(true, mode, value);
    }

    @Override
    public void removeMode(final char mode, final String value) {
        channelInfo.alterMode(false, mode, value);
    }

    @Override
    public void flushModes() {
        channelInfo.flushModes();
    }

    @Override
    public String getModes() {
        return channelInfo.getModes();
    }

    @Override
    public String getModeValue(final char mode) {
        return channelInfo.getMode(mode);
    }

    @Override
    public void requestUsersInfo() {
        channelInfo.sendWho();
    }

}
