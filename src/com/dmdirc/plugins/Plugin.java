/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

/**
 * Defines the standard methods that should be implemented by plugins.
 */
public interface Plugin {

    /**
     * Check any further Prerequisites for this plugin to load that can not be
     * checked using metainfo.
     *
     * @return ValidationResponse detailign if the plugin passes any extra
     * checks that plugin.info can't handle
     */
    ValidationResponse checkPrerequisites();

    /**
     * Get the domain name settings for this plugin should be stored in.
     *
     * @return Domain name for plugin settings
     */
    String getDomain();

    /**
     * Called when the plugin is loaded.
     */
    void onLoad();

    /**
     * Called when the plugin is about to be unloaded.
     */
    void onUnload();

    /**
     * Called by PluginInfo to set the domain name.
     * This can only be called once, all other attempts will be ignored.
     *
     * @param newDomain Domain name for plugin settings
     */
    void setDomain(final String newDomain);

    /**
     * Called to allow plugins to add their configuration options to the
     * manager. PreferencesCategories added from this method should be of type
     * {@link com.dmdirc.config.prefs.PluginPreferencesCategory} as this gives
     * the user feedback on the status of your plugin.
     *
     * @param manager The preferences manager that configuration options
     * need to be added to.
     */
    void showConfig(final PreferencesDialogModel manager);

}
