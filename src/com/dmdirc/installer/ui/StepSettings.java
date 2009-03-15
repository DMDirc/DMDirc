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

package com.dmdirc.installer.ui;

import com.dmdirc.installer.Main;
import com.dmdirc.installer.Settings;
import com.dmdirc.installer.DefaultSettings;
import com.dmdirc.installer.Installer.ShortcutType;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

/**
 * Queries the user for where to install dmdirc, and if they want to setup shortcuts
 */
public final class StepSettings extends SwingStep implements Settings {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Menu Shorcuts checkbox. */
    private final JCheckBox shortcutMenu = new JCheckBox("Create " + Main.
            getInstaller().getMenuName() + " shortcut");
    /** Desktop Shorcuts checkbox. */
    private final JCheckBox shortcutDesktop = new JCheckBox(
            "Create desktop shortcut");
    /** Quick-Launch Shorcuts checkbox. */
    private final JCheckBox shortcutQuick = new JCheckBox(
            "Create Quick Launch shortcut");
    /** Register IRC:// protocol. */
    private final JCheckBox shortcutProtocol = new JCheckBox(
            "Make DMDirc handle irc:// links");
    /** Install Location input. */
    private final JTextField location = new JTextField(Main.getInstaller().
            defaultInstallLocation(), 20);

    /**
     * Creates a new instance of StepSettings.
     */
    public StepSettings() {
        super();

        DefaultSettings defaultSettings = new DefaultSettings();
        shortcutMenu.setSelected(defaultSettings.getShortcutMenuState());
        shortcutDesktop.setSelected(defaultSettings.getShortcutDesktopState());
        shortcutQuick.setSelected(defaultSettings.getShortcutQuickState());
        shortcutProtocol.setSelected(defaultSettings.getShortcutProtocolState());

        final GridBagConstraints constraints = new GridBagConstraints();
		setLayout(new GridBagLayout());

        constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
        add(new TextLabel("Here you can choose options for the install." +
                          "\n\nInstall Location:"), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 1;
        constraints.gridy = 1;
        add(location, constraints);

        constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.insets = new Insets(InstallerDialog.SMALL_GAP, 0, 0, 0);

        if (Main.getInstaller().supportsShortcut(ShortcutType.MENU)) {
            constraints.gridy = (constraints.gridy + 1);
            add(shortcutMenu, constraints);
        }
        if (Main.getInstaller().supportsShortcut(ShortcutType.DESKTOP)) {
            constraints.gridy = (constraints.gridy + 1);
            add(shortcutDesktop, constraints);
        }
        if (Main.getInstaller().supportsShortcut(ShortcutType.QUICKLAUNCH)) {
            constraints.gridy = (constraints.gridy + 1);
            add(shortcutQuick, constraints);
        }
        if (Main.getInstaller().supportsShortcut(ShortcutType.PROTOCOL)) {
            constraints.gridy = (constraints.gridy + 1);
            add(shortcutProtocol, constraints);
        }

        constraints.gridy = (constraints.gridy + 1);
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
        add(Box.createVerticalGlue(), constraints);
    }

    /** {@inheritDoc} */
    @Override
    public String getStepName() {
        return "Settings";
    }

    /** {@inheritDoc} */
    @Override
    public Image getIcon() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getStepDescription() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public boolean getShortcutMenuState() {
        return shortcutMenu.isSelected();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getShortcutDesktopState() {
        return shortcutDesktop.isSelected();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getShortcutQuickState() {
        return shortcutQuick.isSelected();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getShortcutProtocolState() {
        return shortcutProtocol.isSelected();
    }

    /** {@inheritDoc} */
    @Override
    public String getInstallLocation() {
        return location.getText().trim();
    }
}
