/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.dialogs.wizard.firstrun;


import com.dmdirc.ui.swing.JWrappingLabel;
import java.awt.Dimension;
import net.miginfocom.swing.MigLayout;

/**
 * Queries the user for which core actions they wish to extract.
 */
public final class MigrationExtrationStep extends ExtractionStep {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /**
     * Creates a new instance of StepOne.
     */
    public MigrationExtrationStep() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void initComponents() {
        setLayout(new MigLayout("fillx, wrap 1"));
        
        JWrappingLabel infoLabel;
        
        infoLabel = new JWrappingLabel("This version of DMDirc contains an update to "
                + " the core plugins and actions.\n\nWould you like to extract the"
                + " core plugins to update them? Note that older plugins may not"
                + " function correctly with this version of DMDirc.");
        infoLabel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        add(infoLabel, "growx, pushx, wmax 400");
        add(plugins, "");
        
        infoLabel = new JWrappingLabel("Would you like to extract the default actions"
                + " to update them? Any changes you have made to the default"
                + " actions will be lost.");
        infoLabel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        add(infoLabel, "growx, pushx, wmax 400");
        add(actions, "");
    }
    
}
