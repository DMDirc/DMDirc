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

/**
 * Defines the standard methods that should be implemented by plugins.
 */
public interface Plugin {
	/**
	 * Called when the plugin is loaded.
	 *
	 * @returns false if the plugin can not be loaded
	 */
	boolean onLoad();
	
	/**
	 * Called when the plugin is about to be unloaded.
	 */
	void onUnload();

	/**
	 * Called when this plugin becomes active.
	 */
	void onActivate();
	
	/**
	 * Check to see if a plugin is active.
	 * (Non-Active PLugins will not recieve Events)
	 *
	 * @return True if active, else False.
	 */
	boolean isActive();
	
	/**
	 * Called when this plugin is deactivated.
	 */
	void onDeactivate();
	
	/**
	 * Called to see if the plugin has configuration options (via dialog).
	 *
	 * @returns true if the plugin has configuration options via a dialog.
	 */
	boolean isConfigurable();
	
	/**
	 * Called to show the Configuration dialog of the plugin if appropriate.
	 */
	void showConfig();
	
	/**
	 * Get the plugin version
	 *
	 * @return Plugin Version
	 */
	String getVersion();
	
	/**
	 * Get the plugin Author.
	 *
	 * @return Author of plugin
	 */
	String getAuthor();
	
	/**
	 * Get the plugin Description.
	 *
	 * @return Description of plugin
	 */
	String getDescription();
	
	/**
	 * Get the name of the plugin (used in "Manage Plugins" dialog).
	 *
	 * @return Name of plugin
	 */
	String toString();
}
