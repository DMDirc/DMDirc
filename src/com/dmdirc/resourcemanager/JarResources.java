/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Provides an easy way to access files inside a jar.
 */
public final class JarResources {
    
    /** Zipfile instance. */
    private final JarFile jarFile;
    
    /** Entries list. */
    private final List<String> entries;
    
    /**
     * Instantiates JarResources.
     *
     * @param fileName Filename of the jar to load
     *
     * @throws IOException Throw when the jar fails to load
     */
    public JarResources(final String fileName) throws IOException {
        this.jarFile = new JarFile(fileName);
        entries = new ArrayList<String>();
        initEntries();
    }
    
    /** Initialises the entries list. */
    private void initEntries() {
        final Enumeration<? extends JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            entries.add(jarEntries.nextElement().getName());
        }
    }
    
    /**
     * Returns the name of the jar file.
     *
     * @return Jar name
     */
    public String getName() {
        return jarFile.getName();
    }
    
    /**
     * Returns the number of files inside this jar.
     *
     * @return Number of files in the jar
     */
    public int getNumResources() {
        return entries.size();
    }
    
    /**
     * Gets a byte[] of the specified resource.
     *
     * @param resource Name of the resource to return
     *
     * @return byte[] for the resource, or an empty byte[] if not found
     */
    public byte[] getResourceBytes(final String resource) {
        final ZipEntry jarEntry = jarFile.getEntry(resource);
        BufferedInputStream inputStream;
        
        
        if (jarEntry == null) {
            return new byte[0];
        }
        
        if (jarEntry.isDirectory()) {
            return new byte[0];
        }
        
        final byte[] bytes = new byte[(int) jarEntry.getSize()];
        
        try {
            inputStream =
                    new BufferedInputStream(jarFile.getInputStream(jarEntry));
        } catch (IOException ex) {
            return new byte[0];
        }
        
        try {
            inputStream.read(bytes);
        } catch (IOException ex) {
            return new byte[0];
        }
        
        return bytes;
    }
    
    /**
     * Gets an InputStream for the specified resource.
     *
     * @param resource Name of the resource to return
     *
     * @return InputStream for the resource, or null if not found
     */
    public InputStream getResourceInputStream(final String resource) {
        final JarEntry jarEntry = (JarEntry) jarFile.getEntry(resource);
        
        if (jarEntry == null) {
            return null;
        }
        
        try {
            return jarFile.getInputStream(jarEntry);
        } catch (IOException ex) {
            return null;
        }
        
    }
    
    /**
     * Gets a Map of byte[]s of the resources starting with the specified
     * prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return Map of byte[]s of resources found
     */
    public Map<String, byte[]> getResourcesStartingWithAsBytes(
            final String resourcesPrefix) {
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        
        for (String entry : entries) {
            if (entry.startsWith(resourcesPrefix)) {
                resources.put(entry, getResourceBytes(entry));
            }
        }
        
        return resources;
    }
    
    /**
     * Gets a Map of InputStreams of the resources starting with the specified
     * prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return Map of InputStreams of resources found
     */
    public Map<String, InputStream> getResourcesStartingWithAsInputStreams(
            final String resourcesPrefix) {
        final Map<String, InputStream> resources =
                new HashMap<String, InputStream>();
        
        for (String entry : entries) {
            if (entry.startsWith(resourcesPrefix)) {
                resources.put(entry, getResourceInputStream(entry));
            }
        }
        
        return resources;
    }
    
    /**
     * Writes a resource to a file.
     *
     * @param resource Resource to write
     * @param file File to write to
     *
     * @throws IOException if the write operation fails
     */
    public void resourceToFile(final byte[] resource, final File file)
    throws IOException {
        final FileOutputStream out = new FileOutputStream(file, false);
        
        out.write(resource);
        
        out.flush();
        out.close();
    }
    
    /**
     * Extracts the specified resource to the specified directory.
     *
     * @param resourceName The name of the resource to extract
     * @param directory The name of the directory to extract to
     *
     * @throws IOException if the write operation fails
     *
     * @return success of failure of the operation
     */
    public boolean extractResource(final String resourceName,
            final String directory) throws IOException {
        final byte[] resource = getResourceBytes(resourceName);
        
        if (resource.length == 0) {
            return false;
        }
        
        final File newDir = new File(directory,
                resourceName.substring(0, resourceName.lastIndexOf('/')) + "/");
        
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
     * @param directory The name of the directory to extract to
     *
     * @throws IOException if the write operation fails
     */
    public void extractResources(final String resourcesPrefix,
            final String directory) throws IOException {
        final Map<String, byte[]> resourcesBytes =
                getResourcesStartingWithAsBytes(resourcesPrefix);
        for (Entry<String, byte[]> entry : resourcesBytes.entrySet()) {
            extractResource(entry.getKey(), directory);
        }
    }
}

