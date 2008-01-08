/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides a launch method independant way of accessing resources.
 */
public abstract class ResourceManager {
    
    /** Previously assigned ResourceManager. */
    private static ResourceManager me;
    
    /** Enum indicating resource manager type. */
    public enum Type {
        /** File resource manager. */
        FILE,
        /** Jar resource manager. */
        JAR,
        /** No resource manager. */
        NONE,
    }
    
    /**
     * Returns an appropriate instance of ResourceManager.
     *
     * @return ResourceManager implementation
     */
    public static final synchronized ResourceManager getResourceManager() {
        if (me == null) {
            String path = Thread.currentThread().getContextClassLoader().
                    getResource("com/dmdirc/Main.class").getPath();
            
            try {
                path = java.net.URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to decode path");
            }
            
            final String protocol = Thread.currentThread().getContextClassLoader().
                    getResource("com/dmdirc/Main.class").getProtocol();
            
            try {
                if ("file".equals(protocol)) {
                    me = new FileResourceManager(Thread.currentThread().
                            getContextClassLoader().getResource("").getPath());
                } else if ("jar".equals(protocol)) {
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        me = new ZipResourceManager(path.substring(6, path.length() - 23));
                    } else {
                        me = new ZipResourceManager(path.substring(5, path.length() - 23));
                    }
                }
            } catch (IOException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to determine how DMDirc"
                        + " has been executed", ex);
            }
        }
        return me;
    }
    
    /**
     * Returns an appropriate instance of ResourceManager for the specified url string.
     *
     * @param type  file://path/to/base
     *              jar://path/to/jar
     *              zip://path/to/zip
     * 
     * @return ResourceManager implementation
     * 
     * @throws java.io.IOException if an IO Error occurs opening the file
     */
    public static final ResourceManager getResourceManager(final String type) throws IOException {
        if (type.startsWith("file://")) {
            return new FileResourceManager(type.substring(7));
        } else if (type.startsWith("jar://") || type.startsWith("zip://")) {
            return new ZipResourceManager(type.substring(6));
        } else {
            throw new IllegalArgumentException("Unknown resource manager type");
        }
    }
    
    /**
     * Returns the working directory for the application.
     * 
     * @return Current working directory
     */
    public static final synchronized String getCurrentWorkingDirectory() {
        final URL resource = Thread.currentThread().getContextClassLoader().
                        getResource("com/dmdirc/Main.class");
        String path = "";
        
        final String protocol = resource.getProtocol();
        
        if ("file".equals(protocol)) {
            path = Thread.currentThread().
                    getContextClassLoader().getResource("").getPath();
        } else if ("jar".equals(protocol)) {
            final String tempPath = resource.getPath();
            if (System.getProperty("os.name").startsWith("Windows")) {
                path = tempPath.substring(6, tempPath.length() - 23);
            } else {
                path = tempPath.substring(5, tempPath.length() - 23);
            }
            path = path.substring(0, path.lastIndexOf('/') + 1);
        }
        
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to decode path");
            path = "";
        }
        return path;
    }
    
    /**
     * Returns the type of the resource manager.
     *
     * @return ResourceManager Type
     */
    public static final synchronized Type getResourceManagerType() {
        if (me == null) {
            getResourceManager();
        }
        
        if (me instanceof ZipResourceManager) {
            return Type.JAR;
        } else if (me instanceof FileResourceManager) {
            return Type.FILE;
        } else {
            return Type.NONE;
        }
    }
    
    /**
     * Writes a resource to a file.
     *
     * @param resource Resource to write
     * @param file File to write to
     *
     * @throws IOException if the write operation fails
     */
    public final void resourceToFile(final byte[] resource, final File file)
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
     * @param usePath If true, append the path of the files in the resource
     * to the extraction path
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
        
        File newDir;
        
        if (usePath) {
            newDir = new File(directory,
                    resourceName.substring(0, resourceName.lastIndexOf('/')) + "/");
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
     * @param directory The name of the directory to extract to
     * @param usePath If true, append the path of the files in the resource
     * to the extraction path
     *
     * @throws IOException if the write operation fails
     */
    public final void extractResources(final String resourcesPrefix,
            final String directory, final boolean usePath) throws IOException {
        final Map<String, byte[]> resourcesBytes =
                getResourcesStartingWithAsBytes(resourcesPrefix);
        for (Entry<String, byte[]> entry : resourcesBytes.entrySet()) {
            extractResource(entry.getKey(), directory, usePath);
        }
    }
    
    /**
     * Extracts the specified resources to the specified directory.
     *
     * @param resourcesPrefix The prefix of the resources to extract
     * @param directory The name of the directory to extract to
     *
     * @throws IOException if the write operation fails
     */
    public final void extractResources(final String resourcesPrefix,
            final String directory) throws IOException {
        extractResources(resourcesPrefix, directory, true);
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
     * Gets a Map of byte[]s of the resources starting with the specified
     * prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return Map of byte[]s of resources found
     */
    public abstract Map<String, byte[]> getResourcesStartingWithAsBytes(
            final String resourcesPrefix);
    
    /**
     * Gets a Map of InputStreams of the resources starting with the specified
     * prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return Map of InputStreams of resources found
     */
    public abstract Map<String, InputStream> getResourcesStartingWithAsInputStreams(
            final String resourcesPrefix);
    
    /**
     * Gets a List of the resources starting with the specified
     * prefix.
     *
     * @param resourcesPrefix Prefix of the resources to return
     *
     * @return List of resources found
     */
    public abstract List<String> getResourcesStartingWith(final String resourcesPrefix);
}
