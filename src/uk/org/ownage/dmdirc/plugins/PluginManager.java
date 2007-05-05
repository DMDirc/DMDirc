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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.actions.ActionType;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.logger.ErrorLevel;

public class PluginManager {
	/**
	 * List of known plugins.
	 */
	private final Hashtable<String,Plugin> knownPlugins = new Hashtable<String,Plugin>();
	/**
	 * List of known plugin classNames.
	 */
	private final Hashtable<String,String> knownPluginNames = new Hashtable<String,String>();
	
	/**
	 * Directory where plugins are stored.
	 */
	 private final String myDir;
	
	/**
	 * Singleton instance of the plugin manager.
	 */
	private static PluginManager me;
	
	/**
	 * Create a new PluginManager.
	 */
	private PluginManager() {
		final String fs = System.getProperty("file.separator");
		myDir = Config.getConfigDir() + "plugins" + fs;

		if (Config.hasOption("plugins", "autoload")) {
			final String[] autoLoadList = Config.getOption("plugins", "autoload").split("\n");
			for (String plugin : autoLoadList) {
                        	plugin = plugin.trim();
				if (plugin.length() > 0 && plugin.charAt(0) != '#' && addPlugin(plugin, plugin)) {
					getPlugin(plugin).onActivate();
				}
			}
		}
	}
	
	/**
	 * Retrieves the singleton instance of the plugin manager.
	 * @return A singleton instance of PluginManager.
	 */
	public final static synchronized PluginManager getPluginManager() {
		if (me == null) {
			me = new PluginManager();
		}
		return me;
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
		try {
			final Plugin plugin = loadPlugin(className);
			if (plugin == null) { return false; }
			if (plugin.onLoad()) {
				knownPlugins.put(pluginName.toLowerCase(), plugin);
				knownPluginNames.put(pluginName.toLowerCase(), className);
				return true;
			}
		} catch (Exception e) {
			Logger.error(ErrorLevel.ERROR, "[addPlugin] Error loading '"+pluginName+"' ["+className+"]", e);
		}
		return false;
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
			Logger.error(ErrorLevel.ERROR, "[delPlugin] Error in onUnload() for '"+pluginName+"'", e);
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
	 * Retrieves a list of all installed plugins. Any file under the
	 * main plugin directory (~/.DMDirc/plugins or similar) that matches
	 * *Plugin.class is deemed to be a valid plugin.
	 * @return A list of all installed plugins
	 */
	public List<Plugin> getPossiblePlugins() {
		final ArrayList<Plugin> res = new ArrayList<Plugin>();
		
		final LinkedList<File> dirs = new LinkedList<File>();
		
		dirs.add(new File(myDir));
		
		// I guess a mess of symlinks could make this loop forever.
		// TODO: Add a list of things we've checked
		while (!dirs.isEmpty()) {
			final File dir = dirs.pop();
			if (dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					dirs.add(file);
				}
			} else if (dir.isFile() && dir.getName().matches("^.*Plugin\\.class$")) {
				String target = dir.getPath();
				// Remove the plugin dir & .class suffix
				target = target.substring(myDir.length(), target.length() - 6);
				// Change / to .
				target = target.replace('/', '.');
				
				if (knownPlugins.containsKey(target)) {
					res.add(knownPlugins.get(target));
				} else {
					res.add(loadPlugin(target));
				}
			}
		}
		
		return res;
	}
	
	/**
	 * Get Plugin[] of known plugins.
	 *
	 * @return Plugin[] of known plugins.
	 */
	public Plugin[] getPlugins() {
		final Plugin[] result = new Plugin[knownPlugins.size()];
		int i = 0;
		for (String name : knownPlugins.keySet()) {
			result[i++] = getPlugin(name);
		}
		return result;
	}
	
	/**
	 * Get classname of a given plugin name.
	 *
	 * @return classname of a given plugin name.
	 */
	public String getClassName(final String pluginName) {
		if (knownPluginNames.containsKey(pluginName)) {
			return knownPluginNames.get(pluginName);
		} else {
			return "";
		}
	}
	
	/**
	 * Load a plugin with a given className
	 *
	 * @param className Class Name of plugin to load.
	 */
	private Plugin loadPlugin(final String className) {
		Plugin result;
		try {
			final ClassLoader cl = new PluginClassLoader(myDir);
			
			final Class<?> c = cl.loadClass(className);
			final Constructor<?> constructor = c.getConstructor(new Class[] {});
		
			result = (Plugin)constructor.newInstance(new Object[] {});
		} catch (ClassNotFoundException cnfe) {
			Logger.error(ErrorLevel.ERROR, "[LoadPlugin] Class '"+className+"' not found", cnfe);
			result = null;
		} catch (NoSuchMethodException nsme) {
			Logger.error(ErrorLevel.ERROR, "[LoadPlugin] Constructor missing", nsme);
			result = null;
		} catch (IllegalAccessException iae) {
			Logger.error(ErrorLevel.ERROR, "[LoadPlugin] Unable to access constructor", iae);
			result = null;
		} catch (InvocationTargetException ite) {
			Logger.error(ErrorLevel.ERROR, "[LoadPlugin] Unable to invoke target", ite);
			result = null;
		} catch (InstantiationException ie) {
			Logger.error(ErrorLevel.ERROR, "[LoadPlugin] Unable to instantiate plugin", ie);
			result = null;
		}
		
		return result;
	}

	/**
	 * Send an event of the specified type to plugins.
	 *
	 * @param type The type of the event to process
	 * @param arguments The arguments for the event
	 */
	public void processEvent(final ActionType type, final Object ... arguments) {
		for (String pluginName : knownPlugins.keySet()) {
			final Plugin plugin = knownPlugins.get(pluginName);
			if (plugin instanceof EventPlugin && plugin.isActive()) {
				((EventPlugin)plugin).processEvent(type, arguments);
			}
		}
	}
	
}
