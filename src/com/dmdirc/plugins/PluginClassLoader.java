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

package com.dmdirc.plugins;

import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.IOException;

public class PluginClassLoader extends ClassLoader {

    /** The plugin Info object for the plugin we are loading. */
    final PluginInfo pluginInfo;

    /**
     * Create a new PluginClassLoader.
     *
     * @param info PluginInfo this classloader will be for
     */
    public PluginClassLoader(final PluginInfo info) {
        super();
        pluginInfo = info;
    }

    /**
     * Create a new PluginClassLoader.
     *
     * @param info PluginInfo this classloader will be for
     * @param parent Parent ClassLoader
     */
    private PluginClassLoader(final PluginInfo info, final PluginClassLoader parent) {
        super(parent);
        pluginInfo = info;
    }

    /**
     * Get a PluginClassLoader that is a subclassloader of this one.
     *
     * @param info PluginInfo the new classloader will be for
     * @return A classloader configured with this one as its parent
     */
    public PluginClassLoader getSubClassLoader(final PluginInfo info) {
        return (new PluginClassLoader(info, this));
    }

    /**
     * Load the plugin with the given className
     *
     * @param name Class Name of plugin
     * @return plugin class
     * @throws ClassNotFoundException if the class to be loaded could not be found.
     */
    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        return loadClass(name, true);
    }

    /**
     * Have we already loaded the given class name?
     *
     * @param name Name to check.
     * @param checkGlobal Should we check if the GCL has loaded it aswell?
     * @return True if the specified class is loaded, false otherwise
     */
    public boolean isClassLoaded(final String name, final boolean checkGlobal) {
        // Don't duplicate a class
        final Class existing = findLoadedClass(name);
        final boolean gcl = (checkGlobal) ? GlobalClassLoader.getGlobalClassLoader().isClassLoaded(name) : false;
        return (existing != null || gcl);
    }

    /**
     * Load the plugin with the given className
     *
     * @param name Class Name of plugin
     * @param askGlobal Ask the gobal class loaded for this class if we can't find it?
     * @return plugin class
     * @throws ClassNotFoundException if the class to be loaded could not be found.
     */
    @Override
    public Class<?> loadClass(final String name, final boolean askGlobal) throws ClassNotFoundException {
        Class<?> loadedClass = null;
        if (getParent() instanceof PluginClassLoader) {
            try {
                loadedClass = ((PluginClassLoader) getParent()).loadClass(name, false);
                if (loadedClass != null) {
                    return loadedClass;
                }
            } catch (ClassNotFoundException cnfe) {
                /* Parent doesn't have the class, load ourself */
            }
        }

        ResourceManager res;
        try {
            res = pluginInfo.getResourceManager();
        } catch (IOException ioe) {
            throw new ClassNotFoundException("Error with resourcemanager", ioe);
        }

        final String fileName = name.replace('.', '/') + ".class";
        try {
            if (pluginInfo.isPersistent(name) || !res.resourceExists(fileName)) {
                if (!pluginInfo.isPersistent(name) && askGlobal) {
                    return GlobalClassLoader.getGlobalClassLoader().loadClass(name);
                } else {
                    // Try to load class from previous load.
                    try {
                        if (askGlobal) {
                            return GlobalClassLoader.getGlobalClassLoader().loadClass(name, pluginInfo);
                        }
                    } catch (ClassNotFoundException e) {
                        /* Class doesn't exist, we load it ourself below */
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException("Error loading '" + name + "' (wanted by " + pluginInfo.getName() + ") -> " + e.getMessage(), e);
        }


        // Don't duplicate a class
        if (isClassLoaded(name, false)) {
            return findLoadedClass(name);
        }

        // We are ment to be loading this one!
        byte[] data = null;

        if (res.resourceExists(fileName)) {
            data = res.getResourceBytes(fileName);
        } else {
            throw new ClassNotFoundException("Resource '" + name + "' (wanted by " + pluginInfo.getName() + ") does not exist.");
        }

        try {
            if (pluginInfo.isPersistent(name)) {
                GlobalClassLoader.getGlobalClassLoader().defineClass(name, data);
            } else {
                loadedClass = defineClass(name, data, 0, data.length);
            }
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        }

        if (loadedClass == null) {
            throw new ClassNotFoundException("Could not load " + name);
        } else {
            resolveClass(loadedClass);
        }

        return loadedClass;
    }

}
