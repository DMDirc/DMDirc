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

package com.dmdirc.ui.dialogs.wizard;

import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Basic wizard container
 */
public class WizardDialog extends JDialog implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Button panel. */
    private JPanel buttonsPanel;
    /** Title panel. */
    private JPanel titlePanel;
    /** Step panel list. */
    private List<Step> steps;
    /** Current step. */
    private int currentStep;
    /** Prevous step button. */
    private JButton prev;
    /** Next step button. */
    private JButton next;
    /** Progress label. */
    private JLabel progressLabel;
    /** Wizard title. */
    private String title;
    /** Wizard. */
    private Wizard wizard;
    
    /** 
     * Creates a new instance of WizardFrame. 
     *
     * @param title Title for the wizard
     * @param steps Steps for the wizard
     * @param wizard Wizard to inform of changes
     */
    public WizardDialog(final String title, final List<Step> steps, 
            final Wizard wizard) {
        this.title = title;
        this.steps = new ArrayList<Step>(steps);
        this.wizard = wizard;
        initComponents();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(
                (float) (titleLabel.getFont().getSize() * 1.5)));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(new JSeparator(), BorderLayout.PAGE_END);
        
        initButtonsPanel();
    }
    
    /** Lays out the components. */
    private void layoutComponents() {
        this.setLayout(new BorderLayout());
        this.add(titlePanel, BorderLayout.PAGE_START);
        this.add(buttonsPanel, BorderLayout.PAGE_END);
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {
        buttonsPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        prev = new JButton("<< Previous");
        next = new JButton("Next >>");
        progressLabel = new JLabel();
        
        prev.setPreferredSize(new Dimension(110, 30));
        next.setPreferredSize(new Dimension(110, 30));
        
        prev.setMargin(new Insets(prev.getMargin().top, 0, 
                prev.getMargin().bottom, 0));
        next.setMargin(new Insets(next.getMargin().top, 0, 
                next.getMargin().bottom, 0));
        
        prev.addActionListener(this);
        next.addActionListener(this);
        
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(progressLabel);
        buttonPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(prev);
        buttonPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonPanel.add(next);
        
        buttonsPanel.setLayout(new BorderLayout());
        
        buttonsPanel.add(new JSeparator(), BorderLayout.PAGE_START);
        buttonsPanel.add(buttonPanel, BorderLayout.CENTER);
    }
    
    /** Displays the wizard. */
    public void display() {
        if (steps.size() > 0) {
            
            add(steps.get(0), BorderLayout.CENTER);
            currentStep = 0;
            
            prev.setEnabled(false);
            if (steps.size() == 1) {
                next.setText("Finish");
            }
            
            updateProgressLabel();
            
            setTitle(title);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            pack();
            //setResizable(false);
            setVisible(true);
        }
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == next) {
            nextStep();
        } else if (e.getSource() == prev) {
            prevStep();
        }
    }
    
    /**
     * Adds a step to the wizard.
     * 
     * @param step Step to add
     */
    public void addStep(Step step) {
        steps.add(step);
    }
    
    /** Moves to the next step. */
    private void nextStep() {
        steps.get(currentStep).setVisible(false);
        if ("Next >>".equals(next.getText())) {
            remove(steps.get(currentStep));
            add(steps.get(++currentStep), BorderLayout.CENTER);
            prev.setEnabled(true);
            if (currentStep == steps.size() - 1) {
                next.setText("Finish");
            }
            updateProgressLabel();
            wizard.stepChanged(currentStep - 1, currentStep);
        } else if ("Finish".equals(next.getText())) {
            wizard.wizardFinished();
            this.dispose();
        }
        steps.get(currentStep).setVisible(true);
    }
    
    /** Moves to the previous step. */
    private void prevStep() {
        steps.get(currentStep).setVisible(false);
        remove(steps.get(currentStep));
        add(steps.get(--currentStep), BorderLayout.CENTER);
        if (currentStep == 0) {
            prev.setEnabled(false);
        }
        next.setText("Next >>");
        steps.get(currentStep).setVisible(true);
        updateProgressLabel();
        wizard.stepChanged(currentStep + 1, currentStep);
    }
    
    /**
     * Returns the step at the specified index.
     *
     * @return Specified step.
     */
    public Step getStep(final int stepNumber) {
        return steps.get(stepNumber);
    }
    
    /** Updates the progress label. */
    private void updateProgressLabel() {
        progressLabel.setText("Step " + (currentStep + 1) + " of " + steps.size());
    }
    
}
