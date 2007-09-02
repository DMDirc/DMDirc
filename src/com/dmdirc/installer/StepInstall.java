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

import com.dmdirc.ui.swing.dialogs.wizard.SpecialStep;
import com.dmdirc.ui.swing.dialogs.wizard.TextStep;
import com.dmdirc.ui.swing.dialogs.wizard.Step;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JTextArea;

/**
 * This confirms the settings chosen in the previous step
 */
public final class StepInstall extends Step implements SpecialStep, TextStep {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 1;
	
	/** Text area showing the install information */
	private JTextArea infoLabel = new JTextArea("Beginning Install");
	
	/**
	 * Creates a new instance of StepInstall.
	 */
	public StepInstall() {
		super();
		final GridBagConstraints constraints = new GridBagConstraints();
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
		
		infoLabel.setEditable(false);
		infoLabel.setWrapStyleWord(true);
		infoLabel.setLineWrap(true);
		infoLabel.setHighlighter(null);
		infoLabel.setBackground(getBackground());
		infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, SMALL_BORDER, 0));
			
		constraints.weightx = 1.0;
		constraints.fill = constraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(infoLabel, constraints);

		constraints.weighty = 1.0;
		constraints.gridy = 4;
		add(Box.createVerticalGlue(), constraints);
	}
	
	/**
	 * Add text to the infolabel.
	 *
	 * @param text Text to add to infoLabel
	 */
	public synchronized void addText(final String text) {
		infoLabel.setText(infoLabel.getText() + text +"\n");
	}
	
	/**
	 * Display Step.
	 */
	public void showStep() {
		Main.getWizardDialog().enableNextStep(false);
		infoLabel.setText("Beginning Install..\n");
		final String location = ((StepSettings) Main.getWizardDialog().getStep(1)).getInstallLocation();
		
		addText("Installing files to: "+location);
		Main.getInstaller().doSetup(location, this);
		
		if (((StepSettings) Main.getWizardDialog().getStep(1)).getShortcutsState()) {
			addText("Setting up shortcuts");
			Main.getInstaller().setupShortcuts(location, this);
		} else {
			addText("Not setting up shortcuts");
		}
		addText("");
		addText("Installation finished\n");
		Main.getWizardDialog().enableNextStep(true);
	}
}
