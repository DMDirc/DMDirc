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

package com.dmdirc.ui.swing.dialogs.actionseditor;

import com.dmdirc.ui.IconManager;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.ui.swing.components.ImageButton;
import com.dmdirc.ui.swing.components.JWrappingLabel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Conditions tab panel, conditions editing for the actions editor dialog.
 */
public final class ConditionsTabPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Number of components for each condition in the editor tab. */
    private static final int CONDITION_COMPONENT_COUNT = 3;
    
    /** Offset from the first component for the delete button. */
    private static final int DELETE_BUTTON_OFFSET = 1;
    
    /** Parent ActionsEditorDialog. */
    private final ActionsEditorDialog owner;
    
    /** Comparisons components panel. */
    private JPanel comparisonsPanel;
    
    /** Info blurb. */
    private JWrappingLabel infoLabel;
    /** No conditions label. */
    private JLabel noConditions;
    /** New comparison button. */
    private JButton newComparison;
    
    /** List of conditions. */
    private final List<ActionCondition> conditions;
    
    /**
     * Creates a new instance of ConditionsTabPanel.
     *
     * @param owner Parent dialog
     */
    public ConditionsTabPanel(final ActionsEditorDialog owner) {
        super();
        
        this.owner = owner;
        if (owner.getAction() == null) {
            this.conditions = new ArrayList<ActionCondition>();
        } else {
            this.conditions = new ArrayList<ActionCondition>(owner.getAction().getConditions());
        }
        
        initComponents();
        addListeners();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        comparisonsPanel = new JPanel(new MigLayout());
        noConditions = new JLabel("No conditions set.");
        newComparison = new JButton("New");
        infoLabel = new JWrappingLabel("This action will only be executed if the "
                + "following are true:");
        
        setNewConditionButton(false);
        
        doConditions();
        
        if (owner.getAction() == null) {
            return;
        }
        
        if (owner.getTrigger().getType().getArgNames().length > 0) {
            setNewConditionButton(true);
        }
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
        newComparison.addActionListener(this);
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 1, fill, debug"));
        
        add(infoLabel, "growx");
        add(comparisonsPanel, "grow");
        
        add(newComparison, "right");
    }

    /** Initialises and lays out the conditions panel. */
    public void doConditions() {
        comparisonsPanel.setVisible(false);
        
        initConditions();
        
        comparisonsPanel.setVisible(true);
    }
    
    /** Initialises the conditions panel. */
    private void initConditions() {
        
        comparisonsPanel.removeAll();
        
        for (ActionCondition condition : conditions) {
            final JLabel label = new JLabel("The "
                    + owner.getTrigger().getType().getArgNames()[condition.getArg()]
                    + "'s " + condition.getComponent().getName() + " "
                    + condition.getComparison().getName()
                    + " '" + condition.getTarget() + "'");
            final ImageButton edit = new ImageButton("edit",
                    IconManager.getIconManager().getIcon("edit-inactive"),
                    IconManager.getIconManager().getIcon("edit"));
            final ImageButton delete = new ImageButton("delete",
                    IconManager.getIconManager().getIcon("close-inactive"),
                    IconManager.getIconManager().getIcon("close-active"));
            
            edit.addActionListener(this);
            
            delete.addActionListener(this);
            
            label.setPreferredSize(new Dimension(Integer.MAX_VALUE, 
                    label.getFont().getSize()));
            
            comparisonsPanel.add(edit);
            comparisonsPanel.add(delete);
            comparisonsPanel.add(label, "growx");
        }
        
        if (comparisonsPanel.getComponentCount() == 0) {
            comparisonsPanel.add(noConditions);
        }
    }
    
    /**
     * Adds a new condition to the panel.
     *
     * @param newCondition Condition to add
     */
    public void addCondition(final ActionCondition newCondition) {
        conditions.add(newCondition);
        
        doConditions();
    }
    
    /**
     * Removes a condition from the panel.
     *
     * @param index Index of the condition to remove
     */
    public void delCondition(final int index) {
        conditions.remove(index);
        
        doConditions();
    }
    
    /** Clears all conditions. */
    public void clearConditions() {
        conditions.clear();
        
        doConditions();
    }
    
    /**
     * Sets the state of the new condition button.
     *
     * @param state State of the button
     */
    public void setNewConditionButton(final boolean state) {
        newComparison.setEnabled(state);
    }
    
    
    /**
     * Returns the conditions for this panel.
     *
     * @return Conditions list
     */
    public List<ActionCondition> getConditions() {
        return conditions;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param event Action event
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == newComparison) {
            ConditionEditorDialog.showConditionEditorDialog(this, owner.getTrigger(), null);
        } else {
            if ("edit".equals(event.getActionCommand())) {
                ConditionEditorDialog.showConditionEditorDialog(this, owner.getTrigger(),
                        conditions.get((Arrays.asList(
                        comparisonsPanel.getComponents()).indexOf(
                        event.getSource())) / CONDITION_COMPONENT_COUNT));
            } else if ("delete".equals(event.getActionCommand())) {
                delCondition((Arrays.asList(
                        comparisonsPanel.getComponents()).indexOf(
                        event.getSource()) - DELETE_BUTTON_OFFSET) 
                        / CONDITION_COMPONENT_COUNT);
            }
        }
    }

    /**
     * Returns this panels parent ActionsEditorDialog.
     *
     * @return ActionsEditorDialog
     */
    public ActionsEditorDialog getOwner() {
        return owner;
    }
}
