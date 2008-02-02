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

import com.dmdirc.Main;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.ui.swing.JWrappingLabel;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;

import com.dmdirc.ui.swing.components.renderers.ActionGroupListCellRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to manage actions.
 */
public final class ActionsManagerDialog extends StandardDialog implements ActionListener,
        ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Previously created instance of ActionsManagerDialog. */
    private static ActionsManagerDialog me;
    /** Info label. */
    private JWrappingLabel infoLabel;
    /** Group list. */
    private JList groups;
    /** Add button. */
    private JButton add;
    /** Edit button. */
    private JButton edit;
    /** Delete button. */
    private JButton delete;
    /** Info panel. */
    private ActionGroupInformationPanel info;
    /** Actions panel. */
    private ActionsGroupPanel actions;
    /** Settings panel. */
    private ActionGroupSettingsPanel settings;

    /** Creates a new instance of ActionsManagerDialog. */
    private ActionsManagerDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        initComponents();
        addListeners();
        layoutComponents();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("DMDirc: Action Manager");
        setResizable(false);
    }

    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showActionsManagerDialog() {
        me = getActionsManagerDialog();

        me.pack();
        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the ActionsManagerDialog.
     *
     * @return The current ActionsManagerDialog instance
     */
    public static synchronized ActionsManagerDialog getActionsManagerDialog() {
        if (me == null) {
            me = new ActionsManagerDialog();
        } else {
            me.reloadGroups();
        }

        return me;
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        infoLabel = new JWrappingLabel("Actions allow you to automate many " +
                "aspects of DMDirc, they alow you to intelligently respond " +
                "to different events.");
        groups = new JList(new DefaultListModel());
        actions = new ActionsGroupPanel(null);
        info = new ActionGroupInformationPanel(null);
        settings = new ActionGroupSettingsPanel(null);
        add = new JButton("Add");
        edit = new JButton("Edit");
        delete = new JButton("Delete");

        groups.setCellRenderer(new ActionGroupListCellRenderer());
        groups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        edit.setEnabled(false);
        delete.setEnabled(false);
        
        info.setVisible(false);
        settings.setVisible(false);

        reloadGroups();
    }

    /**
     * Adds listeners.
     */
    private void addListeners() {
        getOkButton().addActionListener(this);
        add.addActionListener(this);
        edit.addActionListener(this);
        delete.addActionListener(this);
        groups.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        final JPanel groupPanel = new JPanel();

        groupPanel.setLayout(new MigLayout("fill, wrap 1"));

        groupPanel.add(new JScrollPane(groups), "growy, w 200");
        groupPanel.add(add, "sgx button, w 200");
        groupPanel.add(edit, "sgx button, w 200");
        groupPanel.add(delete, "sgx button, w 200");

        setLayout(new MigLayout("fill, wrap 2, hidemode 2"));

        groupPanel.setBorder(BorderFactory.createTitledBorder(groupPanel.getBorder(),
                "Groups"));
        info.setBorder(BorderFactory.createTitledBorder(info.getBorder(),
                "Information"));
        actions.setBorder(BorderFactory.createTitledBorder(actions.getBorder(),
                "Actions"));
        settings.setBorder(BorderFactory.createTitledBorder(settings.getBorder(),
                "Settings"));

        add(infoLabel, "spanx 2");
        add(groupPanel, "growy, spany 3");
        add(info, "grow");
        add(actions, "grow");
        add(settings, "grow");
        add(getRightButton(), "skip, right, sgx button");
    }

    /**
     * Reloads the action groups.
     */
    private void reloadGroups() {
        for (ActionGroup group : ActionManager.getGroups().values()) {
            ((DefaultListModel) groups.getModel()).addElement(group);
        }
    }
    
    /**
     * Changes the active group.
     * 
     * @param group New group
     */
    private void changeActiveGroup(final ActionGroup group) {
        info.setActionGroup(group);
        actions.setActionGroup(group);
        settings.setActionGroup(group);
        
        info.setVisible(info.shouldDisplay());
        settings.setVisible(settings.shouldDisplay());
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == add) {
            JOptionPane.showMessageDialog(this, "Adding an action group");
        } else if (e.getSource() == edit) {
            JOptionPane.showMessageDialog(this, "Editing an action group: " +
                    ((ActionGroup) groups.getSelectedValue()).getName());
        } else if (e.getSource() == delete) {
            JOptionPane.showMessageDialog(this, "Deleting an action group: " +
                    ((ActionGroup) groups.getSelectedValue()).getName());
        } else if (e.getSource() == getOkButton()) {
            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        if (groups.getSelectedIndex() == -1) {
            edit.setEnabled(false);
            delete.setEnabled(false);
        } else {
            edit.setEnabled(true);
            delete.setEnabled(true);
        }
        changeActiveGroup((ActionGroup) groups.getSelectedValue());
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
