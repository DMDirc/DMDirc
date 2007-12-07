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

package com.dmdirc.ui.swing.dialogs.actionsmanager;

import com.dmdirc.Main;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;

import com.dmdirc.ui.swing.dialogs.actionseditor.ActionsEditorDialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to manage actions.
 */
public final class ActionsManagerDialog extends StandardDialog
        implements ActionListener, ChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    /** Previously created instance of ActionsManagerDialog. */
    private static ActionsManagerDialog me;
    /** The tapped pane used for displaying groups. */
    private JTabbedPane groups;
    /** Add action button. */
    private JButton addAction;
    /** Edit action button. */
    private JButton editAction;
    /** Delete action button. */
    private JButton deleteAction;
    /** No groups label. */
    private JLabel noGroups;
    /** Group add button. */
    private JButton addGroup;
    /** Group delete button. */
    private JButton deleteGroup;
    /** Group rename button. */
    private JButton renameGroup;

    /** Creates a new instance of ActionsManagerDialog. */
    private ActionsManagerDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        initComponents();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Action Manager");
        setResizable(false);
        setSize(new Dimension(800, 300));
    }

    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showActionsManagerDialog() {
        me = getActionsManagerDialog();

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
            me.loadGroups();
        }

        return me;
    }

    /** Initialiases the components for this dialog. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        getCancelButton().addActionListener(this);
        getOkButton().addActionListener(this);

        final JLabel blurb = new JLabel("Actions allow you to make DMDirc " +
                "respond automatically to events.");

        groups = new JTabbedPane();

        noGroups = new JLabel("You have no action groups.");
        noGroups.setHorizontalAlignment(JLabel.CENTER);

        addGroup = new JButton("Add Group");
        addGroup.setActionCommand("group.add");
        addGroup.addActionListener(this);
        addGroup.setMargin(new Insets(0, 0, 0, 0));

        deleteGroup = new JButton("Delete Group");
        deleteGroup.setActionCommand("group.delete");
        deleteGroup.addActionListener(this);
        deleteGroup.setMargin(new Insets(0, 0, 0, 0));

        renameGroup = new JButton("Rename Group");
        renameGroup.setActionCommand("group.rename");
        renameGroup.addActionListener(this);
        renameGroup.setMargin(new Insets(0, 0, 0, 0));

        addAction = new JButton("New Action");
        addAction.setActionCommand("action.new");
        addAction.addActionListener(this);
        addAction.setMargin(new Insets(0, 0, 0, 0));

        editAction = new JButton("Edit Action");
        editAction.setActionCommand("action.edit");
        editAction.addActionListener(this);
        editAction.setMargin(new Insets(0, 0, 0, 0));
        editAction.setEnabled(false);

        deleteAction = new JButton("Delete Action");
        deleteAction.setActionCommand("action.delete");
        deleteAction.addActionListener(this);
        deleteAction.setMargin(new Insets(0, 0, 0, 0));
        deleteAction.setEnabled(false);

        getCancelButton().setText("Close");
        getCancelButton().setMargin(new Insets(0, 0, 0, 0));

        loadGroups();

        groups.addChangeListener(this);

        setLayout(new MigLayout("fill, hidemode 3"));
        add(blurb, "span 8, wrap");
        add(groups, "span 8, wrap, grow");
        add(noGroups, "span 8, wrap, grow");
        add(addGroup, "skip 1, sg button");
        add(deleteGroup, "sg button");
        add(renameGroup, "sg button");
        add(addAction, "sg button, gapx unrel");
        add(editAction, "sg button");
        add(deleteAction, "sg button");
        add(getCancelButton(), "sg button, gapx unrel");
    }

    /**
     * Enable or disable the edit and delete action button.
     *
     * @param state new State for the buttons
     */
    public void setEditState(final boolean state) {
        editAction.setEnabled(state);
        deleteAction.setEnabled(state);
    }

    /**
     * Retrieves known actions from the action manager and displays the
     * appropriate groups in the dialog.
     */
    public void loadGroups() {

        final int selectedGroup = groups.getSelectedIndex();

        groups.removeAll();

        final Map<String, ActionGroup> actionGroups = ActionManager.getGroups();

        if (actionGroups == null) {
            return;
        }

        final Object[] keys = actionGroups.keySet().toArray();

        Arrays.sort(keys);

        for (Object group : keys) {
            groups.addTab((String) group, new ActionsGroupPanel(this,
                    actionGroups.get((String) group), (String) group));
        }

        if (groups.getTabCount() == 0) {
            groups.setVisible(false);
            noGroups.setVisible(true);
            deleteGroup.setEnabled(false);
            renameGroup.setEnabled(false);
            addAction.setEnabled(false);
        } else {
            groups.setVisible(true);
            noGroups.setVisible(false);
            deleteGroup.setEnabled(true);
            renameGroup.setEnabled(true);
            addAction.setEnabled(true);
        }

        if (groups.getTabCount() > 0 && selectedGroup < groups.getTabCount()) {
            groups.setSelectedIndex(selectedGroup == -1 ? 0 : selectedGroup);
        }
    }

    /**
     * Returns the currently selected group.
     *
     * @return Selected groups name
     */
    public String getSelectedGroup() {
        return groups.getTitleAt(groups.getSelectedIndex());
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getCancelButton() || e.getSource() == getOkButton()) {
            dispose();
        } else if (e.getActionCommand().equals("group.add")) {
            groupAdd();
        } else if (e.getActionCommand().equals("group.delete")) {
            groupDelete();
        } else if (e.getActionCommand().equals("group.rename")) {
            groupRename();
        } else if (e.getActionCommand().equals("action.edit")) {
            actionEdit();
        } else if (e.getActionCommand().equals("action.new")) {
            ActionsEditorDialog.showActionsEditorDialog(this,
                    groups.getTitleAt(groups.getSelectedIndex()));
        } else if (e.getActionCommand().equals("action.delete")) {
            actionDelete();
        }
    }

    /** Adds a group. */
    private void groupAdd() {
        final String newGroup = JOptionPane.showInputDialog(this,
                "Please enter the name of the group to be created.");
        if (newGroup != null && !newGroup.isEmpty()) {
            ActionManager.makeGroup(newGroup);
            loadGroups();
        }
    }

    /** Deletes a group. */
    private void groupDelete() {
        final String group = groups.getTitleAt(groups.getSelectedIndex());
        final int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to delete the '" + group +
                "' group and all actions within it?",
                "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            ActionManager.removeGroup(group);
            loadGroups();
        }
    }

    /** Renames a group. */
    private void groupRename() {
        final String group = groups.getTitleAt(groups.getSelectedIndex());

        final String newName = JOptionPane.showInputDialog(this,
                "Please enter a new name for the '" + group + "' group.",
                "Group rename", JOptionPane.QUESTION_MESSAGE);
        if (newName != null && !newName.isEmpty()) {
            ActionManager.renameGroup(group, newName);
            loadGroups();
        }
    }

    /** Edits an action. */
    private void actionEdit() {
        final JTable table =
                ((ActionsGroupPanel) groups.getSelectedComponent()).getTable();
        if (table.getSelectedRow() != -1 && table.getSelectedRow() <
                table.getRowCount()) {
            final int row =
                    table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
            ActionsEditorDialog.showActionsEditorDialog(this,
                    ((ActionsGroupPanel) groups.getSelectedComponent()).getAction(row),
                    groups.getTitleAt(groups.getSelectedIndex()));
        }
    }

    /** Deletes an action. */
    private void actionDelete() {
        final int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to delete this action?",
                "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            final JTable table =
                    ((ActionsGroupPanel) groups.getSelectedComponent()).getTable();
            if (table.getSelectedRow() != -1 && table.getSelectedRow() <
                    table.getRowCount()) {
                final int row =
                        table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
                ActionManager.deleteAction(((ActionsGroupPanel) groups.getSelectedComponent()).getAction(row));
                loadGroups();
                setEditState(false);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stateChanged(final ChangeEvent e) {
        setEditState(false);
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
