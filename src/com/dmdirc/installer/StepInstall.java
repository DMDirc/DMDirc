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

package com.dmdirc.installer;

import com.dmdirc.installer.cliparser.CLIParser;
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
public final class StepInstall extends Step implements StepListener {
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
	 * Add text to the infolabel.
	 *
	 * @param text Text to add to infoLabel
	 */
	public synchronized void addText(final String text) {
		infoLabel.setText(infoLabel.getText() + text +"\n");
		infoLabel.setCaretPosition(infoLabel.getText().length());
	}

	/**
	 * Perform the installation.
	 */
	public void performInstall(final Installer myInstaller) {
		infoLabel.setText("Beginning Install..\n");
		
		final boolean isUnattended = (CLIParser.getCLIParser().getParamNumber("-unattended") == 0);
		final StepSettings settings = (isUnattended) ? new StepSettings() : ((StepSettings) Main.getWizardFrame().getStep(1));
		
		final String location = settings.getInstallLocation();

		addText("Installing files to: "+location);
		if (!myInstaller.doSetup(location)) {
			addText("");
			addText("Installation failed\n");
			Main.getWizardFrame().enableNextStep(true);
			return;
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.MENU)) {
			if (settings.getShortcutMenuState()) {
				addText("Setting up "+Main.getInstaller().getMenuName()+" shortcut");
				myInstaller.setupShortcut(location, ShortcutType.MENU);
			} else {
				addText("Not setting up "+Main.getInstaller().getMenuName()+" shortcut");
			}
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.DESKTOP)) {
			if (settings.getShortcutDesktopState()) {
				addText("Setting up Desktop shortcut");
				myInstaller.setupShortcut(location, ShortcutType.DESKTOP);
			} else {
				addText("Not setting up Desktop shortcut");
			}
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.QUICKLAUNCH)) {
			if (settings.getShortcutQuickState()) {
				addText("Setting up Quick Launch shortcut");
				myInstaller.setupShortcut(location, ShortcutType.QUICKLAUNCH);
			} else {
				addText("Not setting up Quick Launch shortcut");
			}
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.UNINSTALLER)) {
			addText("Creating uninstaller");
			myInstaller.setupShortcut(location, ShortcutType.UNINSTALLER);
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.PROTOCOL)) {
			if (settings.getShortcutProtocolState()) {
				addText("Setting up irc:// handler");
				myInstaller.setupShortcut(location, ShortcutType.PROTOCOL);
			} else {
				addText("Not setting up irc:// handler");
			}
		}

		myInstaller.postInstall(location);

		addText("");
		addText("Installation finished\n");
		Main.getWizardFrame().enableNextStep(true);
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
