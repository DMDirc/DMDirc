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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.Logger;
import com.dmdirc.logger.ErrorLevel;

public class PluginManager {
	/**
	 * List of known plugins.
	 */
	private final Hashtable<String,Plugin> knownPlugins = new Hashtable<String,Plugin>();
	
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
		myDir = Main.getConfigDir() + "plugins" + fs;
	}
	
	/**
	 * Autoloads plugins.
	 */
	private void doAutoLoad() {
		if (IdentityManager.getGlobalConfig().hasOption("plugins", "autoload")) {
			final String[] autoLoadList = IdentityManager.getGlobalConfig().getOption("plugins", "autoload").split("\n");
			for (String plugin : autoLoadList) {
				plugin = plugin.trim();
				if (!plugin.isEmpty() && plugin.charAt(0) != '#' && addPlugin(plugin)) {
					getPlugin(plugin).setActive(true);
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
			me.doAutoLoad();
		}
		return me;
	}
		
	/**
	 * Add a new plugin.
	 *
	 * @param className Class Name of Plugin object
	 * @return True if loaded, false if failed to load or if already loaded.
	 */
	public boolean addPlugin(final String className) {
		if (knownPlugins.containsKey(className.toLowerCase())) { return false; }
		try {
			final Plugin plugin = loadPlugin(className);
			if (plugin == null) { return false; }
			if (plugin.onLoad()) {
				knownPlugins.put(className.toLowerCase(), plugin);
				ActionManager.processEvent(CoreActionType.PLUGIN_LOADED, null, plugin);
				return true;
			}
		} catch (Exception e) {
			Logger.userError(ErrorLevel.MEDIUM, "Error loading "+className);
		}
		return false;
	}
	
	/**
	 * Remove a plugin.
	 *
	 * @param className Class name of plugin
	 * @return True if removed.
	 */
	public boolean delPlugin(final String className) {
		if (!knownPlugins.containsKey(className.toLowerCase())) { return false; }
		Plugin plugin = getPlugin(className);
		try {
			plugin.setActive(false);
			ActionManager.processEvent(CoreActionType.PLUGIN_UNLOADED, null, plugin);
			plugin.onUnload();
		} catch (Exception e) {
			Logger.userError(ErrorLevel.MEDIUM, "Error in onUnload() for '"+className+"'");
		}
		knownPlugins.remove(className.toLowerCase());
		plugin = null;
		return true;
	}
	
	/**
	 * Reload a plugin.
	 *
	 * @param className Class name of plugin
	 * @return True if reloaded.
	 */
	public boolean reloadPlugin(final String className) {
		if (!knownPlugins.containsKey(className.toLowerCase())) { return false; }
		delPlugin(className);
		return addPlugin(className);
	}
	
	/**
	 * Reload all plugins.
	 */
	public void reloadAllPlugins() {
		for (String pluginName : getNames()) {
			reloadPlugin(pluginName);
		}
	}
	
	/**
	 * Get a plugin instance.
	 *
	 * @param className Class name of plugin
	 * @return Plugin instance, or null
	 */
	public Plugin getPlugin(final String className) {
		if (!knownPlugins.containsKey(className.toLowerCase())) { return null; }
		return knownPlugins.get(className.toLowerCase());
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
	 * Retrieves a list of all installed plugins.
	 * Any file under the main plugin directory (~/.DMDirc/plugins or similar)
	 * that matches *Plugin.class is deemed to be a valid plugin.
	 *
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
				// Change / (or \) to .
				target = target.replace(File.separatorChar, '.');
				addPlugin(target);
			}
		}
		
		for (String name : knownPlugins.keySet()) {
			res.add(getPlugin(name));
		}
		
		return res;
	}
	
	/**
	 * Update the autoLoadList
	 *
	 * @param plugin to add/remove (Decided automatically based on isActive())
	 */
	public void updateAutoLoad(final Plugin plugin) {
		if (IdentityManager.getGlobalConfig().hasOption("plugins", "autoload")) {
			final String[] autoLoadList = IdentityManager.getGlobalConfig().getOption("plugins", "autoload").split("\n");
			final StringBuffer newAutoLoadList = new StringBuffer();
			boolean found = false;
			for (String pluginName : autoLoadList) {
				pluginName = pluginName.trim();
				if (pluginName.equals(plugin.getClass().getName())) {
					found = true;
					if (plugin.isActive()) {
						newAutoLoadList.append(pluginName+"\n");
					}
				} else if (!pluginName.isEmpty()) {
					newAutoLoadList.append(pluginName+"\n");
				}
			}
			if (!found && plugin.isActive()) {
				newAutoLoadList.append(plugin.getClass().getName()+"\n");
			}
			IdentityManager.getConfigIdentity().setOption("plugins", "autoload", newAutoLoadList.toString());
		} else if (plugin.isActive()) {
			IdentityManager.getConfigIdentity().setOption("plugins", "autoload", plugin.getClass().getName());
		}
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
	 * Load a plugin with a given className
	 *
	 * @param className Class Name of plugin to load.
	 * @return Loaded plugin or null
	 */
	private Plugin loadPlugin(final String className) {
		Plugin result;
		try {
			final ClassLoader cl = new PluginClassLoader(myDir);
			
			final Class<?> c = cl.loadClass(className);
			final Constructor<?> constructor = c.getConstructor(new Class[] {});
		
			final Object temp = constructor.newInstance(new Object[] {});
			
			if (temp instanceof Plugin) {
				result = (Plugin) temp;
			} else {
				result = null;
			}
		} catch (ClassNotFoundException cnfe) {
			Logger.userError(ErrorLevel.LOW, "Class not found ('"+className+"')");
			result = null;
		} catch (NoSuchMethodException nsme) {
			Logger.userError(ErrorLevel.LOW, "Constructor missing ('"+className+"')");
			result = null;
		} catch (IllegalAccessException iae) {
			Logger.userError(ErrorLevel.LOW, "Unable to access constructor ('"+className+"')");
			result = null;
		} catch (InvocationTargetException ite) {
			Logger.userError(ErrorLevel.LOW, "Unable to invoke target ('"+className+"')");
			result = null;
		} catch (InstantiationException ie) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+className+"')");
			result = null;
		} catch (NoClassDefFoundError ncdf) {
			Logger.userError(ErrorLevel.LOW, "Unable to instantiate plugin ('"+className+"'): Unable to find class: " + ncdf.getMessage());
			result = null;
		}
		
		return result;
	}

	/**
	 * Send an event of the specified type to plugins.
	 *
	 * @param type The type of the event to process
	 * @param format Format of messages that are about to be sent. (May be null)
	 * @param arguments The arguments for the event
	 */
	public void processEvent(final ActionType type, final StringBuffer format, final Object ... arguments) {
		for (String pluginName : knownPlugins.keySet()) {
			final Plugin plugin = knownPlugins.get(pluginName);
			if (plugin instanceof EventPlugin && plugin.isActive()) {
				((EventPlugin)plugin).processEvent(type, format, arguments);
			}
		}
	}
	
}
