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

import com.dmdirc.actions.ActionFactory;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.CommandModule;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.ServerFactory;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginInjectorInitialiser;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WarningDialog;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.updater.UpdaterModule;
import com.dmdirc.updater.Version;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.URLBuilder;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

/**
 * Provides dependencies for the client.
 */
@Module(
    injects = {Main.class, CommandLineParser.class},
    includes = {CommandLineOptionsModule.class, CommandModule.class, UpdaterModule.class},
    library = true
)
public class ClientModule {

    /** Qualifier that identities a global configuration source. */
    @Qualifier
    public @interface GlobalConfig {}

    /** Qualifier that identities the user settings config provider. */
    @Qualifier
    public @interface UserConfig {}

    /** The object graph to inject where necessary. */
    private ObjectGraph objectGraph;

    /**
     * Provides an identity manager for the client.
     *
     * @param baseDirectory The base directory to load settings from.
     * @param identitiesDirectory The directory to store and read identities in.
     * @param commandLineParser The CLI parser to read command line settings from.
     * @return An initialised {@link IdentityManager}.
     */
    @Provides
    @Singleton
    public IdentityManager getIdentityManager(
            @Directory(DirectoryType.BASE) final String baseDirectory,
            @Directory(DirectoryType.IDENTITIES) final String identitiesDirectory,
            final CommandLineParser commandLineParser) {
        final IdentityManager identityManager =
                new IdentityManager(baseDirectory, identitiesDirectory);
        IdentityManager.setIdentityManager(identityManager);
        identityManager.loadVersionIdentity();

        try {
            identityManager.initialise();
        } catch (InvalidIdentityFileException ex) {
            handleInvalidConfigFile(identityManager, baseDirectory);
        }

        if (commandLineParser.getDisableReporting()) {
            identityManager.getUserSettings()
                    .setOption("temp", "noerrorreporting", true);
        }

        return identityManager;
    }

    /**
     * Provides an identity controller.
     *
     * @param manager The identity manager to use as a controller.
     * @return An identity controller to use.
     */
    @Provides
    public IdentityController getIdentityController(final IdentityManager manager) {
        return manager;
    }

    /**
     * Provides a global config provider.
     *
     * @param controller The controller to retrieve the config from.
     * @return A global configuration provider.
     */
    @Provides
    @GlobalConfig
    public AggregateConfigProvider getGlobalConfig(final IdentityController controller) {
        return controller.getGlobalConfiguration();
    }

    /**
     * Provides an icon manager backed by the global configuration.
     *
     * @param globalConfig The global configuration provider.
     * @param urlBuilder The builder to use to construct icon URLs.
     * @return An icon manager backed by the global config.
     */
    @Provides
    @GlobalConfig
    @Singleton
    public IconManager getGlobalIconManager(
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final URLBuilder urlBuilder) {
        return new IconManager(globalConfig, urlBuilder);
    }

    /**
     * Provides the user's configuration provider.
     *
     * @param controller The controller to retrieve the config from.
     * @return The user's configuration provider.
     */
    @Provides
    @UserConfig
    public ConfigProvider getUserConfig(final IdentityController controller) {
        return controller.getUserSettings();
    }

    /**
     * Provides an action manager.
     *
     * @param serverManager The server manager to use to iterate servers.
     * @param identityController The identity controller to use to look up settings.
     * @param actionFactory The factory to use to create actions.
     * @param actionWrappersProvider Provider of action wrappers.
     * @param updateManagerProvider Provider of an update manager.
     * @param directory The directory to read and write actions in.
     * @return An unitialised action manager.
     */
    @Provides
    @Singleton
    public ActionManager getActionManager(
            final ServerManager serverManager,
            final IdentityController identityController,
            final ActionFactory actionFactory,
            final Provider<Set<ActionGroup>> actionWrappersProvider,
            final Provider<UpdateManager> updateManagerProvider,
            @Directory(DirectoryType.ACTIONS) final String directory) {
        final ActionManager actionManager = new ActionManager(serverManager, identityController,
                actionFactory, actionWrappersProvider, updateManagerProvider, directory);
        ActionManager.setActionManager(actionManager);
        return actionManager;
    }

    /**
     * Provides an action controller.
     *
     * @param actionManager The action manager to use as a controller.
     * @return An action controller to use.
     */
    @Provides
    public ActionController getActionController(final ActionManager actionManager) {
        return actionManager;
    }

