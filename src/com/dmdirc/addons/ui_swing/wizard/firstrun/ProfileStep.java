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

import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.wizard.Step;

import javax.swing.JCheckBox;

import net.miginfocom.swing.MigLayout;

/**
 * Queries the user for which core actions they wish to extract.
 */
public final class ProfileStep extends Step {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Profile manager checkbox. */
    private final JCheckBox profile;
    
    /**
     * Creates a new instance of StepOne.
     */
    public ProfileStep() {
        super();
        setLayout(new MigLayout("fillx, wrap 1"));
        
        profile = new JCheckBox("Launch profile manager?");
        
        final TextLabel infoLabel = new TextLabel("DMDirc uses a set of 'profiles' to determine"
                + " your nickname, realname, etc when connecting to a server. "
                + "If you would like the wizard to open the profile manager "
                + "for you, tick the box below.");
        add(infoLabel, "growx, pushx");
        add(profile, "");
    }
    
    /**
     * Returns the state of the profile manager checkbox.
     *
     * @return Profile manager checkbox state
     */
    public boolean getProfileManagerState() {
        return profile.isSelected();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return "Profile setup";
    }
    
}
