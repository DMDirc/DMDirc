/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.updater.components;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.util.resourcemanager.ZipResourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

/**
 * Represents the default identities.
 */
public class DefaultsComponent implements UpdateComponent {

    /** The controller to read settings from. */
    private final IdentityController identityController;
    /** The directory to put settings in. */
    private final String directory;

    /**
     * Creates a new instance of {@link DefaultsComponent}.
     *
     * @param identityController The controller to read settings from.
     * @param directory          The directory to place default settings in.
     */
    @Inject
    public DefaultsComponent(
            final IdentityController identityController,
            @Directory(DirectoryType.IDENTITIES) final String directory) {
        this.identityController = identityController;
        this.directory = directory;
    }

    @Override
    public String getName() {
        return "defaultsettings";
    }

    @Override
    public String getFriendlyName() {
        return "Default settings";
    }

    @Override
    public String getFriendlyVersion() {
        return String.valueOf(getVersion());
    }

    @Override
    public Version getVersion() {
        final AggregateConfigProvider globalConfig = identityController.getGlobalConfiguration();

        if (globalConfig.hasOptionString("identity", "defaultsversion")) {
            return new Version(globalConfig.getOption("identity",
                    "defaultsversion"));
        } else {
            return new Version(-1);
        }
    }

    @Override
    public boolean requiresRestart() {
        return false;
    }

    @Override
    public boolean requiresManualInstall() {
        return false;
    }

    @Override
    public String getManualInstructions(final Path path) {
        return "";
    }

    @Override
    public boolean doInstall(final Path path) throws IOException {
        final ZipResourceManager ziprm =
                ZipResourceManager.getInstance(path.toAbsolutePath().toString());

        ziprm.extractResources("", directory);

        identityController.loadUserIdentities();

        Files.delete(path);

        return false;
    }

}
