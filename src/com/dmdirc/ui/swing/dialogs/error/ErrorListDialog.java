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

package com.dmdirc.ui.swing.dialogs.error;

import com.dmdirc.ui.swing.components.renderers.ErrorLevelIconCellRenderer;
import com.dmdirc.ui.swing.components.renderers.DateCellRenderer;
import com.dmdirc.Main;
import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.PackingTable;
import com.dmdirc.ui.swing.components.StandardDialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Error list dialog.
 */
public final class ErrorListDialog extends StandardDialog implements
        ActionListener, ErrorListener, ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** Previously instantiated instance of ErrorListDialog. */
    private static ErrorListDialog me;
    /** Error manager. */
    private final ErrorManager errorManager;
    /** Table scrollpane. */
    private JScrollPane scrollPane;
    /** Error table. */
    private JTable table;
    /** Table model. */
    private ErrorTableModel tableModel;
    /** Error detail panel. */
    private ErrorDetailPanel errorDetails;
    /** Send button. */
    private JButton sendButton;
    /** Delete button. */
    private JButton deleteButton;
    /** Delete all button. */
    private JButton deleteAllButton;

    /** Creates a new instance of ErrorListDialog. */
    private ErrorListDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        setTitle("DMDirc: Error list");

        errorManager = ErrorManager.getErrorManager();

        initComponents();
        layoutComponents();
        initListeners();

        pack();
    }

    /** Returns the instance of ErrorListDialog. */
    public static synchronized void showErrorListDialog() {
        me = getErrorListDialog();

        me.setLocationRelativeTo(me.getParent());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the ErrorListDialog.
     *
     * @return The current PluginDErrorListDialogialog instance
     */
    public static synchronized ErrorListDialog getErrorListDialog() {
        if (me == null) {
            me = new ErrorListDialog();
        } else if (me.tableModel.getRowCount() !=
                me.errorManager.getErrorCount()) {
            me.tableModel = new ErrorTableModel(new ArrayList<ProgramError>(
                    me.errorManager.getErrorList().values()));
            me.table.setModel(me.tableModel);
            if (me.tableModel.getRowCount() > 0) {
                me.deleteAllButton.setEnabled(true);
            } else {
                me.deleteAllButton.setEnabled(false);
            }
        }

        return me;
    }

    /** Initialises the components. */
    private void initComponents() {
        initButtons();

        scrollPane = new JScrollPane();

        tableModel = new ErrorTableModel(new ArrayList<ProgramError>(
                errorManager.getErrorList().values()));
        table = new PackingTable(tableModel, false, scrollPane) {

            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public TableCellRenderer getCellRenderer(final int row,
                    final int column) {
                switch (column) {
                    case 1:
                        return new DateCellRenderer();
                    case 2:
                        return new ErrorLevelIconCellRenderer();
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

        table.setPreferredScrollableViewportSize(new Dimension(600, 150));

        scrollPane.setViewportView(table);

        errorDetails = new ErrorDetailPanel();
    }

    /** Initialises the buttons. */
    private void initButtons() {
        orderButtons(new JButton(), new JButton());

        getCancelButton().setText("Close");
        sendButton = new JButton("Send");
        deleteButton = new JButton("Delete");
        deleteAllButton = new JButton("Delete All");

        sendButton.setEnabled(false);
        deleteButton.setEnabled(false);
        if (ErrorManager.getErrorManager().getErrorCount() > 0) {
            deleteAllButton.setEnabled(true);
        } else {
            deleteAllButton.setEnabled(false);
        }
    }

    /** Initialises the listeners. */
    private void initListeners() {
        ErrorManager.getErrorManager().addErrorListener(this);
        table.getSelectionModel().addListSelectionListener(this);
        sendButton.addActionListener(this);
        deleteButton.addActionListener(this);
        deleteAllButton.addActionListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                true);
        final JPanel panel = new JPanel();

        panel.setLayout(new MigLayout("fill"));

        panel.add(errorDetails, "wrap, grow");
        panel.add(deleteAllButton, "split 4, tag left, sgx button");
        panel.add(deleteButton, "tag other, sgx button");
        panel.add(sendButton, "tag other, sgx button");
        panel.add(getCancelButton(), "tag ok, sgx button");

        splitPane.setTopComponent(scrollPane);
        splitPane.setBottomComponent(panel);
        
        splitPane.setDividerSize((int) PlatformDefaults.getPanelInsets(0).getValue());

        getContentPane().add(splitPane);
    }

    /** {@inheritDoc}. */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (table.getSelectedRow() > -1) {
                final ProgramError error = tableModel.getError(
                        table.getRowSorter().convertRowIndexToModel(
                        table.getSelectedRow()));
                errorDetails.setError(error);
                deleteButton.setEnabled(true);
                if (error.getReportStatus() == ErrorReportStatus.NOT_APPLICABLE ||
                        error.getReportStatus() == ErrorReportStatus.FINISHED) {
                    sendButton.setEnabled(false);
                } else {
                    sendButton.setEnabled(true);
                }
            } else {
                errorDetails.setError(null);
                deleteButton.setEnabled(false);
                sendButton.setEnabled(false);
            }
        }
    }

    /** 
     * {@inheritDoc}.
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getCancelButton()) {
            setVisible(false);
        } else if (e.getSource() == deleteButton) {
            ErrorManager.getErrorManager().deleteError(tableModel.getError(
                    table.getRowSorter().convertRowIndexToModel(
                    table.getSelectedRow())));
        } else if (e.getSource() == sendButton) {
            ErrorManager.getErrorManager().sendError(tableModel.getError(
                    table.getRowSorter().convertRowIndexToModel(
                    table.getSelectedRow())));
        } else if (e.getSource() == deleteAllButton) {
            final Collection<ProgramError> errors =
                    ErrorManager.getErrorManager().getErrorList().values();
            for (ProgramError error : errors) {
                ErrorManager.getErrorManager().deleteError(error);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void errorAdded(final ProgramError error) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (tableModel) {
                    final int selectedRow = table.getSelectedRow();
                    tableModel.addRow(error);
                    table.getSelectionModel().setSelectionInterval(selectedRow,
                            selectedRow);
                    deleteAllButton.setEnabled(true);
                }
            }
            });
    }

    /** {@inheritDoc} */
    @Override
    public void fatalError(final ProgramError error) {
        new FatalErrorDialog(error);
    }

    /** {@inheritDoc} */
    @Override
    public void errorDeleted(final ProgramError error) {
        synchronized (tableModel) {
            int selectedRow = table.getSelectedRow();
            tableModel.removeRow(error);
            if (selectedRow >= tableModel.getRowCount()) {
                selectedRow = tableModel.getRowCount() - 1;
            }
            table.getSelectionModel().setSelectionInterval(selectedRow,
                    selectedRow);

            if (tableModel.getRowCount() > 0) {
                deleteAllButton.setEnabled(true);
            } else {
                deleteAllButton.setEnabled(false);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void errorStatusChanged(final ProgramError error) {
        final int errorRow;
        synchronized (tableModel) {
            errorRow = tableModel.indexOf(error);

            if (errorRow != -1 && errorRow < tableModel.getRowCount()) {
                tableModel.fireTableRowsUpdated(errorRow, errorRow);
            }
        }
        if (errorRow > -1) {
                deleteButton.setEnabled(true);
                if (error.getReportStatus() == ErrorReportStatus.NOT_APPLICABLE ||
                        error.getReportStatus() == ErrorReportStatus.FINISHED) {
                    sendButton.setEnabled(false);
                } else {
                    sendButton.setEnabled(true);
                }
            } else {
                errorDetails.setError(null);
                deleteButton.setEnabled(false);
                sendButton.setEnabled(false);
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
    public boolean isReady() {
        return Main.getUI().getStatusBar().isVisible();
    }
}
