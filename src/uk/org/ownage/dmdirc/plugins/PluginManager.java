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

package uk.org.ownage.dmdirc.plugins;

import java.util.Hashtable;

/**
 * Managers plugins for the client, also forwards events from the client or irc
 * to the plugins as required.
 */
public final class PluginManager {
    
    /**
     * list of plugins currently loaded.
     */
    private Hashtable<String, AbstractPlugin> loadedPlugins;
    
    /**
     * Creates a new plugin manager.
     */
    public PluginManager() {
	loadedPlugins = new Hashtable<String, AbstractPlugin>();
    }
    
    /**
     * forwards callbacks to the plugins.
     */
    public void forwardCallback() {
    }
    
    /**
     * Adds a new plugin to the loaded plugins list.
     * @param pluginClassName plugin name
     * @param plugin boolean success
     * @return plugin instance
     */
    public boolean addPlugin(final String pluginClassName, 
            final AbstractPlugin plugin) {
	if (!loadedPlugins.containsKey(pluginClassName)) {
	    loadedPlugins.put(pluginClassName, plugin);
	    
	    plugin.onLoad();
	} else {
	    return false;
	}
	plugin.start();
	return true;
    }
    
    /**
     * Removes a plugin from the loaded plugins list, unloading the plugin class
     * as it does so.
     * @param plugin boolean success
     * @return plugin to remove
     */
    protected synchronized boolean removePlugin(final AbstractPlugin plugin) {
	if (loadedPlugins.contains(plugin)) {
	    loadedPlugins.remove(plugin.toString());
            System.gc();
	    return true;
	}
	
	return false;
    }
    
    /**
     * Calls a plugins onunload events and removes it from the loaded plugins 
     * list.
     * @param pluginClassName classname of the plugin to stop
     */
    public void stopPlugin(final String pluginClassName) {
	AbstractPlugin plugin = null;
	
	if (loadedPlugins.containsKey(pluginClassName)) {
	    plugin = loadedPlugins.get(pluginClassName);
	    plugin.onUnload();
	    plugin.stopPlugin();
	}
    }
}
