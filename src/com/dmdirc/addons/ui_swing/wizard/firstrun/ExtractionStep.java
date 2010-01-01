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

package com.dmdirc.addons.ui_swing.wizard.firstrun;

import com.dmdirc.addons.ui_swing.wizard.Step;

import javax.swing.JCheckBox;

/**
 * Queries the user for which core actions they wish to extract.
 */
public abstract class ExtractionStep extends Step {
    
    /** Plugins checkbox. */
    protected final JCheckBox plugins;
    /** Actions checkbox. */
    protected final JCheckBox actions;
    
    /** Creates a new instance of StepOne. */
    public ExtractionStep() {
        super();
        
        plugins = new JCheckBox("Install core plugins?");
        actions = new JCheckBox("Install core actions?");
        
        plugins.setSelected(true);
        actions.setSelected(true);
        
        initComponents();
    }
    
    /**
     * Initialises the components.
     */
    protected abstract void initComponents();
    
    /**
     * Returns the state of the plugins checkbox.
     *
     * @return Plugins checkbox state
     */
    public final boolean getPluginsState() {
        return plugins.isSelected();
    }

    /**
     * Returns the state of the actions checkbox.
     *
     * @return Actions checkbox state
     */
    public final boolean getActionsState() {
        return actions.isSelected();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return "Core addon extraction";
    }
    
}
