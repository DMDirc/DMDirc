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
import com.dmdirc.installer.StepListener;
import com.dmdirc.installer.WizardListener;
import com.dmdirc.util.ListenerList;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 */
public class InstallerDialog extends JFrame implements ActionListener {

    private static final long serialVersionUID = -2001827768443747849L;
    private final TitlePanel title;
    private final WizardPanel wizard;
    private final WizardControlPanel control;
    private final ListenerList listeners;

    /**
     *
     * @param dialogTitle
     */
    public InstallerDialog(final String dialogTitle) {
        title = new TitlePanel(dialogTitle);
        wizard = new WizardPanel(this);
        control = new WizardControlPanel();
        listeners = new ListenerList();

        setLayout(new BorderLayout());

        add(title, BorderLayout.NORTH);
        add(wizard, BorderLayout.CENTER);
        add(control, BorderLayout.SOUTH);

        //setIconImage(IconManager.getIconManager().getImage("icon"));
        setPreferredSize(new Dimension(400, 350));
        setMaximumSize(new Dimension(400, 350));

        control.getPrevButton().addActionListener(this);
        control.getNextButton().addActionListener(this);
    }

    /**
     *
     * 
     * @param step
     */
    public void addStep(final SwingStep step) {
        wizard.addStep(step);
    }

    /**
     *
     */
    public void display() {
        wizard.display();
        control.setTotal(wizard.getTotalSteps());
        control.setProgress(wizard.getCurrentStepIndex());
        addWindowListener(new WindowAdapter() {

            /** {@inheritDoc} */
            @Override
            public void windowClosing(final WindowEvent e) {
                fireWizardCancelled();
            }
        });

        pack();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setVisible(true);
        fireStepAboutToBeDisplayed(wizard.getStep(wizard.getCurrentStepIndex()));
    }

    /**
     *
     * @param steps 
     */
    public void display(final List<Step> steps) {
        final List<SwingStep> swingSteps = new ArrayList<SwingStep>();
        for (Step step : steps) {
            if (step instanceof SwingStep) {
                swingSteps.add((SwingStep) step);
            }
        }
        display();
    }

    /**
     *
     *
     * @param enable
     */
    public void enableNextStep(final boolean enable) {
        control.getNextButton().setEnabled(enable);
    }

    /**
     *
     *
     * @param enable
     */
    public void enablePreviousStep(final boolean enable) {
        control.getPrevButton().setEnabled(enable);
    }

    /**
     *
     * 
     * @return
     */
    public boolean showCancelConfirmation() {
        return JOptionPane.showConfirmDialog(this,
                                             "Are you sure you want to cancel?",
                                             "Cancel confirmation",
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.WARNING_MESSAGE) ==
               JOptionPane.YES_OPTION;
    }

    /**
     *
     *
     * @param step
     * @return
     */
    public Step getStep(final int step) {
        return wizard.getStep(step);
    }

    /**
     *
     * @param name
     * @return
     */
    public Step getStep(final String name) {
        return wizard.getStep(name);
    }

    /**
     *
     *
     * @return
     */
    public Step getCurrentStep() {
        return wizard.getCurrentStep();
    }

    /**
     *
     *
     * @return
     */
    public int getCurrentStepIndex() {
        return wizard.getCurrentStepIndex();
    }

    /**
     *
     *
     * @return
     */
    public String getCurrentStepName() {
        return wizard.getCurrentStepName();
    }

    /**
     *
     *
     * @param step
     */
    void fireStepAboutToBeDisplayed(final Step step) {
        for (StepListener listener : listeners.get(StepListener.class)) {
            listener.stepAboutToDisplay(step);
        }
    }

    /**
     *
     * 
     * @param step
     */
    void fireStepHidden(final Step step) {
        for (StepListener listener : listeners.get(StepListener.class)) {
            listener.stepHidden(step);
        }
    }

    /**
     *
     * 
     * @param listener
     */
    public void addStepListener(final StepListener listener) {
        listeners.add(StepListener.class, listener);
    }

    /**
     *
     *
     * @param listener
     */
    public void removeStepListener(final StepListener listener) {
        listeners.remove(StepListener.class, listener);

    }

    /**
     *
     */
    void fireWizardCancelled() {
        for (WizardListener listener : listeners.get(WizardListener.class)) {
            listener.wizardCancelled();
        }
    }

    /**
     * 
     */
    void fireWizardFinished() {
        for (WizardListener listener : listeners.get(WizardListener.class)) {
            listener.wizardFinished();
        }
    }

    /**
     *
     *
     * @param listener
     */
    public void addWizardListener(final WizardListener listener) {
        listeners.add(WizardListener.class, listener);
    }

    /**
     *
     * 
     * @param listener
     */
    public void removeWizardListener(final WizardListener listener) {
        listeners.remove(WizardListener.class, listener);
    }

    /**
     * Initialises any settings required by this UI (this is always called
     * before any aspect of the UI is instansiated).
     *
     * @throws UnsupportedOperationException If unable to switch to the system
     * look and feel
     */
    public static void initUISettings() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException("Unable to switch to the " +
                                                    "system look and feel", ex);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("Unable to switch to the " +
                                                    "system look and feel", ex);
        } catch (UnsupportedLookAndFeelException ex) {
            throw new UnsupportedOperationException("Unable to switch to the " +
                                                    "system look and feel", ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Unable to switch to the " +
                                                    "system look and feel", ex);
        }

        UIManager.put("swing.useSystemFontSettings", true);
        UIManager.put("swing.boldMetal", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final int currentStep = wizard.getCurrentStepIndex();
        Step hiddenStep = null;
        Step shownStep = null;
        if (e.getSource() == control.getPrevButton()) {
            wizard.previousStep();
            hiddenStep = wizard.getStep(currentStep);
            shownStep = wizard.getStep(currentStep - 1);
        } else if (e.getSource() == control.getNextButton()) {
            if ("Finish".equals(control.getNextButton().getText())) {
                fireWizardFinished();
                shownStep = wizard.getStep(currentStep);
                dispose();
            } else {
                wizard.nextStep();
                hiddenStep = wizard.getStep(currentStep);
                shownStep = wizard.getStep(currentStep + 1);
            }
        } else {
            return;
        }
        if (shownStep != null) {
            fireStepAboutToBeDisplayed(shownStep);
        }
        if (hiddenStep != null) {
            fireStepHidden(hiddenStep);
        }
        control.setProgress(wizard.getCurrentStepIndex());
    }
}
