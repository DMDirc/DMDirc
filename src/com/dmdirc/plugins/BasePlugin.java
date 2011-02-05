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
 * Base implementation of the Plugin interface
 */
public abstract class BasePlugin implements Plugin {

    /** Domain name for the settings in this plugin. */
    private String myDomain = "plugin-unknown";
    /** Has the domain been set? */
    private boolean domainSet = false;
    /** Associated Plugin info. */
    private PluginInfo pluginInfo;
    /** Files directory for this plugin. */
    private File filesDir = null;

    /** {@inheritDoc} */
    @Override
    public void setDomain(final String newDomain) {
        if (!domainSet) {
            domainSet = true;
            myDomain = newDomain;
            domainUpdated();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setPluginInfo(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    /** {@inheritDoc} */
    @Override
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    /** {@inheritDoc} */
    @Override
    public String getDomain() {
        return myDomain;
    }

    /**
     * Called when the domain for plugin settings has been set.
     * This will only be called once (either when the plugin is loading, or when
     * its config is being shown).
     */
    protected void domainUpdated() {
        //Define this here so only implementations that care have to override
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
            final String dir = PluginManager.getPluginManager()
                    .getFilesDirectory();
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
        return getFilesDir().getAbsolutePath()
                + System.getProperty("file.separator");
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse checkPrerequisites() {
        return new ValidationResponse();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        //Define this here so only implementations that care have to override
    }
}
