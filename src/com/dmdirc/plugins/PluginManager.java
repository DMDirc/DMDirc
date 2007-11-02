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
	private final Hashtable<String,PluginInfo> knownPlugins = new Hashtable<String,PluginInfo>();
	
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
					getPluginInfo(plugin).loadPlugin();
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
	 * @param filename Filename of Plugin jar
	 * @return True if loaded, false if failed to load or if already loaded.
	 */
	public boolean addPlugin(final String filename) {
		if (knownPlugins.containsKey(filename.toLowerCase())) { return false; }
		try {
			final PluginInfo pluginInfo = new PluginInfo(filename);
			try {
				pluginInfo.checkRequirements();
				knownPlugins.put(filename.toLowerCase(), pluginInfo);
				return true;
			} catch (PluginException e) {
				Logger.userError(ErrorLevel.MEDIUM, "Plugin requirements not met: "+e.getMessage(), e);
			}
		} catch (Exception e) {
			Logger.userError(ErrorLevel.MEDIUM, e.getMessage(), e);
		}
		return false;
	}
	
	/**
	 * Remove a plugin.
	 *
	 * @param filename Filename of Plugin jar
	 * @return True if removed.
	 */
	public boolean delPlugin(final String filename) {
		if (!knownPlugins.containsKey(filename.toLowerCase())) { return false; }
		
		PluginInfo pluginInfo = getPluginInfo(filename);
		try {
			ActionManager.processEvent(CoreActionType.PLUGIN_UNLOADED, null, pluginInfo);
			pluginInfo.unloadPlugin();
		} catch (Exception e) {
			Logger.userError(ErrorLevel.MEDIUM, e.getMessage(), e);
		}
		knownPlugins.remove(filename.toLowerCase());
		pluginInfo = null;
		return true;
	}
	
	/**
	 * Reload a plugin.
	 *
	 * @param filename Filename of Plugin jar
	 * @return True if reloaded.
	 */
	public boolean reloadPlugin(final String filename) {
		if (!knownPlugins.containsKey(filename.toLowerCase())) { return false; }
		final boolean wasLoaded = getPluginInfo(filename).isLoaded();
		delPlugin(filename);
		final boolean result = addPlugin(filename);
		if (wasLoaded) {
			getPluginInfo(filename).loadPlugin();
		}
		return result;
		
	}
	
	/**
	 * Reload all plugins.
	 */
	public void reloadAllPlugins() {
		for (String pluginName : getFilenames()) {
			reloadPlugin(pluginName);
		}
	}
	
	/**
	 * Get a plugin instance.
	 *
	 * @param filename File name of plugin jar
	 * @return PluginInfo instance, or null
	 */
	public PluginInfo getPluginInfo(final String filename) {
		if (!knownPlugins.containsKey(filename.toLowerCase())) { return null; }
		return knownPlugins.get(filename.toLowerCase());
	}
	
	/**
	 * Get a plugin instance by plugin name.
	 *
	 * @param name Name of plugin to find.
	 * @return PluginInfo instance, or null
	 */
	public PluginInfo getPluginInfoByName(final String name) {
		for (String pluginName : knownPlugins.keySet()) {
			final PluginInfo pluginInfo = knownPlugins.get(pluginName);
			if (pluginInfo.getName().equalsIgnoreCase(name)) {
				return pluginInfo;
			}
		}
		return null;
	}
	
	/**
	 * Get directory where plugins are stored.
	 *
	 * @return Directory where plugins are stored.
	 */
	public String getDirectory() {
		return myDir;
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
			result[i++] = knownPlugins.get(name).getName();
		}
		return result;
	}
	
	/**
	 * Get string[] of known plugin file names.
	 *
	 * @return string[] of known plugin file names.
	 */
	public String[] getFilenames() {
		final String[] result = new String[knownPlugins.size()];
		int i = 0;
		for (String name : knownPlugins.keySet()) {
			result[i++] = knownPlugins.get(name).getFilename();
		}
		return result;
	}
	
	
	/**
	 * Get string[] of known plugin mainclass names.
	 *
	 * @return string[] of known plugin mainclass names.
	 */
	public String[] getMainClassNames() {
		final String[] result = new String[knownPlugins.size()];
		int i = 0;
		for (String name : knownPlugins.keySet()) {
			result[i++] = knownPlugins.get(name).getMainClass();
		}
		return result;
	}
	
	/**
	 * Retrieves a list of all installed plugins.
	 * Any file under the main plugin directory (~/.DMDirc/plugins or similar)
	 * that matches *.jar is deemed to be a valid plugin.
	 *
	 * @return A list of all installed plugins
	 */
	public List<PluginInfo> getPossiblePluginInfos() {
		final ArrayList<PluginInfo> res = new ArrayList<PluginInfo>();
		
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
			} else if (dir.isFile() && dir.getName().matches("^.*\\.jar$")) {
				String target = dir.getPath();
//				// Remove the plugin dir & .class suffix
//				target = target.substring(myDir.length(), target.length() - 6);
//				// Change / (or \) to .
//				target = target.replace(File.separatorChar, '.');

				// Remove the plugin dir
				target = target.substring(myDir.length(), target.length());
				addPlugin(target);
			}
		}
		
		for (String name : knownPlugins.keySet()) {
			res.add(getPluginInfo(name));
		}
		
		return res;
	}
	
	/**
	 * Update the autoLoadList
	 *
	 * @param plugin to add/remove (Decided automatically based on isLoaded())
	 */
	public void updateAutoLoad(final PluginInfo plugin) {
		if (IdentityManager.getGlobalConfig().hasOption("plugins", "autoload")) {
			final String[] autoLoadList = IdentityManager.getGlobalConfig().getOption("plugins", "autoload").split("\n");
			final StringBuffer newAutoLoadList = new StringBuffer();
			boolean found = false;
			for (String pluginName : autoLoadList) {
				pluginName = pluginName.trim();
				if (pluginName.equals(plugin.getFilename())) {
					found = true;
					if (plugin.isLoaded()) {
						newAutoLoadList.append(pluginName+"\n");
					}
				} else if (!pluginName.isEmpty()) {
					newAutoLoadList.append(pluginName+"\n");
				}
			}
			if (!found && plugin.isLoaded()) {
				newAutoLoadList.append(plugin.getFilename()+"\n");
			}
			IdentityManager.getConfigIdentity().setOption("plugins", "autoload", newAutoLoadList.toString());
		} else if (plugin.isLoaded()) {
			IdentityManager.getConfigIdentity().setOption("plugins", "autoload", plugin.getFilename());
		}
	}
	
	/**
	 * Get PluginInfo[] of known plugins.
	 *
	 * @return PluginInfo[] of known plugins.
	 */
	public PluginInfo[] getPluginInfos() {
		final PluginInfo[] result = new PluginInfo[knownPlugins.size()];
		int i = 0;
		for (String name : knownPlugins.keySet()) {
			result[i++] = getPluginInfo(name);
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
			final PluginInfo pluginInfo = knownPlugins.get(pluginName);
			if (pluginInfo.isLoaded()) {
				final Plugin plugin = pluginInfo.getPlugin();
				if (plugin instanceof EventPlugin) {
					try {
						((EventPlugin)plugin).processEvent(type, format, arguments);
					} catch (Exception e) {
						Logger.userError(ErrorLevel.LOW, "Error with processEvent for "+pluginName+" ("+type+") - "+e.getMessage(), e);
					}
				}
			}
		}
	}
	
}
