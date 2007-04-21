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
package uk.org.ownage.dmdirc.plugins;

import java.util.Hashtable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PluginManager {
	/**
	 * List of known plugins.
	 */
	private Hashtable<String,Plugin> knownPlugins = new Hashtable<String,Plugin>();
	/**
	 * List of known plugin classNames.
	 */
	private Hashtable<String,String> knownPluginNames = new Hashtable<String,String>();
	
	/**
	 * Directory where plugins are stored.
	 */
	 private String myDir;
	
	/**
	 * Create a new PluginManager.
	 */
	public PluginManager() {
		myDir = ".";
	}
	
	/**
	 * Create a new PluginManager.
	 */
	public PluginManager(final String directory) {
		myDir = directory;
	}
	
	/**
	 * Add a new plugin.
	 *
	 * @param pluginName Name of plugin
	 * @param className Class Name of Plugin object
	 * @return True if loaded.
	 */
	public boolean addPlugin(final String pluginName, final String className) {
		if (knownPlugins.containsKey(pluginName.toLowerCase())) { return false; }
		Plugin plugin = loadPlugin(className);
		if (plugin == null) { return false; }
		plugin.onLoad();
		knownPlugins.put(pluginName.toLowerCase(), plugin);
		knownPluginNames.put(pluginName.toLowerCase(), className);
		return true;
	}
	
	/**
	 * Remove a plugin.
	 *
	 * @param pluginName Name of plugin
	 * @return True if removed.
	 */
	public boolean delPlugin(final String pluginName) {
		if (!knownPlugins.containsKey(pluginName.toLowerCase())) { return false; }
		Plugin plugin = getPlugin(pluginName);
		try {
			plugin.onUnload();
		} catch (Exception e) {
			// TODO: Log Unload Errors somewhere.
		}
		knownPlugins.remove(pluginName.toLowerCase());
		knownPluginNames.remove(pluginName.toLowerCase());
		plugin = null;
		return true;
	}
	
	/**
	 * Reload a plugin.
	 *
	 * @param pluginName Name of plugin
	 * @return True if reloaded.
	 */
	public boolean reloadPlugin(final String pluginName) {
		if (!knownPlugins.containsKey(pluginName.toLowerCase())) { return false; }
		final String filename = knownPluginNames.get(pluginName.toLowerCase());
		delPlugin(pluginName);
		return addPlugin(pluginName, filename);
	}
	
	/**
	 * Get a plugin instance.
	 *
	 * @param pluginName Name of plugin
	 * @return Plugin instance, or null
	 */
	public Plugin getPlugin(final String pluginName) {
		if (!knownPlugins.containsKey(pluginName.toLowerCase())) { return null; }
		return knownPlugins.get(pluginName.toLowerCase());
	}
	
	/**
	 * Get string[] of known plugin names.
	 *
	 * @return string[] of known plugin names.
	 */
	public String[] getNames() {
		final String[] result = new String[knownPlugins.size()];
		int i = 0;
		for (String name : knownPlugins.keySet()) {
			result[i++] = name;
		}
		return result;
	}
	
	/**
	 * Get classname of a given plugin name.
	 *
	 * @return classname of a given plugin name.
	 */
	public String getClassName(final String pluginName) {
		if (!knownPluginNames.containsKey(pluginName)) { return ""; }
		else { return knownPluginNames.get(pluginName); }
	}
	
	/**
	 * Load a plugin with a given className
	 *
	 * @param className Class Name of plugin to load.
	 */
	private Plugin loadPlugin(final String className) {
		Plugin result;
		
		try {
			ClassLoader cl = new PluginClassLoader(myDir);
			
			Class<?> c = (Class<?>)cl.loadClass(className);
			Constructor<?> constructor = c.getConstructor(new Class[] {});
		
			result = (Plugin)constructor.newInstance(new Object[] {});
		} catch (ClassNotFoundException cnfe) {
			System.out.println("ClassNotFoundException "+cnfe.getMessage());
			cnfe.printStackTrace();
			result = null;
		} catch (NoSuchMethodException nsme) {
			System.out.println("NoSuchMethodException");
			result = null;
		} catch (IllegalAccessException iae) {
			System.out.println("IllegalAccessException");
			result = null;
		} catch (InvocationTargetException ite) {
			System.out.println("InvocationTargetException");
			result = null;
		} catch (InstantiationException ie) {
			System.out.println("InstantiationException");
			result = null;
		}
		
		return result;
	}

}
