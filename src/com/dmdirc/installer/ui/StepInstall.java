/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.installer.TextStep;

import java.awt.BorderLayout;
import java.awt.Image;

import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * This confirms the settings chosen in the previous step
 */
public final class StepInstall extends SwingStep implements TextStep {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Text area showing the install information */
    private final TextLabel infoLabel = new TextLabel("Beginning Install");
    /** Scroll pane holding text area */
    final JScrollPane scrollPane;

    /**
     * Creates a new instance of StepInstall.
     */
    public StepInstall() {
        super();
        setLayout(new BorderLayout());
        scrollPane = new JScrollPane(infoLabel,
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        infoLabel.setMargin(new Insets(5, 5, 5, 5));
        add(scrollPane, BorderLayout.CENTER);
    }

    /** {@inheritDoc} */
    @Override
    public String getStepName() {
        return "Install";
    }

    /** {@inheritDoc} */
    @Override
    public Image getIcon() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getStepDescription() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void addText(final String text) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                infoLabel.setText(infoLabel.getText() + text + "\n");
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setText(final String text) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                infoLabel.setText(text);
            }
        });
    }
}
