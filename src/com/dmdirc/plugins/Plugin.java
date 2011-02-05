/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.util.validators.ValidationResponse;

import java.io.File;

/**
 * Defines the standard methods that should be implemented by plugins.
 */
public abstract class Plugin implements Comparable<Plugin> {

    /** Domain name for the settings in this plugin. */
    private String myDomain = "plugin-unknown";
    /** Has the domain been set? */
    private boolean domainSet = false;
    /** Associated Plugin info. */
    private PluginInfo pluginInfo;
    /** Files directory for this plugin. */
    private File filesDir = null;

    /**
     * Called by PluginInfo to set the domain name.
     * This can only be called once, all other attempts will be ignored.
     *
     * @param newDomain Domain name for plugin settings
     */
    public void setDomain(final String newDomain) {
        if (!domainSet) {
            domainSet = true;
            myDomain = newDomain;
            domainUpdated();
        }
    }

    /**
     * Sets the associated plugin info for this plugin.
     *
     * @param pluginInfo Associated plugin info
     */
    public void setPluginInfo(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    /**
     * Returns the plugin info associated with this plugin.
     *
     * @return Plugin info or null
     */
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    /**
     * Get the domain name settings for this plugin should be stored in.
     *
     * @return Domain name for plugin settings
     */
    public String getDomain() {
        return myDomain;
    }

    /**
     * Called when the domain for plugin settings has been set.
     * This will only be called once (either when the plugin is loading, or when
     * its config is being shown).
     */
    public void domainUpdated() {
    }

    /**
     * Get the files directory for this plugin.
     * This will attempt to create the directory if it doesn't exist the first
     * time the directory name is requested.
     *
     * @return Files directory for this plugin.
     */
    protected File getFilesDir() {
        if (filesDir == null) {
            final String fs = System.getProperty("file.separator");
            final String dir = PluginManager.getPluginManager().getFilesDirectory();
            filesDir = new File(dir + pluginInfo.getName() + fs);
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
        }

        return filesDir;
    }

    /**
     * Convenience Method.
     *
     * @return Filesdir as a string with trailing path separator
     */
    protected String getFilesDirString() {
        return getFilesDir().getAbsolutePath() + System.getProperty("file.separator");
    }

    /**
     * Called when the plugin is loaded.
     */
    public abstract void onLoad();

    /**
     * Check any further Prerequisites for this plugin to load that can not be
     * checked using metainfo.
     *
     * @return ValidationResponse detailign if the plugin passes any extra checks
     *         that plugin.info can't handle
     */
    public ValidationResponse checkPrerequisites() {
        return new ValidationResponse();
    }

    /**
     * Called when the plugin is about to be unloaded.
     */
    public abstract void onUnload();

    /**
     * Called to allow plugins to add their configuration options to the manager.
     * PreferencesCategories added from this method should be of type
     * {@link com.dmdirc.config.prefs.PluginPreferencesCategory} as this gives
     * the user feedback on the status of your plugin.
     *
     * @param manager The preferences manager that configuration options
     * need to be added to.
     */
    public void showConfig(final PreferencesDialogModel manager) {
    }

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as per String.compareTo();
     *
     * @param o Object to compare to
     * @return a negative integer, zero, or a positive integer.
     */
    @Override
    public int compareTo(final Plugin o) {
        return toString().compareTo(o.toString());
    }
}
