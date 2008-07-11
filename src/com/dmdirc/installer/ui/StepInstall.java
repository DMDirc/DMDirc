/*
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

import com.dmdirc.installer.Main;
import com.dmdirc.installer.TextStep;
import com.dmdirc.installer.Installer.ShortcutType;
import com.dmdirc.ui.swing.dialogs.wizard.Step;
import com.dmdirc.ui.swing.dialogs.wizard.StepListener;
import com.dmdirc.ui.swing.dialogs.wizard.WizardFrame;

import javax.swing.UIManager;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

/**
 * This confirms the settings chosen in the previous step
 */
public final class StepInstall extends Step implements StepListener, TextStep {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 2;

	/** Text area showing the install information */
	private final JTextArea infoLabel = new JTextArea("Beginning Install");

	/** Scroll pane holding text area */
	final JScrollPane scrollPane;

	/**
	* Creates a new instance of StepInstall.
	* @param dialog parent wizard dialog
	*/
	public StepInstall(final WizardFrame dialog) {
		super();
		dialog.addStepListener(this);
		setLayout(new MigLayout("fill"));

		infoLabel.setFont(UIManager.getFont("Label.font"));
		infoLabel.setEditable(false);
		infoLabel.setWrapStyleWord(true);
		infoLabel.setLineWrap(true);
		//infoLabel.setHighlighter(null);
		infoLabel.setOpaque(false);
		infoLabel.setFont(UIManager.getFont("TextField.font"));
//		infoLabel.setBackground(getBackground());

		scrollPane = new JScrollPane(infoLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, "grow");
	}

	/**
	 * Add text to the infolabel on this step
	 *
	 * @param text Text to add to infoLabel
	 */
	public synchronized void addText(final String text) {
		infoLabel.setText(infoLabel.getText() + text +"\n");
		infoLabel.setCaretPosition(infoLabel.getText().length());
	}
	
	/**
	 * Sets the text in the infolabel on this step
	 *
	 * @param text Text to set the infoLabel to
	 */
	public synchronized void setText(final String text) {
		infoLabel.setText(text);
	}

	/** {@inheritDoc} */
	@Override
	public void stepAboutToDisplay(final Step step) {
		if (step != this) { return; }
		Main.getWizardFrame().enableNextStep(false);
		Main.getWizardFrame().enablePreviousStep(false);
		Main.getInstaller().setInstallStep(this);
		Main.getInstaller().start();
	}

	/** {@inheritDoc} */
	@Override
	public void stepHidden(final Step step) {
		//Ignore
	}
}
