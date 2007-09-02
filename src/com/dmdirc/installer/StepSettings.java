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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Queries the user for where to install dmdirc, and if they want to setup shortcuts
 */
public final class StepSettings extends Step {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 1;
	
	/** Shorcuts checkbox. */
	private final JCheckBox shortcuts = new JCheckBox("Setup shortcuts to DMDirc?");
	/** Install Location input. */
	private final JTextField location = new JTextField(Main.getInstaller().defaultInstallLocation(), 20);
	
	/**
	 * Creates a new instance of StepSettings.
	 */
	public StepSettings() {
		super();
		final GridBagConstraints constraints = new GridBagConstraints();
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
		
		JTextArea infoLabel;
		infoLabel = new JTextArea("Here you can choose options for the install.\n");
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
		constraints.gridwidth = 2;
		add(infoLabel, constraints);

		constraints.gridwidth = 1;
		constraints.gridy = 1;
		add(new JLabel("Install Location:"), constraints);
		constraints.gridx = 1;
		add(location, constraints);
		
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(new JLabel(" "), constraints);
		constraints.gridy = 3;
		shortcuts.setSelected(true);
		add(shortcuts, constraints);

		constraints.weighty = 1.0;
		constraints.gridy = 4;
		add(Box.createVerticalGlue(), constraints);
	}
	
	/**
	 * Returns the state of the shortcuts checkbox.
	 *
	 * @return shortcuts checkbox state
	 */
	public boolean getShortcutsState() {
		return shortcuts.isSelected();
	}
	
	/**
	 * Returns the location chosen for installation.
	 *
	 * @return location chosen for installation.
	 */
	public String getInstallLocation() {
		return location.getText();
	}
	
}
