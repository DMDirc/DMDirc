/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.Arrays;

import lombok.Getter;

/**
 * A window which can be used to execute global commands.
 */
public class GlobalWindow extends WritableFrameContainer {

    /** The global window that's in use, if any. */
    @Getter
    private static GlobalWindow globalWindow;

    /** The tab completer we use. */
    @Getter
    private final TabCompleter tabCompleter;

    /**
     * Creates a new instance of GlobalWindow.
     *
     * @param config The ConfigManager to retrieve settings from.
     * @param parser The command parser to use to parse input.
     */
    public GlobalWindow(final ConfigManager config, final CommandParser parser) {
        super("icon", "Global", "(Global)",
                config, parser,
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier()));

        tabCompleter = new TabCompleter(config);
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                CommandManager.getCommandManager().getCommandNames(CommandType.TYPE_GLOBAL));
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                AliasWrapper.getAliasWrapper().getAliases());

        WindowManager.getWindowManager().addWindow(this);
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
        globalWindow = null;
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void sendLine(final String line) {
        addLine("commandError", "You may only enter commands in the global window.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return -1;
    }

    /**
     * Initialises the global window if it's enabled in the config.
     */
    public static void init() {
        IdentityManager.getIdentityManager().getGlobalConfiguration()
                .addChangeListener("general", "showglobalwindow",
                new ConfigChangeListener() {

            @Override
            public void configChanged(final String domain, final String key) {
                updateWindowState();
            }
        });

        updateWindowState();
    }

    /**
     * Updates the state of the global window in line with the
     * general.showglobalwindow config setting.
     */
    protected static void updateWindowState() {
        final ConfigManager configManager = IdentityManager.getIdentityManager()
                .getGlobalConfiguration();

        synchronized (GlobalWindow.class) {
            if (configManager.getOptionBool("general", "showglobalwindow")) {
                if (globalWindow == null) {
                    globalWindow = new GlobalWindow(configManager,
                            new GlobalCommandParser(configManager,
                            CommandManager.getCommandManager()));
                }
            } else {
                if (globalWindow != null) {
                    globalWindow.close();
                }
            }
        }
    }
}
