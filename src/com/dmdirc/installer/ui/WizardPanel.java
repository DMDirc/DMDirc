/*
 * 
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.installer.ui;

import com.dmdirc.installer.Step;

import java.util.List;
import javax.swing.JPanel;

/**
 * Wizard panel, displays and controls the flow of steps.
 */
public class WizardPanel extends JPanel {

    private static final long serialVersionUID = 7903362315297158222L;
    private final StepLayout layout;
    private final InstallerDialog dialog;

    /**
     * Instantiates a new wizard panel.
     *
     * @param dialog Parent installer dialog
     */
    public WizardPanel(final InstallerDialog dialog) {
        super();

        this.dialog = dialog;
        layout = new StepLayout();
        setLayout(layout);
    }

    /**
     * Displays this panel with the specified steps.
     * 
     * @param steps Steps to display
     */
    public void display(final List<SwingStep> steps) {
       for (SwingStep step : steps) {
           addStep(step);
       }
       display();
    }

    /**
     * Displays this panel with the existing steps.
     */
    public void display() {
        showFirst();
    }

    /**
     * Returns the step specified.
     *
     * @param step Step index
     * 
     * @return Step
     */
    public Step getStep(final int step) {
        return layout.getStep(step);
    }

    /**
     * Returns the step specified.
     *
     * @param stepName Step name
     *
     * @return Step
     */
    public Step getStep(final String stepName) {
        Step step = null;
        for (SwingStep loopStep : layout.getSteps()) {
            if (stepName.equals(loopStep.getStepName())) {
                return loopStep;
            }
        }
        return step;
    }

    /**
     * Returns the current Step.
     *
     * @return Current step index
     */
    public int getCurrentStepIndex() {
        return layout.getCurrentStepIndex();
    }

    /**
     * Returns the current Step.
     *
     * @return Current step index
     */
    public String getCurrentStepName() {
        return layout.getCurrentStepName();
    }

    /**
     * Returns the current Step.
     *
     * @return Current step index
     */
    public Step getCurrentStep() {
        return layout.getCurrentStep();
    }

    /**
     * Returns the total number of steps.
     *
     * @return Total number of steps
     */
    public int getTotalSteps() {
        return layout.getSteps().size();
    }

    /**
     * Adds a step to this panel.
     *
     * @param step Step to add
     */
    public void addStep(final SwingStep step) {
        add(step, step.getStepName());
    }

    /**
     * Shows the first step.
     */
    public void showFirst() {
        layout.first(this);
    }

    /**
     * Shows the last step.
     */
    public void showLast() {
        layout.last(this);
    }

    /**
     * Shows the next step.
     */
    public void nextStep() {
        layout.next(this);
    }

    /**
     * Shows the previous step.
     */
    public void previousStep() {
        layout.previous(this);
    }

    /**
     * Shows the specified step.
     * 
     * @param step Step index
     */
    public void showStep(final int step) {
        layout.show(step, this);
    }
}
