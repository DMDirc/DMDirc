/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.plugins.implementations;

import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.PluginMetaData;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Helper file to manage plugins that need extracted files on disk.
 */
public class PluginFilesHelper {

    /** This plugins meta data object. */
    private final PluginMetaData metaData;
    /** Plugin manager to use to get directory. */
    private final PluginManager pluginManager;
    /** This plugins information object. */
    private final PluginInfo pluginInfo;
    /** This plugins files directory. */
    private File filesDir;

    /**
     * Creates a new instance of this helper
     *
     * @param pluginManager Plugin manager to use to get directory
     * @param pluginInfo This plugin's information object
     */
    public PluginFilesHelper(final PluginManager pluginManager, final PluginInfo pluginInfo) {
        this.pluginManager = pluginManager;
        this.pluginInfo = pluginInfo;
        this.metaData = pluginInfo.getMetaData();
        filesDir = initFilesDir();
    }

    public File getFilesDir() {
        return filesDir;
    }

    /**
     * Initialises the files directory for this plugin. This will attempt to create the directory if
     * it doesn't exist the first time the directory name is requested.
     *
     * @return Files directory for this plugin.
     */
    private File initFilesDir() {
        if (filesDir == null) {
            final String fs = System.getProperty("file.separator");
            final String dir = pluginManager.getFilesDirectory();
            filesDir = new File(dir + metaData.getName() + fs);
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
        }
        return filesDir;
    }

    /**
     * Returns the path of the directory this plugin should use for files it requires extracted.
     *
     * @return File directory as a string
     */
    public String getFilesDirString() {
        return getFilesDir().getAbsolutePath() + System.getProperty("file.separator");
    }

    /**
     * Extracts the specified resource to the specified directory.
     *
     * @param resourceName The name of the resource to extract
     *
     * @throws IOException if the write operation fails
     */
    public void extractResource(final String resourceName) throws IOException {
        final Path resource = pluginInfo.getPath(resourceName);
        Files.copy(resource, filesDir.toPath().resolve(resource.getFileName().toString()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Extracts files starting with the given prefix to the files directory.
     *
     * @param prefix Prefix to extract
     *
     * @throws IOException if the resources failed to extract
     */
    public void extractResourcesStartingWith(final String prefix) throws IOException {
        Files.walkFileTree(pluginInfo.getPath("/"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                if (file.getFileName().toString().startsWith(prefix)) {
                    Files.copy(file, filesDir.toPath().resolve(file.getFileName().toString()),
                            StandardCopyOption.REPLACE_EXISTING);
                } return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Extracts files ending with the given suffix to the files directory.
     *
     * @param suffix Suffix to extract
     *
     * @throws IOException if the resources failed to extract
     */
    public void extractResourcesEndingWith(final String suffix) throws IOException {
        Files.walkFileTree(pluginInfo.getPath("/"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                if (file.getFileName().toString().endsWith(suffix)) {
                    Files.copy(file, filesDir.toPath().resolve(file.getFileName().toString()),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
