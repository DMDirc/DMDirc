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

package com.dmdirc.plugins;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class PluginClassLoader extends ClassLoader {

    /** The plugin Info object for the plugin we are loading. */
    private final PluginInfo pluginInfo;
    /** Global Class Loader */
    private final GlobalClassLoader globalLoader;
    /** The parent class loaders. */
    private final PluginClassLoader[] parents;

    /**
     * Create a new PluginClassLoader.
     *
     * @param info         PluginInfo this classloader will be for
     * @param globalLoader Global class loader to use where needed.
     * @param parents      Parent ClassLoaders
     */
    public PluginClassLoader(final PluginInfo info, final GlobalClassLoader globalLoader,
            final PluginClassLoader... parents) {
        this.pluginInfo = info;
        this.parents = parents;
        this.globalLoader = globalLoader;
    }

    /**
     * Get a PluginClassLoader that is a subclassloader of this one.
     *
     * @param info PluginInfo the new classloader will be for
     *
     * @return A classloader configured with this one as its parent
     */
    public PluginClassLoader getSubClassLoader(final PluginInfo info) {
        return new PluginClassLoader(info, globalLoader, this);
    }

    /**
     * Load the plugin with the given className.
     *
     * @param name Class Name of plugin
     *
     * @return plugin class
     *
     * @throws ClassNotFoundException if the class to be loaded could not be found.
     */
    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        return loadClass(name, true);
    }

    /**
     * Have we already loaded the given class name?
     *
     * @param name        Name to check.
     * @param checkGlobal Should we check if the GCL has loaded it as well?
     *
     * @return True if the specified class is loaded, false otherwise
     */
    public boolean isClassLoaded(final String name, final boolean checkGlobal) {
        return findLoadedClass(name) != null || (checkGlobal
                && globalLoader.isClassLoaded(name));
    }

    /**
     * Load the plugin with the given className.
     *
     * @param name      Class Name of plugin
     * @param askGlobal Ask the global class loaded for this class if we can't find it?
     *
     * @return plugin class
     *
     * @throws ClassNotFoundException if the class to be loaded could not be found.
     */
    @Override
    public Class<?> loadClass(final String name, final boolean askGlobal) throws
            ClassNotFoundException {
        Class<?> loadedClass = null;
        for (PluginClassLoader parent : parents) {
            try {
                loadedClass = parent.loadClass(name, false);
                if (loadedClass != null) {
                    return loadedClass;
                }
            } catch (ClassNotFoundException cnfe) {
                // Parent doesn't have the class, carry on trying...
            }
        }

        final String fileName = name.replace('.', '/') + ".class";
        try {
            if (pluginInfo.isPersistent(name) || !Files.exists(
                    pluginInfo.getPath(fileName))) {
                if (!pluginInfo.isPersistent(name) && askGlobal) {
                    return globalLoader.loadClass(name);
                } else {
                    // Try to load class from previous load.
                    try {
                        if (askGlobal) {
                            return globalLoader.loadClass(name, pluginInfo);
                        }
                    } catch (ClassNotFoundException e) {
                        /* Class doesn't exist, we load it ourself below */
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException("Error loading '" + name + "' (wanted by "
                    + pluginInfo.getMetaData().getName() + ") -> " + e.getMessage(), e);
        }

        // Don't duplicate a class
        if (isClassLoaded(name, false)) {
            return findLoadedClass(name);
        }

        // We are meant to be loading this one!
        final byte[] data;
        if (Files.exists(pluginInfo.getPath(fileName))) {
            try {
                data = Files.readAllBytes(pluginInfo.getPath(fileName));
            } catch (IOException ex) {
                throw new ClassNotFoundException(ex.getMessage(), ex);
            }
        } else {
            throw new ClassNotFoundException("Resource '" + name + "' (wanted by " + pluginInfo.
                    getMetaData().getName() + ") does not exist.");
        }

        try {
            if (pluginInfo.isPersistent(name)) {
                globalLoader.defineClass(name, data);
            } else {
                loadedClass = defineClass(name, data, 0, data.length);
            }
        } catch (LinkageError e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        }

        if (loadedClass == null) {
            throw new ClassNotFoundException("Could not load " + name);
        } else {
            resolveClass(loadedClass);
        }

        return loadedClass;
    }

    @Override
    protected URL findResource(final String name) {
        try {
            final URL url = pluginInfo.getPath(name).toUri().toURL();
            if (url != null) {
                return url;
            }
        } catch (IOException ioe) {
            // Do nothing, fall through
        }

        // Try the parents
        for (PluginClassLoader parent : parents) {
            final URL url = parent.findResource(name);

            if (url != null) {
                return url;
            }
        }

        return super.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(final String name)
            throws IOException {
        final URL resource = findResource(name);
        final List<URL> resources = new ArrayList<>();

        if (resource != null) {
            resources.add(resource);
        }

        final Enumeration<URL> urls = super.findResources(name);

        while (urls.hasMoreElements()) {
            resources.add(urls.nextElement());
        }

        return Collections.enumeration(resources);
    }

}
