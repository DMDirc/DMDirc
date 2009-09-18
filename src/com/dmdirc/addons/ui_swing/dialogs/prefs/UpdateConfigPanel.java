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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.addons.ui_swing.components.PackingTable;
import com.dmdirc.updater.UpdateChecker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

/**
 * Updates configuration UI.
 */
public class UpdateConfigPanel extends JPanel implements ActionListener,
        PreferencesInterface {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Global checkbox. */
    private JCheckBox enable;
    /** Table scroll pane, */
    private JScrollPane scrollPane;
    /** Component table model. */
    private UpdateTableModel tableModel;
    /** Component table. */
    private PackingTable table;
    /** Check now button. */
    private JButton checkNow;

    /**
     * Instantiates a new update config panel.
     */
    public UpdateConfigPanel() {
        initComponents();
        addListeners();
        layoutComponents();
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        final Identity identity = IdentityManager.getConfigIdentity();
        if (enable.isSelected()) {
            identity.setOption("updater", "enable", true);
        } else {
            identity.setOption("updater", "enable", false);
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            final String componentName = tableModel.getComponent(i).getName();
            if ((Boolean) tableModel.getValueAt(i, 1)) {
                identity.unsetOption("updater", "enable-" + componentName);
            } else {
                identity.setOption("updater", "enable-" + componentName, false);
            }
        }
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        final ConfigManager config = IdentityManager.getGlobalConfig();
        enable = new JCheckBox();
        scrollPane = new JScrollPane();
        tableModel = new UpdateTableModel(UpdateChecker.getComponents());
        table = new PackingTable(tableModel, false, scrollPane);
        checkNow = new JButton("Check now");

        enable.setSelected(config.getOptionBool("updater", "enable"));
        scrollPane.setViewportView(table);
    }

    /**
     * Adds the listeners.
     */
    private void addListeners() {
        checkNow.addActionListener(this);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fillx, ins 0, hmax " +
                SwingPreferencesDialog.CLIENT_HEIGHT));

        add(new JLabel("Update checking:"), "split");
        add(enable, "growx, pushx, wrap");
        add(scrollPane, "wrap");
        add(checkNow, "right");
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        UpdateChecker.checkNow();
    }
}
