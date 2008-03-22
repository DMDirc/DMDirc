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

package com.dmdirc.ui.swing.dialogs.updater;

import com.dmdirc.Main;
import com.dmdirc.interfaces.UpdateCheckerListener;
import com.dmdirc.ui.interfaces.UpdaterDialog;
import com.dmdirc.ui.swing.components.JWrappingLabel;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.PackingTable;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.renderers.UpdateComponentTableCellRenderer;
import com.dmdirc.ui.swing.components.renderers.UpdateStatusTableCellRenderer;
import com.dmdirc.updater.Update;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateChecker.STATE;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * The updater dialog informs the user of the new update that is available,
 * and walks them through the process of downloading the update.
 */
public final class SwingUpdaterDialog extends StandardDialog implements
        ActionListener, UpdaterDialog, UpdateCheckerListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Previously created instance of SwingUpdaterDialog. */
    private static SwingUpdaterDialog me;
    /** Update table. */
    private JTable table;
    /** Table scrollpane. */
    private JScrollPane scrollPane;
    /** The label we use for the dialog header. */
    private JWrappingLabel header;
    /** UpdateComponent renderer. */
    private UpdateComponentTableCellRenderer updateComponentRenderer;
    /** Update.Status renderer. */
    private UpdateStatusTableCellRenderer updateStatusRenderer;

    /**
     * Creates a new instance of the updater dialog.
     * 
     * @param updates A list of updates that are available.
     */
    private SwingUpdaterDialog(final List<Update> updates) {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        initComponents(updates);
        layoutComponents();

        UpdateChecker.addListener(this);

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        setTitle("Update available");
        setSize(new Dimension(400, 400));
    }

    /**
     * Creates the dialog if one doesn't exist, and displays it.
     * 
     * @param updates The updates that are available
     */
    public static synchronized void showSwingUpdaterDialog(
            final List<Update> updates) {
        me = getSwingUpdaterDialog(updates);
        me.display();
    }

    /**
     * Gets the dialog if one doesn't exist.
     * 
     * @param updates The updates that are available
     * 
     * @return SwingUpdaterDialog instance
     */
    public static synchronized SwingUpdaterDialog getSwingUpdaterDialog(
            final List<Update> updates) {
        if (me == null) {
            me = new SwingUpdaterDialog(updates);
        } else {
            ((UpdateTableModel) me.table.getModel()).setUpdates(updates);
        }

        return me;
    }

    /** 
     * Initialises the components.
     * 
     * @param updates The updates that are available
     */
    private void initComponents(final List<Update> updates) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        updateStatusRenderer = new UpdateStatusTableCellRenderer();
        updateComponentRenderer = new UpdateComponentTableCellRenderer();

        header = new JWrappingLabel("An update is available for one or more "
                + "components of DMDirc:");

        scrollPane = new JScrollPane();
        table = new PackingTable(new UpdateTableModel(updates), false,
                scrollPane) {

            private static final long serialVersionUID = 1;

            @Override
            public TableCellRenderer getCellRenderer(final int row,
                    final int column) {
                switch (column) {
                    case 1:
                        return updateComponentRenderer;
                    case 3:
                        return updateStatusRenderer;
                    default:
                        return super.getCellRenderer(row, column);
                }
            }
        };

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

        scrollPane.setViewportView(table);

        orderButtons(new JButton(), new JButton());
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fill"));

        add(header, "wrap 1.5*unrel");
        add(scrollPane, "grow, wrap");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");
    }

    /** {@inheritDoc} */
    public void display() {
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        setVisible(true);
        requestFocus();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(getOkButton())) {
            getOkButton().setEnabled(false);

            header.setText("DMDirc is updating the following components:");

            for (Update update : ((UpdateTableModel) table.getModel()).getUpdates()) {
                if (!((UpdateTableModel) table.getModel()).isEnabled(update)) {
                    UpdateChecker.removeUpdate(update);
                }
            }

            
            UpdateChecker.applyUpdates();
            
            if (UpdateChecker.getStatus() == STATE.IDLE) {
                dispose();
            }
        } else if (e.getSource().equals(getCancelButton())) {
            setVisible(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void statusChanged(final STATE newStatus) {
        if (newStatus == STATE.UPDATING) {
            getOkButton().setEnabled(false);
        } else {
            getOkButton().setEnabled(true);
        }
    }
}
