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

package com.dmdirc;

import com.dmdirc.commandline.BaseDirectoryLocator;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.updater.Version;
import com.dmdirc.util.SystemInfo;

/**
 * Utility class to help launcher interface with client.
 */
public class LauncherUtils {

    /**
     * Retrieves the config directory from the CLI arguments.
     *
     * @param args CLI arguments
     *
     * @return Returns the config directory to use
     */
    public static String getDirectory(final String... args) {
        final SystemInfo systemInfo = new SystemInfo();
        final CommandLineParser cliParser = new CommandLineParser(null, null, null, systemInfo);
        final BaseDirectoryLocator locator = new BaseDirectoryLocator(systemInfo);
        cliParser.parse(args);
        final String configDirectory = cliParser.getConfigDirectory();
        return configDirectory == null ? locator.getDefaultBaseDirectory() : configDirectory;
    }

    /**
     * Compares the first version to the second version to check which is newer.
     *
     * @param version1 First version to compare
     * @param version2 Second version to compare
     *
     * @return negative if older, 0 is equal, positive if newer.
     */
    public static int getIsNewer(final String version1, final String version2) {
        final Version versionOne = new Version(version1);
        final Version versionTwo = new Version(version2);
        return versionOne.compareTo(versionTwo);
    }

}
