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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.aliases.AliasesModule;
import com.dmdirc.commandparser.auto.AutoCommandModule;
import com.dmdirc.commandparser.commands.CommandModule;
import com.dmdirc.config.ConfigModule;
import com.dmdirc.config.profiles.ProfilesModule;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ConnectionFactory;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.plugins.PluginModule;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.ColourManagerFactory;
import com.dmdirc.ui.messages.UiMessagesModule;
import com.dmdirc.ui.messages.sink.MessagesModule;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.updater.UpdaterModule;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.io.Downloader;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

/**
 * Provides dependencies for the client.
 */
@SuppressWarnings("TypeMayBeWeakened")
@Module(
        injects = {Main.class, CommandLineParser.class},
        includes = {
                AliasesModule.class,
                AutoCommandModule.class,
                CommandLineOptionsModule.class,
                CommandModule.class,
                ConfigModule.class,
                MessagesModule.class,
                PluginModule.class,
                ProfilesModule.class,
                UiMessagesModule.class,
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
    public ConnectionManager getConnectionManager(final ServerManager serverManager) {
        return serverManager;
    }

    @Provides
    @Singleton
    public DMDircMBassador getMBassador() {
        return new DMDircMBassador();
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
            final ConnectionManager connectionManager,
            final Provider<GlobalWindow> globalWindowProvider,
            @GlobalConfig final AggregateConfigProvider globalConfig) {
        final CommandManager manager = new CommandManager(connectionManager, globalWindowProvider);
        manager.initialise(globalConfig);
        return manager;
    }

    @Provides
    public CommandController getCommandController(final CommandManager commandManager) {
        return commandManager;
    }

    @Provides
    @Singleton
    public ThemeManager getThemeManager(
            final DMDircMBassador eventBus,
            final IdentityController controller,
            @Directory(DirectoryType.THEMES) final String directory) {
        final ThemeManager manager = new ThemeManager(eventBus, controller, directory);
        manager.refreshAndLoadThemes();
        return manager;
    }

    @Provides
    @Singleton
    @GlobalConfig
    public ColourManager getGlobalColourManager(final ColourManagerFactory colourManagerFactory,
            @GlobalConfig final  AggregateConfigProvider globalConfig) {
        return colourManagerFactory.getColourManager(globalConfig);
    }

    @Provides
    public ConnectionFactory getServerFactory(final ConnectionManager connectionManager) {
        return connectionManager;
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

}
