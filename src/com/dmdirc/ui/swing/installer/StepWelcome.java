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

package com.dmdirc.ui.swing.installer;

import com.dmdirc.ui.swing.dialogs.wizard.Step;
import com.dmdirc.ui.swing.components.TextLabel;


import net.miginfocom.swing.MigLayout;

/**
 * Tells the user what this application does
 */
public final class StepWelcome extends Step {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 2;
	
	/**
	 * Creates a new instance of StepWelcome.
	 */
	public StepWelcome(final String releaseName) {
		super();
		setLayout(new MigLayout());
		
		TextLabel infoLabel;
		infoLabel = new TextLabel("Welcome to the "+releaseName+" installer. This program will install DMDirc on this computer.<br><br>"
		                        + "DMDirc is a cross-platform IRC client developed by Chris Smith, Shane Mc Cormack and "
		                        + "Gregory Holmes. DMDirc is released for free under the MIT license; for more information, "
		                        + "please visit www.DMDirc.com.<br><br>"
		                        + "Click \"Next\" to continue, or close this program to cancel the installation.");
		infoLabel.setOpaque(false);
			
		add(infoLabel, "grow");
	}
}
