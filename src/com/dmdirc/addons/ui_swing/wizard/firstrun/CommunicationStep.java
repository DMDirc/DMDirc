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
 * Sets communication options.
 */
public final class CommunicationStep extends Step {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Update info. */
    private TextLabel updatesInfo;
    /** Update checker. */
    private JCheckBox updates;
    /** Error reports info. */
    private TextLabel errorsInfo;
    /** Error reports. */
    private JCheckBox errors;

    /**
     * Creates a new instance of SetupStep.
     */
    public CommunicationStep() {
        super();

        initComponents();
        layoutComponents();
    }

    /**
     * Initialises the components.
     */
    protected void initComponents() {
        updatesInfo =
                new TextLabel("DMDirc can automatically check for " +
                "updates for various parts of the client, you can globally " +
                "disable that behaviour here, you can also fine tune the " +
                "behaviour in the preferences dialog once the client is running.");
        updates = new JCheckBox("Enable update checks?", true);
        errorsInfo = new TextLabel("DMDirc will automatically report application " +
                "errors to the developers, whilst this is of great help to the developers " +
                "you may disable this behaviour here.");
        errors = new JCheckBox("Enable error reporting?", true);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fillx, wrap 1"));

        add(updatesInfo, "growx, pushx");
        add(updates, "");
        add(errorsInfo, "growx, pushx");
        add(errors, "");
    }

    /**
     * Checks if updates are enabled.
     * 
     * @return true iif updates are enabled
     */
    public boolean checkUpdates() {
        return updates.isSelected();
    }

    /**
     * Checks if error reports are enabled.
     * 
     * @return true iif error reports  are enabled
     */
    public boolean checkErrors() {
        return updates.isSelected();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return "Communication settings";
    }
}
