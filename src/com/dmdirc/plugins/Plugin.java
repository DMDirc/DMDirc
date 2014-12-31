/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import dagger.ObjectGraph;

/**
 * Defines the standard methods that should be implemented by plugins.
 */
public interface Plugin {

    /**
     * Check any further Prerequisites for this plugin to load that can not be checked using
     * metainfo.
     *
     * @return ValidationResponse detailing if the plugin passes any extra checks that plugin.info
     *         can't handle
     */
    ValidationResponse checkPrerequisites();

    /**
     * Called when the plugin is loaded.
     */
    void onLoad();

    /**
     * Called when the plugin is about to be unloaded.
     */
    void onUnload();

    /**
     * Loads the plugin. This method is called when the plugin has been activated by the client,
     * either in response to a direct user request, or automatically as part of client startup or
     * during the course of another plugin being loaded.
     *
     * <p>
     * Plugins are provided with an {@link ObjectGraph} which they may extend and use to inflate
     * their classes. This is the only supported way of obtaining instances of core DMDirc classes,
     * or objects shared by this plugin's parent.
     *
     * <p>
     * Plugins wishing to use this form of dependency injection should define a new
     * {@link dagger.Module} which specifies an {@link dagger.Module#addsTo()} argument of either:
     *
     * <ul>
     * <li>For plugins with no parents, or plugins which do not have a dependency on their parent,
     * {@link com.dmdirc.ClientModule}.</li>
     * <li>For plugins with dependencies on a parent plugin, that plugin's own {@link dagger.Module}
     * implementation.</li>
     * </ul>
     *
     * <p>
     * The implementation of this method should then call
     * {@link ObjectGraph#plus(java.lang.Object[])} with an instance of the plugin's own module.
     *
     * <p>
     * To expose dependencies to child plugins, the relevant {@link ObjectGraph} should be returned
     * from the {@link #getObjectGraph()} method.
     *
     * <p>
     * While both this method and {@link #onLoad()} are defined, this method is guaranteed to be
     * called first. Implementations that support this method of loading can simply treat
     * {@link #onLoad()} as a no-op.
     *
     * @param pluginInfo The information object corresponding to this plugin.
     * @param graph      The dependency-injection graph that may be used to instantiate plugin
     *                   classes.
     */
    void load(PluginInfo pluginInfo, ObjectGraph graph);

    /**
     * Returns an {@link ObjectGraph} that may be used by subplugins to inject dependencies provided
     * by this class.
     *
     * <p>
     * This should always be an extension of the {@link ObjectGraph} provided to the
     * {@link #load(com.dmdirc.plugins.PluginInfo, dagger.ObjectGraph)} method. If the plugin has no
     * dependencies it wishes to expose, it may return {@code null} and any subplugins will be given
     * the global {@link ObjectGraph}.
     *
     * <p>
     * It is recommended that implementations separate their internal dependencies from those that
     * will be published. This can be accomplished by using two modules, e.g.:
     *
     * <pre><code>
     *   ObjectGraph external = graph.plus(new ExternalModule());
     *   ObjectGraph internal = external.plus(new InternalModule());
     * </code></pre>
     *
     * <p>
     * The plugin can then inflate its own classes using the 'internal' graph, while only exposing
     * relevant dependencies in the 'external' graph.
     *
     * @return A graph to be used by child plugins, or {@code null} for the default graph.
     */
    ObjectGraph getObjectGraph();

    /**
     * Called to allow plugins to add their configuration options to the manager.
     * PreferencesCategories added from this method should be of type
     * {@link com.dmdirc.config.prefs.PluginPreferencesCategory} as this gives the user feedback on
     * the status of your plugin.
     *
     * @param manager The preferences manager that configuration options need to be added to.
     */
    void showConfig(final PreferencesDialogModel manager);

}
