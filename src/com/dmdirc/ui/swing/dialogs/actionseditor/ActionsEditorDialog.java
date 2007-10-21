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

package com.dmdirc.ui.swing.dialogs.actionseditor;

import com.dmdirc.Main;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionType;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Actions editor dialog, used for adding and creating new actions.
 */
public final class ActionsEditorDialog extends StandardDialog implements
        ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    
    /** Previously created instance of ActionsEditorDialog. */
    private static ActionsEditorDialog me;
    
    /** Parent dialog, informed of changes on close. */
    private final ActionsManagerDialog parent;
    /** Action being edited or null. */
    private Action action;
    /** Tabbed pane. */
    private JTabbedPane tabbedPane;
    /** Buttons panel. */
    private JPanel buttonsPanel;
    /** Actions group. */
    private final String group;
    
    /**
     * Creates a new instance of ActionsEditorDialog.
     *
     * @param parent parent dialog
     * @param action actions to be edited
     * @param group group name
     */
    private ActionsEditorDialog(final ActionsManagerDialog parent,
            final Action action, final String group) {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        
        this.parent = parent;
        this.action = action;
        this.group = group;
        
        setTitle("Actions Editor");
        
        initComponents();
        addListeners();
        layoutComponents();
        
        setSize(new Dimension(770, 300));
        
        setResizable(false);
    }
    
    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parent parent dialog
     * @param group group name
     */
    public static synchronized void showActionsEditorDialog(
            final ActionsManagerDialog parent, final String group) {
        showActionsEditorDialog(parent, null, group);
    }
    
    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parent parent dialog
     * @param action actions to be edited
     * @param group group name
     */
    public static synchronized void showActionsEditorDialog(
            final ActionsManagerDialog parent, final Action action,
            final String group) {
        me = getActionsEditorDialog(parent, action, group);
        
        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }
    
    /**
     * Returns the current instance of the ActionsEditorDialog.
     *
     * @param parent parent dialog
     * @param action actions to be edited
     * @param group group name
     *
     * @return The current ActionsEditorDialog instance
     */
    public static synchronized ActionsEditorDialog getActionsEditorDialog(
            final ActionsManagerDialog parent, final Action action,
            final String group) {
        if (me == null) {
            me = new ActionsEditorDialog(parent, action, group);
        } else if (JOptionPane.showConfirmDialog(parent, 
                "This will discard any changed you have made to existing " 
                + "action, are you sure you want to edit a new action?", 
                "Discard changes", JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            me.dispose();
            me = new ActionsEditorDialog(parent, action, group);
        }
        
        return me;
    }
    
    /** Initialises the components. */
    private void initComponents() {
        initButtonsPanel();
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        tabbedPane.addTab("General", new GeneralTabPanel(this));
        
        tabbedPane.addTab("Conditions", new ConditionsTabPanel(this));
        
        tabbedPane.addTab("Response", new ResponseTabPanel(this));
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {
        orderButtons(new JButton(), new JButton());
        
        buttonsPanel = new JPanel();
        
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(getLeftButton());
        buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonsPanel.add(getRightButton());
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }
    
    /** Lays out the components in the dialog. */
    private void layoutComponents() {
        this.setLayout(new BorderLayout());
        
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.PAGE_END);
        
        pack();
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == getOkButton()) {
            if (ConditionEditorDialog.isConditionEditorDialogOpen()) {
                ConditionEditorDialog.disposeDialog();
            }
            saveSettings();
        } else if (event.getSource() == getCancelButton()) {
            if (ConditionEditorDialog.isConditionEditorDialogOpen()) {
                ConditionEditorDialog.disposeDialog();
            }
            dispose();
        }
    }
    
    /** Clears all conditions. */
    public void clearConditions() {
        ((ConditionsTabPanel) tabbedPane.getComponentAt(1)).clearConditions();
    }
    
    /**
     * Sets the state of the further steps.
     *
     * @param state details tabs state.
     */
    public void setNewConditionButtonState(final boolean state) {
        ((ConditionsTabPanel) tabbedPane.getComponentAt(1)).setNewConditionButton(state);
    }
    
    /** Saves this (new|edited) actions. */
    private void saveSettings() {
        if (((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName().isEmpty()) {
            showError("Empty name", "The action name must not be empty");
            tabbedPane.setSelectedIndex(0);
            ((GeneralTabPanel) tabbedPane.getComponentAt(0)).requestNameFocus();
            return;
        } else if (((GeneralTabPanel) tabbedPane.getComponentAt(0)).getTrigger() == null) {
            showError("No trigger", "The action must have a trigger");
            tabbedPane.setSelectedIndex(0);
            ((GeneralTabPanel) tabbedPane.getComponentAt(0)).requestTriggerFocus();
            return;
        } else if (checkDuplicateName(((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName())) {
            showError("Duplicates", "Another action already has this name, you will ened to choose another");
            tabbedPane.setSelectedIndex(0);
            ((GeneralTabPanel) tabbedPane.getComponentAt(0)).requestNameFocus();
            return;
        } 
        if (action == null) {
            action = new Action(parent.getSelectedGroup(),
                    ((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName(),
                    ((GeneralTabPanel) tabbedPane.getComponentAt(0)).getTriggers().toArray(new ActionType[0]),
                    ((ResponseTabPanel) tabbedPane.getComponentAt(2)).getResponses().split("\\n"),
                    ((ConditionsTabPanel) tabbedPane.getComponentAt(1)).getConditions(),
                    ((ResponseTabPanel) tabbedPane.getComponentAt(2)).getFormatter());
        } else {
            if (!action.getName().equals(((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName())) {
                action.setName(((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName());
            }
            action.setTriggers(((GeneralTabPanel) tabbedPane.getComponentAt(0)).getTriggers().toArray(new ActionType[0]));
            action.setConditions(((ConditionsTabPanel) tabbedPane.getComponentAt(1)).getConditions());
            action.setResponse(((ResponseTabPanel) tabbedPane.getComponentAt(2)).getResponses().split("\\n"));
            action.setNewFormat(((ResponseTabPanel) tabbedPane.getComponentAt(2)).getFormatter());
        }
        action.save();
        parent.loadGroups();
        dispose();
    }
    
    /**
     * Returns the action being edited.
     *
     * @return Action being edited (or null)
     */
    public Action getAction() {
        return action;
    }
    
    /**
     * Returns the active primary trigger.
     *
     * @return Selected primary trigger.
     */
    public ActionType getTrigger() {
        return ((GeneralTabPanel) tabbedPane.getComponentAt(0)).getTrigger();
    }
    
    /**
     * Returns the active primary trigger.
     *
     * @return Selected primary trigger.
     */
    public ActionType[] getTriggers() {
        return ((GeneralTabPanel) tabbedPane.getComponentAt(0)).getTriggers().toArray(new ActionType[0]);
    }
    
    /**
     * Returns the number of conditions in the tab panel.
     *
     * @return Number of Conditions in the dialog.
     */
    public int getConditionCount() {
        return ((ConditionsTabPanel) tabbedPane.getComponentAt(1)).getConditions().size();
    }
    
    /**
     * Sets the action type for the dialog/
     *
     * @param type New ActionType for the dialog
     */
    public void setType(final ActionType type) {
        if (tabbedPane.getComponentCount() > 2) {
            ((ResponseTabPanel) tabbedPane.getComponentAt(2)).setTrigger(type);
        }
    }
    
    /**
     * Checks for duplicate action name.
     *
     * @param name Name to check for duplicate actions
     */
    private boolean checkDuplicateName(final String name) {
        final List<Action> actions = ActionManager.getGroups().get(group);
        
        for (Action loopAction : actions) {
            if (loopAction.getName().equals(name)
            && !loopAction.equals(action)) {
                return true;
            }
        }
        
        return false;
    }
    
    /** Display an error message. */
    private void showError(final String title, final String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
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
