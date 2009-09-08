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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesInterface;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used for the custom nick colour settings component in the plugin's
 * config dialog.
 *
 * @author Chris
 */
public class NickColourPanel extends JPanel implements ActionListener,
        PreferencesInterface, ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** The table used for displaying the options. */
    private final JTable table;
    /** The plugin we're associated with. */
    private final transient NickColourPlugin plugin;
    /** The table headings. */
    private static final String[] headers = {"Network", "Nickname",
        "Text colour", "Nicklist colour"};

    /** Edit and delete buttons. */
    private final JButton editButton, deleteButton;

    /**
     * Creates a new instance of NickColourPanel.
     * 
     * @param plugin The plugin that owns this panel
     */
    public NickColourPanel(final NickColourPlugin plugin) {
        super();

        this.plugin = plugin;

        final Object[][] data = plugin.getData();

        table = new JTable(new DefaultTableModel(data, headers)) {

            /**
             * A version number for this class. It should be changed whenever the class
             * structure is changed (or anything else that would prevent serialized
             * objects being unserialized with the new class).
             */
            private static final long serialVersionUID = 1;
            /** The colour renderer we're using for colour cells. */
            private final ColourRenderer colourRenderer = new ColourRenderer();

            /** {@inheritDoc} */
            @Override
            public TableCellRenderer getCellRenderer(final int row,
                    final int column) {
                if (column == 2 || column == 3) {
                    return colourRenderer;
                } else {
                    return super.getCellRenderer(row, column);
                }
            }

            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        final JScrollPane scrollPane = new JScrollPane(table);

        table.getSelectionModel().addListSelectionListener(this);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Color.class, new ColourRenderer());

        setLayout(new MigLayout("ins 0, fill, h " +
                SwingPreferencesDialog.CLIENT_HEIGHT));
        add(scrollPane, "grow, wrap, spanx, hmax 100%");

        final JButton addButton = new JButton("Add");
        addButton.addActionListener(this);
        add(addButton, "sg button, growx, pushx");

        editButton = new JButton("Edit");
        editButton.addActionListener(this);
        add(editButton, "sg button, growx, pushx");

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this);
        add(deleteButton, "sg button, growx, pushx");

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());

        if (e.getActionCommand().equals("Add")) {
            new NickColourInputDialog(this);
        } else if (e.getActionCommand().equals("Edit")) {
            final int row = table.getSelectedRow();

            final String network = (String) model.getValueAt(row, 0);
            final String nickname = (String) model.getValueAt(row, 1);

            String textcolour = (String) model.getValueAt(row, 2);
            String nickcolour = (String) model.getValueAt(row, 3);

            if (textcolour == null) {
                textcolour = "";
            }

            if (nickcolour == null) {
                nickcolour = "";
            }

            new NickColourInputDialog(this, row, nickname, network, textcolour,
                    nickcolour);
        } else if (e.getActionCommand().equals("Delete")) {
            final int row = table.getSelectedRow();

            if (row > -1) {
                model.removeRow(row);
            }
        }
    }

    /**
     * Removes a row from the table.
     * 
     * @param row The row to be removed
     */
    void removeRow(final int row) {
        ((DefaultTableModel) table.getModel()).removeRow(row);
    }

    /**
     * Adds a row to the table.
     * 
     * @param network The network setting
     * @param nickname The nickname setting
     * @param textcolour The textpane colour setting
     * @param nickcolour The nick list colour setting
     */
    void addRow(final String network, final String nickname,
            final String textcolour, final String nickcolour) {
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());
        model.addRow(new Object[]{network, nickname, textcolour, nickcolour});
    }

    /**
     * Retrieves the current data in use by this panel.
     * 
     * @return This panel's current data.
     */
    List<Object[]> getData() {
        final List<Object[]> res = new ArrayList<Object[]>();
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());

        for (Object row : model.getDataVector()) {
            final Vector vrow = (Vector) row;

            res.add(new Object[]{vrow.elementAt(0), vrow.elementAt(1), vrow.
                        elementAt(2), vrow.elementAt(3)});
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        // Remove all old config entries
        for (Object[] parts : plugin.getData()) {
            IdentityManager.getConfigIdentity().unsetOption(
                    plugin.getDomain(), "color:" + parts[0] + ":" + parts[1]);
        }

        // And write the new ones
        for (Object[] row : getData()) {
            IdentityManager.getConfigIdentity().setOption(plugin.getDomain(),
                    "color:" + row[0] + ":" + row[1], row[2] + ":" + row[3]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean enable = table.getSelectedRow() > -1 && table.getModel().getRowCount() > 0;

        editButton.setEnabled(enable);
        deleteButton.setEnabled(enable);
    }
}
