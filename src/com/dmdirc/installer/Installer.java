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

import com.dmdirc.installer.cliparser.CLIParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Installs DMDirc.
 */
public abstract class Installer extends Thread {

    /** Types of shortcut. */
    public static enum ShortcutType {

        /** Desktop shortcut. */
        DESKTOP,
        /** Menu (start/k/etc) shortcut. */
        MENU,
        /** Quick launch shortcut. */
        QUICKLAUNCH,
        /** The actual uninstaller (not a shortcut, as far as I can tell). */
        UNINSTALLER,
        /** Associate DMDirc with the irc:// protocol (not a shortcut). */
        PROTOCOL;
    }

    /** Step where the installation output should be. */
    protected TextStep step;

    /**
     * Create a new Installer.
     */
    public Installer() {
        super("Installer-Thread");
    }

    /**
     * Get the default install location.
     *
     * @return The default install location
     */
    public abstract String defaultInstallLocation();

    /**
     * This is what helps actually perform the installation in run().
     * This is a hack to keep the installing and the GUI separate.
     *
     * @param step The step that called this
     */
    public final void setInstallStep(final TextStep step) {
        this.step = step;
    }

    /** {@inheritDoc} */
    @Override
    public final void run() {
        step.setText("Beginning Install..\n");

        final boolean isUnattended = (CLIParser.getCLIParser().getParamNumber(
                "-unattended") != 0);
        final Settings settings = (isUnattended) ? new DefaultSettings() : ((Settings) Main.getWizardFrame().
                getStep(1));

        final String location = settings.getInstallLocation();
        step.addText("Installing files to: " + location);
        if (!doSetup(location)) {
            step.addText("");
            step.addText("Installation failed\n");
            Main.getWizardFrame().enableNextStep(true);
            return;
        }

        if (Main.getInstaller().supportsShortcut(ShortcutType.MENU)) {
            if (settings.getShortcutMenuState()) {
                step.addText("Setting up " + Main.getInstaller().getMenuName() +
                             " shortcut");
                setupShortcut(location, ShortcutType.MENU);
            } else {
                step.addText("Not setting up " +
                             Main.getInstaller().getMenuName() + " shortcut");
            }
        }

        if (Main.getInstaller().supportsShortcut(ShortcutType.DESKTOP)) {
            if (settings.getShortcutDesktopState()) {
                step.addText("Setting up Desktop shortcut");
                setupShortcut(location, ShortcutType.DESKTOP);
            } else {
                step.addText("Not setting up Desktop shortcut");
            }
        }

        if (Main.getInstaller().supportsShortcut(ShortcutType.QUICKLAUNCH)) {
            if (settings.getShortcutQuickState()) {
                step.addText("Setting up Quick Launch shortcut");
                setupShortcut(location, ShortcutType.QUICKLAUNCH);
            } else {
                step.addText("Not setting up Quick Launch shortcut");
            }
        }

        if (Main.getInstaller().supportsShortcut(ShortcutType.UNINSTALLER)) {
            step.addText("Creating uninstaller");
            setupShortcut(location, ShortcutType.UNINSTALLER);
        }

        if (Main.getInstaller().supportsShortcut(ShortcutType.PROTOCOL)) {
            if (settings.getShortcutProtocolState()) {
                step.addText("Setting up irc:// handler");
                setupShortcut(location, ShortcutType.PROTOCOL);
            } else {
                step.addText("Not setting up irc:// handler");
            }
        }

        postInstall(location);

        step.addText("");
        step.addText("Installation finished\n");
        Main.getWizardFrame().enableNextStep(true);
    }

    /**
     * Is the given file name vaild to copy to the installation directory?
     *
     * @param filename File to check
     * @return true If the file should be copied, else false.
     */
    public abstract boolean validFile(final String filename);

    /**
     * Main Setup stuff.
     *
     * @param location Location where app will be installed to.
     * @return True if installation passed, else false
     */
    public boolean doSetup(final String location) {
        // Create the directory
        final File directory = new File(location);
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            final File dir = new File(".");
            final FilenameFilter filter = new FilenameFilter() {

                /** {@inheritDoc} */
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.charAt(0) != '.' && validFile(name);
                }
            };
            final String[] children = dir.list(filter);
            if (children != null) {
                for (String filename : children) {
                    step.addText("Copying " + filename);
                    copyFile(filename, location + File.separator + filename);
                }
            }
        } catch (IOException e) {
            step.addText("Error copying files: " + e.getMessage());
            return false;
        }
        step.addText("File Copying Complete.");
        return true;
    }

    /**
     * Check if this OS supports a given shortcut Type.
     *
     * @param shortcutType Type of shortcut to check
     * @return True if this OS supports a given shortcut Type
     */
    public boolean supportsShortcut(final ShortcutType shortcutType) {
        return false;
    }

    /**
     * Check what name to show for the menu shortcut
     *
     * @return Name for menu shortcutType
     */
    public String getMenuName() {
        return "menu";
    }

    /**
     * Setup shortcut.
     *
     * @param location Location where app will be installed to.
     * @param shortcutType Type of shortcut to add.
     */
    protected abstract void setupShortcut(final String location,
                                          final ShortcutType shortcutType);

    /**
     * Copy a file from one location to another.
     * Based on http://www.exampledepot.com/egs/java.io/CopyFile.html
     *
     * @param srcFile Original file
     * @param dstFile New file
     * @throws java.io.IOException If an exception occurs while copying
     */
    protected final void copyFile(final String srcFile, final String dstFile)
            throws IOException {
        if (new File(srcFile).exists()) {
            final FileChannel srcChannel = new FileInputStream(srcFile).
                    getChannel();
            final FileChannel dstChannel = new FileOutputStream(dstFile).
                    getChannel();

            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

            srcChannel.close();
            dstChannel.close();
        } else {
            throw new IOException(srcFile + " does not exist.");
        }
    }

    /**
     * Any post-install tasks should be done here.
     *
     * @param location Location where app was installed to.
     */
    public void postInstall(final String location) {
        // Nothing to do by default, installers may override
    }
}