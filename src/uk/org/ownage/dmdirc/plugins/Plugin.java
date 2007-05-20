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
public abstract class Plugin implements Comparable<Plugin> {
	/** Is this plugin active? */
	private boolean isActive = false;

	/**
	 * Called when the plugin is constructed.
	 */
	public Plugin() { }

	/**
	 * Called when the plugin is loaded.
	 *
	 * @return false if the plugin can not be loaded
	 */
	public abstract boolean onLoad();
	
	/**
	 * Called when the plugin is about to be unloaded.
	 */
	public void onUnload() { }

	/**
	 * Change the active state of this plugin.
	 * (Non-Active Plugins will not recieve Events)
	 *
	 * @param newState True if activating, else False.
	 */
	public final void setActive(boolean newState) {
		if (isActive != newState) {
			isActive = newState;
			if (isActive) {
				onActivate();
			} else {
				onDeactivate();
			}
		}
	}
	
	/**
	 * Check to see if a plugin is active.
	 * (Non-Active Plugins will not recieve Events)
	 *
	 * @return True if active, else False.
	 */
	public final boolean isActive() { return isActive; }
	
	/**
	 * Called when this plugin becomes active.
	 */
	protected void onActivate() { }
	
	/**
	 * Called when this plugin is deactivated.
	 */
	protected void onDeactivate() { }
	
	/**
	 * Called to see if the plugin has configuration options (via dialog).
	 *
	 * @return true if the plugin has configuration options via a dialog.
	 */
	public boolean isConfigurable() { return false; }
	
	/**
	 * Called to show the Configuration dialog of the plugin if appropriate.
	 */
	public void showConfig() { }
	
	/**
	 * Get the plugin version
	 *
	 * @return Plugin Version
	 */
	public abstract String getVersion();
	
	/**
	 * Get the plugin Author.
	 *
	 * @return Author of plugin
	 */
	public abstract String getAuthor();
	
	/**
	 * Get the plugin Description.
	 *
	 * @return Description of plugin
	 */
	public abstract String getDescription();
	
	/**
	 * Get the name of the plugin (used in "Manage Plugins" dialog).
	 *
	 * @return Name of plugin
	 */
	public abstract String toString();

	/** {@inheritDoc} */
	public int compareTo(Plugin o) {
		return toString().compareTo(o.toString());
	}
}
