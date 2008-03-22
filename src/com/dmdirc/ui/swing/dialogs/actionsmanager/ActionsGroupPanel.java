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

package com.dmdirc.ui.swing.dialogs.actionsmanager;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.ui.swing.components.PackingTable;
import com.dmdirc.ui.swing.components.renderers.ActionTypeTableCellRenderer;
import com.dmdirc.ui.swing.components.renderers.ArrayCellRenderer;

import com.dmdirc.ui.swing.dialogs.actionseditor.ActionsEditorDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * The actions group panel is the control displayed within the tabbed control
 * of the actions manager dialog. It shows the user all actions belonging to
 * a particular group.
 */
public final class ActionsGroupPanel extends JPanel implements ActionListener,
        ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Table scrollpane. */
    private JScrollPane scrollPane;
    /** Actions table. */
    private PackingTable table;
    /** Table mode. */
    private ActionTableModel model;
    /** Add button. */
    private JButton add;
    /** Edit button. */
    private JButton edit;
    /** Delete button. */
    private JButton delete;
    /** Action group. */
    private ActionGroup group;

    /** 
     * Creates a new instance of ActionsManagerDialog.
     * 
     * @param group Action group to display
     */
    public ActionsGroupPanel(final ActionGroup group) {
        super();

        this.group = group;

        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        scrollPane = new JScrollPane();
        model = new ActionTableModel(group);
        table = new PackingTable(model, false, scrollPane) {

            /**
             * A version number for this class. It should be changed whenever the class
             * structure is changed (or anything else that would prevent serialized
             * objects being unserialized with the new class).
             */
            private static final long serialVersionUID = 1;
            /** Action type renderer. */
            private final ActionTypeTableCellRenderer typeRenderer =
                    new ActionTypeTableCellRenderer();
            /** Action response renrderer. */
            private final ArrayCellRenderer arrayRenderer =
                    new ArrayCellRenderer();

            /** {@inheritDoc} */
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                switch (column) {
                    case 1:
                        return typeRenderer;
                    case 2:
                        return arrayRenderer;
                    default:
                        return super.getCellRenderer(row, column);
                }
            }
        };
        table.addMouseListener(new MouseAdapter() {

            /** {@inheritDoc} */
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    edit.doClick();
                }
            }
        });
        add = new JButton("Add");
        edit = new JButton("Edit");
        delete = new JButton("Delete");

        scrollPane.setViewportView(table);

        table.setAutoCreateRowSorter(true);
        table.setAutoCreateColumnsFromModel(true);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setDragEnabled(false);
        table.setFillsViewportHeight(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getRowSorter().toggleSortOrder(0);

        table.getTableHeader().setReorderingAllowed(false);
        edit.setEnabled(false);
        delete.setEnabled(false);
    }

    /**
     * Adds listeners.
     */
    private void addListeners() {
        add.addActionListener(this);
        edit.addActionListener(this);
        delete.addActionListener(this);
        table.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fill"));

        add(scrollPane, "grow, span 3, wrap");
        add(add, "right, sgx button");
        add(edit, "right, sgx button");
        add(delete, "right, sgx button");
    }

    /**
     * Sets the action group for the panel.
     * 
     * @param group New action group
     */
    public void setActionGroup(final ActionGroup group) {
        this.group = group;

        model.setActionGroup(group);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == add) {
            ActionsEditorDialog.showActionsEditorDialog(group.getName());
        } else if (e.getSource() == edit) {
            ActionsEditorDialog.showActionsEditorDialog(model.getAction(table.getSelectedRow()),
                    group.getName());
        } else if (e.getSource() == delete) {
            JOptionPane.showMessageDialog(this, "Deleting an action: " +
                    model.getValueAt(table.getSelectedRow(), 0));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        if (table.getSelectedRow() == -1) {
            edit.setEnabled(false);
            delete.setEnabled(false);
        } else {
            edit.setEnabled(true);
            delete.setEnabled(true);
        }
    }
}
