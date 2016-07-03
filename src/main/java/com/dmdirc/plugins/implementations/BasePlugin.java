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

package com.dmdirc.plugins.implementations;

import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.util.validators.ValidationResponse;

import dagger.ObjectGraph;

/**
 * Base implementation of the Plugin interface.
 */
public abstract class BasePlugin implements Plugin {

    /** The object graph to return for subplugins, if any. */
    private ObjectGraph objectGraph;

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        // Do nothing, for now.
    }

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

    @Override
    public void onLoad() {
        //Define this here so only implementations that care have to override
    }

    @Override
    public void onUnload() {
        //Define this here so only implementations that care have to override
    }

    @Override
    public ValidationResponse checkPrerequisites() {
        return new ValidationResponse();
    }

}
