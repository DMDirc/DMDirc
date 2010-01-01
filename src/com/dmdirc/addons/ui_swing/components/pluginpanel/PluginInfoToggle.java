/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.ui_swing.components.pluginpanel;

import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;

/**
 * Wraps a PluginInfo object with a boolean to indicate whether it should be
 * toggled or not.
 * 
 * @author chris
 */
public class PluginInfoToggle {
    
    /** The PluginInfo object we're wrapping. */
    private final PluginInfo pi;
    
    /** Whether or not to toggle it. */
    private boolean toggle = false;

    /**
     * Creates a new instance of PluginInfoToggle to wrap the specified
     * PluginInfo.
     * 
     * @param pi The PluginInfo to be wrapped
     */
    public PluginInfoToggle(final PluginInfo pi) {
        this.pi = pi;
    }
    
    /**
     * Toggles this PluginInfoToggle.
     */
    public void toggle() {
        toggle = !toggle;
    }
    
    /**
     * Gets the state of this PluginInfo, taking into account the state
     * of the toggle setting.
     * 
     * @return True if the plugin is or should be loaded, false otherwise.
     */
    public boolean getState() {
        return toggle ^ pi.isLoaded();
    }

    /**
     * Retrieves the PluginInfo object associated with this toggle.
     * 
     * @return This toggle's PluginInfo object.
     */
    public PluginInfo getPluginInfo() {
        return pi;
    }
    
    /**
     * Applies the changes to the PluginInfo, if any.
     */
    public void apply() {
        if (toggle) {
            if (pi.isLoaded()) {
                pi.unloadPlugin();
            } else {
                pi.loadPlugin();
            }
            
            PluginManager.getPluginManager().updateAutoLoad(pi);
        }
    }

}
