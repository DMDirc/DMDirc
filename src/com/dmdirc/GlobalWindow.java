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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.util.URLBuilder;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * A window which can be used to execute global commands.
 */
public class GlobalWindow extends FrameContainer {

    /**
     * Creates a new instance of GlobalWindow.
     *
     * @param config              The ConfigManager to retrieve settings from.
     * @param parser              The command parser to use to parse input.
     * @param tabCompleterFactory The factory to use to create tab completers.
     * @param messageSinkManager  The sink manager to use to despatch messages.
     * @param urlBuilder          The URL builder to use when finding icons.
     */
    public GlobalWindow(
            final AggregateConfigProvider config,
            final CommandParser parser,
            final TabCompleterFactory tabCompleterFactory,
            final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder) {
        super("icon", "Global", "(Global)", config, urlBuilder, parser,
                tabCompleterFactory.getTabCompleter(config, CommandType.TYPE_GLOBAL),
                messageSinkManager,
                Arrays.asList(
                        WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier()));
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void sendLine(final String line) {
        addLine("commandError", "You may only enter commands in the global window.");
    }

    @Override
    public int getMaxLineLength() {
        return -1;
    }

    /** Handles the state of the global window. */
    @Singleton
    public static class GlobalWindowManager implements ConfigChangeListener {

        /** The global configuration to read settings from. */
        private final AggregateConfigProvider globalConfig;
        /** The factory to use to create tab completers. */
        private final TabCompleterFactory tabCompleterFactory;
        /** The provider to use to retrieve a window manager. */
        private final Provider<WindowManager> windowManagerProvider;
        /** The provider to use to retrieve message sink managers. */
        private final Provider<MessageSinkManager> messageSinkManagerProvider;
        /** The provider to use to retrieve a global command parser. */
        private final Provider<GlobalCommandParser> globalCommandParserProvider;
        /** The URL builder to use when finding icons. */
        private final URLBuilder urlBuilder;
        /** The global window that's in use, if any. */
        private GlobalWindow globalWindow;

        /**
         * Creates a new instance of {@link GlobalWindowManager}.
         *
         * @param globalConfig                Configuration provider to read settings from.
         * @param tabCompleterFactory         Factory to use to create tab completers.
         * @param windowManagerProvider       The provider to use to retrieve a window manager.
         * @param messageSinkManagerProvider  The provider to use to retrieve a sink manager.
         * @param globalCommandParserProvider The provider to use to retrieve a global command
         *                                    parser.
         * @param urlBuilder                  The URL builder to use when finding icons.
         */
        @Inject
        public GlobalWindowManager(
                @GlobalConfig final AggregateConfigProvider globalConfig,
                final TabCompleterFactory tabCompleterFactory,
                final Provider<WindowManager> windowManagerProvider,
                final Provider<MessageSinkManager> messageSinkManagerProvider,
                final Provider<GlobalCommandParser> globalCommandParserProvider,
                final URLBuilder urlBuilder) {
            this.globalConfig = globalConfig;
            this.tabCompleterFactory = tabCompleterFactory;
            this.windowManagerProvider = windowManagerProvider;
            this.messageSinkManagerProvider = messageSinkManagerProvider;
            this.globalCommandParserProvider = globalCommandParserProvider;
            this.urlBuilder = urlBuilder;
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
            synchronized (GlobalWindow.class) {
                if (globalConfig.getOptionBool("general", "showglobalwindow")) {
                    if (globalWindow == null) {
                        globalWindow = new GlobalWindow(globalConfig,
                                globalCommandParserProvider.get(),
                                tabCompleterFactory,
                                messageSinkManagerProvider.get(),
                                urlBuilder);
                        addCloseListener(globalWindow);
                        windowManagerProvider.get().addWindow(globalWindow);
                    }
                } else {
                    if (globalWindow != null) {
                        globalWindow.close();
                    }
                }
            }
        }

        /**
         * Adds a {@link FrameCloseListener} to the specified window to update the global window
         * state if the user closes it from the UI.
         */
        private void addCloseListener(final GlobalWindow window) {
            window.addCloseListener(new FrameCloseListener() {
                @Override
                public void windowClosing(final FrameContainer container) {
                    synchronized (GlobalWindow.class) {
                        globalWindow = null;
                    }
                }
            });
        }

    }

}
