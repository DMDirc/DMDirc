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

package uk.org.ownage.dmdirc.ui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import uk.org.ownage.dmdirc.actions.Action;
import uk.org.ownage.dmdirc.actions.ActionManager;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.ActionsGroupPanel;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;
import uk.org.ownage.dmdirc.ui.dialogs.actionseditor.ActionsEditorDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Allows the user to manage actions.
 */
public final class ActionsManagerDialog extends StandardDialog
        implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Previously created instance of ActionsManagerDialog. */
    private static ActionsManagerDialog me;
    
    /** Height of the buttons, in pixels. */
    private static final int BUTTON_HEIGHT = 25;
    /** Width of the ubttons, in pixels. */
    private static final int BUTTON_WIDTH = 100;
    
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
    /** Group delete button. */
    private JButton deleteGroup;
    /** Group rename button. */
    private JButton renameGroup;
    
    /** Creates a new instance of ActionsManagerDialog. */
    private ActionsManagerDialog() {
        super(MainFrame.getMainFrame(), false);
        
        initComponents();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Action Manager");
        setResizable(false);
        setLocationRelativeTo(MainFrame.getMainFrame());
        setVisible(true);
    }
    
    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showActionsManagerDialog() {
        if (me == null) {
            me = new ActionsManagerDialog();
        } else {
            me.loadGroups();
            me.setVisible(true);
            me.requestFocus();
        }
    }
    
    /** Initialiases the components for this dialog. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        getCancelButton().addActionListener(this);
        getOkButton().addActionListener(this);
        
        setLayout(new GridBagLayout());
        
        final GridBagConstraints constraints = new GridBagConstraints();
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 7;
        final JLabel blurb = new JLabel("Actions allow you to make DMDirc "
                + "respond automatically to events.");
        blurb.setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER));
        add(blurb, constraints);
        
        constraints.gridy++;
        groups = new JTabbedPane();
        groups.setPreferredSize(new Dimension(400, 200));
        groups.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER));
        add(groups, constraints);
        
        noGroups = new JLabel("You have no action groups.");
        noGroups.setHorizontalAlignment(JLabel.CENTER);
        noGroups.setPreferredSize(new Dimension(400, 200));
        noGroups.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER));
        add(noGroups, constraints);
        
        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.insets.set(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER, 0);
        JButton myButton = new JButton("Add Group");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("group.add");
        myButton.addActionListener(this);
        myButton.setMargin(new Insets(0, 0, 0, 0));
        add(myButton, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        deleteGroup = new JButton("Delete Group");
        deleteGroup.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        deleteGroup.setActionCommand("group.delete");
        deleteGroup.addActionListener(this);
        deleteGroup.setMargin(new Insets(0, 0, 0, 0));
        add(deleteGroup, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        renameGroup = new JButton("Rename Group");
        renameGroup.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        renameGroup.setActionCommand("group.rename");
        renameGroup.addActionListener(this);
        renameGroup.setMargin(new Insets(0, 0, 0, 0));
        add(renameGroup, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER, 0);
        addAction = new JButton("New Action");
        addAction.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        addAction.setActionCommand("action.new");
        addAction.addActionListener(this);
        addAction.setMargin(new Insets(0, 0, 0, 0));
        add(addAction, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        editAction = new JButton("Edit Action");
        editAction.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        editAction.setActionCommand("action.edit");
        editAction.addActionListener(this);
        editAction.setMargin(new Insets(0, 0, 0, 0));
        editAction.setEnabled(false);
        add(editAction, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        deleteAction = new JButton("Delete Action");
        deleteAction.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        deleteAction.setActionCommand("action.delete");
        deleteAction.addActionListener(this);
        deleteAction.setMargin(new Insets(0, 0, 0, 0));
        deleteAction.setEnabled(false);
        add(deleteAction, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER,
                LARGE_BORDER);
        myButton = new JButton("Close");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("close");
        myButton.addActionListener(this);
        myButton.setMargin(new Insets(0, 0, 0, 0));
        add(myButton, constraints);
        
        loadGroups();
        
        pack();
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
        
        final Map<String, List<Action>> actionGroups = ActionManager.getGroups();
        
        final Object[] keys = actionGroups.keySet().toArray();
        
        Arrays.sort(keys);
        
        for (Object group : keys) {
            groups.addTab((String) group,
                    new ActionsGroupPanel(this, actionGroups.get(group)));
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
        
        groups.setSelectedIndex(selectedGroup == -1 ? 0 : selectedGroup);
    }
    
    /**
     * Returns the currently selected group.
     *
     * @return Selected groups name
     */
    public String getSelectedGroup() {
        return groups.getTitleAt(groups.getSelectedIndex());
        
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("close") 
        || e.getSource() == getCancelButton() || e.getSource() == getOkButton()) {
            dispose();
        } else if (e.getActionCommand().equals("group.add")) {
            final String newGroup = JOptionPane.showInputDialog(this,
                    "Please enter the name of the group to be created.");
            if (newGroup != null && newGroup.length() > 0) {
                ActionManager.makeGroup(newGroup);
                loadGroups();
            }
        } else if (e.getActionCommand().equals("group.delete")) {
            final String group = groups.getTitleAt(groups.getSelectedIndex());
            final Map<String, List<Action>> actionGroups = ActionManager.getGroups();
            
            final int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to delete the '" + group
                    + "' group and all actions within it?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                ActionManager.removeGroup(group);
                loadGroups();
            }
        } else if (e.getActionCommand().equals("group.rename")) {
            final String group = groups.getTitleAt(groups.getSelectedIndex());
            final Map<String, List<Action>> actionGroups = ActionManager.getGroups();
            
            final String newName = JOptionPane.showInputDialog(this,
                    "Please enter a new name for the '" + group
                    + "' group.",
                    "Group rename", JOptionPane.QUESTION_MESSAGE);
            if (newName != null && newName.length() > 0) {
                ActionManager.renameGroup(group, newName);
                loadGroups();
            }
        } else if (e.getActionCommand().equals("action.edit")) {
            final JTable table = ((ActionsGroupPanel) groups.getSelectedComponent()).getTable();
            final int row = table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
            ActionsEditorDialog.showActionsEditorDialog(this,
                    ((ActionsGroupPanel) groups.getSelectedComponent()).getAction(row));
        } else if (e.getActionCommand().equals("action.new")) {
            ActionsEditorDialog.showActionsEditorDialog(this);
        } else if (e.getActionCommand().equals("action.delete")) {
            final int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to delete this action?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                final JTable table = ((ActionsGroupPanel) groups.getSelectedComponent()).getTable();
                final int row = table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
                ActionManager.deleteAction(((ActionsGroupPanel) groups.getSelectedComponent()).getAction(row));
                loadGroups();
            }
        }
    }
    
}
