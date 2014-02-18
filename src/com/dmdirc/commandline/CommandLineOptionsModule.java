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

package com.dmdirc.commandline;

import java.io.File;

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
    public enum DirectoryType {

        /** The base directory, where everything else lives. */
        BASE,
        /** The directory that identities are stored in. */
        IDENTITIES,
        /** The directory that plugins are stored in. */
        PLUGINS,
        /** The directory that themes are stored in. */
        THEMES,
        /** The directory that actions are stored in. */
        ACTIONS,
        /** The
         * directory to use for temporary files (downloads in flight, caches, etc). */
        TEMPORARY;

    }

    /**
     * Provides a mean of identifying the type of directory a class wants injected.
     */
    @Qualifier
    public @interface Directory {

        DirectoryType value();

    }

    /**
     * Provides the base directory that all DMDirc user data is stored in.
     *
     * @param parser The parser to get the user-supplied directory from.
     *
     * @return The base directory.
     */
    @Provides
    @Singleton
    @Directory(DirectoryType.BASE)
    public String getBaseDirectory(final CommandLineParser parser) {
        if (parser.getConfigDirectory() == null) {
            return getDefaultBaseDirectory();
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

    /**
     * Initialises the location of the configuration directory.
     */
    private String getDefaultBaseDirectory() {
        final String fs = System.getProperty("file.separator");
        final String osName = System.getProperty("os.name");
        String configdir;

        if (System.getenv("DMDIRC_HOME") != null) {
            configdir = System.getenv("DMDIRC_HOME");
        } else if (osName.startsWith("Mac OS")) {
            configdir = System.getProperty("user.home") + fs + "Library"
                    + fs + "Preferences" + fs + "DMDirc" + fs;
        } else if (osName.startsWith("Windows")) {
            if (System.getenv("APPDATA") == null) {
                configdir = System.getProperty("user.home") + fs + "DMDirc" + fs;
            } else {
                configdir = System.getenv("APPDATA") + fs + "DMDirc" + fs;
            }
        } else {
            configdir = System.getProperty("user.home") + fs + ".DMDirc" + fs;
            final File testFile = new File(configdir);
            if (!testFile.exists()) {
                final String configHome = System.getenv("XDG_CONFIG_HOME");
                configdir = (configHome == null || configHome.isEmpty())
                        ? System.getProperty("user.home") + fs + ".config" + fs
                        : configHome;
                configdir += fs + "DMDirc" + fs;
            }
        }

        return new File(configdir).getAbsolutePath() + fs;
    }

}
