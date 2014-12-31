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

package com.dmdirc.util.resourcemanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Provides an easy way to access files inside a zip or jar.
 */
public final class ZipResourceManager extends ResourceManager {

    /** Zipfile instance. */
    private final ZipFile zipFile;
    /** Entries list. */
    private final List<String> entries;

    /**
     * Instantiates ZipResourceManager.
     *
     * @param file Filename of the zip to load
     *
     * @throws IOException Throw when the zip fails to load
     */
    protected ZipResourceManager(final String file) throws IOException {
        this(new File(file));
    }

    /**
     * Instantiates ZipResourceManager.
     *
     * @param file File of the zip to load
     *
     * @throws IOException Throw when the zip fails to load
     */
    protected ZipResourceManager(final File file) throws IOException {
        this(new ZipFile(file));
    }

    /**
     * Instantiates ZipResourceManager.
     *
     * @param file ZipFile to load
     */
    protected ZipResourceManager(final ZipFile file) {
        zipFile = file;
        entries = new ArrayList<>();
        final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            entries.add(zipEntries.nextElement().getName());
        }
    }

    /**
     * Returns an instance of a ZipResourceManager for the specified file.
     *
     * @param filename Filename of the zip to load
     *
     * @return ZipResourceManager instance
     *
     * @throws IOException Throw when the zip fails to load
     */
    public static synchronized ZipResourceManager getInstance(final String filename) throws
            IOException {
        return new ZipResourceManager(filename);
    }

    @Override
    public boolean resourceExists(final String resource) {
        final ZipEntry zipEntry = zipFile.getEntry(resource);
        return zipEntry != null && !zipEntry.isDirectory();
    }

    @Override
    public byte[] getResourceBytes(final String resource) {
        final ZipEntry zipEntry = zipFile.getEntry(resource);
        if (zipEntry == null) {
            return new byte[0];
        }

        if (zipEntry.isDirectory()) {
            return new byte[0];
        }

        final byte[] bytes = new byte[(int) zipEntry.getSize()];

        try (BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream
                (zipEntry))) {
            if (inputStream.read(bytes) != bytes.length) {
                inputStream.close();
                return new byte[0];
            }
        } catch (IOException ex) {
            return new byte[0];
        }

        return bytes;
    }

    @Override
    public InputStream getResourceInputStream(final String resource) {
        final ZipEntry zipEntry = zipFile.getEntry(resource);
        if (zipEntry == null) {
            return null;
        }

        try {
            return zipFile.getInputStream(zipEntry);
        } catch (IOException ex) {
            return null;
        }

    }

    @Override
    public URL getResourceURL(final String resource) throws MalformedURLException {
        if (resourceExists(resource)) {
            return new URL("jar:file:/" + zipFile.getName() + "!"
                    + (resource.charAt(0) == '/' ? resource : "/" + resource));
        } else {
            return null;
        }
    }

    @Override
    public Map<String, byte[]> getResourcesEndingWithAsBytes(
            final String resourcesSuffix) {
        final Map<String, byte[]> resources = new HashMap<>();

        entries.stream().filter(entry -> entry.endsWith(resourcesSuffix))
                .forEach(entry -> resources.put(entry, getResourceBytes(entry)));

        return resources;
    }

    @Override
    public Map<String, byte[]> getResourcesStartingWithAsBytes(
            final String resourcesPrefix) {
        final Map<String, byte[]> resources = new HashMap<>();

        entries.stream().filter(entry -> entry.startsWith(resourcesPrefix))
                .forEach(entry -> resources.put(entry, getResourceBytes(entry)));

        return resources;
    }

    @Override
    public Map<String, InputStream> getResourcesStartingWithAsInputStreams(
            final String resourcesPrefix) {
        final Map<String, InputStream> resources = new HashMap<>();

        entries.stream().filter(entry -> entry.startsWith(resourcesPrefix))
                .forEach(entry -> resources.put(entry, getResourceInputStream(entry)));

        return resources;
    }

    @Override
    public List<String> getResourcesStartingWith(final String resourcesPrefix) {
        return entries.stream()
                .filter(entry -> entry.startsWith(resourcesPrefix))
                .collect(Collectors.toList());
    }

}
