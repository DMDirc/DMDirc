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

package com.dmdirc.installer;

import com.dmdirc.ui.swing.dialogs.wizard.Step;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

/**
 * This step shows an unable to install error
 */
public final class StepError extends Step {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 2;
	
	/**
	 * Creates a new instance of StepOne.
	 */
	public StepError() {
		super();
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
		
		JTextArea infoLabel;
		infoLabel = new JTextArea("Sorry, it is not possible to install DMDirc on this system at this time.\n\n");
		infoLabel.setEditable(false);
		infoLabel.setWrapStyleWord(true);
		infoLabel.setLineWrap(true);
		infoLabel.setHighlighter(null);
		infoLabel.setOpaque(false);
//		infoLabel.setBackground(getBackground());
		infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, SMALL_BORDER, 0));
			
		add(infoLabel, BorderLayout.CENTER);
	}
}
