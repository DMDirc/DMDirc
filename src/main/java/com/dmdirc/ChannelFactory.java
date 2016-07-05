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
import com.dmdirc.commandparser.parsers.ChannelCommandParser;
import com.dmdirc.events.ChannelOpenedEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBufferFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link Channel}s.
 */
@Singleton
public class ChannelFactory {

    private final TabCompleterFactory tabCompleterFactory;
    private final CommandController commandController;
    private final DMDircMBassador eventBus;
    private final BackBufferFactory backBufferFactory;
    private final GroupChatUserManager groupChatUserManager;
    private final WindowManager windowManager;

    @Inject
    public ChannelFactory(final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController,
            final DMDircMBassador eventBus, final BackBufferFactory backBufferFactory,
            final GroupChatUserManager groupChatUserManager, final WindowManager windowManager) {
        this.tabCompleterFactory = tabCompleterFactory;
        this.commandController = commandController;
        this.eventBus = eventBus;
        this.backBufferFactory = backBufferFactory;
        this.groupChatUserManager = groupChatUserManager;
        this.windowManager = windowManager;
    }

    public Channel getChannel(final Connection connection,
            final ChannelInfo channelInfo,
            final ConfigProviderMigrator configMigrator) {
        final Channel channel = new Channel(connection, channelInfo, configMigrator,
                backBufferFactory, groupChatUserManager);
        channel.setInputModel(new DefaultInputModel(
                channel::sendLine,
                new ChannelCommandParser(
                        connection.getWindowModel(),
                        commandController,
                        eventBus,
                        channel),
                tabCompleterFactory.getTabCompleter(
                        connection.getWindowModel().getInputModel().get().getTabCompleter(),
                        configMigrator.getConfigProvider(),
                        CommandType.TYPE_CHANNEL,
                        CommandType.TYPE_CHAT),
                channel::getMaxLineLength));
        windowManager.addWindow(connection.getWindowModel(), channel);
        connection.getWindowModel().getEventBus().publish(new ChannelOpenedEvent(channel));
        channel.selfJoin();
        return channel;
    }
}
