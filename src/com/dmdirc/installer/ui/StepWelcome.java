/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;

/**
 * Tells the user what this application does
 */
public final class StepWelcome extends SwingStep {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;

    /**
     * Creates a new instance of StepWelcome.
     *
     * @param releaseName 
     */
    public StepWelcome(final String releaseName) {
        super();

        setLayout(new BorderLayout());

        add(new TextLabel(
                "Welcome to the " + releaseName + " installer. This program "
                + "will install DMDirc on this computer.\n\nDMDirc is a "
                + "cross-platform IRC client developed by Chris Smith, Shane "
                + "Mc Cormack and Gregory Holmes. DMDirc is released for free "
                + "under the MIT licence; for more information, please visit "
                + "www.DMDirc.com.\n\nClick \"Next\" to continue, or close "
                + "this program to cancel the installation."), BorderLayout.CENTER);
    }

    /** {@inheritDoc} */
    @Override
    public String getStepName() {
        return "Welcome";
    }

    /** {@inheritDoc} */
    @Override
    public Image getIcon() {
        return Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("com/dmdirc/res/icon.png"));
    }

    /** {@inheritDoc} */
    @Override
    public String getStepDescription() {
        return "";
    }
}
