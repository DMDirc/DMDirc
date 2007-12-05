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
 */
package com.dmdirc.plugins;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.Logger;
import com.dmdirc.logger.ErrorLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PluginManager {
	/** List of known plugins. */
	private final Map<String, PluginInfo> knownPlugins = new Hashtable<String, PluginInfo>();

	/** Directory where plugins are stored. */
	private final String myDir;

	/** Singleton instance of the plugin manager. */
	private static PluginManager me;

	/**
	 * Creates a new instance of PluginManager.
	 */
	private PluginManager() {
		final String fs = System.getProperty("file.separator");
		myDir = Main.getConfigDir() + "plugins" + fs;
	}

	/**
	 * Autoloads plugins.
	 */
	private void doAutoLoad() {
		for (String plugin : IdentityManager.getGlobalConfig().getOptionList("plugins", "autoload")) {
			plugin = plugin.trim();
			if (!plugin.isEmpty() && plugin.charAt(0) != '#' && addPlugin(plugin)) {
				getPluginInfo(plugin).loadPlugin();
			}
		}
	}

	/**
	 * Retrieves the singleton instance of the plugin manager.
	 *
	 * @return A singleton instance of PluginManager.
	 */
	public static final synchronized PluginManager getPluginManager() {
		if (me == null) {
			me = new PluginManager();
			me.doAutoLoad();
		}
		
		return me;
	}

	/**
	 * Adds a new plugin.
	 *
	 * @param filename Filename of Plugin jar
	 * @return True if loaded, false if failed to load or if already loaded.
	 */
	public boolean addPlugin(final String filename) {
		if (knownPlugins.containsKey(filename.toLowerCase())) {
			return false;
		}
		
		PluginInfo pluginInfo;
		
		try {
			pluginInfo = new PluginInfo(filename);
			pluginInfo.checkRequirements();
			knownPlugins.put(filename.toLowerCase(), pluginInfo);
			
			return true;
		} catch (PluginException e) {
			Logger.userError(ErrorLevel.MEDIUM, "Error loading plugin " + filename + ": " + e.getMessage(), e);
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
		if (!knownPlugins.containsKey(filename.toLowerCase())) {
			return false;
		}

		PluginInfo pluginInfo = getPluginInfo(filename);
		
		try {
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
		if (!knownPlugins.containsKey(filename.toLowerCase())) {
			return false;
		}
		
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
		return knownPlugins.get(filename.toLowerCase());
	}

	/**
	 * Get a plugin instance by plugin name.
	 *
	 * @param name Name of plugin to find.
	 * @return PluginInfo instance, or null
	 */
	public PluginInfo getPluginInfoByName(final String name) {
		for (PluginInfo pluginInfo : knownPlugins.values()) {
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
	 * Get string[] of known plugin file names.
	 *
	 * @return string[] of known plugin file names.
	 * @deprecated Pointless method. Iterate getPluginInfos instead.
	 */
	@Deprecated
	public String[] getFilenames() {
		final String[] result = new String[knownPlugins.size()];
		int i = 0;
		for (String name : knownPlugins.keySet()) {
			result[i++] = knownPlugins.get(name).getFilename();
		}
		return result;
	}

	/**
	 * Retrieves a list of all installed plugins.
	 * Any file under the main plugin directory (~/.DMDirc/plugins or similar)
	 * that matches *.jar is deemed to be a valid plugin.
	 *
	 * @param addPlugins Should all found plugins be automatically have addPlugin() called?
	 * @return A list of all installed plugins
	 */
	public List<PluginInfo> getPossiblePluginInfos(final boolean addPlugins) {
		final ArrayList<PluginInfo> res = new ArrayList<PluginInfo>();

		final LinkedList<File> dirs = new LinkedList<File>();

		dirs.add(new File(myDir));

		while (!dirs.isEmpty()) {
			final File dir = dirs.pop();
			if (dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					dirs.add(file);
				}
			} else if (dir.isFile() && dir.getName().endsWith(".jar")) {
				String target = dir.getPath();
				
				// Remove the plugin dir
				target = target.substring(myDir.length(), target.length());
				if (addPlugins) {
					addPlugin(target);
				} else {
					try {
						res.add(new PluginInfo(target, false));
					} catch (PluginException pe) { /* This can not be thrown when the second param is false */}
				}
			}
		}

		if (addPlugins) {
			for (String name : knownPlugins.keySet()) {
				res.add(getPluginInfo(name));
			}
		}

		return res;
	}

	/**
	 * Update the autoLoadList
	 *
	 * @param plugin to add/remove (Decided automatically based on isLoaded())
	 */
	public void updateAutoLoad(final PluginInfo plugin) {
		final List<String> list = IdentityManager.getGlobalConfig().getOptionList("plugins", "autoload");
		
		if (plugin.isLoaded() && !list.contains(plugin.getFilename())) {
			list.add(plugin.getFilename());
		} else if (!plugin.isLoaded() && list.contains(plugin.getFilename())) {
			list.remove(plugin.getFilename());
		}
		
		IdentityManager.getConfigIdentity().setOption("plugins", "autoload", list);
	}
	
	/**
	 * Get Collection<PluginInfo> of known plugins.
	 *
	 * @return Collection<PluginInfo> of known plugins.
	 */
	public Collection<PluginInfo> getPluginInfos() {
		return knownPlugins.values();
	}
}
