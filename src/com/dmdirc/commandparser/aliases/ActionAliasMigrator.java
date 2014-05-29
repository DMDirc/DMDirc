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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.Migrator;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;

import com.google.common.base.Joiner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Migrates "alias" actions into proper aliases.
 */
@Singleton
public class ActionAliasMigrator implements Migrator {

    private final File directory;
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
            @Directory(DirectoryType.ACTIONS) final String directory,
            final AliasFactory aliasFactory,
            final AliasManager aliasManager) {
        this.directory = new File(directory, "aliases");
        this.aliasFactory = aliasFactory;
        this.aliasManager = aliasManager;
    }

    @Override
    public boolean needsMigration() {
        return directory.exists();
    }

    @Override
    public void migrate() {
        for (File child : directory.listFiles()) {
            if (migrate(child)) {
                child.delete();
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
                    && section.containsKey("target")
                    && "INT_GREATER".equals(section.get("comparison"))) {
                return 1 + Integer.valueOf(section.get("target"));
            }
        }
        return 0;
    }

}
