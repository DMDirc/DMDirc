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

import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.URLBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link Channel}s.
 */
@Singleton
public class ChannelFactory {

    private final TabCompleterFactory tabCompleterFactory;
    private final CommandController commandController;
    private final MessageSinkManager messageSinkManager;
    private final URLBuilder urlBuilder;
    private final DMDircMBassador eventBus;
    private final BackBufferFactory backBufferFactory;

    @Inject
    public ChannelFactory(final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController, final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder, final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory) {
        this.tabCompleterFactory = tabCompleterFactory;
        this.commandController = commandController;
        this.messageSinkManager = messageSinkManager;
        this.urlBuilder = urlBuilder;
        this.eventBus = eventBus;
        this.backBufferFactory = backBufferFactory;
    }

    public Channel getChannel(final Server server,
            final ChannelInfo channelInfo,
            final ConfigProviderMigrator configMigrator) {
        return new Channel(server, channelInfo, configMigrator, tabCompleterFactory,
                commandController, messageSinkManager, urlBuilder, eventBus, backBufferFactory);
    }
}
