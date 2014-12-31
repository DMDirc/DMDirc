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

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.URLBuilder;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Factory for {@link Server}s
 */
@Singleton
public class ServerFactoryImpl {

    private final Provider<ServerManager> manager;
    private final ParserFactory parserFactory;
    private final TabCompleterFactory tabCompleterFactory;
    private final IdentityFactory identityFactory;
    private final MessageSinkManager messageSinkManager;
    private final WindowManager windowManager;
    private final Provider<ChannelFactory> channelFactory;
    private final Provider<QueryFactory> queryFactory;
    private final URLBuilder urlBuilder;
    private final DMDircMBassador eventBus;
    private final MessageEncoderFactory messageEncoderFactory;
    private final ConfigProvider userSettings;
    private final BackBufferFactory backBufferFactory;
    private final UserManager userManager;

    @Inject
    public ServerFactoryImpl(
            final Provider<ServerManager> manager,
            final ParserFactory parserFactory,
            final TabCompleterFactory tabCompleterFactory,
            final IdentityFactory identityFactory,
            final MessageSinkManager messageSinkManager,
            final WindowManager windowManager,
            final Provider<ChannelFactory> channelFactory,
            final Provider<QueryFactory> queryFactory,
            final URLBuilder urlBuilder,
            final DMDircMBassador eventBus,
            final MessageEncoderFactory messageEncoderFactory,
            @ClientModule.UserConfig final ConfigProvider userSettings,
            final BackBufferFactory backBufferFactory,
            final UserManager userManager) {
        this.manager = manager;
        this.parserFactory = parserFactory;
        this.tabCompleterFactory = tabCompleterFactory;
        this.identityFactory = identityFactory;
        this.messageSinkManager = messageSinkManager;
        this.windowManager = windowManager;
        this.channelFactory = channelFactory;
        this.queryFactory = queryFactory;
        this.urlBuilder = urlBuilder;
        this.eventBus = eventBus;
        this.messageEncoderFactory = messageEncoderFactory;
        this.userSettings = userSettings;
        this.backBufferFactory = backBufferFactory;
        this.userManager = userManager;
    }

    public Server getServer(
            final ConfigProviderMigrator configMigrator,
            final CommandParser commandParser,
            final ScheduledExecutorService executorService,
            final URI uri,
            final Profile profile) {
        return new Server(manager.get(), configMigrator, commandParser, parserFactory,
                tabCompleterFactory, identityFactory, messageSinkManager, windowManager,
                channelFactory.get(), queryFactory.get(), urlBuilder, eventBus,
                messageEncoderFactory, userSettings, executorService, uri, profile,
                backBufferFactory, userManager);
    }
}
