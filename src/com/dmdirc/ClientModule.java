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
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.aliases.AliasesModule;
import com.dmdirc.commandparser.auto.AutoCommandModule;
import com.dmdirc.commandparser.commands.CommandModule;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.ServerFactory;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.Logger;
import com.dmdirc.messages.MessagesModule;
import com.dmdirc.plugins.CorePluginExtractor;
import com.dmdirc.plugins.CorePluginHelper;
import com.dmdirc.plugins.LegacyServiceLocator;
import com.dmdirc.plugins.PluginInjectorInitialiser;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.ServiceLocator;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WarningDialog;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.updater.UpdaterModule;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.DynamicAsyncEventBus;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.io.Downloader;

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
        includes = {
            AliasesModule.class,
            AutoCommandModule.class,
            CommandLineOptionsModule.class,
            CommandModule.class,
            MessagesModule.class,
            UpdaterModule.class
        },
        library = true)
public class ClientModule {

    /** Qualifier that identities a global configuration source. */
    @Qualifier
    public @interface GlobalConfig {
    }

    /** Qualifier that identities the user settings config provider. */
    @Qualifier
    public @interface UserConfig {
    }

    /** Qualifier that identities the addon defaults config provider. */
    @Qualifier
    public @interface AddonConfig {
    }

    /** The object graph to inject where necessary. */
    private ObjectGraph objectGraph;

    @Provides
    @Singleton
    public IdentityManager getIdentityManager(
            @Directory(DirectoryType.BASE) final String baseDirectory,
            @Directory(DirectoryType.IDENTITIES) final String identitiesDirectory,
            @Directory(DirectoryType.ERRORS) final String errorsDirectory,
            final CommandLineParser commandLineParser,
            final EventBus eventBus) {
        final IdentityManager identityManager = new IdentityManager(baseDirectory,
                identitiesDirectory, eventBus);
        ErrorManager.getErrorManager()
                .initialise(identityManager.getGlobalConfiguration(), errorsDirectory, eventBus);
        identityManager.loadVersionIdentity();

        try {
            identityManager.initialise();
        } catch (InvalidIdentityFileException ex) {
            handleInvalidConfigFile(identityManager, baseDirectory);
        }

        if (commandLineParser.getDisableReporting()) {
            identityManager.getUserSettings().setOption("temp", "noerrorreporting", true);
        }

        return identityManager;
    }

    @Provides
    public IdentityController getIdentityController(final IdentityManager manager) {
        return manager;
    }

    @Provides
    @GlobalConfig
    public AggregateConfigProvider getGlobalConfig(final IdentityController controller) {
        return controller.getGlobalConfiguration();
    }

    @Provides
    @GlobalConfig
    @Singleton
    public IconManager getGlobalIconManager(
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final URLBuilder urlBuilder) {
        return new IconManager(globalConfig, urlBuilder);
    }

    @Provides
    @UserConfig
    public ConfigProvider getUserConfig(final IdentityController controller) {
        return controller.getUserSettings();
    }

    @Provides
    @AddonConfig
    public ConfigProvider getAddonConfig(final IdentityController controller) {
        return controller.getAddonSettings();
    }

    @Provides
    @Singleton
    public ActionManager getActionManager(
            final IdentityController identityController,
            final ActionFactory actionFactory,
            final Provider<Set<ActionGroup>> actionWrappersProvider,
            final Provider<UpdateManager> updateManagerProvider,
            final EventBus eventBus,
            @Directory(DirectoryType.ACTIONS) final String directory) {
        final ActionManager actionManager = new ActionManager(identityController,
                actionFactory, actionWrappersProvider, updateManagerProvider, eventBus, directory);
        ActionManager.setActionManager(actionManager);
        return actionManager;
    }

    @Provides
    public ActionController getActionController(final ActionManager actionManager) {
        return actionManager;
    }

    @Provides
    public LifecycleController getLifecycleController(final SystemLifecycleController controller) {
        return controller;
    }

    @Provides
    @Singleton
    public CommandManager getCommandManager(
            final ServerManager serverManager,
            @GlobalConfig final AggregateConfigProvider globalConfig) {
        final CommandManager manager = new CommandManager(serverManager);
        manager.initialise(globalConfig);
        return manager;
    }

    @Provides
    public CommandController getCommandController(final CommandManager commandManager) {
        return commandManager;
    }

    @Provides
    @Singleton
    public PluginManager getPluginManager(
            final EventBus eventBus,
            final IdentityController identityController,
            final UpdateManager updateManager,
            final Provider<PluginInjectorInitialiser> initialiserProvider,
            final ObjectGraph objectGraph,
            final CorePluginHelper pluginHelper,
            @Directory(DirectoryType.PLUGINS) final String directory) {
        final PluginManager manager = new PluginManager(eventBus, identityController,
                updateManager, initialiserProvider, objectGraph, directory);
        final CorePluginExtractor extractor = new CorePluginExtractor(manager, directory, eventBus);
        pluginHelper.checkBundledPlugins(extractor, manager,
                identityController.getGlobalConfiguration());

        for (String service : new String[]{"ui", "tabcompletion", "parser"}) {
            pluginHelper.ensureExists(extractor, manager, service);
        }

        // The user may have an existing parser plugin (e.g. twitter) which
        // will satisfy the service existence check above, but will render the
        // client pretty useless, so we'll force IRC extraction for now.
        extractor.extractCorePlugins("parser_irc");
        manager.refreshPlugins();
        return manager;
    }

    @Provides
    public ServiceManager getServiceManager(final PluginManager pluginManager) {
        return pluginManager;
    }

    @Provides
    @Singleton
    public ThemeManager getThemeManager(
            final EventBus eventBus,
            final IdentityController controller,
            @Directory(DirectoryType.THEMES) final String directory) {
        final ThemeManager manager = new ThemeManager(eventBus, controller, directory);
        manager.refreshAndLoadThemes();
        return manager;
    }

    @Provides(type = Provides.Type.SET)
    @Singleton
    public ActionGroup getPerformWrapper(final PerformWrapper wrapper) {
        return wrapper;
    }

    @Provides
    @Singleton
    public ColourManager getColourManager(@GlobalConfig final AggregateConfigProvider globalConfig) {
        return new ColourManager(globalConfig);
    }

    @Provides
    public ServerFactory getServerFactory(final ServerManager serverManager) {
        return serverManager;
    }

    @Provides
    public IdentityFactory getIdentityFactory(final IdentityManager identityManager) {
        return identityManager;
    }

    @Provides
    public ServiceLocator getServiceLocator(final LegacyServiceLocator locator) {
        return locator;
    }

    @Provides
    @Singleton
    public EventBus getEventBus() {
        return new DynamicAsyncEventBus(Executors.newSingleThreadExecutor());
    }

    @Provides
    public Downloader getDownloader() {
        return new Downloader();
    }

    /**
     * Sets the object graph that will be injected. Must be called before any provider method.
     *
     * @param objectGraph The object graph to inject.
     */
    public void setObjectGraph(final ObjectGraph objectGraph) {
        this.objectGraph = objectGraph;
    }

    @Provides
    @Singleton
    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    /**
     * Called when the global config cannot be loaded due to an error. This method informs the user
     * of the problem and installs a new default config file, backing up the old one.
     *
     * @param identityManager The identity manager to re-initialise after installing defaults.
     * @param configdir       The directory to extract default settings into.
     */
    private void handleInvalidConfigFile(final IdentityManager identityManager,
            final String configdir) {
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

}
