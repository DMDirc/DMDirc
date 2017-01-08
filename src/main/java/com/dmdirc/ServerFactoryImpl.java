/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc;

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.ServerCommandParser;
import com.dmdirc.config.UserConfig;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.config.provider.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Factory for {@link Server}s.
 */
@Singleton
public class ServerFactoryImpl {

    private final ParserFactory parserFactory;
    private final TabCompleterFactory tabCompleterFactory;
    private final IdentityFactory identityFactory;
    private final Provider<QueryFactory> queryFactory;
    private final Provider<CommandController> commandController;
    private final EventBus eventBus;
    private final MessageEncoderFactory messageEncoderFactory;
    private final ConfigProvider userSettings;
    private final BackBufferFactory backBufferFactory;
    private final GroupChatManagerImplFactory groupChatManagerFactory;
    private final UserManager userManager;

    @Inject
    public ServerFactoryImpl(
            final ParserFactory parserFactory,
            final TabCompleterFactory tabCompleterFactory,
            final IdentityFactory identityFactory,
            final Provider<QueryFactory> queryFactory,
            final Provider<CommandController> commandController,
            final EventBus eventBus,
            final MessageEncoderFactory messageEncoderFactory,
            @UserConfig final ConfigProvider userSettings,
            final BackBufferFactory backBufferFactory,
            final GroupChatManagerImplFactory groupChatManagerFactory,
            final UserManager userManager) {
        this.parserFactory = parserFactory;
        this.tabCompleterFactory = tabCompleterFactory;
        this.identityFactory = identityFactory;
        this.queryFactory = queryFactory;
        this.commandController = commandController;
        this.eventBus = eventBus;
        this.messageEncoderFactory = messageEncoderFactory;
        this.userSettings = userSettings;
        this.backBufferFactory = backBufferFactory;
        this.groupChatManagerFactory = groupChatManagerFactory;
        this.userManager = userManager;
    }

    public Server getServer(
            final ConfigProviderMigrator configMigrator,
            final ScheduledExecutorService executorService,
            final URI uri,
            final Profile profile) {
        final FrameContainer windowModel =
                new FrameContainer("server-disconnected", getHost(uri), getHost(uri),
                        configMigrator.getConfigProvider(), backBufferFactory, eventBus,
                        Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                                WindowComponent.INPUTFIELD.getIdentifier(),
                                WindowComponent.CERTIFICATE_VIEWER.getIdentifier()));
        final Server server = new Server(windowModel, configMigrator, parserFactory,
                identityFactory, queryFactory.get(),
                messageEncoderFactory, userSettings, groupChatManagerFactory, executorService,
                uri, profile, userManager);
        windowModel.setConnection(server);
        windowModel.initBackBuffer();
        windowModel.setInputModel(new DefaultInputModel(
                server::sendLine,
                new ServerCommandParser(
                        server.getWindowModel().getConfigManager(),
                        commandController.get(),
                        eventBus,
                        server),
                tabCompleterFactory.getTabCompleter(
                        configMigrator.getConfigProvider(),
                        CommandType.TYPE_SERVER,
                        CommandType.TYPE_GLOBAL),
                server::getMaxLineLength));
        return server;
    }

    /**
     * Retrieves the host component of the specified URI, or throws a relevant exception if this is
     * not possible.
     *
     * @param uri The URI to be processed
     *
     * @return The URI's host component, as returned by {@link URI#getHost()}.
     *
     * @throws NullPointerException     If <code>uri</code> is null
     * @throws IllegalArgumentException If the specified URI has no host
     * @since 0.6.4
     */
    private static String getHost(final URI uri) {
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("URIs must have hosts");
        }

        return uri.getHost();
    }

}
