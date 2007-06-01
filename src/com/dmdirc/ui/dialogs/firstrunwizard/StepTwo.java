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

package com.dmdirc.ui.dialogs.firstrunwizard;

import com.dmdirc.ui.dialogs.wizard.Step;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.UIUtilities.layoutGrid;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;

/**
 * Queries the user for which core actions they wish to extract.
 */
public class StepTwo extends Step {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Profile manager checkbox. */
    private JCheckBox profile;
    
    /**
     * Creates a new instance of StepOne.
     */
    public StepTwo() {
        final GridBagConstraints constraints = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                0, SMALL_BORDER));
        
        profile = new JCheckBox("Launch profile manager?");
        
        JTextArea infoLabel;
        
        infoLabel = new JTextArea("DMDirc uses a set of 'profiles' to determine"
                + " your nickname, realname, etc when connecting to a server. "
                + "If you would like the wizard to open the profile manager "
                + "for you, tick the box below.");
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
        add(profile, constraints);
        
        constraints.weighty = 1.0;
        constraints.gridy = 2;
        add(Box.createVerticalGlue(), constraints);
    }
    
    /**
     * Returns the state of the profile manager checkbox.
     *
     * @return Profile manager checkbox state
     */
    public boolean getProfileManagerState() {
        return profile.isSelected();
    }
    
}
