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

package com.dmdirc.plugins;

import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * This classloader knows about plugins and is used to store persistent classes.
 */
public final class GlobalClassLoader extends ClassLoader {

    /** HashMap containing sources of Global class files. */
    private final Map<String, String> resourcesList = new HashMap<>();
    /** Plugin Manager that owns this GlobalClassLoader. */
    private final PluginManager manager;

    /**
     * Create a new GlobalClassLoader.
     *
     * @param manager Plugin Manager that this GCL is used by.
     */
    public GlobalClassLoader(final PluginManager manager) {
        this.manager = manager;
    }

    /**
     * Have we already loaded the given class name?
     *
     * @param name Name to check.
     *
     * @return True if the class is loaded, false otherwise
     */
    public boolean isClassLoaded(final String name) {
        return findLoadedClass(name) != null;
    }

    /**
     * Load the plugin with the given className.
     *
     * @param name Class Name of plugin
     * @param pi   The PluginInfo that contains this class
     *
     * @return plugin class
     *
     * @throws ClassNotFoundException if the class to be loaded could not be found.
     */
    public Class<?> loadClass(final String name, final PluginInfo pi) throws ClassNotFoundException {

        pi.getPersistentClasses().stream()
                .filter(classname -> !resourcesList.containsKey(classname))
                .forEach(classname -> resourcesList.put(classname,
                        pi.getMetaData().getPluginPath().toAbsolutePath().toString()));
        return loadClass(name);
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            final byte[] data = getClassData(name);
            if (data != null) {
                return defineClass(name, data);
            }
        }

        // Check the other plugins.
        for (PluginInfo pi : manager.getPluginInfos()) {
            final List<String> classList = pi.getClassList();
            if (classList.contains(name) && pi.getPluginClassLoader() != null) {
                return pi.getPluginClassLoader().loadClass(name, false);
            }
        }

        throw new ClassNotFoundException("Not found in global classloader:" + name);
    }

    /**
     * Look in all known sources of persistent classes for file asked for.
     *
     * @param classname Class name to define.
     * @param data      Data to define class with.
     *
     * @return The resulting {@link Class} object
     */
    public Class<?> defineClass(final String classname, final byte... data) {
        return defineClass(classname, data, 0, data.length);
    }

    /**
     * Get the requested class from its plugin jar.
     *
     * @param classname Class to look for.
     */
    @Nullable
    private byte[] getClassData(final String classname) {
        try {
            final String jarname = resourcesList.get(classname);
            if (jarname != null) {
                final ResourceManager rm = ResourceManager.getResourceManager("jar://" + jarname);
                final String filename = classname.replace('.', '/') + ".class";
                if (rm.resourceExists(filename)) {
                    return rm.getResourceBytes(filename);
                }
            }
        } catch (IOException e) {
            // File might have been deleted, oh well.
        }
        return null;
    }

}
