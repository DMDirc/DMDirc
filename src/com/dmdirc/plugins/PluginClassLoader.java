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
 *
 * SVN: $Id$
 */
package com.dmdirc.plugins;

import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.IOException;

//import java.lang.reflect.Method;
//import java.lang.reflect.InvocationTargetException;

public class PluginClassLoader extends ClassLoader {
	/** The plugin Info object for the plugin we are loading */
	final PluginInfo pluginInfo;
	
	/**
	 * Create a new PluginClassLoader.
	 *
	 * @param directory Directory where plugins are stored.
	 */
	public PluginClassLoader(final PluginInfo info) {
		super();
		pluginInfo = info;
	}
	
	/**
	 * Load the plugin with the given className
	 *
	 * @param name Class Name of plugin
	 * @return plugin class
	 * @throws ClassNotFoundException if the class to be loaded could not be found.
	 */
	public Class< ? > loadClass(final String name) throws ClassNotFoundException {
		ResourceManager res;
		try {
			res = pluginInfo.getResourceManager();
		} catch (IOException ioe) {
			throw new ClassNotFoundException("Error with resourcemanager", ioe);
		}
	
		Class< ? > loadedClass = null;

		final String fileName = name.replace('.', '/')+".class";
		try {
			if (pluginInfo.isPersistant(name) || !res.resourceExists(fileName)) {
				if (!pluginInfo.isPersistant(name)) {
					return GlobalClassLoader.getGlobalClassLoader().loadClass(name);
				} else {
					// Try to load class from previous load.
					try {
						return GlobalClassLoader.getGlobalClassLoader().loadClass(name, pluginInfo);
					} catch (Exception e) {
						/* Class doesn't exist, we load it outself below */
					}
				}
			}
		} catch (NoClassDefFoundError e) {
			throw new ClassNotFoundException("Error loading '"+name+"' (wanted by "+pluginInfo.getName()+") -> "+e.getMessage(), e);
		}
		
		
		// Don't duplicate a class
		Class existing = findLoadedClass(name);
		if (existing != null) { return existing; }
		
		// We are ment to be loading this one!
		byte[] data = null;
		
		if (res.resourceExists(fileName)) {
			data = res.getResourceBytes(fileName);
		} else {
			throw new ClassNotFoundException("Resource '"+name+"' (wanted by "+pluginInfo.getName()+") does not exist.");
		}
		
		try {
			if (pluginInfo.isPersistant(name)) {
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
