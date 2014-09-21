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

package com.dmdirc.updater.components;

import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

/**
 * Represents the client component, which covers the core client resources.
 */
public class ClientComponent implements UpdateComponent {

    /** The controller to read settings from. */
    private final IdentityController identityController;
    /** The manager to add status bar messages to. */
    private final StatusBarManager statusBarManager;

    /**
     * Creates a new instance of {@link ClientComponent}.
     *
     * @param identityController The controller to read settings from.
     * @param statusBarManager   The manager to add status bar messages to.
     */
    @Inject
    public ClientComponent(
            final IdentityController identityController,
            final StatusBarManager statusBarManager) {
        this.identityController = identityController;
        this.statusBarManager = statusBarManager;
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
    public String getManualInstructions(final String path) {
        final File targetFile = new File(new File(path).getParent()
                + File.separator + ".DMDirc.jar");

        if (requiresManualInstall()) {
            Path appPath;
            String appDir;
            try {
                appPath = Paths.get(getClass().getProtectionDomain().getCodeSource()
                        .getLocation().toURI());
                appDir = appPath.toAbsolutePath().toString();
            } catch (URISyntaxException e) {
                appPath = null;
                appDir = "[Error getting path]";
            }
            if (appPath != null && appPath.getFileName().endsWith(".jar")) {
                return "A new version of DMDirc has been downloaded, but as you\n"
                        + "do not seem to be using the DMDirc launcher, it will\n"
                        + "not be installed automatically.\n\n"
                        + "To install this update manually, please replace the\n"
                        + "existing DMDirc.jar file, located at:\n"
                        + " " + appDir + "\n with the following file:\n "
                        + targetFile.getAbsolutePath();
            } else {
                return "A new version of DMDirc has been downloaded, but as you\n"
                        + "do not seem to be using the DMDirc launcher, it will\n"
                        + "not be installed automatically.\n\n"
                        + "To install this update manually, please extract the\n"
                        + "new DMDirc.jar file, located at:\n"
                        + " " + targetFile.getAbsolutePath() + "\n"
                        + "over your existing DMDirc install located in:\n"
                        + "  " + appDir;
            }
        }
        return "";
    }

    @Override
    public String getFriendlyVersion() {
        return identityController.getGlobalConfiguration().getOption("version", "version");
    }

    @Override
    public boolean doInstall(final String path) {
        final File tmpFile = new File(path);
        final File targetFile = new File(tmpFile.getParent() + File.separator
                + ".DMDirc.jar");

        if (targetFile.exists()) {
            targetFile.delete();
        }

        tmpFile.renameTo(targetFile);

        if (requiresManualInstall()) {
            // @deprecated Should be removed when updater UI changes are
            // implemented.
            final String message = this.getManualInstructions(path);
            statusBarManager.setMessage(new StatusMessage(message,
                    identityController.getGlobalConfiguration()));
        }

        return true;
    }

}
