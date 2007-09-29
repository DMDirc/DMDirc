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

import com.dmdirc.installer.cliparser.CLIParser;
import com.dmdirc.installer.cliparser.BooleanParam;
import com.dmdirc.installer.cliparser.StringParam;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.dialogs.wizard.Step;
import com.dmdirc.ui.swing.dialogs.wizard.Wizard;
import com.dmdirc.ui.swing.dialogs.wizard.WizardDialog;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Main installer window
 *
 * @author Shane Mc Cormack
 */
public final class Main implements Wizard {
	/** Wizard dialog. */
	private static WizardDialog wizardDialog;
	
	/** Installer. */
	private static Installer myInstaller;
	
	/** CLI Parser */
	private static CLIParser cli = CLIParser.getCLIParser();
	
	/**
	 * Notification of step change.
	 *
	 * @param oldStep the step that just stopped being displayed
	 * @param newStep the step now being displayed
	 */
	public void stepChanged(final int oldStep, final int newStep) {	}

	/**
	 * Called when the wizard finishes.
	 */
	public void wizardFinished() {
		Thread temp = myInstaller;
		myInstaller = null;
		if (temp != null) { temp.interrupt(); }
	}
	
	/**
	 * Called when the wizard is cancelled.
	 */
	public void wizardCancelled() {	
		if (wizardDialog.getCurrentStep() != 3
			&& JOptionPane.showConfirmDialog(wizardDialog, 
				"Are you sure you want to cancel?",
				"Cancel confirmation", JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
			                wizardDialog.dispose();
		}
        }

	/**
	 * Creates and Displays the Installer wizard.
	 */
	private Main() {
		try {
			UIUtilities.initUISettings();
		} catch (UnsupportedOperationException ex) {
			//Ignore, revert to default
		}
		
		final List<Step> steps = new ArrayList<Step>();
		final String osName = System.getProperty("os.name");
		
		steps.add(new StepWelcome());
		if (osName.startsWith("Mac OS")) {
			steps.add(new StepError());
		} else {
			steps.add(new StepSettings());
			steps.add(new StepConfirm());
			steps.add(new StepInstall());
		}
		
		String releaseName = "";
		if (cli.getParamNumber("-release") > 0) {
			releaseName = " "+cli.getParam("-release").getStringValue();
		}
		
		setWizardDialog(new WizardDialog("DMDirc"+releaseName+" Installer", steps, this));
		wizardDialog.setPreferredSize(new Dimension(400, 350));
	}
	
	/**
	 * Get the Installer object for this OS.
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
		cli.add(new BooleanParam((char)0, "isroot", "Installing as Root"));
		cli.add(new StringParam('r', "release", "Release Name"));
	}
	
	/**
	 * Get the WizardDialog
	 *
	 * @return The current wizardDialog
	 */
	public static synchronized WizardDialog getWizardDialog() {
		if (wizardDialog == null) {
			new Main();
		}
		return wizardDialog;
	}
	
	/**
	 * Set the WizardDialog
	 *
	 * @return Set the current wizardDialog to the given one
	 */
	private static void setWizardDialog(final WizardDialog dialog) {
		wizardDialog = dialog;
	}

	/**
	 * Run the installer
	 */
	public static void main (String[] args) {
		setupCLIParser();
		cli.parseArgs(args, false);
		getWizardDialog().display();
	}
}
