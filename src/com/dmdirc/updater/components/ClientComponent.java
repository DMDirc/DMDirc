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

package com.dmdirc.updater.components;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.Main;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.events.StatusBarMessageEvent;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.util.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

/**
 * Represents the client component, which covers the core client resources.
 */
public class ClientComponent implements UpdateComponent {

    /** The controller to read settings from. */
    private final IdentityController identityController;
    /** The event bus to post messages to. */
    private final DMDircMBassador eventBus;
    /** Base directory to move updates to. */
    private final Path baseDirectory;

    /**
     * Creates a new instance of {@link ClientComponent}.
     *
     * @param identityController The controller to read settings from.
     * @param eventBus           The event bus to post messages to.
     */
    @Inject
    public ClientComponent(
            final IdentityController identityController,
            final DMDircMBassador eventBus,
            @Directory(DirectoryType.BASE) final Path baseDirectory) {
        this.identityController = identityController;
        this.eventBus = eventBus;
        this.baseDirectory = baseDirectory;
    }

    @Override
    public String getName() {
        return "client";
    }

    @Override
    public String getFriendlyName() {
        return "DMDirc client";
    }

    @Override
    public Version getVersion() {
        return new Version(getFriendlyVersion());
    }

    @Override
    public boolean requiresRestart() {
        return true;
    }

    @Override
    public boolean requiresManualInstall() {
        return !LauncherComponent.isUsingLauncher();
    }

    @Override
    public String getManualInstructions(final Path path) {
        final Path targetFile = baseDirectory.resolve(".DMDirc.jar");

        if (requiresManualInstall()) {
            if (FileUtils.isRunningFromJar(Main.class)) {
                return "A new version of DMDirc has been downloaded, but as you\n"
                        + "do not seem to be using the DMDirc launcher, it will\n"
                        + "not be installed automatically.\n\n"
                        + "To install this update manually, please replace the\n"
                        + "existing DMDirc.jar file, located at:\n"
                        + ' ' + FileUtils.getApplicationPath(Main.class)
                        + "\n with the following file:\n "
                        + targetFile.toAbsolutePath();
            } else {
                return "A new version of DMDirc has been downloaded, but as you\n"
                        + "do not seem to be using the DMDirc launcher, it will\n"
                        + "not be installed automatically.\n\n"
                        + "To install this update manually, please extract the\n"
                        + "new DMDirc.jar file, located at:\n"
                        + ' ' + targetFile.toAbsolutePath() + '\n'
                        + "over your existing DMDirc install located in:\n"
                        + "  " + FileUtils.getApplicationPath(Main.class);
            }
        }
        return "";
    }

    @Override
    public String getFriendlyVersion() {
        return identityController.getGlobalConfiguration().getOption("version", "version");
    }

    @Override
    public boolean doInstall(final Path path) throws IOException {
        final Path targetFile = baseDirectory.resolve(".DMDirc.jar");

        Files.deleteIfExists(targetFile);
        Files.move(path, targetFile);

        if (requiresManualInstall()) {
            // @deprecated Should be removed when updater UI changes are
            // implemented.
            final String message = getManualInstructions(path);
            eventBus.publishAsync(new StatusBarMessageEvent(new StatusMessage(message,
                    identityController.getGlobalConfiguration())));
        }

        return true;
    }

}
