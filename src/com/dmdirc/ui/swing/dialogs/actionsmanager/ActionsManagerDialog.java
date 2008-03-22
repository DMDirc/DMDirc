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
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.prefs.validator.RegexStringValidator;
import com.dmdirc.ui.swing.components.JWrappingLabel;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.SwingController;
import com.dmdirc.ui.swing.components.StandardDialog;

import com.dmdirc.ui.swing.components.StandardInputDialog;
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
        ListSelectionListener, com.dmdirc.interfaces.ActionListener {

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
    /** Filename regex. */
    private static final String FILENAME_REGEX = "[A-Za-z0-9 ]+";

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
        ActionManager.addListener(this, CoreActionType.ACTION_CREATED);
        ActionManager.addListener(this, CoreActionType.ACTION_UPDATED);
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

        setLayout(new MigLayout("fill, wrap 2, hidemode 3, nocache"));

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
        add(info, "growx");
        add(actions, "grow");
        add(settings, "growx");
        add(getRightButton(), "skip, right, sgx button");
    }

    /**
     * Reloads the action groups.
     */
    private void reloadGroups() {
        ((DefaultListModel) groups.getModel()).clear();
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
            final StandardInputDialog inputDialog = new StandardInputDialog(SwingController.getMainFrame(), false,
                    "New action group",
                    "Please enter the name of the new action group",
                    new RegexStringValidator(FILENAME_REGEX,
                    "Must be a valid filename")) {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 1;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    if (getText() != null && !getText().isEmpty()) {
                        ActionManager.makeGroup(getText());
                        reloadGroups();
                        return true;
                    } else {
                        return false;
                    }
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                //Ignore
                }
            };
            inputDialog.pack();
            inputDialog.setLocationRelativeTo(this);
            inputDialog.setVisible(true);
        } else if (e.getSource() == edit) {
            final String oldName = ((ActionGroup) groups.getSelectedValue()).getName();
            System.out.println(oldName);
            final StandardInputDialog inputDialog = new StandardInputDialog(SwingController.getMainFrame(), false,
                    "Edit action group",
                    "Please enter the new name of the action group",
                    new RegexStringValidator(FILENAME_REGEX,
                    "Must be a valid filename")) {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything eloh blese that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 1;

                public void StandardInputDialog() {
                }

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    if (getText() != null && !getText().isEmpty()) {
                        ActionManager.renameGroup(oldName, getText());
                        reloadGroups();
                        return true;
                    } else {
                        return false;
                    }
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                //Ignore
                }
            };
            inputDialog.pack();
            inputDialog.setLocationRelativeTo(this);
            inputDialog.setText(oldName);
            inputDialog.setVisible(true);
        } else if (e.getSource() == delete) {
            final String group =
                    ((ActionGroup) groups.getSelectedValue()).getName();
            final int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to delete the '" + group +
                    "' group and all actions within it?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                ActionManager.removeGroup(group);
                reloadGroups();
            }
        } else if (e.getSource() == getOkButton()) {
            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
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

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        reloadGroups();
    }
}
