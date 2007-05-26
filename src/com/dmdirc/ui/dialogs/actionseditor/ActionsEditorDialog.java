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

package com.dmdirc.ui.dialogs.actionseditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionType;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.components.StandardDialog;
import com.dmdirc.ui.dialogs.ActionsManagerDialog;

import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

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
    private static final long serialVersionUID = 2;
    
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
    
    /**
     * Creates a new instance of ActionsEditorDialog.
     *
     * @param parent parent dialog
     * @param action actions to be edited
     */
    private ActionsEditorDialog(final ActionsManagerDialog parent,
            final Action action) {
        super(MainFrame.getMainFrame(), false);
        
        this.parent = parent;
        this.action = action;
        
        this.setTitle("Actions Editor");
        
        initComponents();
        addListeners();
        layoutComponents();
        
        this.setLocationRelativeTo(MainFrame.getMainFrame());
        
        this.setVisible(true);
    }
    
    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parent parent dialog
     */
    public static synchronized void showActionsEditorDialog(
            final ActionsManagerDialog parent) {
        showActionsEditorDialog(parent, null);
    }
    
    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parent parent dialog
     * @param action actions to be edited
     */
    public static synchronized void showActionsEditorDialog(
            final ActionsManagerDialog parent, final Action action) {
        if (me != null) {
            me.dispose();
        }
        me = new ActionsEditorDialog(parent, action);
        me.setVisible(true);
        me.requestFocus();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        initButtonsPanel();
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.setPreferredSize(new Dimension(400, 160));
        
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
            if (ConditionEditorDialog.getConditionEditorDialog() != null) {
                ConditionEditorDialog.getConditionEditorDialog().dispose();
            }
            saveSettings();
            this.dispose();
        } else if (event.getSource() == getCancelButton()) {
            if (ConditionEditorDialog.getConditionEditorDialog() != null) {
                ConditionEditorDialog.getConditionEditorDialog().dispose();
            }
            this.dispose();
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
        if (action == null) {
            action = new Action(parent.getSelectedGroup(),
                    ((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName(),
                    ((GeneralTabPanel) tabbedPane.getComponentAt(0)).getTriggers().toArray(new ActionType[0]),
                    ((ResponseTabPanel) tabbedPane.getComponentAt(2)).getResponses().split("\\n"),
                    ((ConditionsTabPanel) tabbedPane.getComponentAt(1)).getConditions(),
                    ((ResponseTabPanel) tabbedPane.getComponentAt(2)).getFormatter());
        } else {
            if (!action.getName().equals(((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName())) {
                action.rename(((GeneralTabPanel) tabbedPane.getComponentAt(0)).getName());
            }
            action.setTriggers(((GeneralTabPanel) tabbedPane.getComponentAt(0)).getTriggers().toArray(new ActionType[0]));
            action.setConditions(((ConditionsTabPanel) tabbedPane.getComponentAt(1)).getConditions());
            action.setResponse(((ResponseTabPanel) tabbedPane.getComponentAt(2)).getResponses().split("\\n"));
            action.setNewFormat(((ResponseTabPanel) tabbedPane.getComponentAt(2)).getFormatter());
        }
        action.save();
        parent.loadGroups();
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
}
