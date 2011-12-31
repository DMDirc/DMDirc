/*
 * Copyright (c) 2006-2012 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.plugins;

import java.io.File;

/**
 * Base implementation of the Plugin interface that also require local file in
 * in order to run.
 */
public class BaseFileDependantPlugin extends BasePlugin {

    /** This plugins meta data object. */
    private final PluginMetaData metaData;
    /** This plugins files directory. */
    private File filesDir;

    /**
     * Creates a new instance of this plugin
     *
     * @param metaData This plugin's meta data
     */
    public BaseFileDependantPlugin(final PluginMetaData metaData) {
        super();
        this.metaData = metaData;
        filesDir = initFilesDir();
    }

    /**
     * Initialises the files directory for this plugin. This will attempt to
     * create the directory if it doesn't exist the first time the directory
     * name is requested.
     *
     * @return Files directory for this plugin.
     */
    private File initFilesDir() {
        if (filesDir == null) {
            final String fs = System.getProperty("file.separator");
            final String dir = PluginManager.getPluginManager()
                    .getFilesDirectory();
            filesDir = new File(dir + metaData.getName() + fs);
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
        }
        return filesDir;
    }

    /**
     * Returns the directory this plugin should use for files it requires
     * extracted.
     *
     * @return File directory for this plugin
     */
    protected File getFilesDir() {
        return filesDir;
    }

    /**
     * Returns the path of the directory this plugin should use for files it
     * requires extracted.
     *
     * @return File directory as a string
     */
    protected String getFilesDirString() {
        return getFilesDir().getAbsolutePath()
                + System.getProperty("file.separator");
    }
}
