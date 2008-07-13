/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.Logger;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.updater.components.PluginComponent;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PluginManager implements ActionListener {
	/** List of known plugins. */
	private final Map<String, PluginInfo> knownPlugins = new Hashtable<String, PluginInfo>();

	/** Directory where plugins are stored. */
	private final String myDir;

	/** Singleton instance of the plugin manager. */
	private static PluginManager me;

	/** Possible plugins available during autoload. */
	private List<PluginInfo> possible = null;
	
	/**
	 * Creates a new instance of PluginManager.
	 */
	private PluginManager() {
		final String fs = System.getProperty("file.separator");
		myDir = Main.getConfigDir() + "plugins" + fs;
		ActionManager.addListener(this, CoreActionType.CLIENT_PREFS_OPENED, CoreActionType.CLIENT_PREFS_CLOSED);
	}

	/**
	 * Autoloads plugins.
	 */
	private void doAutoLoad() {
		possible = getPossiblePluginInfos(false);
		for (String plugin : IdentityManager.getGlobalConfig().getOptionList("plugins", "autoload")) {
			plugin = plugin.trim();
			if (!plugin.isEmpty() && plugin.charAt(0) != '#' && addPlugin(plugin)) {
				getPluginInfo(plugin).loadPlugin();
			}
		}
		
		// And now addPlugin() the rest
		for (PluginInfo plugin : possible) {
			addPlugin(plugin.getFilename());
		}
		possible = null;
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
		
		if (!(new File(getDirectory() + filename)).exists()) {
			Logger.userError(ErrorLevel.MEDIUM, "Error loading plugin " + filename + ": File does not exist");
			return false;
		}
		
		PluginInfo pluginInfo;
		
		try {
			pluginInfo = new PluginInfo(new URL("file://"+getDirectory()+filename));
			new PluginComponent(pluginInfo);
			
			final String requirements = pluginInfo.getRequirementsError();
			if (requirements.isEmpty()) {
				knownPlugins.put(filename.toLowerCase(), pluginInfo);
			
				return true;
			} else {
				throw new PluginException("Plugin "+filename+" was not loaded, one or more requirements not met ("+requirements+")");
			}
		} catch (MalformedURLException mue) {
			Logger.userError(ErrorLevel.MEDIUM, "Error creating URL for plugin " + filename + ": " + mue.getMessage(), mue);
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
		
		pluginInfo.unloadPlugin();
		
		knownPlugins.remove(filename.toLowerCase());

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
		boolean result = addPlugin(filename);
		
		if (wasLoaded) {
			getPluginInfo(filename).loadPlugin();
			result = getPluginInfo(filename).isLoaded();
		}
		
		return result;
	}

	/**
	 * Reload all plugins.
	 */
	public void reloadAllPlugins() {
		for (PluginInfo pluginInfo : getPluginInfos()) {
			reloadPlugin(pluginInfo.getFilename());
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
		return getPluginInfoByName(name, false);
	}
	
	/**
	 * Get a plugin instance by plugin name.
	 *
	 * @param name Name of plugin to find.
	 * @param checkPossible Check possible plugins aswell? (used in autoload)
	 * @return PluginInfo instance, or null
	 */
	protected PluginInfo getPluginInfoByName(final String name, final boolean checkPossible) {
		for (PluginInfo pluginInfo : knownPlugins.values()) {
			if (pluginInfo.getName().equalsIgnoreCase(name)) {
				return pluginInfo;
			}
		}
		
		if (checkPossible && possible != null) {
			for (PluginInfo pluginInfo : possible) {
				if (pluginInfo.getName().equalsIgnoreCase(name)) {
					if (addPlugin(pluginInfo.getFilename())) {
						return pluginInfo;
					} else {
						return null;
					}
				}
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
	 * Retrieves a list of all installed plugins.
	 * Any file under the main plugin directory (~/.DMDirc/plugins or similar)
	 * that matches *.jar is deemed to be a valid plugin.
	 *
	 * @param addPlugins Should all found plugins be automatically have addPlugin() called?
	 * @return A list of all installed plugins
	 */
	public List<PluginInfo> getPossiblePluginInfos(final boolean addPlugins) {
		final Map<String, PluginInfo> res = new Hashtable<String, PluginInfo>();
		
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
						final PluginInfo pi = new PluginInfo(new URL("file://"+getDirectory()+target), false);
						res.put(target, pi);
					} catch (MalformedURLException mue) {
						Logger.userError(ErrorLevel.MEDIUM, "Error creating URL for plugin " + target + ": " + mue.getMessage(), mue);
					} catch (PluginException pe) { /* This can not be thrown when the second param is false */}
				}
			}
		}

		final Map<String, PluginInfo> knownPluginsCopy = new Hashtable<String, PluginInfo>(knownPlugins);
		for (PluginInfo pi : knownPluginsCopy.values()) {
			if (!(new File(pi.getFullFilename())).exists()) {
				delPlugin(pi.getFilename());
			} else if (addPlugins) {
				res.put(pi.getFilename().toLowerCase(), pi);
			}
		}
		
		return new LinkedList<PluginInfo>(res.values());
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
		return new ArrayList<PluginInfo>(knownPlugins.values());
	}

	/** {@inheritDoc} */
	@Override
	public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
		if (type.equals(CoreActionType.CLIENT_PREFS_OPENED)) {
			for (PluginInfo pi : getPluginInfos()) {
				if (!pi.isLoaded() && !pi.isTempLoaded()) {
					pi.loadPluginTemp();
				}
				if (pi.isLoaded() || pi.isTempLoaded()) {
					pi.getPlugin().showConfig((PreferencesManager) arguments[0]);
				}
			}
		} else if (type.equals(CoreActionType.CLIENT_PREFS_CLOSED)) {
			for (PluginInfo pi : getPluginInfos()) {
				if (pi.isTempLoaded()) {
					pi.unloadPlugin();
				}
			}
		}
	}
}
