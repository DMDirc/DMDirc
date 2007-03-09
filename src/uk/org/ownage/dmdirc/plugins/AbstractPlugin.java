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

/**
 * Abstract plugin, all plugins will need to subclass this method to be accepted
 * by the plugin manager and loaded as a plugin.
 */
public abstract class AbstractPlugin {
    
    /**
     * PluginManager associated with this plugin.
     */
    private PluginManager pluginManager;

    /**
     * Creates a new instance of the plugin.
     * @param newPluginManager PluginManager associated with this plugin
     */
    public AbstractPlugin(final PluginManager newPluginManager) {
	this.pluginManager = newPluginManager;
    }
    
    /**
     * Starts the plugin running in its own thread.
     */
    abstract void start();

    /**
     * Called once the plugin has been loaded and initialised.
     */
    void onLoad() {
    }
    
    /**
     * Called just before the plugin is to be unloaded.
     */
    void onUnload() {
    }
    
    /**
     * Called to terminate the plugins thread.
     */
    abstract void stopPlugin(); 
}
