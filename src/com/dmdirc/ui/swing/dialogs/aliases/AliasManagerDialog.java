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

package com.dmdirc.ui.swing.dialogs.error;

import com.dmdirc.Main;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorStatus;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.swing.components.PackingTable;
import com.dmdirc.ui.swing.components.StandardDialog;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import com.dmdirc.ui.swing.dialogs.aliases.AliasPanel;

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
 * Alias manager dialog.
 */
public final class AliasManagerDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Previously instantiated instance of AliasManagerDialog. */
    private static AliasManagerDialog me;
    
    /** Table headers. */
    private static final String[] HEADERS = new String[]{"Name", };
    
    /** Table scrollpane. */
    private JScrollPane scrollPane;
    
    /** Error table. */
    private JTable table;
    
    /** Error detail panel. */
    private AliasPanel aliasDetails;
    
    /** Buttons pane. */
    private JPanel buttonsPanel;
    
    /** Creates a new instance of ErrorListDialog. */
    private AliasManagerDialog() {
        super(Main.getUI().getMainWindow(), false);
        
        setTitle("DMDirc: Alias manager");
        
        initComponents();
        layoutComponents();
        initListeners();
        
        pack();
        
        setLocationRelativeTo(getParent());
    }
    
    /**
     * Returns the instance of AliasManagerDialog.
     *
     * @return Instance of AliasManagerDialog
     */
    public static synchronized AliasManagerDialog getAliasManagerDialog() {
        if (me == null) {
            me = new AliasManagerDialog();
        }
        return me;
    }
    
    /** Initialises the components. */
    private void initComponents() {
        initButtonsPanel();
        
        scrollPane = new JScrollPane();
        
        table = new PackingTable(new DefaultTableModel(getTableData(), HEADERS), false, scrollPane);
        
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
        
        aliasDetails = new AliasPanel();
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {
        buttonsPanel = new JPanel();
        
        orderButtons(new JButton(), new JButton());
        
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        
        buttonsPanel.setPreferredSize(new Dimension(600, 35));
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.add(getLeftButton());
        buttonsPanel.add(Box.createHorizontalStrut(LARGE_BORDER));
        buttonsPanel.add(getRightButton());
    }
    
    /** Initialises the listeners. */
    private void initListeners() {
        table.getSelectionModel().addListSelectionListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }
    
    /** Lays out the components. */
    private void layoutComponents() {
        getContentPane().setLayout(new BoxLayout(getContentPane(),
                BoxLayout.PAGE_AXIS));
        
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                0, SMALL_BORDER),
                scrollPane.getBorder()));
        aliasDetails.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, 0, 0));
        
        getContentPane().add(scrollPane);
        getContentPane().add(aliasDetails);
        getContentPane().add(buttonsPanel);
    }
    
    /**
     * Retrieves the error data from the ErrorManager.
     *
     * @return Error data
     */
    private Object[][] getTableData() {
        final List<String> aliases = AliasWrapper.getAliasWrapper().getAliases();
        
        final Object[][] data = new Object[aliases.size()][1];
        
        for (int i = 0; i < aliases.size(); i++) {
            data[i][0] = aliases.get(i);
        }
        
        return data;
    }
    
    /** {@inheritDoc}. */
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (table.getSelectedRow() > -1) {
                table.getRowSorter().convertRowIndexToModel(
                        table.getSelectedRow());
                //errorDetails.setError(error);
            } else {
                //errorDetails.setError(null);
            }
        }
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }
    
    /** Updates and resizes the table. */
    private void updateTable() {
        if (table.getRowCount() == 0) {
            return;
        }
        
        final int row = table.getSelectedRow();
        ((DefaultTableModel) table.getModel()).setDataVector(getTableData(),
                HEADERS);
        if (table.getRowCount() > row) {
            table.getSelectionModel().setSelectionInterval(row, row);
        }
    }
    
}
