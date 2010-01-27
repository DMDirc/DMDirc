/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.logger.DMDircExceptionHandler;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.WarningDialog;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.Level;

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

        for (Handler handler : java.util.logging.Logger.getLogger("").getHandlers()) {
            handler.setLevel(Level.OFF); // Needs to be changed to enable debugging
        }

        // Enable finer debugging for specific components like so:
        //java.util.logging.Logger.getLogger("com.dmdirc.plugins").setLevel(Level.ALL);

        IdentityManager.loadVersion();

        final CommandLineParser clp = new CommandLineParser(args);

        try {
            IdentityManager.load();
        } catch (InvalidIdentityFileException iife) {
            handleInvalidConfigFile();
        }

        final PluginManager pm = PluginManager.getPluginManager();
        
        ThemeManager.loadThemes();

        clp.applySettings();

        CommandManager.initCommands();

        for (String service : new String[]{"ui", "tabcompletion"}) {
            ensureExists(pm, service);
        }

        loadUI(pm, IdentityManager.getGlobalConfig());
        if (getUI() == null) {
            handleMissingUI();
        } else {
            // The fix worked!
            if (IdentityManager.getGlobalConfig().hasOptionBool("debug", "uiFixAttempted")) {
                IdentityManager.getConfigIdentity().unsetOption("debug", "uiFixAttempted");
            }
        }

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
     * Called when the UI has failed to initialise correctly. This method
     * attempts to extract any and all UI plugins bundled with the client, and
     * requests a restart. If this has already been attempted, it shows an error
     * and exits.
     */
    private static void handleMissingUI() {
        // Check to see if we have already tried this
        if (IdentityManager.getGlobalConfig().hasOptionBool("debug", "uiFixAttempted")) {
            System.out.println("DMDirc is unable to load any compatible UI plugins.");
            if (!GraphicsEnvironment.isHeadless()) {
                new WarningDialog(WarningDialog.NO_COMPAT_UIS_TITLE,
                        WarningDialog.NO_RECOV_UIS).displayBlocking();
            }
            IdentityManager.getConfigIdentity().unsetOption("debug", "uiFixAttempted");
            System.exit(1);
        } else {
            // Try to extract the UIs again incase they changed between versions
            // and the user didn't update the UI plugin.
            extractCorePlugins("ui_");

            System.out.println("DMDirc has updated the UI plugins and needs to restart.");

            if (!GraphicsEnvironment.isHeadless()) {
                new WarningDialog(WarningDialog.NO_COMPAT_UIS_TITLE,
                        WarningDialog.NO_COMPAT_UIS_BODY).displayBlocking();
            }

            // Allow the rebooted DMDirc to know that we have attempted restarting.
            IdentityManager.getConfigIdentity().setOption("debug", "uiFixAttempted", "true");
            // Force the UI to swing to prevent problematic 3rd party UIs.
            IdentityManager.getConfigIdentity().setOption("general", "ui", "swing");
            // Tell the launcher to restart!
            System.exit(42);
        }
    }

    /**
     * Called when the global config cannot be loaded due to an error. This
     * method informs the user of the problem and installs a new default config
     * file, backing up the old one.
     */
    private static void handleInvalidConfigFile() {
        final String date = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());

        final String message = "DMDirc has detected that your config file "
                + "has become corrupted.<br><br>DMDirc will now backup "
                + "your current config and try restarting with a default "
                + "config.<br><br>Your old config will be saved as:<br>"
                + "dmdirc.config." + date;

        if (!GraphicsEnvironment.isHeadless()) {
            new WarningDialog("Invalid Config File", message).displayBlocking();
        }

        // Let command-line users know what is happening.
        System.out.println(message.replaceAll("<br>", "\n"));

        final File configFile = new File(getConfigDir() + "dmdirc.config");
        final File newConfigFile = new File(getConfigDir() + "dmdirc.config." + date);

        if (configFile.renameTo(newConfigFile)) {
            try {
                IdentityManager.load();
            } catch (InvalidIdentityFileException iife2) {
                // This shouldn't happen!
                Logger.appError(ErrorLevel.FATAL, "Unable to load global config", iife2);
            }
        } else {
            final String newMessage = "DMDirc was unable to rename the "
                    + "global config file and is unable to fix this issue.";
            if (!GraphicsEnvironment.isHeadless()) {
                new WarningDialog("Invalid Config File", newMessage).displayBlocking();
            }
            System.out.println(newMessage.replaceAll("<br>", "\n"));
            System.exit(1);
        }
    }

    /**
     * Ensures that there is at least one provider of the specified
     * service type by extracting matching core plugins. Plugins must be named
     * so that their file name starts with the service type, and then an
     * underscore.
     *
     * @param pm The plugin manager to use to access services
     * @param serviceType The type of service that should exist
     */
    public static void ensureExists(final PluginManager pm, final String serviceType) {
        if (pm.getServicesByType(serviceType).isEmpty()) {
            extractCorePlugins(serviceType + "_");
            pm.getPossiblePluginInfos(true);
        }
    }

    /**
     * Attempts to find and activate a service which provides a UI that we
     * can use.
     *
     * @param pm The plugin manager to use to load plugins
     * @param cm The config manager to use to retrieve settings
     */
    protected static void loadUI(final PluginManager pm, final ConfigManager cm) {
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

        if (!GraphicsEnvironment.isHeadless()) {
            // Show a dialog informing the user that no UI was found.
            new WarningDialog().displayBlocking();
            System.exit(2);
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
        quit(0);
    }
    
    /**
     * Quits the client nicely, with the default closing message.
     * 
     * @param exitCode This is the exit code that will be returned to the 
     *                  operating system when the client exits
     */
    public static void quit(final int exitCode) {
        quit(IdentityManager.getGlobalConfig().getOption("general", 
                "closemessage"), exitCode);
    }

    /**
     * Quits the client nicely.
     *
     * @param reason The quit reason to send
     */
    public static void quit(final String reason) {
        quit(reason, 0);
    }
    
    /**
     * Quits the client nicely.
     *
     * @param reason The quit reason to send
     * @param exitCode This is the exit code that will be returned to the 
     *                  operating system when the client exits
     */
    public static void quit(final String reason, final int exitCode) {
        ServerManager.getServerManager().disconnectAll(reason);

        System.exit(exitCode);
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
            initialiseConfigDir();
        }

        return configdir;
    }

    /**
     * Initialises the location of the configuration directory.
     */
    protected static void initialiseConfigDir() {
        final String fs = System.getProperty("file.separator");
        final String osName = System.getProperty("os.name");

        if (System.getenv("DMDIRC_HOME") != null) {
            configdir = System.getenv("DMDIRC_HOME");
        } else if (osName.startsWith("Mac OS")) {
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
            final File testFile = new File(configdir);
            if (!testFile.exists()) {
                final String configHome = System.getenv("XDG_CONFIG_HOME");
                configdir = (configHome == null || configHome.isEmpty()) ?
                    System.getProperty("user.home") + fs + ".config" + fs :
                    configHome;
                configdir += fs + "DMDirc" + fs;
            }
        }

        configdir = new File(configdir).getAbsolutePath() + fs;
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
