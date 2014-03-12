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

package com.dmdirc;

import com.dmdirc.commandline.BaseDirectoryLocator;
import com.dmdirc.commandline.CommandLineParser;

/**
 * Utility class to help launcher interface with client.
 */
public class LauncherUtils {

    public native static void setDirectory(final String directory);

    /**
     * Retrieves the config directory from the CLI arguments.
     *
     * @param args CLI arguments
     */
    public static void getDirectory(final String... args) {
        final CommandLineParser cliParser = new CommandLineParser(null, null, null);
        final BaseDirectoryLocator locator = new BaseDirectoryLocator();
        cliParser.parse(args);
        final String configDirectory = cliParser.getConfigDirectory();
        setDirectory(configDirectory == null ? locator.getDefaultBaseDirectory() : configDirectory);
    }

}
