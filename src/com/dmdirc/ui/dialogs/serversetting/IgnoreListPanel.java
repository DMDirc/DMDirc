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

package com.dmdirc.ui.dialogs.serversetting;

import com.dmdirc.Server;
import com.dmdirc.identities.ConfigManager;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Ignore list panel.
 */
public class IgnoreListPanel extends JPanel implements ActionListener,
        ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent server. */
    private final transient Server server;
    
    /** Add button. */
    private JButton addButton;
    
    /** Remove button. */
    private JButton delButton;
    
    /** Ignore list. */
    private JList list;
    
    /** Ignore list model . */
    private DefaultListModel listModel;
    
    /**
     * Creates a new instance of IgnoreList.
     *
     * @param server Parent server
     */
    public IgnoreListPanel(final Server server) {
        super();
        
        this.server = server;
        
        initComponents();
        addListeners();
        populateList();
    }
    
    /** Initialises teh components. */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        listModel = new DefaultListModel();
        list = new JList(listModel);
        final JScrollPane scrollPane = new JScrollPane(list);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        setLayout(new GridBagLayout());
        
        addButton = new JButton("Add");
        delButton = new JButton("Remove");
        
        addButton.setPreferredSize(new Dimension(100, 25));
        delButton.setPreferredSize(new Dimension(100, 25));
        addButton.setMinimumSize(new Dimension(100, 25));
        delButton.setMinimumSize(new Dimension(100, 25));
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(SMALL_BORDER, SMALL_BORDER, 0,
                SMALL_BORDER);
        add(scrollPane, constraints);
        
        constraints.weightx = 0.5   ;
        constraints.weighty = 0.0;
        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, 0);
        add(addButton, constraints);
        
        constraints.gridx = 1;
        constraints.insets = new Insets(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        add(delButton, constraints);
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
        addButton.addActionListener(this);
        delButton.addActionListener(this);
        list.getSelectionModel().addListSelectionListener(this);
    }
    
    /** Populates the ignore list. */
    private void populateList() {
        final ConfigManager config = server.getConfigManager();
        
        if (config.hasOption("network", "ignorelist")) {
            final String[] ignoreList = config.getOption("network", "ignorelist").split("\n");
            
            for (String ignoreListItem : ignoreList) {
                listModel.addElement(ignoreListItem);
            }
        }
        
        if (list.getSelectedIndex() == -1) {
            delButton.setEnabled(false);
        }
    }
    
    /** Saves the ignore list. */
    public void saveList() {
        //Save list
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addButton) {
            final String newName = JOptionPane.showInputDialog(this,
                    "Please enter the new ignore item", "");
            if (newName != null && !"".equals(newName)) {
                listModel.addElement(newName);
            }
        } else if (e.getSource() == delButton && list.getSelectedIndex() != -1
                && JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item?",
                "Delete Confirmation", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
            listModel.removeElementAt(list.getSelectedIndex());
        }
    }
    
    /** {@inheritDoc} */
    public void valueChanged(final ListSelectionEvent e) {
        if (list.getSelectedIndex() == -1) {
            delButton.setEnabled(false);
        } else {
            delButton.setEnabled(true);
        }
    }
    
}
