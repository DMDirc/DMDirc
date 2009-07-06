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
package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.IgnoreList;
import com.dmdirc.Server;
import com.dmdirc.config.prefs.validator.NotEmptyValidator;
import com.dmdirc.config.prefs.validator.RegexValidator;
import com.dmdirc.config.prefs.validator.ValidatorChain;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.addons.ui_swing.UIUtilities;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private IgnoreListModel listModel;
    /** Parent window. */
    private Window parentWindow;

    /**
     * Creates a new instance of IgnoreList.
     *
     * @param server Parent server
     * @param parentWindow Parent window
     */
    public IgnoreListPanel(final Server server, final Window parentWindow) {
        super();

        this.server = server;
        this.parentWindow = parentWindow;

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
        addListeners();
        populateList();
    }

    /** Initialises teh components. */
    private void initComponents() {
        cachedIgnoreList = new IgnoreList(server.getIgnoreList().getRegexList());

        listModel = new IgnoreListModel(cachedIgnoreList);
        list = new JList(listModel);

        final JScrollPane scrollPane = new JScrollPane(list);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addButton = new JButton("Add");
        delButton = new JButton("Remove");

        sizeLabel = new JLabel("0 entries");
        viewToggle = new JCheckBox("Use advanced expressions");
        viewToggle.setOpaque(UIUtilities.getTabbedPaneOpaque());
        viewToggle.setSelected(!cachedIgnoreList.canConvert());
        viewToggle.setEnabled(cachedIgnoreList.canConvert());

        setLayout(new MigLayout("fill, wrap 1"));
        add(scrollPane, "grow, push");
        add(sizeLabel, "split 2, pushx, growx");
        add(viewToggle, "alignx center");
        add(addButton, "split 2, width 50%");
        add(delButton, "width 50%");
    }

    /** Updates the size label. */
    private void updateSizeLabel() {
        sizeLabel.setText(cachedIgnoreList.count() + " entr" + (cachedIgnoreList.count() == 1 ? "y" : "ies"));
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
        if (list.getSelectedIndex() == -1) {
            delButton.setEnabled(false);
        }

        updateSizeLabel();
    }

    /** Updates the list. */
    private void updateList() {
        listModel.notifyUpdated();

        if (cachedIgnoreList.canConvert()) {
            viewToggle.setEnabled(true);
        } else {
            viewToggle.setEnabled(false);
            viewToggle.setSelected(true);
        }
    }

    /** Saves the ignore list. */
    public void saveList() {
        server.getIgnoreList().clear();
        server.getIgnoreList().addAll(cachedIgnoreList.getRegexList());
        server.saveIgnoreList();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addButton) {
            new StandardInputDialog(parentWindow, ModalityType.MODELESS, 
                    "New ignore list entry",
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

                    updateList();
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    //Ignore
                }
            }.display();
        } else if (e.getSource() == delButton && list.getSelectedIndex() != -1 && JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item?",
                "Delete Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            cachedIgnoreList.remove(list.getSelectedIndex());

            updateList();
        } else if (e.getSource() == viewToggle) {
            listModel.setIsSimple(!viewToggle.isSelected());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (list.getSelectedIndex() == -1) {
            delButton.setEnabled(false);
        } else {
            delButton.setEnabled(true);
        }
    }
}