    /**
     * Provides a lifecycle controller.
     *
     * @param controller The concrete implementation to use.
     * @return The lifecycle controller the app should use.
     */
    @Provides
    public LifecycleController getLifecycleController(final SystemLifecycleController controller) {
        return controller;
    }

    /**
     * Gets the message sink manager for the client.
     *
     * @param statusBarManager The status bar manager to use for status bar sinks.
     * @param windowManager The window manager to use for sinks that iterate windows.
     * @return The message sink manager the client should use.
     */
    @Provides
    @Singleton
    public MessageSinkManager getMessageSinkManager(
            final StatusBarManager statusBarManager,
            final WindowManager windowManager) {
        final MessageSinkManager messageSinkManager = new MessageSinkManager();
        messageSinkManager.loadDefaultSinks(statusBarManager, windowManager);
        return messageSinkManager;
    }

    /**
     * Gets the command manager the client should use.
     *
     * @param serverManager The manager to use to iterate servers.
     * @param globalConfig The global configuration provider to read settings from.
     * @return The command manager the client should use.
     */
    @Provides
    @Singleton
    public CommandManager getCommandManager(
            final ServerManager serverManager,
            @GlobalConfig final AggregateConfigProvider globalConfig) {
        final CommandManager manager = new CommandManager(serverManager);
        manager.initialise(globalConfig);
        return manager;
    }

    /**
     * Gets a command controller for use in the client.
     *
     * @param commandManager The manager to use as a controller.
     * @return The command controller the client should use.
     */
    @Provides
    public CommandController getCommandController(final CommandManager commandManager) {
        return commandManager;
    }

    /**
     * Gets an initialised plugin manager for the client.
     *
     * @param identityController The controller to read settings from.
     * @param actionController The action controller to use for events.
     * @param updateManager The update manager to inform about plugins.
     * @param initialiserProvider Provider to use to create plugin initialisers.
     * @param objectGraph The graph to provide to plugins for DI purposes.
     * @param directory The directory to load and save plugins in.
     * @return An initialised plugin manager for the client.
     */
    @Provides
    @Singleton
    public PluginManager getPluginManager(
            final IdentityController identityController,
            final ActionController actionController,
            final UpdateManager updateManager,
            final Provider<PluginInjectorInitialiser> initialiserProvider,
            final ObjectGraph objectGraph,
            @Directory(DirectoryType.PLUGINS) final String directory) {
        final PluginManager manager = new PluginManager(identityController,
                actionController, updateManager, initialiserProvider, objectGraph, directory);
        final CorePluginExtractor extractor = new CorePluginExtractor(manager, directory);
        checkBundledPlugins(extractor, manager, identityController.getGlobalConfiguration());

        for (String service : new String[]{"ui", "tabcompletion", "parser"}) {
            ensureExists(extractor, manager, service);
        }

        // The user may have an existing parser plugin (e.g. twitter) which
        // will satisfy the service existance check above, but will render the
        // client pretty useless, so we'll force IRC extraction for now.
        extractor.extractCorePlugins("parser_irc");
        manager.refreshPlugins();
        return manager;
    }

    /**
     * Provides a service manager.
     *
     * @param pluginManager The plugin manager to use as the service manager implementation.
     * @return A service manager.
     */
    @Provides
    public ServiceManager getServiceManager(final PluginManager pluginManager) {
        return pluginManager;
    }

    /**
     * Gets a core plugin extractor.
     *
     * @param pluginManager The plugin manager to notify about updates.
     * @param directory The directory to extract plugins to.
     * @return A plugin extractor for the client to use.
     */
    @Provides
    public CorePluginExtractor getCorePluginExtractor(
            final PluginManager pluginManager,
            @Directory(DirectoryType.PLUGINS) final String directory) {
        return new CorePluginExtractor(pluginManager, directory);
    }

    /**
     * Gets a theme manager for the client.
     *
     * @param controller The identity controller to use to access settings.
     * @param directory The directory to load themes from.
     * @return An initialised theme manager instance.
     */
    @Provides
    @Singleton
    public ThemeManager getThemeManager(
            final IdentityController controller,
            @Directory(DirectoryType.THEMES) final String directory) {
        final ThemeManager manager = new ThemeManager(controller, directory);
        manager.refreshAndLoadThemes();
        return manager;
    }

