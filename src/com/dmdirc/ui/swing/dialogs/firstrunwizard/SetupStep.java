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

package com.dmdirc.ui.swing.dialogs.firstrunwizard;

import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import com.dmdirc.ui.swing.components.TextLabel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;

/**
 * Queries the user for which core actions they wish to extract.
 */
public final class SetupStep extends StepOne {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * Creates a new instance of SetupStep.
     */
    public SetupStep() {
        super();
    }
    
    protected void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER));
        
        TextLabel infoLabel;
        
        infoLabel = new TextLabel("It appears that this is the first time "
                + "that you have run DMDirc. "
                + "This short wizard will help you setup DMDirc.\n\n"
                + "DMDirc comes with a set of core plugins that add "
                + "functionality such as channel logging to the client. "
                + "Thse plugins need to be copied into your home directory to"
                + " be used. If you would like the wizard to copy the core "
                + "plugins for you, tick the box below.", this);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 0,
                SMALL_BORDER, 0));
        
        constraints.weightx = 1.0;
        constraints.fill = constraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(infoLabel, constraints);
        
        constraints.gridy = 1;
        add(plugins, constraints);
        
        infoLabel = new TextLabel("DMDirc also comes with a set of core"
                + " 'actions' that serve as examples and add some basic"
                + " functionality such as highlighting. If you would like"
                + " the wizard to copy the core actions for you, tick the"
                + " box below.", this);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 0,
                SMALL_BORDER, 0));
        
        constraints.gridy = 2;
        add(infoLabel, constraints);
        
        constraints.gridy = 3;
        add(actions, constraints);
        
        constraints.weighty = 1.0;
        constraints.gridy = 4;
        add(Box.createVerticalGlue(), constraints);
    }
}
