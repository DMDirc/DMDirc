/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.config.prefs;

import com.dmdirc.plugins.PluginInfo;

/**
 * A specialised {@link PreferencesCategory} which warns the user if an associated plugin is not
 * loaded
 *
 * @since 0.6.3
 */
public class PluginPreferencesCategory extends PreferencesCategory {

    /**
     * Creates a new preferences category that contains an object.
     *
     * @param plugin      The plugin which owns this category
     * @param title       The title of this preferences category
     * @param description The description of this category
     * @param icon        The icon to use for this category
     * @param object      The replacement object for this category
     */
    public PluginPreferencesCategory(final PluginInfo plugin, final String title,
            final String description, final String icon, final PreferencesInterface object) {
        super(title, description, icon, object);
        setPlugin(plugin);
    }

    /**
     * Creates a new preferences category that contains an object.
     *
     * @param plugin      The plugin which owns this category
     * @param title       The title of this preferences category
     * @param description The description of this category
     * @param object      The replacement object for this category
     */
    public PluginPreferencesCategory(final PluginInfo plugin, final String title,
            final String description, final PreferencesInterface object) {
        super(title, description, object);
        setPlugin(plugin);
    }

    /**
     * Creates a new preferences category that contains settings.
     *
     * @param plugin      The plugin which owns this category
     * @param title       The title of this preferences category
     * @param description The description of this category
     * @param icon        The icon to use for this category
     */
    public PluginPreferencesCategory(final PluginInfo plugin, final String title,
            final String description, final String icon) {
        super(title, description, icon);
        setPlugin(plugin);
    }

    /**
     * Creates a new preferences category that contains settings.
     *
     * @param plugin      The plugin which owns this category
     * @param title       The title of this preferences category
     * @param description The description of this category
     */
    public PluginPreferencesCategory(final PluginInfo plugin, final String title,
            final String description) {
        super(title, description);
        setPlugin(plugin);
    }

    /**
     * Declares that this category has been created by the specified plugin. If the plugin is not
     * loaded, then the category's warning field is set to a message reflecting the fact.
     *
     * @param plugin The plugin that owns this category
     */
    private void setPlugin(final PluginInfo plugin) {
        if (!plugin.isLoaded()) {
            setWarning("These are settings for the '" + plugin.getMetaData()
                    .getFriendlyName() + "' plugin, " + "which is not currently"
                    + " loaded. You must enable the plugin in the main 'Plugins'"
                    + " category for these settings to have any effect.");
        }
    }

}
