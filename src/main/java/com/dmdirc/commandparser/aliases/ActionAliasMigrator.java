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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.Migrator;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.APP_ERROR;

/**
 * Migrates "alias" actions into proper aliases.
 */
@Singleton
public class ActionAliasMigrator implements Migrator {

    private static final Logger LOG = LoggerFactory.getLogger(ActionAliasMigrator.class);

    private final Path directory;
    private final AliasFactory aliasFactory;
    private final AliasManager aliasManager;

    /**
     * Creates a new alias migrator.
     *
     * @param directory    The base directory to read alias actions from.
     * @param aliasFactory The factory to use to create new aliases.
     * @param aliasManager The manager to add aliases to.
     */
    @Inject
    public ActionAliasMigrator(
            @Directory(DirectoryType.ACTIONS) final Path directory,
            final AliasFactory aliasFactory,
            final AliasManager aliasManager) {
        this.directory = directory.resolve("aliases");
        this.aliasFactory = aliasFactory;
        this.aliasManager = aliasManager;
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
            LOG.warn(APP_ERROR, "Unable to migrate aliases", ex);
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
            final String name = getTrigger(configFile);
            final int minArguments = getArguments(configFile);

            aliasManager.addAlias(aliasFactory.createAlias(name, minArguments, response));
            return true;
        } catch (IOException | InvalidConfigFileException | NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Finds and returns the trigger for an alias.
     *
     * @param configFile The config file to read triggers from.
     *
     * @return The trigger for the alias.
     *
     * @throws InvalidConfigFileException If the config file is missing a trigger
     */
    private String getTrigger(final ConfigFile configFile) throws InvalidConfigFileException {
        for (Map<String, String> section : configFile.getKeyDomains().values()) {
            if (section.containsKey("comparison")
                    && section.containsKey("target")
                    && "STRING_EQUALS".equals(section.get("comparison"))) {
                return section.get("target");
            }
        }
        throw new InvalidConfigFileException("No trigger found");
    }

    /**
     * Finds and returns the minimum number of arguments an alias requires.
     *
     * @param configFile The config file to read minimum arguments from.
     *
     * @return The minimum number of arguments if present, or <code>0</code> otherwise.
     *
     * @throws NumberFormatException If the config file contains an invalid number of args.
     */
    private int getArguments(final ConfigFile configFile) throws NumberFormatException {
        for (Map<String, String> section : configFile.getKeyDomains().values()) {
            if (section.containsKey("comparison")
                    && section.containsKey("target")) {
                if ("INT_GREATER".equals(section.get("comparison"))) {
                    return 1 + Integer.parseInt(section.get("target"));
                }
                if ("INT_EQUALS".equals(section.get("comparison"))) {
                    return Integer.parseInt(section.get("target"));
                }
            }
        }
        return 0;
    }

}
