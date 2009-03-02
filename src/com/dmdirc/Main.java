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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.DMDircExceptionHandler;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.NoUIDialog;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class, handles initialisation.
 *
 * @author chris
 */
public final class Main {

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
            Logger.appError(ErrorLevel.FATAL, "Exception while initialising", ex);
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

        final PluginManager pm = PluginManager.getPluginManager();
        
        ThemeManager.loadThemes();

        clp.applySettings();

        CommandManager.initCommands();

        loadUI(pm, IdentityManager.getGlobalConfig(), true);

        getUI().initUISettings();

        doFirstRun();

        ActionManager.init();

        pm.doAutoLoad();

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
     * Attempts to find and activate a service which provides a UI that we
     * can use.
     *
     * @param pm The plugin manager to use to load plugins
     * @param cm The config manager to use to retrieve settings
     * @param tryExtracting If no suitable plugins are found and tryExtracting
     * is true, the method will try extracting core UI plugins bundled with
     * DMDirc before giving up.
     */
    protected static void loadUI(final PluginManager pm, final ConfigManager cm,
            final boolean tryExtracting) {
        final List<Service> uis = pm.getServicesByType("ui");
        final String desired = cm.getOption("general", "ui");

        // First try: go for our desired service type
        for (Service service : uis) {
            if (service.getName().equals(desired) && service.activate()) {
                return;
            }
        }

        // Second try: go for any service type
        for (Service service : uis) {
            if (service.activate()) {
                return;
            }
        }

        // Third try: extract some core plugins and go again
        if (tryExtracting) {
            extractCorePlugins("ui_");
            pm.getPossiblePluginInfos(true);

            loadUI(pm, cm, false);
            return;
        }

        if (!GraphicsEnvironment.isHeadless()) {
            // Show a dialog informing the user that no UI was found.
            NoUIDialog.displayBlocking();
        }

        // Can't find any
        throw new IllegalStateException("No UIs could be loaded");
    }
    
    /**
     * Executes the first run or migration wizards as required.
     */
    private static void doFirstRun() {
        if (IdentityManager.getGlobalConfig().getOptionBool("general", "firstRun")) {
            IdentityManager.getConfigIdentity().setOption("general", "firstRun", "false");
            getUI().showFirstRunWizard();
            new Timer().schedule(new TimerTask() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    getUI().showFeedbackNag();
                }
            }, FEEDBACK_DELAY);
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
        return controller;
    }

    /**
     * Sets the UI controller that should be used by this client.
     *
     * @param newController The new UI Controller
     */
    public static synchronized void setUI(final UIController newController) {
        if (controller == null) {
            controller = newController;
        } else {
            throw new IllegalStateException("User interface is already set");
        }
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

    /**
     * Extracts plugins bundled with DMDirc to the user's profile's plugin
     * directory.
     *
     * @param prefix If non-null, only plugins whose file name starts with
     * this prefix will be extracted.
     */
    public static void extractCorePlugins(final String prefix) {
        final Map<String, byte[]> resources = ResourceManager.getResourceManager()
                .getResourcesStartingWithAsBytes("plugins");
        for (Map.Entry<String, byte[]> resource : resources.entrySet()) {
            try {
                final String resourceName = Main.getConfigDir() + "plugins"
                        + resource.getKey().substring(7);

                if (prefix != null && !resource.getKey().substring(8).startsWith(prefix)) {
                    continue;
                }

                final File newDir = new File(resourceName.substring(0,
                        resourceName.lastIndexOf('/')) + "/");

                if (!newDir.exists()) {
                    newDir.mkdirs();
                }

                final File newFile = new File(newDir,
                        resourceName.substring(resourceName.lastIndexOf('/') + 1,
                        resourceName.length()));

                if (!newFile.isDirectory()) {
                    ResourceManager.getResourceManager().
                            resourceToFile(resource.getValue(), newFile);
                }
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW, "Failed to extract plugins", ex);
            }
        }
    }

}
