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

import com.dmdirc.installer.ui.InstallerDialog;
import com.dmdirc.installer.ui.StepWelcome;
import com.dmdirc.installer.ui.StepError;
import com.dmdirc.installer.ui.StepSettings;
import com.dmdirc.installer.ui.StepConfirm;
import com.dmdirc.installer.ui.StepInstall;

/**
 * Main installer entry point.
 */
public final class Main {

    /** Wizard dialog. */
    private static InstallerDialog wizardDialog;
    /** Installer. */
    private static Installer myInstaller;
    /** CLI Parser. */
    private static CLIParser cli = CLIParser.getCLIParser();

    /**
     * Creates and Displays the Installer wizard.
     */
    private Main() {
        try {
            InstallerDialog.initUISettings();
        } catch (UnsupportedOperationException ex) {
            //Ignore, revert to default
        }

        String releaseName = "DMDirc";
        if (cli.getParamNumber("-release") > 0) {
            releaseName = releaseName + " " + cli.getParam("-release").
                    getStringValue();
        }

        setWizardFrame(new InstallerDialog(releaseName + " Installer"));
        getWizardFrame().addWizardListener(new InstallerListener(this));
        getWizardFrame().addStepListener(new InstallerListener(this));

        final String osName = System.getProperty("os.name");
        wizardDialog.addStep(new StepWelcome(releaseName));
        if (osName.startsWith("Mac OS")) {
            wizardDialog.addStep(
                    new StepError(
                    "Sorry, OSX Installation should be done using the downloadable dmg file, not this installer.\n\n"));
        } else {
            if (CLIParser.getCLIParser().getParamNumber("-unattended") == 0) {
                wizardDialog.addStep(new StepSettings());
                wizardDialog.addStep(new StepConfirm());
            }
            wizardDialog.addStep(new StepInstall());
        }
    }

    /**
     * Disposes of the current installer.
     */
    public void disposeOfInstaller() {
        final Thread temp = myInstaller;
        myInstaller = null;
        if (temp != null) {
            temp.interrupt();
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
        cli.add(new BooleanParam('u', "unattended",
                                 "Perform an unattended installation"));
        cli.add(new BooleanParam((char) 0, "no-shortcut-desktop",
                                 "Don't offer a desktop shortcut as the default"));
        cli.add(new BooleanParam((char) 0, "no-shortcut-menu",
                                 "Don't offer a menu shortcut as the default"));
        cli.add(new BooleanParam((char) 0, "no-shortcut-quicklaunch",
                                 "Don't offer a quick launch shortcut as the default"));
        cli.add(new BooleanParam((char) 0, "no-shortcut-protocol",
                                 "Don't offer to handle irc:// links as the default"));
    }

    /**
     * Get the WizardFrame.
     *
     * @return The current wizardDialog
     */
    public static synchronized InstallerDialog getWizardFrame() {
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
    private static void setWizardFrame(final InstallerDialog dialog) {
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
