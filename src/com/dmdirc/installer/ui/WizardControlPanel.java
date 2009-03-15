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

import com.dmdirc.installer.ui.EtchedLineBorder.BorderSide;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * Simple Panel representing the control buttons for a wizard.
 */
public class WizardControlPanel extends JPanel {

    private static final long serialVersionUID = 7903362315297158222L;
    private final JButton prev;
    private final JButton next;
    private final JLabel progress;
    private int total;
    private int step;

    /**
     * Instantiates a new wizard control panel.
     */
    public WizardControlPanel() {
        this(0);
    }

    /**
     * Instantiates a new wizard control panel using the specified number of
     * steps.
     * 
     * @param total Total number of steps
     */
    public WizardControlPanel(final int total) {
        this.total = total;
        this.step = 0;
        
        prev = new JButton("\u00AB Previous");
        next = new JButton("Next \u00BB");
        progress = new JLabel();
        updateProgressLabel();

        prev.setPreferredSize(new Dimension(100, prev.getFont().getSize() + 2 * InstallerDialog.SMALL_GAP));
        next.setPreferredSize(new Dimension(100, next.getFont().getSize() + 2 * InstallerDialog.SMALL_GAP));

        setBorder(new EtchedLineBorder(EtchedBorder.LOWERED, BorderSide.TOP));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(progress);
        add(Box.createHorizontalGlue());
        add(prev);
        add(Box.createHorizontalStrut(InstallerDialog.SMALL_GAP));
        add(next);
    }

    /**
     * Returns the previous button.
     *
     * @return Previous button
     */
    public JButton getPrevButton() {
        return prev;
    }

    /**
     * Returns the next button.
     * 
     * @return Next button
     */
    public JButton getNextButton() {
        return next;
    }

    /**
     * Returns the progress label.
     * 
     * @return Progress Label
     */
    public JLabel getProgressLabel() {
        return progress;
    }

    /**
     * Updates the progress label.
     */
    public void updateProgressLabel() {
        progress.setText("Step " + step + " of " + total);
    }

    /**
     * Sets the new total number of steps.
     * 
     * @param total New total number of steps
     */
    public void setTotal(final int total) {
        this.total = total;
        updateProgressLabel();
    }

    /**
     * Sets the current progress step.
     * 
     * @param step Progress step
     */
    public void setProgress(final int step) {
        this.step = step + 1;
        updateProgressLabel();
        if (step + 1 == total) {
            next.setText("Finish");
        } else if (step == 0) {
            prev.setEnabled(false);
        } else if (step > 0) {
            prev.setEnabled(true);
        } else {
            next.setText("Next \u00BB");
        }
    }
}
