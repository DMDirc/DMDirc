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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JTextArea;

/**
 * Queries the user for which core actions they wish to extract.
 */
public final class UpdateStep extends StepOne {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * Creates a new instance of StepOne.
     */
    public UpdateStep() {
        super();
    }
    
    protected void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER));
        
        JTextArea infoLabel;
        
        infoLabel = new JTextArea("The core plugins have been been updated" +
                " would you like to re-extract these to update them?");
        infoLabel.setEditable(false);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        infoLabel.setHighlighter(null);
        infoLabel.setBackground(getBackground());
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0,
                SMALL_BORDER, 0));
        
        constraints.weightx = 1.0;
        constraints.fill = constraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(infoLabel, constraints);
        
        constraints.gridy = 1;
        add(plugins, constraints);
        
        infoLabel = new JTextArea("It's possible the actions have also been"
                + " updated, do you want to re-extract these too?");
        infoLabel.setEditable(false);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        infoLabel.setHighlighter(null);
        infoLabel.setBackground(getBackground());
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
