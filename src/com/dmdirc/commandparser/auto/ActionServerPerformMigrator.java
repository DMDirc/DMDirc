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

package com.dmdirc.commandparser.auto;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.interfaces.Migrator;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;

import com.google.common.base.Joiner;

import java.util.Optional;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Migrates server/network-based action performs into auto commands.
 */
@Singleton
public class ActionServerPerformMigrator implements Migrator {

    private final Path directory;
    private final AutoCommandManager autoCommandManager;
    private final DMDircMBassador eventBus;

    @Inject
    public ActionServerPerformMigrator(
            @Directory(DirectoryType.ACTIONS) final Path directory,
            final AutoCommandManager autoCommandManager,
            final DMDircMBassador eventBus) {
        this.directory = directory.resolve("performs");
        this.autoCommandManager = autoCommandManager;
        this.eventBus = eventBus;
    }

    @Override
    public boolean needsMigration() {
        return Files.isDirectory(directory);
    }

    @Override
    public void migrate() {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path path : directoryStream) {
                if (migrate(path)) {
                    Files.delete(path);
                }
            }
            Files.delete(directory);
        } catch (IOException ex) {
            eventBus.publish(new AppErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to migrate performs", ex.getMessage()));
        }
    }

    /**
     * Migrates the specified file.
     *
     * @param file The file to be migrated.
     *
     * @return True if the file was migrated successfully, false otherwise.
     */
    private boolean migrate(final Path file) {
        try {
            final ConfigFile configFile = new ConfigFile(file);
            configFile.read();

            final String response = Joiner.on('\n').join(configFile.getFlatDomain("response"));
            final Optional<String> server = getCondition(configFile, "SERVER_NAME");
            final Optional<String> network = getCondition(configFile, "SERVER_NETWORK");
            final Optional<String> profile = getCondition(configFile,
                    "SERVER_PROFILE.IDENTITY_NAME");

            autoCommandManager.addAutoCommand(
                    AutoCommand.create(server, network, profile, response));
            return true;
        } catch (IOException | InvalidConfigFileException | NumberFormatException ex) {
            return false;
        }
    }

    private Optional<String> getCondition(final ConfigFile configFile, final String component) {
        for (Map.Entry<String, Map<String, String>> section : configFile.getKeyDomains().entrySet()) {
            if (section.getKey().startsWith("condition")
                    && component.equals(section.getValue().get("component"))) {
                return Optional.ofNullable(section.getValue().get("target"));
            }
        }
        return Optional.empty();
    }

}
