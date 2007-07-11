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

package com.dmdirc.ui.dialogs.error;

import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorStatus;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.components.StandardDialog;
import static com.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

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
    private static final long serialVersionUID = 2;
    
    /** Previously instantiated instance of ErrorListDialog. */
    private static ErrorListDialog me;
    
    /** Error table headers. */
    private static final String[] HEADERS = new String[]{"ID", "Time",
    "Severity", "Report status", "Message", };
    
    /** Error manager. */
    private final ErrorManager errorManager;
    
    /** Error table. */
    private JTable table;
    
    /** Error detail panel. */
    private ErrorDetailPanel errorDetails;
    
    /** Buttons pane. */
    private JPanel buttonsPanel;
    
    /** Send button. */
    private JButton sendButton;
    
    /** Delete button. */
    private JButton deleteButton;
    
    /** Delete all button. */
    private JButton deleteAllButton;
    
    /** Creates a new instance of ErrorListDialog. */
    private ErrorListDialog() {
        super(MainFrame.getMainFrame(), false);
        
        setTitle("DMDirc: Error list");
        
        errorManager = ErrorManager.getErrorManager();
        
        initComponents();
        layoutComponents();
        initListeners();
        
        pack();
        
        setLocationRelativeTo(getParent());
    }
    
    /**
     * Returns the instance of ErrorListDialog.
     *
     * @return Instance of ErrorListDialog
     */
    public static synchronized ErrorListDialog getErrorListDialog() {
        if (me == null) {
            me = new ErrorListDialog();
        }
        return me;
    }
    
    /** Initialises the components. */
    private void initComponents() {
        initButtonsPanel();
        
        table = new JTable(new DefaultTableModel(getTableData(), HEADERS) {
            private static final long serialVersionUID = 1;
            
            public boolean isCellEditable(final int x, final int y) {
                return false;
            }
        });
        
        table.setAutoCreateRowSorter(true);
        table.setAutoCreateColumnsFromModel(true);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setDragEnabled(false);
        table.setFillsViewportHeight(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getRowSorter().toggleSortOrder(0);
        
        table.getTableHeader().setReorderingAllowed(false);
        
        table.setPreferredScrollableViewportSize(new Dimension(600, 150));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(280);
        
        errorDetails = new ErrorDetailPanel();
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {
        buttonsPanel = new JPanel();
        
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
        
        sendButton.setPreferredSize(new Dimension(100, 25));
        deleteButton.setPreferredSize(new Dimension(100, 25));
        sendButton.setMinimumSize(new Dimension(100, 25));
        deleteButton.setMinimumSize(new Dimension(100, 25));
        deleteAllButton.setMinimumSize(new Dimension(100, 25));
        
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        
        buttonsPanel.setPreferredSize(new Dimension(600, 35));
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(deleteAllButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonsPanel.add(sendButton);
        buttonsPanel.add(Box.createHorizontalStrut(LARGE_BORDER));
        buttonsPanel.add(getCancelButton());
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
        getContentPane().setLayout(new BoxLayout(getContentPane(),
                BoxLayout.PAGE_AXIS));
        
        final JScrollPane scrollPane = new JScrollPane(table);
        
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                0, SMALL_BORDER),
                scrollPane.getBorder()));
        errorDetails.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, 0, 0));
        
        getContentPane().add(scrollPane);
        getContentPane().add(errorDetails);
        getContentPane().add(buttonsPanel);
    }
    
    /**
     * Retrieves the error data from the ErrorManager.
     *
     * @return Error data
     */
    private Object[][] getTableData() {
        final List<ProgramError> errors = errorManager.getErrorList();
        
        final Object[][] data = new Object[errors.size()][5];
        
        for (int i = 0; i < errors.size(); i++) {
            final ProgramError error = errors.get(i);
            data[i][0] = i;
            data[i][1] = error.getDate();
            data[i][2] = error.getLevel();
            data[i][3] = error.getStatus();
            data[i][4] = error.getMessage();
        }
        
        return data;
    }
    
    /** {@inheritDoc}. */
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (table.getSelectedRow() > -1) {
                final ProgramError error = errorManager.getError(
                        table.getRowSorter().convertRowIndexToModel(
                        table.getSelectedRow()));
                errorDetails.setError(error);
                deleteButton.setEnabled(true);
                if (error.getStatus() == ErrorStatus.NOT_APPLICABLE
                        || error.getStatus() == ErrorStatus.FINISHED) {
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
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getCancelButton()) {
            dispose();
        } else if (e.getSource() == deleteButton) {
            ErrorManager.getErrorManager().deleteError(errorManager.getError(
                    table.getRowSorter().convertRowIndexToModel(
                    table.getSelectedRow())));
        } else if (e.getSource() == sendButton) {
            ErrorManager.getErrorManager().sendError(errorManager.getError(
                    table.getRowSorter().convertRowIndexToModel(
                    table.getSelectedRow())));
        } else if (e.getSource() == deleteAllButton) {
            final List<ProgramError> errors =
                    ErrorManager.getErrorManager().getErrorList();
            for (ProgramError error : errors) {
                ErrorManager.getErrorManager().deleteError(error);
            }
        }
    }
    
    /** Updates and resizes the table. */
    private void updateTable() {
        ((DefaultTableModel) table.getModel()).setDataVector(getTableData(),
                HEADERS);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(280);
    }
    
    /** {@inheritDoc} */
    public void errorAdded(final ProgramError error) {
        updateTable();
        deleteAllButton.setEnabled(true);
    }
    
    /** {@inheritDoc} */
    public void errorDeleted(final ProgramError error) {
        updateTable();
        if (ErrorManager.getErrorManager().getErrorCount() > 0) {
            deleteAllButton.setEnabled(true);
        }
    }
    
    /** {@inheritDoc} */
    public void errorStatusChanged(final ProgramError error) {
        updateTable();
    }
    
}
