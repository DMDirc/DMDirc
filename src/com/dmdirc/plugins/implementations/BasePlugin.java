/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.plugins.implementations;

import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.util.validators.ValidationResponse;

import dagger.ObjectGraph;

/**
 * Base implementation of the Plugin interface.
 */
public abstract class BasePlugin implements Plugin {

    /** Domain name for the settings in this plugin. */
    private String myDomain = "plugin-unknown";
    /** Has the domain been set? */
    private boolean domainSet;
    /** The object graph to return for subplugins, if any. */
    private ObjectGraph objectGraph;

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void setDomain(final String newDomain) {
        if (!domainSet) {
            domainSet = true;
            myDomain = newDomain;
            domainUpdated();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        // Do nothing, for now.
    }

    /** {@inheritDoc} */
    @Override
    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    /**
     * Sets the object graph that this implementation will return for subplugins.
     *
     * @param objectGraph The object graph to return.
     */
    protected void setObjectGraph(final ObjectGraph objectGraph) {
        this.objectGraph = objectGraph;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        //Define this here so only implementations that care have to override
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        //Define this here so only implementations that care have to override
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public String getDomain() {
        return myDomain;
    }

    /**
     * Called when the domain for plugin settings has been set.
     * This will only be called once (either when the plugin is loading, or when
     * its config is being shown).
     *
     * @deprecated Domain should be obtained from {@link PluginInfo}, and will never be updated.
     */
    @Deprecated
    protected void domainUpdated() {
        //Define this here so only implementations that care have to override
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
