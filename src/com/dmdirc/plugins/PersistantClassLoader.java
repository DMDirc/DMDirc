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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;

public class PersistantClassLoader extends ClassLoader {
	/** Singleton instance of the PersistantClassLoader. */
	private static PersistantClassLoader me;
	
	/** Arraylist containing potential sources of persistant class files */
	private ArrayList<String> resourcesList = new ArrayList<String>();
	
	/**
	 * Last Resource looked in. 
	 * (Usually this is the one with the file we want so we check it first)
	 */
	private String lastResource = "";

	/**
	 * Create a new PersistantClassLoader.
	 */
	private PersistantClassLoader() {
		super();
	}
	
	/**
	 * Retrieves the singleton instance of the PersistantClassLoader.
	 *
	 * @return A singleton instance of PersistantClassLoader.
	 */
	public static final synchronized PersistantClassLoader getPersistantClassLoader() {
		if (me == null) {
			me = new PersistantClassLoader();
		}
		
		return me;
	}

	/**
	 * Load the plugin with the given className
	 *
	 * @param name Class Name of plugin
	 * @param pi The PluginInfo that contains this class
	 * @return plugin class
	 * @throws ClassNotFoundException if the class to be loaded could not be found.
	 */
	public Class< ? > loadClass(final String name, final PluginInfo pi) throws ClassNotFoundException {
		if (pi.hasPersistant()) {
			final String jarfile = pi.getFullFilename();
			if (!resourcesList.contains(jarfile)) {
				lastResource = jarfile;
				resourcesList.add(jarfile);
			}
		}
		return loadClass(name);
	}
	
	/**
	 * Load the plugin with the given className
	 *
	 * @param name Class Name of plugin
	 * @return plugin class
	 * @throws ClassNotFoundException if the class to be loaded could not be found.
	 */
	public Class< ? > loadClass(final String name) throws ClassNotFoundException {
		try {
			return super.loadClass(name);
		} catch (Exception e) {
			byte[] data = getClassData(name.replace('.', '/')+".class");
			if (data != null) {
				return defineClass(name, data);
			}
		}
		return null;
	}
	
	/**
	 * Look in all known sources of persisant classes for file asked for.
	 *
	 * @param classname Class name to define.
	 * @param data Data to define class with.
	 */
	public Class< ? > defineClass(final String classname, final byte[] data) {
		return defineClass(classname, data, 0, data.length);
	}
	
	/**
	 * Look in all known sources of persisant classes for file asked for.
	 *
	 * @param filename File to look for.
	 */
	private byte[] getClassData(final String filename) {
		// Try last resource first
		try {
			ResourceManager rm = ResourceManager.getResourceManager("jar://"+lastResource);
			if (rm.resourceExists(filename)) {
				return rm.getResourceBytes(filename);
			}
		} catch (IOException e) {
			// File might have been deleted, oh well.
		}
		
		// Now try others.
		for (String resource : resourcesList) {
			// See if we have already tried this one
			if (resource.equals(lastResource)) { continue; }
			try {
				ResourceManager rm = ResourceManager.getResourceManager("jar://"+resource);
				if (rm.resourceExists(filename)) {
					lastResource = resource;
					return rm.getResourceBytes(filename);
				}
			} catch (IOException e) {
				// File might have been deleted, oh well.
			}
		}
		return null;
	}
	
}
