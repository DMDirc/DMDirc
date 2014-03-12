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

import javax.inject.Inject;

/**
 * Locates the default base directory on OS X, Windows and Linux.
 */
public class BaseDirectoryLocator {

    @Inject
    public BaseDirectoryLocator(){}

    /**
     * Initialises the location of the configuration directory.
     *
     * @return Directory
     */
    public String getDefaultBaseDirectory() {
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
