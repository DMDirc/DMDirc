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

import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandLoader;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.logger.DMDircExceptionHandler;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceProvider;
import com.dmdirc.ui.WarningDialog;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.Version;
import com.dmdirc.util.resourcemanager.ResourceManager;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Main class, handles initialisation.
 */
public class Main implements LifecycleController {

    /** Feedback nag delay. */
    private final int FEEDBACK_DELAY = 30 * 60 * 1000;

    /** The UI to use for the client. */
    private final Collection<UIController> CONTROLLERS = new HashSet<>();

    /** The identity manager the client will use. */
    private final IdentityManager identityManager;

    /** The server manager the client will use. */
    private final ServerManager serverManager;

    /** The action manager the client will use. */
    private final ActionManager actionManager;

    /** The command-line parser used for this instance. */
    private final CommandLineParser commandLineParser;

    /** The plugin manager the client will use. */
    private final PluginManager pluginManager;

    /** The command manager the client will use. */
    private final CommandManager commandManager;

    /** The command loader to use to initialise the command manager. */
    private final CommandLoader commandLoader;

    /** The config dir to use for the client. */
    private final String configdir;

    /** Instance of main, protected to allow subclasses direct access. */
    @Deprecated
    public static Main mainInstance;

    /**
     * Creates a new instance of {@link Main}.
     *
     * @param identityManager The identity manager the client will use.
     * @param serverManager The server manager the client will use.
     * @param actionManager The action manager the client will use.
     * @param commandLineParser The command-line parser used for this instance.
     * @param pluginManager The plugin manager the client will use.
     * @param commandManager The command manager the client will use.
     * @param commandLoader The command loader to use to initialise the command manager.
     * @param configDir The base configuration directory to use.
     */
    @Inject
    public Main(
            final IdentityManager identityManager,
            final ServerManager serverManager,
            final ActionManager actionManager,
            final CommandLineParser commandLineParser,
            final PluginManager pluginManager,
            final CommandManager commandManager,
            final CommandLoader commandLoader,
            @Directory(DirectoryType.BASE) final String configDir) {
        this.identityManager = identityManager;
        this.serverManager = serverManager;
        this.actionManager = actionManager;
        this.commandLineParser = commandLineParser;
        this.pluginManager = pluginManager;
        this.commandManager = commandManager;
        this.commandLoader = commandLoader;
        this.configdir = configDir;
    }

    /**
     * Entry procedure.
     *
     * @param args the command line arguments
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public static void main(final String[] args) {
        try {
            ObjectGraph graph = ObjectGraph.create(
                    new ClientModule(),
                    new CommandLineOptionsModule(new CommandLineParser(args)));
            mainInstance = graph.get(Main.class);
            mainInstance.init();
        } catch (Throwable ex) {
            Logger.appError(ErrorLevel.FATAL, "Exception while initialising",
                    ex);
        }
    }

    /**
     * Initialises the client.
     *
     * @param args The command line arguments
     */
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(new DMDircExceptionHandler());

        try {
            identityManager.initialise(configdir);
        } catch (InvalidIdentityFileException iife) {
            handleInvalidConfigFile();
        }

        UpdateChecker.init(this);

        MessageSinkManager.getManager().loadDefaultSinks();

        pluginManager.refreshPlugins();
        checkBundledPlugins(pluginManager, identityManager.getGlobalConfiguration());

        ThemeManager.loadThemes();

        commandLineParser.applySettings(identityManager.getGlobalConfigIdentity());

        commandManager.initialise(identityManager.getGlobalConfiguration());
        CommandManager.setCommandManager(commandManager);
        commandLoader.loadCommands(commandManager);

        for (String service : new String[]{"ui", "tabcompletion", "parser"}) {
            ensureExists(pluginManager, service);
        }

        // The user may have an existing parser plugin (e.g. twitter) which
        // will satisfy the service existance check above, but will render the
        // client pretty useless, so we'll force IRC extraction for now.
        extractCorePlugins("parser_irc");
        pluginManager.refreshPlugins();

        loadUIs(pluginManager);

        doFirstRun();

        actionManager.initialise();
        pluginManager.doAutoLoad();
        actionManager.loadUserActions();
        actionManager.triggerEvent(CoreActionType.CLIENT_OPENED, null);

        commandLineParser.processArguments(serverManager);

