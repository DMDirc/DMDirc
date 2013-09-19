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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.IdentityController;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.WarningDialog;
import com.dmdirc.ui.core.components.StatusBarManager;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides dependencies for the client.
 */
@Module(injects = Main.class, includes = CommandLineOptionsModule.class)
public class ClientModule {

    /**
     * Provides an identity manager for the client.
     *
     * @param directory The directory to load settings from.
     * @return An initialised {@link IdentityManager}.
     */
    @Provides
    @Singleton
    public IdentityManager getIdentityManager(
            @Directory(DirectoryType.BASE) final String directory) {
        final IdentityManager identityManager = new IdentityManager(directory);
        IdentityManager.setIdentityManager(identityManager);
        identityManager.loadVersionIdentity();

        try {
            identityManager.initialise();
        } catch (InvalidIdentityFileException ex) {
            handleInvalidConfigFile(identityManager, directory);
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
     * Provides a parser factory.
     *
     * @param pluginManager The plugin manager to use to find parsers.
     * @return A parser factory for use in the client.
     */
    @Provides
    public ParserFactory getParserFactory(final PluginManager pluginManager) {
        return new ParserFactory(pluginManager);
    }

    /**
     * Provides an action manager.
     *
     * @param serverManager The server manager to use to iterate servers.
     * @param identityController The identity controller to use to look up settings.
     * @return An unitialised action manager.
     */
    @Provides
    @Singleton
    public ActionManager getActionManager(final ServerManager serverManager, final IdentityController identityController) {
        final ActionManager actionManager = new ActionManager(serverManager, identityController);
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
     * Provides a status bar manager.
     *
     * @return The status bar manager the client should use.
     */
    @Provides
    @Singleton
    public StatusBarManager getStatusBarManager() {
        final StatusBarManager manager = new StatusBarManager();
        StatusBarManager.setStatusBarManager(manager);
        return manager;
    }

    /**
     * Gets the message sink manager for the client.
     *
     * @param statusBarManager The status bar manager to use for status bar sinks.
     * @return The message sink manager the client should use.
     */
    @Provides
    @Singleton
    public MessageSinkManager getMessageSinkManager(final StatusBarManager statusBarManager) {
        final MessageSinkManager messageSinkManager = new MessageSinkManager();
        MessageSinkManager.setManager(messageSinkManager);
        messageSinkManager.loadDefaultSinks(statusBarManager);
        return messageSinkManager;
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

}
