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

package com.dmdirc.plugins.implementations;

import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginMetaData;

import java.io.File;
import java.io.IOException;

import lombok.Getter;

/**
 * Helper file to manage plugins that need extracted files on disk.
 */
public class PluginFilesHelper {

    /** This plugins meta data object. */
    private final PluginMetaData metaData;
    /** This plugins information object. */
    private final PluginInfo pluginInfo;
    /** This plugins files directory. */
    @Getter
    private File filesDir;

    /**
     * Creates a new instance of this helper
     *
     * @param pluginInfo This plugin's information object
     */
    public PluginFilesHelper(final PluginInfo pluginInfo) {
        super();
        this.pluginInfo = pluginInfo;
        this.metaData = pluginInfo.getMetaData();
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
            final String dir = metaData.getManager().getFilesDirectory();
            filesDir = new File(dir + metaData.getName() + fs);
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
        }
        return filesDir;
    }

    /**
     * Returns the path of the directory this plugin should use for files it
     * requires extracted.
     *
     * @return File directory as a string
     */
    public String getFilesDirString() {
        return getFilesDir().getAbsolutePath()
                + System.getProperty("file.separator");
    }

    /**
     * Extracts the specified resource to the specified directory.
     *
     * @param resourceName The name of the resource to extract
     *
     * @throws IOException if the write operation fails
     */
    public void extractResource(final String resourceName) throws IOException {
        pluginInfo.getResourceManager().extractResource(resourceName, filesDir.toString(), true);
    }

    /**
     * Extracts files starting with the given prefix to the files directory.
     *
     * @param prefix Prefix to extract
     *
     * @throws IOException if the resources failed to extract
     */
    public void extractResoucesStartingWith(final String prefix) throws IOException {
        pluginInfo.getResourceManager().extractResources(prefix, filesDir.toString(), true);
    }

    /**
     * Extracts files ending with the given suffix to the files directory.
     *
     * @param suffix Suffix to extract
     *
     * @throws IOException if the resources failed to extract
     */
    public void extractResoucesEndingWith(final String suffix) throws IOException {
        pluginInfo.getResourceManager().extractResoucesEndingWith(filesDir, suffix);
    }
}
