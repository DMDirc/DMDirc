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

package com.dmdirc.addons.ui_swing.wizard;


import com.dmdirc.ui.CoreUIUtils;

import java.util.List;

import javax.swing.JFrame;

/**
 * Basic wizard container.
 */
public final class WizardFrame extends JFrame {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Wizard. */
    private final WizardPanel wizard;

    /**
     * Creates a new instance of WizardFrame that requires a mainframe.
     *
     * @param title Title for the wizard
     * @param steps Steps for the wizard
     * @param wizard Wizard to inform of changes
     */
    public WizardFrame(final String title, final List<Step> steps,
            final WizardListener wizard) {
        super();

        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.wizard = new WizardPanel(title, steps, wizard);
        layoutComponents();
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setContentPane(wizard);
    }

    /** Displays the wizard. */
    public void display() {
        wizard.display();
        CoreUIUtils.centreWindow(this);
        setResizable(false);
        setVisible(true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void validate() {
        super.validate();
        CoreUIUtils.centreWindow(this);
    }

    /**
     * Adds a step to the wizard.
     *
     * @param step Step to add
     */
    public void addStep(final Step step) {
        wizard.addStep(step);
    }

    /**
     * Returns the step at the specified index.
     *
     * @param stepNumber step number
     *
     * @return Specified step.
     */
    public Step getStep(final int stepNumber) {
        return wizard.getStep(stepNumber);
    }

    /**
     * Returns the current step.
     *
     * @return Current step number
     */
    public int getCurrentStep() {
        return wizard.getCurrentStep();
    }

    /**
     * Enables or disables the "next step" button.
     *
     * @param newValue boolean true to make "next" button enabled, else false
     */
    public void enableNextStep(final boolean newValue) {
        wizard.enableNextStep(newValue);
    }

    /**
     * Enables or disables the "previous step" button.
     *
     * @param newValue boolean true to make "previous" button enabled, else false
     */
    public void enablePreviousStep(final boolean newValue) {
        wizard.enablePreviousStep(newValue);
    }

    /**
     * Adds a step listener to the list.
     *
     * @param listener
     */
    public void addStepListener(final StepListener listener) {
        wizard.addStepListener(listener);
    }

    /**
     * Removes a step listener from the list.
     *
     * @param listener
     */
    public void removeStepListener(final StepListener listener) {
        wizard.removeStepListener(listener);
    }

    /**
     * Adds a wizard listener to the list.
     *
     * @param listener
     */
    public void addWizardListener(final WizardListener listener) {
        wizard.addWizardListener(listener);
    }

    /**
     * Removes a wizard listener from the list.
     *
     * @param listener
     */
    public void removeWizardListener(final WizardListener listener) {
        wizard.removeWizardListener(listener);
    }
}
