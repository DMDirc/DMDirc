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

package com.dmdirc.installer;

import com.dmdirc.installer.cliparser.BooleanParam;
import com.dmdirc.installer.cliparser.CLIParser;
import com.dmdirc.installer.cliparser.StringParam;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.dialogs.wizard.Step;
import com.dmdirc.ui.swing.dialogs.wizard.WizardFrame;
import com.dmdirc.ui.swing.dialogs.wizard.WizardListener;

import com.dmdirc.ui.swing.installer.StepWelcome;
import com.dmdirc.ui.swing.installer.StepError;
import com.dmdirc.ui.swing.installer.StepSettings;
import com.dmdirc.ui.swing.installer.StepConfirm;
import com.dmdirc.ui.swing.installer.StepInstall;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * Main installer window.
 *
 * @author Shane Mc Cormack
 */
public final class Main implements WizardListener {
	
	/** Wizard dialog. */
	private static WizardFrame wizardDialog;
	
	/** Installer. */
	private static Installer myInstaller;
	
	/** CLI Parser. */
	private static CLIParser cli = CLIParser.getCLIParser();
	
	/**
	 * Creates and Displays the Installer wizard.
	 */
	private Main() {
		try {
			UIUtilities.initUISettings();
		} catch (UnsupportedOperationException ex) {
			//Ignore, revert to default
		}
		
		String releaseName = "DMDirc";
		if (cli.getParamNumber("-release") > 0) {
			releaseName = releaseName + " " + cli.getParam("-release").getStringValue();
		}
		
		setWizardFrame(new WizardFrame(releaseName + " Installer", new ArrayList<Step>(), this));
		wizardDialog.setIconImage(IconManager.getIconManager().getImage("icon"));
		wizardDialog.setPreferredSize(new Dimension(400, 350));
		wizardDialog.setMaximumSize(new Dimension(400, 350));
		wizardDialog.addWizardListener(this);

		final String osName = System.getProperty("os.name");
		wizardDialog.addStep(new StepWelcome(releaseName));
		if (osName.startsWith("Mac OS")) {
			wizardDialog.addStep(new StepError("Sorry, OSX Installation should be done using the downloadable dmg file, not this installer.\n\n"));
		} else {
			if (CLIParser.getCLIParser().getParamNumber("-unattended") == 0) {
				wizardDialog.addStep(new StepSettings());
				wizardDialog.addStep(new StepConfirm(wizardDialog));
			}
			wizardDialog.addStep(new StepInstall(wizardDialog));
		}
	}
		
	/**
	 * Called when the wizard finishes.
	 */
	@Override
	public void wizardFinished() {
		final Thread temp = myInstaller;
		myInstaller = null;
		if (temp != null) { temp.interrupt(); }
		wizardDialog.dispose();
	}
	
	/**
	 * Called when the wizard is cancelled.
	 */
	@Override
	public void wizardCancelled() {	
		if (wizardDialog.getCurrentStep() != 3 && JOptionPane.showConfirmDialog(wizardDialog, "Are you sure you want to cancel?", "Cancel confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
			wizardDialog.dispose();
		}
	}
	
	/**
	 * Get the Installer object for this OS.
	 *
	 * @return The installer for this OS
	 */
	public static synchronized Installer getInstaller() {
		if (myInstaller == null) {
			final String osName = System.getProperty("os.name");
			if (osName.startsWith("Mac OS")) {
				// myInstaller = new MacInstaller();
			} else if (osName.startsWith("Windows")) {
				myInstaller = new WindowsInstaller();
			} else {
				myInstaller = new LinuxInstaller();
			}
		}
		
		return myInstaller;
	}

	/**
	 * Setup the cli parser.
	 * This clears the current CLIParser params and creates new ones.
	 */
	private static void setupCLIParser() {
		cli.clear();
		cli.add(new StringParam('h', "help", "Get Help"));
		cli.setHelp(cli.getParam("-help"));
		cli.add(new BooleanParam((char) 0, "isroot", "Installing as Root"));
		cli.add(new StringParam('r', "release", "Release Name"));
		cli.add(new StringParam('d', "directory", "Default install directory"));
		cli.add(new BooleanParam('u', "unattended", "Perform an unattended installation"));
		cli.add(new BooleanParam((char) 0, "no-shortcut-desktop", "Don't offer a desktop shortcut as the default"));
		cli.add(new BooleanParam((char) 0, "no-shortcut-menu", "Don't offer a menu shortcut as the default"));
		cli.add(new BooleanParam((char) 0, "no-shortcut-quicklaunch", "Don't offer a quick launch shortcut as the default"));
		cli.add(new BooleanParam((char) 0, "no-shortcut-protocol", "Don't offer to handle irc:// links as the default"));
	}
	
	/**
	 * Get the WizardFrame.
	 *
	 * @return The current wizardDialog
	 */
	public static synchronized WizardFrame getWizardFrame() {
		if (wizardDialog == null) {
			new Main();
		}
		return wizardDialog;
	}
	
	/**
	 * Set the WizardFrame.
	 *
	 * @param dialog The new WizardDialog
	 */
	private static void setWizardFrame(final WizardFrame dialog) {
		wizardDialog = dialog;
	}

	/**
	 * Run the installer.
	 *
	 * @param args Command line arguments
	 */
	public static void main(final String[] args) {
		setupCLIParser();
		if (cli.wantsHelp(args)) {
			cli.showHelp("DMDirc installer Help", "[options [--]]");
			System.exit(0);
		}
		cli.parseArgs(args, false);
		getWizardFrame().display();
	}
}
