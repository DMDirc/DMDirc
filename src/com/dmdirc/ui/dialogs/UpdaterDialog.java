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

package com.dmdirc.ui.dialogs;

import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.components.StandardDialog;
import com.dmdirc.updater.Update;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;

import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.UIUtilities.LARGE_BORDER;

/**
 * The updater dialog informs the user of the new update that is available,
 * and walks them through the process of downloading the update.
 * @author chris
 */
public class UpdaterDialog extends StandardDialog implements ActionListener {
    
    private List<Update> updates;
    
    private JLabel header;
    
    /**
     * Creates a new instance of the updater dialog.
     * @param updates A list of updates that are available.
     */
    public UpdaterDialog(List<Update> updates) {
        super(MainFrame.getMainFrame(), false);
        
        this.updates = updates;
        
        initComponents();
        
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        
        setTitle("Update available");
        setLocationRelativeTo(MainFrame.getMainFrame());
        setVisible(true);
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
        
        final String[][] tableData = new String[updates.size()][4];
        
        for (int i = 0; i < updates.size(); i++) {
            tableData[i][0] = updates.get(i).getComponent();
            tableData[i][1] = updates.get(i).getLocalVersion();
            tableData[i][2] = updates.get(i).getRemoteVersion();
            tableData[i][3] = "Pending";
        }
        
        final JTable table = new JTable(tableData,
                new String[]{"Component", "Local version", "Remote version", "Status"}) {
            private static final long serialVersionUID = 1;
            
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
        getOkButton().setText("Update");
        
        add(buttonContainer, BorderLayout.SOUTH);
        
        pack();
    }

    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(getOkButton())) {
            getOkButton().setEnabled(false);
            
            header.setText("<html><big>Updating...</big><br><br>"
                + "DMDirc is updating the following components:</html>");
        } else if (e.getSource().equals(getCancelButton())) {
            dispose();
        }
    }
}