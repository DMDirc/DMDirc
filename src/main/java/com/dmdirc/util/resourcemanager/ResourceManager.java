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

package com.dmdirc.util.resourcemanager;

import com.dmdirc.Main;
import com.dmdirc.util.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides a launch method independent way of accessing resources.
 */
public abstract class ResourceManager {

    /** Previously assigned ResourceManager. */
    private static ResourceManager me;

    /**
     * Returns an appropriate instance of ResourceManager.
     *
     * @return ResourceManager implementation
     */
    public static synchronized ResourceManager getResourceManager() throws IllegalStateException {
        if (me == null) {
            try {
                if (FileUtils.isRunningFromJar(Main.class)) {
                    me = new ZipResourceManager(FileUtils.getApplicationPath(Main.class)
                            .toAbsolutePath().toString());
                } else {
                    me = new FileResourceManager(FileUtils.getApplicationPath(Main.class)
                            .toAbsolutePath().toString());

                }
            } catch (IllegalStateException | IOException ex) {
                throw new IllegalStateException("Unable to determine how DMDirc has been executed",
                        ex);
            }
        }
        return me;
    }

    /**
     * Returns a resource manager for the specified URL. The following URL types are valid:
     *
     * <ul>
     * <li>file://path/</li>
     * <li>zip://path/filename.zip</li>
     * <li>jar://path/filename.jar</li>
     * </ul>
     *
     * @param url The URL for which a resource manager is required
     *
     * @return A resource manager for the specified URL
     *
     * @throws IOException              if an IO Error occurs opening the file
     * @throws IllegalArgumentException if the URL type is not valid
     */
    public static ResourceManager getResourceManager(final String url)
            throws IOException, IllegalArgumentException {
        if (url.startsWith("file://")) {
            return new FileResourceManager(url.substring(7));
        } else if (url.startsWith("jar://") || url.startsWith("zip://")) {
            return new ZipResourceManager(url.substring(6));
        } else {
            throw new IllegalArgumentException("Unknown resource manager type (" + url + ')');
        }
    }

    /**
     * Writes a resource to a file.
     *
     * @param resource Resource to write
     * @param file     File to write to
     *
     * @throws IOException if the write operation fails
     */
    public final void resourceToFile(final byte[] resource, final File file)
            throws IOException {
        try (FileOutputStream out = new FileOutputStream(file, false)) {
            out.write(resource);
            out.flush();
        }
    }

    /**
     * Extracts the specified resource to the specified directory.
     *
     * @param resourceName The name of the resource to extract
     * @param directory    The name of the directory to extract to
     * @param usePath      If true, append the path of the files in the resource to the extraction
     *                     path
     *
     * @throws IOException if the write operation fails
     *
     * @return success of failure of the operation
     */
    public final boolean extractResource(final String resourceName,
            final String directory, final boolean usePath) throws IOException {
        final byte[] resource = getResourceBytes(resourceName);

        if (resource.length == 0) {
            return false;
        }

        final File newDir;

        if (usePath && resourceName.indexOf('/') > -1) {
            newDir = new File(directory,
                    resourceName.substring(0, resourceName.lastIndexOf('/')) + '/');
        } else {
            newDir = new File(directory);
        }

        if (!newDir.exists()) {
            newDir.mkdirs();
        }

        if (!newDir.exists()) {
            return false;
        }

        final File newFile = new File(newDir,
                resourceName.substring(resourceName.lastIndexOf('/') + 1,
                        resourceName.length()));

        if (!newFile.isDirectory()) {
            resourceToFile(resource, newFile);
        }

        return true;
    }

    /**
     * Extracts the specified resources to the specified directory.
     *
     * @param resourcesPrefix The prefix of the resources to extract
     * @param directory       The name of the directory to extract to
     * @param usePath         If true, append the path of the files in the resource to the
     *                        extraction path
     *
     * @throws IOException if the write operation fails
     */
    public final void extractResources(final String resourcesPrefix,
            final String directory, final boolean usePath) throws IOException {
        final Map<String, byte[]> resourcesBytes = getResourcesStartingWithAsBytes(resourcesPrefix);
        for (Entry<String, byte[]> entry : resourcesBytes.entrySet()) {
            extractResource(entry.getKey(), directory, usePath);
        }
    }

    /**
     * Extracts the specified resources to the specified directory.
     *
     * @param resourcesPrefix The prefix of the resources to extract
     * @param directory       The name of the directory to extract to
     *
     * @throws IOException if the write operation fails
     */
    public final void extractResources(final String resourcesPrefix,
            final String directory) throws IOException {
        extractResources(resourcesPrefix, directory, true);
    }

    /**
     * Extracts files ending with the given suffix
     *
     * @param newDir Directory to extract to.
     * @param suffix Suffix to extract
     *
     * @throws IOException if the resources failed to extract
     */
    public final void extractResourcesEndingWith(final File newDir, final String suffix)
            throws IOException {
        final Map<String, byte[]> resources = getResourcesEndingWithAsBytes(
                suffix);
        for (Entry<String, byte[]> resource : resources.entrySet()) {
            final String key = resource.getKey();
            final String resourceName = key.substring(key.lastIndexOf('/'), key.
                    length());

            final File newFile = new File(newDir, resourceName);

            if (!newFile.isDirectory()) {
                if (newFile.exists()) {
                    newFile.delete();
                }
                ResourceManager.getResourceManager().resourceToFile(resource.
                        getValue(), newFile);
            }
        }
    }

    /**
     * Checks if a resource exists.
     *
     * @param resource Resource to check
     *
     * @return true iif the resource exists
     */
    public abstract boolean resourceExists(final String resource);

    /**
     * Gets a byte[] of the specified resource.
     *
     * @param resource Name of the resource to return
     *
     * @return byte[] for the resource, or an empty byte[] if not found
     */
    public abstract byte[] getResourceBytes(final String resource);

    /**
     * Gets an InputStream for the specified resource.
     *
     * @param resource Name of the resource to return
     *
     * @return InputStream for the resource, or null if not found
     */
    public abstract InputStream getResourceInputStream(final String resource);

    /**
     * Gets an URL for the specified resource.
     *
     * @param resource Name of the resource to return
     *
     * @return A URL for the resource, or null if not found
     *
     * @throws MalformedURLException If the URL created is malformed
     * @since 0.6.3
     */
    public abstract URL getResourceURL(final String resource) throws MalformedURLException;

    /**
     * Gets a Map of byte[]s of the resources ending with the specified suffix.
     *
     * @param resourcesSuffix Suffix of the resources to return
     *
     * @since 0.6
     * @return Map of byte[]s of resources found
     */
    public abstract Map<String, byte[]> getResourcesEndingWithAsBytes(
            final String resourcesSuffix);

    /**
     * Gets a Map of byte[]s of the resources starting with the specified prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return Map of byte[]s of resources found
     */
    public abstract Map<String, byte[]> getResourcesStartingWithAsBytes(
            final String resourcesPrefix);

    /**
     * Gets a Map of InputStreams of the resources starting with the specified prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return Map of InputStreams of resources found
     */
    public abstract Map<String, InputStream> getResourcesStartingWithAsInputStreams(
            final String resourcesPrefix);

    /**
     * Gets a List of the resources starting with the specified prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return List of resources found
     */
    public abstract List<String> getResourcesStartingWith(final String resourcesPrefix);

}
