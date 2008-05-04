/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.DMDircExceptionHandler;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.ui.dummy.DummyController;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.swing.SwingController;
import com.dmdirc.updater.UpdateChannel;
import com.dmdirc.updater.UpdateChecker;

import java.awt.GraphicsEnvironment;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class, handles initialisation.
 *
 * @author chris
 */
public final class Main {

    /** Stores the current textual program version. */
    public static final String VERSION = "SVN";

    /** The SVN revision that this build was made from. */
    public static final int SVN_REVISION = 4007; // 4007; // 4007; // 4007; // 4007; // 4007; // 4007; // 4007; // 3999;

    /** Stores the update channel that this version came from, if any. */
    public static final UpdateChannel UPDATE_CHANNEL = UpdateChannel.NONE;

    /** Feedback nag delay. */
    private static final int FEEDBACK_DELAY = 30 * 60 * 1000;

    /** The UI to use for the client. */
    private static UIController controller;

    /** The config dir to use for the client. */
    private static String configdir;

    /**
     * Prevents creation of main.
     */
    private Main() {
    }

    /**
     * Entry procedure.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        try {
            init(args);
        } catch (Throwable ex) {
            Logger.appError(ErrorLevel.FATAL, "Exception while "
                    + "initialising", ex);
        }
    }
    
    /**
     * Initialises the client.
     * 
     * @param args The command line arguments
     */
    private static void init(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DMDircExceptionHandler());

        final CommandLineParser clp = new CommandLineParser(args);

        IdentityManager.load();
        
        ThemeManager.loadThemes();

        clp.applySettings();

        CommandManager.initCommands();

        getUI().initUISettings();

        doFirstRun();

        ActionManager.init();

        PluginManager.getPluginManager();

        ActionManager.loadActions();

        getUI().getMainWindow();

        ActionManager.processEvent(CoreActionType.CLIENT_OPENED, null);

        UpdateChecker.init();

        clp.processArguments();

        GlobalWindow.init();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                ActionManager.processEvent(CoreActionType.CLIENT_CLOSED, null);
                ServerManager.getServerManager().disconnectAll("Unexpected shutdown");
                IdentityManager.save();
            }
        }, "Shutdown thread"));        
    }
    
    /**
     * Executes the first run or migration wizards as required.
     */
    private static void doFirstRun() {
        if (IdentityManager.getGlobalConfig().getOptionBool("general", "firstRun", true)) {
            IdentityManager.getConfigIdentity().setOption("general", "firstRun", "false");
            getUI().showFirstRunWizard();
            new Timer().schedule(new TimerTask() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    getUI().showFeedbackNag();
                }
            }, FEEDBACK_DELAY);
        } else if (IdentityManager.getGlobalConfig().hasOption("general", "addonrevision")) {
            // @Deprecated - can be removed after 0.6 is released
            IdentityManager.getConfigIdentity().unsetOption("general", "addonrevision");
            getUI().showMigrationWizard();
        }        
    }

    /**
     * Quits the client nicely, with the default closing message.
     */
    public static void quit() {
        quit(IdentityManager.getGlobalConfig().getOption("general", "closemessage"));
    }

    /**
     * Quits the client nicely.
     *
     * @param reason The quit reason to send
     */
    public static void quit(final String reason) {
        ServerManager.getServerManager().disconnectAll(reason);

        System.exit(0);
    }

    /**
     * Retrieves the UI controller that's being used by the client.
     *
     * @return The client's UI controller
     */
    public static UIController getUI() {
        if (controller == null) {
            if (GraphicsEnvironment.isHeadless()) {
                controller = new DummyController();
            } else {
                controller = new SwingController();
            }
        }

        return controller;
    }

    /**
     * Sets the UI controller that should be used by this client.
     *
     * @param newController The new UI Controller
     */
    public static void setUI(final UIController newController) {
        controller = newController;
    }

    /**
     * Returns the application's config directory.
     *
     * @return configuration directory
     */
    public static String getConfigDir() {
        if (configdir == null) {
            final String fs = System.getProperty("file.separator");
            final String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS")) {
                configdir = System.getProperty("user.home") + fs + "Library"
                        + fs + "Preferences" + fs + "DMDirc" + fs;
            } else if (osName.startsWith("Windows")) {
                if (System.getenv("APPDATA") == null) {
                    configdir = System.getProperty("user.home") + fs + "DMDirc" + fs;
                } else {
                    configdir = System.getenv("APPDATA") + fs + "DMDirc" + fs;
                }
            } else {
                configdir = System.getProperty("user.home") + fs + ".DMDirc" + fs;
            }
        }

        return configdir;
    }

    /**
     * Sets the config directory for this client.
     *
     * @param newdir The new configuration directory
     */
    public static void setConfigDir(final String newdir) {
        configdir = newdir;
    }

}
