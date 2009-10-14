/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.util.StreamUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an easy way to access files inside a project.
 */
public final class FileResourceManager extends ResourceManager {
    
    /** Base path for the project. */
    private final String basePath;
    
    /**
     * Creates a new instance of FileResourceManager.
     * 
     * @param basePath Base path for the resource manager
     */
    protected FileResourceManager(final String basePath) {
        super();
        
        this.basePath = basePath;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean resourceExists(final String resource) {
        final File file;
        
        if (resource.startsWith(basePath)) {
            file = new File(resource);
        } else {
            file = new File(basePath, resource);
        }
        
        return file.exists() && !file.isDirectory();
    }
    
    /** {@inheritDoc} */
    @Override
    public byte[] getResourceBytes(final String resource) {
        FileInputStream inputStream = null;
        final File file;
        
        if (resource.startsWith(basePath)) {
            file = new File(resource);
        } else {
            file = new File(basePath, resource);
        }
        
        if (!file.exists()) {
            return new byte[0];
        }
        
        if (file.isDirectory()) {
            return new byte[0];
        }
        
        final byte[] bytes = new byte[(int) file.length()];
        
        try {
            inputStream = new FileInputStream(file);
            inputStream.read(bytes);
        } catch (IOException ex) {
            return new byte[0];
        } finally {
            StreamUtil.close(inputStream);
        }

        return bytes;
    }
    
    /** {@inheritDoc} */
    @Override
    public InputStream getResourceInputStream(final String resource) {
        final File file;
        
        if (resource.startsWith(basePath)) {
            file = new File(resource);
        } else {
            file = new File(basePath, resource);
        }
        
        if (!file.exists()) {
            return null;
        }
        
        if (file.isDirectory()) {
            return null;
        }
        
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Map<String, byte[]> getResourcesEndingWithAsBytes(
            final String resourcesSuffix) {
        final List<File> files = getFileListing(new File(basePath));
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        
        for (File file : files) {
            final String path = file.getPath().substring(basePath.length(),
                    file.getPath().length());
            if (path.endsWith(resourcesSuffix)) {
                resources.put(path, getResourceBytes(path));
            }
        }
        
        return resources;
    }
    
    /** {@inheritDoc} */
    @Override
    public Map<String, byte[]> getResourcesStartingWithAsBytes(
            final String resourcesPrefix) {
        final List<File> files = getFileListing(new File(basePath));
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        
        for (File file : files) {
            final String path = file.getPath().substring(basePath.length(),
                    file.getPath().length());
            if (path.startsWith(resourcesPrefix)) {
                resources.put(path, getResourceBytes(path));
            }
        }
        
        return resources;
    }
    
    /** {@inheritDoc} */
    @Override
    public Map<String, InputStream> getResourcesStartingWithAsInputStreams(
            final String resourcesPrefix) {
        final List<File> files = getFileListing(new File(basePath));
        final Map<String, InputStream> resources = new HashMap<String, InputStream>();
        
        for (File file : files) {
            final String path = file.getPath().substring(basePath.length(),
                    file.getPath().length());
            if (path.startsWith(resourcesPrefix)) {
                resources.put(path, getResourceInputStream(path));
            }
        }
        
        return resources;
    }
    
    /** {@inheritDoc} */
    @Override
    public List<String> getResourcesStartingWith(final String resourcesPrefix) {
        final List<File> files = getFileListing(new File(basePath));
        final List<String> resources = new ArrayList<String>();
        
        for (File file : files) {
            final String path = file.getPath().substring(basePath.length(),
                    file.getPath().length());
            if (path.startsWith(resourcesPrefix)) {
                resources.add(path);
            }
        }
        
        return resources;
    }
    
    /**
     * Returns a resursive listing of a directory tree.
     *
     * @param startingDirectory Starting directory for the file listing
     *
     * @return Recursive directory listing
     */
    private static List<File> getFileListing(final File startingDirectory) {
        final List<File> result = new ArrayList<File>();
        
        if (startingDirectory.listFiles() == null) {
            return result;
        }
        
        final List<File> files = Arrays.asList(startingDirectory.listFiles());
        for (File file : files) {
            if (file.isFile()) {
                result.add(file);
            } else {
                result.addAll(getFileListing(file));
            }
        }
        return result;
    }
}
