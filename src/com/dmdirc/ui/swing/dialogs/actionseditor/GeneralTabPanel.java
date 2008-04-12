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

import com.dmdirc.ui.swing.components.renderers.ActionTypeRenderer;
import com.dmdirc.ui.swing.components.renderers.ActionCellRenderer;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.ui.swing.actions.SanitisedFilenamePasteAction;
import com.dmdirc.ui.swing.components.SanitisedFilenameFilter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

import net.miginfocom.swing.MigLayout;

/**
 * General tab panel, name and trigger editing for the actions editor dialog.
 */
public final class GeneralTabPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Parent ActionsEditorDialog. */
    private final ActionsEditorDialog owner;
    
    /** Name textfield. */
    private JTextField name;
    /** Primary trigger combobox. */
    private JComboBox trigger;
    /** Secondary trigger list. */
    private JList otherTriggers;
    
    /** Currently selected trigger. */
    private ActionType type;
    
    /**
     * Creates a new instance of GeneralTabPanel.
     *
     * @param owner Parent dialog
     */
    public GeneralTabPanel(final ActionsEditorDialog owner) {
        super();
        
        this.owner = owner;
        
        initComponents();
        addListeners();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        name = new JTextField();
        trigger = new JComboBox(new ActionTypeModel(ActionManager.getTypeGroups()));
        //Only fire events on selection not on highlight
        trigger.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        otherTriggers = new JList(new DefaultListModel());
        
        trigger.setRenderer(new ActionTypeRenderer());
        otherTriggers.setCellRenderer(new ActionCellRenderer());
        
        name.setPreferredSize(new Dimension(100, name.getFont().getSize()));
        
        trigger.setPreferredSize(new Dimension(100, trigger.getFont().getSize()));
        
        otherTriggers.setVisibleRowCount(7);
        otherTriggers.setEnabled(false);
        
        ((AbstractDocument) name.getDocument()).setDocumentFilter(new SanitisedFilenameFilter());
        name.getActionMap().put("paste-from-clipboard", new SanitisedFilenamePasteAction());
        
        if (owner.getAction() == null) {
            return;
        }
        
        name.setText(owner.getAction().getName());
        
        trigger.setSelectedItem(owner.getAction().getTriggers()[0]);
        type = (ActionType) trigger.getSelectedItem();
        otherTriggers.setEnabled(true);
        
        populateOtherTriggers();
        
        selectOtherTriggers();
    }
    
    /** Populates the other triggers list with compatible types. */
    private void populateOtherTriggers() {
        ((DefaultListModel) otherTriggers.getModel()).clear();
        if (trigger.getSelectedItem() == null) {
            otherTriggers.setEnabled(false);
        } else {
            for (ActionType thisType
                    : ActionManager.getCompatibleTypes((ActionType) trigger.getSelectedItem())) {
                        ((DefaultListModel) otherTriggers.getModel()).addElement(thisType);
                    }
            owner.setType((ActionType) trigger.getSelectedItem());
            otherTriggers.setEnabled(true);
        }
        otherTriggers.repaint();
    }
    
    /** Selects other triggers that are part of this action. */
    private void selectOtherTriggers() {
        if (owner.getAction() != null && owner.getAction().getTriggers() != null) {
            for (ActionType thisType : owner.getAction().getTriggers()) {
                final int index = ((DefaultListModel) otherTriggers.getModel()).indexOf(thisType);
                if (index != -1) {
                    otherTriggers.getSelectionModel().addSelectionInterval(index, index);
                }
                
            }
        }
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
        trigger.addActionListener(this);
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 2, fill"));
        add(new JLabel("Name: "));
        add(name, "growx");
        add(new JLabel("Primary trigger: "));
        add(trigger, "growx");
        add(new JLabel("Additional triggers: "));
        add(new JScrollPane(otherTriggers), "growx");
    }
    
    /**
     * Returns the name for this panel.
     *
     * @return Action name
     */
    public String getActionName() {
        return name.getText();
    }
    
    /**
     * Returns the primary trigger for this panel.
     *
     * @return Primary trigger
     */
    public ActionType getTrigger() {
        if (trigger.getSelectedIndex() == 0) {
            return null;
        }
        return (ActionType) trigger.getSelectedItem();
    }
    
    /**
     * Returns the triggers for this panel.
     *
     * @return Trigger list
     */
    public List<ActionType> getTriggers() {
        final List<ActionType> triggers = new ArrayList<ActionType>();
        
        if (trigger.getSelectedIndex() == 0) {
            return null;
        }
        triggers.add((ActionType) trigger.getSelectedItem());
        
        for (Object thisType : otherTriggers.getSelectedValues()) {
            triggers.add((ActionType) thisType);
        }
        
        return triggers;
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param event Action event
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == trigger) {
            SwingUtilities.invokeLater(new Runnable() {
                
                /** {@inheritDoc} */
                @Override
                public void run() {
                    handleTriggerChange();
                }
            });
        }
    }
    
    /** Prompts the user for confirmation of type change. */
    private void handleTriggerChange() {
        final boolean compatible = checkTriggerCompatibility();
        
        handleInvalidTrigger();
        
        if (compatible) {
            type = (ActionType) trigger.getSelectedItem();
        } else if (owner.getConditionCount() > 0) {
            final int response = JOptionPane.showConfirmDialog(this,
                    "Changing to this trigger will remove your existing "
                    + "conditions. Are you sure?", "Incompatible triggers",
                    JOptionPane.OK_CANCEL_OPTION);
            if (response == JOptionPane.OK_OPTION) {
                type = (ActionType) trigger.getSelectedItem();
                owner.clearConditions();
            } else {
                trigger.setSelectedItem(type);
            }
        }
        populateOtherTriggers();
    }
    
    /**
     * Checks for an invalid selection in the trigger.
     */
    private void handleInvalidTrigger() {
        if (trigger.getSelectedIndex() == 0) {
            owner.getOkButton().setEnabled(false);
            owner.setNewConditionButtonState(false);
        } else {
            owner.getOkButton().setEnabled(true);
            if (owner.getTrigger() != null && 
                    owner.getTrigger().getType().getArgNames().length > 0) {
                owner.setNewConditionButtonState(true);
            }
        }
    }
    
    /**
     * Checks trigger compatibility.
     *
     * @return Whether triggers are compatible
     */
    private boolean checkTriggerCompatibility() {
        boolean compatible = false;
        
        if (type == null || trigger.getSelectedIndex() == 0) {
            compatible = false;
        } else if (ActionManager.getCompatibleTypes(type).contains(trigger.getSelectedItem())
        || trigger.getSelectedItem() == type) {
            compatible = true;
        }
        
        return compatible;
    }
    
    /** Requests focus on the name component. */
    public void requestNameFocus() {
        name.requestFocus();
    }
    
    /** Requests focus on the trigger component. */
    public void requestTriggerFocus() {
        trigger.requestFocus();
    }
}