        GlobalWindow.init();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                actionManager.triggerEvent(CoreActionType.CLIENT_CLOSED, null);
                serverManager.disconnectAll("Unexpected shutdown");
                identityManager.saveAll();
            }
        }, "Shutdown thread"));
    }

    /**
     * Get the plugin manager for this instance of main.
     *
     * @return PluginManager in use.
     * @Deprecated Global state is bad.
     */
    @Deprecated
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Get our ServerManager
     *
     * @return ServerManager controlled by this Main.
     * @Deprecated Global state is bad.
     */
    @Deprecated
    public ServerManager getServerManager() {
        return serverManager;
    }

    /**
     * Called when the UI has failed to initialise correctly. This method
     * attempts to extract any and all UI plugins bundled with the client, and
     * requests a restart. If this has already been attempted, it shows an error
     * and exits.
     */
    private void handleMissingUI() {
        // Check to see if we have already tried this
        if (IdentityManager.getIdentityManager().getGlobalConfiguration().hasOptionBool("debug", "uiFixAttempted")) {
            System.out.println("DMDirc is unable to load any compatible UI plugins.");
            if (!GraphicsEnvironment.isHeadless()) {
                new WarningDialog(WarningDialog.NO_COMPAT_UIS_TITLE,
                        WarningDialog.NO_RECOV_UIS).displayBlocking();
            }
            IdentityManager.getIdentityManager().getGlobalConfigIdentity().unsetOption("debug", "uiFixAttempted");
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
            IdentityManager.getIdentityManager().getGlobalConfigIdentity()
                    .setOption("debug", "uiFixAttempted", "true");
            // Tell the launcher to restart!
            System.exit(42);
        }
    }

    /**
     * Called when the global config cannot be loaded due to an error. This
     * method informs the user of the problem and installs a new default config
     * file, backing up the old one.
     */
    private void handleInvalidConfigFile() {
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
        System.out.println(message.replace("<br>", "\n"));

        final File configFile = new File(configdir + "dmdirc.config");
        final File newConfigFile = new File(configdir + "dmdirc.config." + date);

        if (configFile.renameTo(newConfigFile)) {
            try {
                identityManager.initialise(configdir);
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
            System.out.println(newMessage.replace("<br>", "\n"));
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
    public void ensureExists(final PluginManager pm, final String serviceType) {
        if (pm.getServicesByType(serviceType).isEmpty()) {
            extractCorePlugins(serviceType + "_");
            pm.refreshPlugins();
        }
    }

    /**
     * Checks whether the plugins bundled with this release of DMDirc are newer
     * than the plugins known by the specified {@link PluginManager}. If the
     * bundled plugins are newer, they are automatically extracted.
     *
     * @param pm The plugin manager to use to check plugins
     * @param config The configuration source for bundled versions
     */
    private void checkBundledPlugins(final PluginManager pm, final ConfigManager config) {
        for (PluginInfo plugin : pm.getPluginInfos()) {
            if (config.hasOptionString("bundledplugins_versions", plugin.getMetaData().getName())) {
                final Version bundled = new Version(config.getOption("bundledplugins_versions",
                        plugin.getMetaData().getName()));
                final Version installed = plugin.getMetaData().getVersion();

                if (installed.compareTo(bundled) < 0) {
                    extractCorePlugins(plugin.getMetaData().getName());
                    pm.reloadPlugin(plugin.getFilename());
                }
            }
        }
    }

    /**
     * Attempts to find and activate a service which provides a UI that we
     * can use.
     *
     * @param pm The plugin manager to use to load plugins
     */
    protected void loadUIs(final PluginManager pm) {
        final List<Service> uis = pm.getServicesByType("ui");

        // First try: go for our desired service type
        for (Service service : uis) {
            if (service.activate()) {
                final ServiceProvider provider = service.getActiveProvider();

                final Object export = provider.getExportedService("getController").execute();

                if (export != null) {
                    CONTROLLERS.add((UIController) export);
                }
            }
        }

        if (CONTROLLERS.isEmpty()) {
            handleMissingUI();
        } else {
            // The fix worked!
            if (IdentityManager.getIdentityManager().getGlobalConfiguration()
                    .hasOptionBool("debug", "uiFixAttempted")) {
                IdentityManager.getIdentityManager().getGlobalConfigIdentity()
                        .unsetOption("debug", "uiFixAttempted");
            }
        }
    }

    /**
     * Executes the first run or migration wizards as required.
     */
    private void doFirstRun() {
        if (IdentityManager.getIdentityManager().getGlobalConfiguration().getOptionBool("general", "firstRun")) {
            IdentityManager.getIdentityManager().getGlobalConfigIdentity().setOption("general", "firstRun", "false");
            for (UIController controller : CONTROLLERS) {
                controller.showFirstRunWizard();
            }

            new Timer().schedule(new TimerTask() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    for (UIController controller : CONTROLLERS) {
                        controller.showFeedbackNag();
                    }
                }
            }, FEEDBACK_DELAY);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use a proper {@link LifecycleController}.
     */
    @Override
    @Deprecated
    public void quit() {
        quit(0);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use a proper {@link LifecycleController}.
     */
    @Override
    @Deprecated
    public void quit(final int exitCode) {
        quit(identityManager.getGlobalConfiguration().getOption("general",
                "closemessage"), exitCode);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use a proper {@link LifecycleController}.
     */
    @Override
    @Deprecated
    public void quit(final String reason) {
        quit(reason, 0);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use a proper {@link LifecycleController}.
     */
    @Override
    @Deprecated
    public void quit(final String reason, final int exitCode) {
        serverManager.disconnectAll(reason);

        System.exit(exitCode);
    }

    /**
     * Retrieves the UI controller that's being used by the client.
     *
     * @return The client's UI controller
     * @deprecated Shouldn't be used. There may be multiple or no controllers.
     */
    @Deprecated
    public UIController getUI() {
        return CONTROLLERS.iterator().next();
    }

    /**
     * Extracts plugins bundled with DMDirc to the user's profile's plugin
     * directory.
     *
     * @param prefix If non-null, only plugins whose file name starts with
     * this prefix will be extracted.
     */
    public void extractCorePlugins(final String prefix) {
        final Map<String, byte[]> resources = ResourceManager.getResourceManager()
                .getResourcesStartingWithAsBytes("plugins");
        for (Map.Entry<String, byte[]> resource : resources.entrySet()) {
            try {
                final String resourceName = configdir + "plugins"
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

                    final PluginInfo plugin = pluginManager.getPluginInfo(newFile
                            .getAbsolutePath().substring(pluginManager.getDirectory().length()));

                    if (plugin != null) {
                        plugin.pluginUpdated();
                    }
                }
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW, "Failed to extract plugins", ex);
            }
        }
    }

}
