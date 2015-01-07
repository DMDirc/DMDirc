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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;

import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * A window which can be used to execute global commands.
 */
@Singleton
public class GlobalWindow extends FrameContainer {

    /**
     * Creates a new instance of GlobalWindow.
     */
    @Inject
    public GlobalWindow(@GlobalConfig final AggregateConfigProvider config,
            final GlobalCommandParser parser, final TabCompleterFactory tabCompleterFactory,
            final MessageSinkManager messageSinkManager,
            final DMDircMBassador eventBus, final BackBufferFactory backBufferFactory) {
        super(null, "icon", "Global", "(Global)", config, backBufferFactory, parser,
                tabCompleterFactory.getTabCompleter(config, CommandType.TYPE_GLOBAL),
                messageSinkManager, eventBus,
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier()));
        initBackBuffer();
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.empty();
    }

    @Override
    public void sendLine(final String line) {
        getEventBus().publishAsync(
                new CommandErrorEvent(this, "You may only enter commands in the global window."));
    }

    @Override
    public int getMaxLineLength() {
        return -1;
    }

    /** Handles the state of the global window. */
    @Singleton
    public static class GlobalWindowManager implements ConfigChangeListener {

        /** Provider to use to obtain global window instances. */
        private final Provider<GlobalWindow> globalWindowProvider;
        /** The global configuration to read settings from. */
        private final AggregateConfigProvider globalConfig;
        /** The provider to use to retrieve a window manager. */
        private final Provider<WindowManager> windowManagerProvider;

        /**
         * Creates a new instance of {@link GlobalWindowManager}.
         *
         * @param globalWindowProvider  The provider to use to obtain global windows.
         * @param globalConfig          Configuration provider to read settings from.
         * @param windowManagerProvider The provider to use to retrieve a window manager.
         */
        @Inject
        public GlobalWindowManager(final Provider<GlobalWindow> globalWindowProvider,
                @GlobalConfig final AggregateConfigProvider globalConfig,
                final Provider<WindowManager> windowManagerProvider) {
            this.globalWindowProvider = globalWindowProvider;
            this.globalConfig = globalConfig;
            this.windowManagerProvider = windowManagerProvider;
        }

        @Override
        public void configChanged(final String domain, final String key) {
            updateWindowState();
        }

        /**
         * Initialises the global window if it's enabled in the config.
         */
        public void init() {
            globalConfig.addChangeListener("general", "showglobalwindow", this);
            updateWindowState();
        }

        /**
         * Updates the state of the global window in line with the general.showglobalwindow config
         * setting.
         */
        protected void updateWindowState() {
            final WindowManager windowManager = windowManagerProvider.get();
            final GlobalWindow globalWindow = globalWindowProvider.get();
            final boolean globalWindowExists =
                    windowManager.getRootWindows().contains(globalWindow);

            if (globalConfig.getOptionBool("general", "showglobalwindow")) {
                if (!globalWindowExists) {
                    windowManager.addWindow(globalWindow);
                }
            } else {
                if (globalWindowExists) {
                    globalWindow.close();
                }
            }
        }
    }
}