    /**
     * Gets the alias actions wrapper entry for the actions wrapper set.
     *
     * @param aliasWrapper The wrapper to use in the set.
     * @return An alias wrapper to use in the client.
     */
    @Provides(type = Provides.Type.SET)
    public ActionGroup getAliasWrapper(final AliasWrapper aliasWrapper) {
        return aliasWrapper;
    }

    /**
     * Gets the performs actions wrapper.
     *
     * @param wrapper Wrapper to return
     *
     * @return An performs wrapper to use in the client.
     */
    @Provides(type = Provides.Type.SET)
    @Singleton
    public ActionGroup getPerformWrapper(final PerformWrapper wrapper) {
        return wrapper;
    }

    /**
     * Gets the colour manager.
     *
     * @param globalConfig A global configuration provider to read settings from.
     * @return A colour manager for the client.
     */
    @Provides
    @Singleton
    public ColourManager getColourManager(@GlobalConfig final AggregateConfigProvider globalConfig) {
        return new ColourManager(globalConfig);
    }

    /**
     * Gets a preferences manager.
     *
     * @return A singleton preferences manager.
     */
    @Provides
    public PreferencesManager getPreferencesManager() {
        return PreferencesManager.getPreferencesManager();
    }

    /**
     * Gets a server factory.
     *
     * @param serverManager The manager to use as a factory.
     * @return A server factory.
     */
    @Provides
    public ServerFactory getServerFactory(final ServerManager serverManager) {
        return serverManager;
    }

    /**
     * Gets an identity factory.
     *
     * @param identityManager  The manager to use as a factory.
     * @return An identity factory.
     */
    @Provides
    public IdentityFactory getIdentityFactory(final IdentityManager identityManager) {
        return identityManager;
    }

    /**
     * Provides the event bus the client will use for dispatching events.
     *
     * @return An event bus for the client to use.
     */
    @Provides
    @Singleton
    public EventBus getEventBus() {
        return new AsyncEventBus(Executors.newFixedThreadPool(1));
    }

    /**
     * Sets the object graph that will be injected. Must be called before any provider method.
     *
     * @param objectGraph The object graph to inject.
     */
    public void setObjectGraph(final ObjectGraph objectGraph) {
        this.objectGraph = objectGraph;
    }

    /**
     * Provides an object graph for future dependency injection.
     *
     * @return An object graph to use.
     */
    @Provides
    @Singleton
    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    /**
     * Called when the global config cannot be loaded due to an error. This
     * method informs the user of the problem and installs a new default config
     * file, backing up the old one.
     *
     * @param identityManager The identity manager to re-initialise after installing defaults.
     * @param configdir The directory to extract default settings into.
     */
    private void handleInvalidConfigFile(final IdentityManager identityManager, final String configdir) {
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
                identityManager.initialise();
            } catch (InvalidIdentityFileException iife) {
                // This shouldn't happen!
                Logger.appError(ErrorLevel.FATAL, "Unable to load global config", iife);
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
     * @param corePluginExtractor Extractor to use if the service doesn't exist
     * @param pm The plugin manager to use to access services
     * @param serviceType The type of service that should exist
     */
    public void ensureExists(
            final CorePluginExtractor corePluginExtractor,
            final PluginManager pm,
            final String serviceType) {
        if (pm.getServicesByType(serviceType).isEmpty()) {
            corePluginExtractor.extractCorePlugins(serviceType + "_");
            pm.refreshPlugins();
        }
    }

    /**
     * Checks whether the plugins bundled with this release of DMDirc are newer
     * than the plugins known by the specified {@link PluginManager}. If the
     * bundled plugins are newer, they are automatically extracted.
     *
     * @param corePluginExtractor Extractor to use if plugins need updating.
     * @param pm The plugin manager to use to check plugins
     * @param config The configuration source for bundled versions
     */
    private void checkBundledPlugins(
            final CorePluginExtractor corePluginExtractor,
            final PluginManager pm,
            final AggregateConfigProvider config) {
        for (PluginInfo plugin : pm.getPluginInfos()) {
            if (config.hasOptionString("bundledplugins_versions", plugin.getMetaData().getName())) {
                final Version bundled = new Version(config.getOption("bundledplugins_versions",
                        plugin.getMetaData().getName()));
                final Version installed = plugin.getMetaData().getVersion();

                if (installed.compareTo(bundled) < 0) {
                    corePluginExtractor.extractCorePlugins(plugin.getMetaData().getName());
                    pm.reloadPlugin(plugin.getFilename());
                }
            }
        }
    }

}
