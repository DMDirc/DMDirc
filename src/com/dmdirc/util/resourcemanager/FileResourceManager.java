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

package com.dmdirc.util.resourcemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an easy way to access files inside a project.
 */
public final class FileResourceManager extends ResourceManager {

    /** Filesystem to use. */
    private final FileSystem fs = FileSystems.getDefault();
    /** Base path for the project. */
    private final Path basePath;

    /**
     * Creates a new instance of FileResourceManager.
     *
     * @param basePath Base path for the resource manager
     */
    protected FileResourceManager(final String basePath) {
        super();

        this.basePath = fs.getPath(basePath);
    }

    @Override
    public boolean resourceExists(final String resource) {
        final Path path = basePath.resolve(resource);
        return Files.exists(path) && !Files.isDirectory(path);
    }

    @Override
    public byte[] getResourceBytes(final String resource) throws IOException {
        return Files.readAllBytes(basePath.resolve(resource));
    }

    @Override
    public InputStream getResourceInputStream(final String resource) throws IOException {
        return Files.newInputStream(basePath.resolve(resource));
    }

    @Override
    public URL getResourceURL(final String resource) throws MalformedURLException {
        return basePath.resolve(resource).toUri().toURL();
    }

    @Override
    public Map<String, byte[]> getResourcesEndingWithAsBytes(final String resourcesSuffix)
            throws IOException {
        final DirectoryStream<Path> paths = Files.newDirectoryStream(basePath);
        final Map<String, byte[]> resources = new HashMap<>();
        for (Path path : paths) {
            if (path.endsWith(resourcesSuffix)) {
                resources.put(path.toString(), Files.readAllBytes(path));
            }
        }
        return resources;
    }

    @Override
    public Map<String, byte[]> getResourcesStartingWithAsBytes(final String resourcesPrefix)
            throws IOException {
        final DirectoryStream<Path> paths = Files.newDirectoryStream(basePath);
        final Map<String, byte[]> resources = new HashMap<>();
        for (Path path : paths) {
            if (path.startsWith(resourcesPrefix)) {
                resources.put(path.toString(), Files.readAllBytes(path));
            }
        }
        return resources;
    }

    @Override
    public Map<String, InputStream> getResourcesStartingWithAsInputStreams(
            final String resourcesPrefix) throws IOException {
        final DirectoryStream<Path> files = Files.newDirectoryStream(basePath);
        final Map<String, InputStream> resources = new HashMap<>();
        for (Path file : files) {
            if (file.startsWith(resourcesPrefix)) {
                resources.put(file.toString(), Files.newInputStream(file));
            }
        }
        return resources;
    }

    @Override
    public List<String> getResourcesStartingWith(final String resourcesPrefix) throws IOException {
        final DirectoryStream<Path> files = Files.newDirectoryStream(basePath);
        final List<String> resources = new ArrayList<>();
        for (Path file : files) {
            if (file.startsWith(resourcesPrefix)) {
                resources.add(file.toString());
            }
        }
        return resources;
    }

}
