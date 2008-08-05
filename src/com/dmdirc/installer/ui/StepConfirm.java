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
import com.dmdirc.installer.Settings;
import com.dmdirc.installer.Installer.ShortcutType;
import com.dmdirc.ui.swing.components.TextLabel;
import com.dmdirc.ui.swing.dialogs.wizard.Step;
import com.dmdirc.ui.swing.dialogs.wizard.StepListener;
import com.dmdirc.ui.swing.dialogs.wizard.WizardFrame;

import net.miginfocom.swing.MigLayout;

/**
 * This confirms the settings chosen in the previous step
 */
public final class StepConfirm extends Step implements StepListener {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 2;

	/** Text area showing the install information */
	private final TextLabel infoLabel = new TextLabel("");

	/**
	* Creates a new instance of StepConfirm.
	* @param dialog parent wizard dialog
	*/
	public StepConfirm(final WizardFrame dialog) {
		super();
		dialog.addStepListener(this);
		setLayout(new MigLayout());

		infoLabel.setOpaque(false);

		add(infoLabel, "grow");
	}

	/** {@inheritDoc} */
	@Override
	public void stepAboutToDisplay(final Step step) {
		if (step != this) { return; }
		String shortcutText = "";

		final Settings settings = ((Settings) Main.getWizardFrame().getStep(1));

		if (Main.getInstaller().supportsShortcut(ShortcutType.MENU) && settings.getShortcutMenuState()) {
			shortcutText = shortcutText + " - Create "+Main.getInstaller().getMenuName()+" shortcut\n";
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.DESKTOP) && settings.getShortcutDesktopState()) {
			shortcutText = shortcutText + " - Create desktop shortcut\n";
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.QUICKLAUNCH) && settings.getShortcutQuickState()) {
			shortcutText = shortcutText + " - Create Quick Launch shortcut\n";
		}

		if (Main.getInstaller().supportsShortcut(ShortcutType.PROTOCOL) && settings.getShortcutProtocolState()) {
			shortcutText = shortcutText + " - Make DMDirc handle irc:// links\n";
		}


		final String installLocation = settings.getInstallLocation();


		if (installLocation.isEmpty()) {
			infoLabel.setText("You have chosen an invalid install location\n\n"
			                + "Please press the \"Previous\" button to go back and correct it.");
			Main.getWizardFrame().enableNextStep(false);
		} else {
			infoLabel.setText("Please review your chosen settings:\n\n"
			                + " - Install Location:\n"
			                + "    " +installLocation+"\n"
			                + shortcutText + "\n"
			                + "If you wish to change any of these settings, press the \"Previous\" button,"
			                + "otherwise click \"Next\" to begin the installation");
			Main.getWizardFrame().enableNextStep(true);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stepHidden(final Step step ) {
	//Ignore
	}
}
