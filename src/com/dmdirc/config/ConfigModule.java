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

package com.dmdirc.config;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.ui.WarningDialog;
import com.dmdirc.util.ClientInfo;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.dmdirc.ClientModule.AddonConfig;
import static com.dmdirc.ClientModule.GlobalConfig;
import static com.dmdirc.ClientModule.UserConfig;
import static com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import static com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;

/**
 * Dagger module for the configuration system.
 */
@SuppressWarnings("TypeMayBeWeakened")
@Module(library = true, complete = false)
public class ConfigModule {

    @Provides
    @Singleton
    public IdentityManager getIdentityManager(
            @Directory(DirectoryType.BASE) final Path baseDirectory,
            @Directory(DirectoryType.IDENTITIES) final Path identitiesDirectory,
            @Directory(DirectoryType.ERRORS) final Path errorsDirectory,
            final CommandLineParser commandLineParser,
            final DMDircMBassador eventBus,
            final ClientInfo clientInfo,
            final ErrorManager errorManager) {
        final IdentityManager identityManager = new IdentityManager(baseDirectory,
                identitiesDirectory, eventBus, clientInfo);
        identityManager.loadVersionIdentity();

        try {
            identityManager.initialise();
        } catch (InvalidIdentityFileException ex) {
            handleInvalidConfigFile(identityManager, baseDirectory);
        }
        errorManager.initialise(identityManager.getGlobalConfiguration());

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
    public IdentityFactory getIdentityFactory(final IdentityManager identityManager) {
        return identityManager;
    }

    /**
     * Called when the global config cannot be loaded due to an error. This method informs the user
     * of the problem and installs a new default config file, backing up the old one.
     *  @param identityManager The identity manager to re-initialise after installing defaults.
     * @param configdir       The directory to extract default settings into.
     */
    private void handleInvalidConfigFile(final IdentityManager identityManager,
            final Path configdir) {
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

        final Path configFile = configdir.resolve("dmdirc.config");
        final Path newConfigFile = configdir.resolve("dmdirc.config." + date);

        try {
            Files.move(configFile, newConfigFile);

            try {
                identityManager.initialise();
            } catch (InvalidIdentityFileException iife) {
                // This shouldn't happen!
                System.err.println("Unable to load global config");
                iife.printStackTrace();
            }
        } catch (IOException ex) {
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
