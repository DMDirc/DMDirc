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

import uk.org.ownage.dmdirc.parser.IRCParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class PluginManager {
    
    private Hashtable<String, AbstractPlugin> loadedPlugins = null;
    
    public PluginManager(IRCParser parser) {
	loadedPlugins = new Hashtable<String, AbstractPlugin>();
    }
    
    public void forwardCallback() {
    }
    
    public boolean addPlugin(String pluginClassName, AbstractPlugin plugin) {
	if( !loadedPlugins.containsKey(pluginClassName) ) {
	    loadedPlugins.put(pluginClassName,plugin);
	    
	    plugin.onLoad();
	} else {
	    return false;
	}
	plugin.start();
	return true;
    }
    
    protected synchronized boolean removePlugin(AbstractPlugin plugin) {
	if(loadedPlugins.contains(plugin) ) {
	    
	    ClassLoader loader = plugin.getClass().getClassLoader();
	    loader = null;
	    plugin = null;
	    
	    System.gc();
	    
	    loadedPlugins.remove(plugin);
	    return true;
	}
	
	return false;
    }
    
    public void stopPlugin(String pluginClassName) {
	AbstractPlugin plugin = null;
	
	if( loadedPlugins.containsKey(pluginClassName) ) {
	    plugin = loadedPlugins.get(pluginClassName);
	    plugin.onUnload();
	    plugin.stopPlugin();
	}
    }
    public void stopPlugin(AbstractPlugin plugin) {
	if( loadedPlugins.contains(plugin) ) {
	    plugin.onUnload();
	    plugin.stopPlugin();
	}
    }
}
