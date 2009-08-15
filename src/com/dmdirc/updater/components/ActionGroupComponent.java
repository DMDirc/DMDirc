/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.updater.components;

import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;

/**
 * Update component for action groups.
 * 
 * @author chris
 */
public class ActionGroupComponent implements UpdateComponent {
    
    /** The group that this component represents. */
    private ActionGroup group;
    
    /**
     * Creates a new ActionGroupComponent for the specified action group.
     * 
     * @param group The action group this component is for
     */
    public ActionGroupComponent(final ActionGroup group) {
        this.group = group;
        
        if (group.getComponent() != -1 && group.getVersion() != -1) {
            UpdateChecker.removeComponent(getName());
            UpdateChecker.registerComponent(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "addon-" + group.getComponent();
    }

    /** {@inheritDoc} */
    @Override
    public String getFriendlyName() {
        return "Action pack: " + group.getName();
    }

    /** {@inheritDoc} */
    @Override
    public Version getVersion() {
        return new Version(group.getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public String getFriendlyVersion() {
        return String.valueOf(getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public boolean doInstall(final String path) throws Exception {
        ActionManager.installActionPack(path);
        return false;
    }

}
