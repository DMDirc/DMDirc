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

package com.dmdirc.ui.swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.ui.interfaces.UpdaterDialog;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.updater.Update;
import com.dmdirc.updater.Update.STATUS;
import com.dmdirc.interfaces.UpdateListener;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/**
 * The updater dialog informs the user of the new update that is available,
 * and walks them through the process of downloading the update.
 */
public final class SwingUpdaterDialog extends StandardDialog implements
        ActionListener, UpdaterDialog, UpdateListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Update table headers. */
    private static final String[] HEADERS = new String[]{"Component", 
    "New version", "Status", };
    
    /** Previously created instance of SwingUpdaterDialog. */
    private static SwingUpdaterDialog me;
    
    /** List of updates. */
    private List<Update> updates;
    
    /** Update table. */
    private JTable table;
    
    /** The label we use for the dialog header. */
    private JLabel header;
    
    /**
     * Creates a new instance of the updater dialog.
     * 
     * @param updates A list of updates that are available.
     */
    private SwingUpdaterDialog(final List<Update> updates) {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        
        this.updates = new ArrayList<Update>(updates);
        
        for (Update update : updates) {
            update.addUpdateListener(this);
        }
        
        initComponents();
        
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        
        setTitle("Update available");
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
            me.updates = updates;
            ((DefaultTableModel) me.table.getModel()).setDataVector(
                    me.getTableData(), HEADERS);
        }
        
        return me;
    }
    
    /** Initialises the components. */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setLayout(new BorderLayout());
        
        header = new JLabel("<html><big>Update Available</big><br><br>"
                + "An update is available for one or more "
                + "components of DMDirc:</html>");
        header.setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER,
                LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
        add(header, BorderLayout.NORTH);
        
        
        table = new JTable(new DefaultTableModel(getTableData(), HEADERS)) {
            private static final long serialVersionUID = 1;
            
            @Override
            public boolean isCellEditable(final int x, final int y) {
                return false;
            }
        };
        
        final JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER),
                BorderFactory.createEtchedBorder()));
        pane.setPreferredSize(new Dimension(400, 150));
        add(pane, BorderLayout.CENTER);
        
        final JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new BorderLayout());
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER, LARGE_BORDER));
        
        final JButton lButton = new JButton();
        lButton.setPreferredSize(new Dimension(100, 30));
        final JButton rButton = new JButton();
        rButton.setPreferredSize(new Dimension(100, 30));
        buttonContainer.add(lButton, BorderLayout.WEST);
        buttonContainer.add(rButton, BorderLayout.EAST);
        
        orderButtons(lButton, rButton);
        //getOkButton().setText("Update");
        
        add(buttonContainer, BorderLayout.SOUTH);
        
        pack();
    }
    
    /**
     * Returns the table data for updates
     *
     * @return Table of updates
     */
    private Object[][] getTableData() {
        final String[][] tableData = new String[updates.size()][4];
        
        for (int i = 0; i < updates.size(); i++) {
            tableData[i][0] = updates.get(i).getComponent();
            tableData[i][1] = updates.get(i).getRemoteVersion();
            tableData[i][2] = updates.get(i).getStatus().toString();
        }
        
        return tableData;
    }
    
    /** {@inheritDoc} */
    public void display() {
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        setVisible(true);
        requestFocus();
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(getOkButton())) {
            getOkButton().setEnabled(false);
         
            header.setText("<html><big>Updating...</big><br><br>"
                + "DMDirc is updating the following components:</html>");
            
            for (Update update : updates) {
                if (update.getStatus() == Update.STATUS.PENDING) {
                    update.doUpdate();
                    return;
                }
            }
            
            dispose();
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
    public void updateStatusChange(final Update update, final STATUS status) {
        for (int i = 0; i < updates.size(); i++) {
            if (table.getModel().getValueAt(i, 0).equals(update.getComponent())) {
                table.getModel().setValueAt(status, i, 2);
            }
        }
        
        if (status == Update.STATUS.ERROR || status == Update.STATUS.INSTALLED) {            
            for (Update myupdate : updates) {
                if (myupdate.getStatus() == Update.STATUS.PENDING) {
                    myupdate.doUpdate();
                    return;
                }
            }            
        }
        
        getOkButton().setEnabled(true);
    }
}
