/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompletionType;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * A window which can be used to execute global commands.
 *
 * @author chris
 */
public class GlobalWindow extends WritableFrameContainer {

    /** The window we're using. */
    private final InputWindow window;

    /** The global window that's in use, if any. */
    private static GlobalWindow globalWindow;

    /** The tab completer we use. */
    private final TabCompleter tabCompleter;

    /** Creates a new instance of GlobalWindow. */
    public GlobalWindow() {
        super("icon", "Global", IdentityManager.getGlobalConfig());

        tabCompleter = new TabCompleter();
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                CommandManager.getCommandNames(CommandType.TYPE_GLOBAL));
        tabCompleter.addEntries(TabCompletionType.COMMAND,
                AliasWrapper.getAliasWrapper().getAliases());

        window = Main.getUI().getInputWindow(this, GlobalCommandParser.getGlobalCommandParser());

        window.setTitle("(Global)");
        window.getInputHandler().setTabCompleter(tabCompleter);

        WindowManager.addWindow(window);

        window.open();
    }

    /** {@inheritDoc} */
    @Override
    public InputWindow getFrame() {
        return window;
    }
    
    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 1: Make the window non-visible
        window.setVisible(false);

        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing

        // 6: Remove the window from the window manager
        WindowManager.removeWindow(window);

        // 7: Remove any references to the window and parents
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void sendLine(final String line) {
        if (!line.isEmpty()) {
            GlobalCommandParser.getGlobalCommandParser().parseCommand(window,
                    CommandManager.getCommandChar() + line);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return -1;
    }

    /**
     * Retrieves the tab completer used by this global window.
     *
     * @return This global window's tab completer.
     */
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }
    
    /**
     * Initialises the global window if it's enabled in the config.
     */
    public static void init() {
        IdentityManager.getGlobalConfig().addChangeListener("general", "showglobalwindow",
                new ConfigChangeListener() {

            @Override
            public void configChanged(final String domain, final String key) {
                updateWindowState();
            }
        });

        updateWindowState();
    }

    protected static void updateWindowState() {
        synchronized (GlobalWindow.class) {
            if (IdentityManager.getGlobalConfig().getOptionBool("general", "showglobalwindow")) {
                if (globalWindow == null) {
                    globalWindow = new GlobalWindow();
                }
            } else {
                if (globalWindow != null) {
                    globalWindow.close();
                }
            }
        }
    }

    /**
     * Retrieves the global window if it has been previously opened by
     * {@link #init()}.
     *
     * @return A Global Window instance or null
     */
    public static GlobalWindow getGlobalWindow() {
        return globalWindow;
    }

}
