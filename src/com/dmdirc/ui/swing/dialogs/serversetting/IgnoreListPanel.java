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

package com.dmdirc.ui.swing.dialogs.serversetting;

import com.dmdirc.IgnoreList;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.config.prefs.validator.NotEmptyValidator;
import com.dmdirc.config.prefs.validator.RegexValidator;
import com.dmdirc.config.prefs.validator.ValidatorChain;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardInputDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Ignore list panel.
 */
public final class IgnoreListPanel extends JPanel implements ActionListener,
        ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Parent server. */
    private final Server server;
    
    /** Add button. */
    private JButton addButton;
    
    /** Remove button. */
    private JButton delButton;
    
    /** View toggle. */
    private JCheckBox viewToggle;
    
    /** Size label. */
    private JLabel sizeLabel;
    
    /** Ignore list. */
    private JList list;
    
    /** Cached ignore list. */
    private IgnoreList cachedIgnoreList;
    
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
        listModel = new DefaultListModel();
        list = new JList(listModel);
        final JScrollPane scrollPane = new JScrollPane(list);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addButton = new JButton("Add");
        delButton = new JButton("Remove");
        
        sizeLabel = new JLabel("0 entries");
        viewToggle = new JCheckBox("Use advanced expressions");

        setLayout(new MigLayout("fill, wrap 1"));
        add(scrollPane, "growx, growy");
        add(sizeLabel, "split 2, pushx, growx");
        add(viewToggle, "alignx center");
        add(addButton, "split 2, width 50%");
        add(delButton, "width 50%");
    }
    
    /** Updates the size label. */
    private void updateSizeLabel() {
        sizeLabel.setText(cachedIgnoreList.count() + " entr"
                + (cachedIgnoreList.count() == 1 ? "y" : "ies"));
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
        addButton.addActionListener(this);
        delButton.addActionListener(this);
        viewToggle.addActionListener(this);
        list.getSelectionModel().addListSelectionListener(this);
    }
    
    /** Populates the ignore list. */
    private void populateList() {
        if (cachedIgnoreList == null) {
            cachedIgnoreList = new IgnoreList(server.getIgnoreList().getRegexList());
        }
        
        List<String> results;
        
        if (viewToggle.isSelected()) {
            results = cachedIgnoreList.getRegexList();
        } else {
            try {
                results = cachedIgnoreList.getSimpleList();
            } catch (UnsupportedOperationException ex) {
                viewToggle.setSelected(true);
                viewToggle.setEnabled(false);
                results = cachedIgnoreList.getRegexList();
            }
        }
        
        // TODO: Make the model use the ignore list directly, instead of
        //       clearing it and readding items every time.
        listModel.clear();
        for (String ignoreListItem : results) {
            listModel.addElement(ignoreListItem);
        }
        
        if (list.getSelectedIndex() == -1) {
            delButton.setEnabled(false);
        }
        
        updateSizeLabel();
    }
    
    /** Saves the ignore list. */
    public void saveList() {
        server.getIgnoreList().clear();
        server.getIgnoreList().addAll(cachedIgnoreList.getRegexList());
        server.saveIgnoreList();
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addButton) {
            new StandardInputDialog((MainFrame) Main.getUI().getMainWindow(),
                    false, "New ignore list entry",
                    "Please enter the new ignore list entry",
                    viewToggle.isSelected() ? new ValidatorChain<String>(
                    new NotEmptyValidator(), new RegexValidator())
                    : new NotEmptyValidator()) {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 2;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    if (viewToggle.isSelected()) {
                        cachedIgnoreList.add(getText());
                    } else {
                        cachedIgnoreList.addSimple(getText());
                    }

                    populateList();
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                //Ignore
                }
            }.display();            
        } else if (e.getSource() == delButton && list.getSelectedIndex() != -1
                && JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item?",
                "Delete Confirmation", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
            cachedIgnoreList.remove(list.getSelectedIndex());
            
            populateList();
        } else if (e.getSource() == viewToggle) {
            populateList();
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
