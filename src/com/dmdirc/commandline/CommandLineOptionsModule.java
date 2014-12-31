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

package com.dmdirc.commandline;

import java.io.File;
import java.nio.file.Path;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides options based on the command line.
 */
@Module(library = true, complete = false)
public class CommandLineOptionsModule {

    /**
     * Enumeration of directory types supported by the client.
     */
    public static final class DirectoryType {

        /**
         * The base directory, where everything else lives.
         */
        public static final String BASE = "base";
        /**
         * The directory that identities are stored in.
         */
        public static final String IDENTITIES = "identities";
        /**
         * The directory that plugins are stored in.
         */
        public static final String PLUGINS = "plugins";
        /**
         * The directory that themes are stored in.
         */
        public static final String THEMES = "themes";
        /**
         * The directory that actions are stored in.
         */
        public static final String ACTIONS = "actions";
        /**
         * The directory that error reports are stored in.
         */
        public static final String ERRORS = "errors";
        /**
         * The directory to use for temporary files (downloads in flight, caches, etc).
         */
        public static final String TEMPORARY = "temp";

        private DirectoryType() {
        }

    }

    /**
     * Provides a mean of identifying the type of directory a class wants injected.
     */
    @Qualifier
    public @interface Directory {

        String value();

    }

    /**
     * Provides the base directory that all DMDirc user data is stored in.
     *
     * @param parser The parser to get the user-supplied directory from.
     * @param locator Base directory locator to find default config directory.
     *
     * @return The base directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.BASE)
    public String getBaseDirectory(final CommandLineParser parser,
            final BaseDirectoryLocator locator) {
        if (parser.getConfigDirectory() == null) {
            return locator.getDefaultBaseDirectory();
        } else {
            return parser.getConfigDirectory();
        }
    }

    /**
     * Provides the path to the plugins directory.
     *
     * @param baseDirectory The base DMDirc directory.
     *
     * @return The plugin directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.PLUGINS)
    public String getPluginsDirectory(@Directory(DirectoryType.BASE) final String baseDirectory) {
        return baseDirectory + "plugins" + File.separator;
    }

    /**
     * Provides the path to the actions directory.
     *
     * @param baseDirectory The base DMDirc directory.
     *
     * @return The actions directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.ACTIONS)
    public String getActionsDirectory(@Directory(DirectoryType.BASE) final String baseDirectory) {
        return baseDirectory + "actions" + File.separator;
    }

    /**
     * Provides the path to the identities directory.
     *
     * @param baseDirectory The base DMDirc directory.
     *
     * @return The identities directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.IDENTITIES)
    public String getIdentitiesDirectory(@Directory(DirectoryType.BASE) final String baseDirectory) {
        return baseDirectory + "identities" + File.separator;
    }

    /**
     * Provides the path to the errors directory.
     *
     * @param baseDirectory The base DMDirc directory.
     *
     * @return The identities directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.ERRORS)
    public String getErrorsDirectory(@Directory(DirectoryType.BASE) final String baseDirectory) {
        return baseDirectory + "errors" + File.separator;
    }

    /**
     * Provides the path to the themes directory.
     *
     * @param baseDirectory The base DMDirc directory.
     *
     * @return The themes directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.THEMES)
    public String getThemesDirectory(@Directory(DirectoryType.BASE) final String baseDirectory) {
        return baseDirectory + "themes" + File.separator;
    }

    /**
     * Provides the path to a DMDirc pseudo-temporary directory. This is somewhere the client can
     * use to store limited-use files such as downloads not yet installed, or cached update
     * information. It is not automatically purged - items placed there must be cleaned up
     * explicitly.
     *
     * @param baseDirectory The base DMDirc directory.
     *
     * @return The temporary directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.TEMPORARY)
    public String getTempDirectory(@Directory(DirectoryType.BASE) final String baseDirectory) {
        return baseDirectory;
    }

    @Provides
    @Singleton
    @Directory(DirectoryType.TEMPORARY)
    public Path getTempDirectory(@Directory(DirectoryType.BASE) final Path baseDirectory) {
        return baseDirectory;
    }

    @Provides
    @Singleton
    @Directory(DirectoryType.BASE)
    public Path getBasePath(@Directory(DirectoryType.BASE) final String directory) {
        return new File(directory).toPath();
    }

    @Provides
    @Singleton
    @Directory(DirectoryType.PLUGINS)
    public Path getPluginsPath(@Directory(DirectoryType.PLUGINS) final String directory) {
        return new File(directory).toPath();
    }

    @Provides
    @Singleton
    @Directory(DirectoryType.ACTIONS)
    public Path getActionsPath(@Directory(DirectoryType.ACTIONS) final String directory) {
        return new File(directory).toPath();
    }

    @Provides
    @Singleton
    @Directory(DirectoryType.IDENTITIES)
    public Path getIdentitiesPath(@Directory(DirectoryType.IDENTITIES) final String directory) {
        return new File(directory).toPath();
    }

    @Provides
    @Singleton
    @Directory(DirectoryType.ERRORS)
    public Path getErrorsPath(@Directory(DirectoryType.ERRORS) final String directory) {
        return new File(directory).toPath();
    }

    @Provides
    @Singleton
    @Directory(DirectoryType.THEMES)
    public Path getThemesPath(@Directory(DirectoryType.THEMES) final String directory) {
        return new File(directory).toPath();
    }

}
