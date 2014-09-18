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

package com.dmdirc.commandparser.auto;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.Migrator;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Migrates server/network-based action performs into auto commands.
 */
@Singleton
public class ActionServerPerformMigrator implements Migrator {

    private final File directory;
    private final AutoCommandManager autoCommandManager;

    @Inject
    public ActionServerPerformMigrator(@Directory(DirectoryType.ACTIONS) final String directory,
            final AutoCommandManager autoCommandManager) {
        this.directory = new File(directory, "performs");
        this.autoCommandManager = autoCommandManager;
    }

    @Override
    public boolean needsMigration() {
        return directory.exists();
    }

    @Override
    public void migrate() {
        @Nullable final File[] files = directory.listFiles();
        if (files != null) {
            for (File child : files) {
                if (migrate(child)) {
                    child.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Migrates the specified file.
     *
     * @param file The file to be migrated.
     *
     * @return True if the file was migrated successfully, false otherwise.
     */
    private boolean migrate(final File file) {
        try {
            final ConfigFile configFile = new ConfigFile(file);
            configFile.read();

            final String response = Joiner.on('\n').join(configFile.getFlatDomain("response"));
            final Optional<String> server = getCondition(configFile, "SERVER_NAME");
            final Optional<String> network = getCondition(configFile, "SERVER_NETWORK");
            final Optional<String> profile = getCondition(configFile,
                    "SERVER_PROFILE.IDENTITY_NAME");

            autoCommandManager.addAutoCommand(new AutoCommand(server, network, profile, response));
            return true;
        } catch (IOException | InvalidConfigFileException | NumberFormatException ex) {
            return false;
        }
    }

    private Optional<String> getCondition(final ConfigFile configFile, final String component) {
        for (Map.Entry<String, Map<String, String>> section : configFile.getKeyDomains().entrySet()) {
            if (section.getKey().startsWith("condition")
                    && component.equals(section.getValue().get("component"))) {
                return Optional.fromNullable(section.getValue().get("target"));
            }
        }
        return Optional.absent();
    }

}
