/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionComparison;
import com.dmdirc.actions.interfaces.ActionComponent;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.ui_swing.components.renderers.ActionCellRenderer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditioneditor panel.
 */
public class ActionConditionEditorPanel extends JPanel implements ActionListener,
        DocumentListener, PropertyChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Condition. */
    private ActionCondition condition;
    /** Trigger. */
    private ActionType trigger;
    /** Argument. */
    private JComboBox arguments;
    /** Component. */
    private JComboBox components;
    /** Comparison. */
    private JComboBox comparisons;
    /** Target. */
    private JTextField target;

    /** 
     * Instantiates the panel.
     * 
     * @param condition Action condition
     * @param trigger Action trigger
     */
    public ActionConditionEditorPanel(final ActionCondition condition,
            final ActionType trigger) {
        super();

        this.condition = condition;
        this.trigger = trigger;

        initComponents();

        if (trigger == null) {
            setEnabled(false);
        } else {
            populateArguments();
            populateComponents();
            populateComparisons();
            populateTarget();
        }

        firePropertyChange("edit", null, null);

        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        arguments = new JComboBox(new DefaultComboBoxModel());
        arguments.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        arguments.setName("argument");
        components = new JComboBox(new DefaultComboBoxModel());
        components.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        components.setName("component");
        comparisons = new JComboBox(new DefaultComboBoxModel());
        comparisons.putClientProperty("JComboBox.isTableCellEditor",
                Boolean.TRUE);
        comparisons.setName("comparison");

        target = new JTextField() {

            /** Serial version UID. */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void setEnabled(final boolean enabled) {
                firePropertyChange("validationResult", target.isEnabled(),
                        enabled);
                super.setEnabled(enabled);
            }
        };

        arguments.setRenderer(new ActionCellRenderer());
        components.setRenderer(new ActionCellRenderer());
        comparisons.setRenderer(new ActionCellRenderer());

        components.setEnabled(false);
        comparisons.setEnabled(false);
        target.setEnabled(false);
    }

    /** Populates the arguments combo box. */
    private void populateArguments() {
        ((DefaultComboBoxModel) arguments.getModel()).removeAllElements();

        for (String arg : trigger.getType().getArgNames()) {
            ((DefaultComboBoxModel) arguments.getModel()).addElement(arg);
        }
        arguments.setSelectedIndex(condition.getArg());
    }

    /** Populates the components combo box. */
    private void populateComponents() {
        ((DefaultComboBoxModel) components.getModel()).removeAllElements();

        if (condition.getArg() != -1) {
            for (ActionComponent comp : ActionManager.getCompatibleComponents(
                    trigger.getType().getArgTypes()[condition.getArg()])) {
                ((DefaultComboBoxModel) components.getModel()).addElement(comp);
            }
        }
        components.setSelectedItem(condition.getComponent());
    }

    /** Populates the comparisons combo box. */
    private void populateComparisons() {
        ((DefaultComboBoxModel) comparisons.getModel()).removeAllElements();

        if (condition.getComponent() != null) {
            for (ActionComparison comp : ActionManager.getCompatibleComparisons(
                    condition.getComponent().getType())) {
                ((DefaultComboBoxModel) comparisons.getModel()).addElement(comp);
            }
        }
        comparisons.setSelectedItem(condition.getComparison());
    }

    /** Populates the target textfield. */
    private void populateTarget() {
        target.setText(condition.getTarget());
    }

    /** Handles the argument changing. */
    private void handleArgumentChange() {
        condition.setArg(arguments.getSelectedIndex());
        populateComponents();
        components.setEnabled(true);
        components.setSelectedItem(null);
        comparisons.setSelectedItem(null);
        comparisons.setEnabled(false);
        target.setText(null);
        target.setEnabled(false);
    }

    /** Handles the component changing. */
    private void handleComponentChange() {
        condition.setComponent((ActionComponent) components.getSelectedItem());
        populateComparisons();
        comparisons.setEnabled(true);
        comparisons.setSelectedItem(null);
        target.setText(null);
        target.setEnabled(false);
    }

    /** Handles the comparison changing. */
    private void handleComparisonChange() {
        condition.setComparison((ActionComparison) comparisons.getSelectedItem());
        populateTarget();
        target.setEnabled(true);
    }

    /** Adds the listeners. */
    private void addListeners() {
        arguments.addActionListener(this);
        components.addActionListener(this);
        comparisons.addActionListener(this);
        target.getDocument().addDocumentListener(this);
        target.addPropertyChangeListener("validationResult", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 2, pack"));

        add(new JLabel("Argument:"), "align right");
        add(arguments, "growx, pushx");
        add(new JLabel("Component:"), "align right");
        add(components, "growx, pushx");
        add(new JLabel("Comparison:"), "align right");
        add(comparisons, "growx, pushx");
        add(new JLabel("Target:"), "align right");
        add(target, "growx, pushx");
    }

    /** 
     * {@inheritDoc} 
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == arguments) {
            handleArgumentChange();
        } else if (e.getSource() == components) {
            handleComponentChange();
        } else if (e.getSource() == comparisons) {
            handleComparisonChange();
        }
        firePropertyChange("edit", null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        synchronized (condition) {
            condition.setTarget(target.getText());
        }
        firePropertyChange("edit", null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        synchronized (condition) {
            condition.setTarget(target.getText());
        }
        firePropertyChange("edit", null, null);
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(final DocumentEvent e) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        arguments.setEnabled(enabled);
        if (enabled) {
            components.setEnabled(arguments.getSelectedIndex() != -1);
            comparisons.setEnabled(components.getSelectedIndex() != -1);
            target.setEnabled(comparisons.getSelectedIndex() != -1);
        } else {
            components.setEnabled(false);
            comparisons.setEnabled(false);
            target.setEnabled(false);
        }
    }

    /**
     * Sets the action trigger.
     * 
     * @param trigger new trigger
     */
    void setTrigger(final ActionType trigger) {
        this.trigger = trigger;

        setEnabled(trigger != null);
        if (trigger != null && !trigger.equals(this.trigger)) {
            populateArguments();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange("validationResult", evt.getOldValue(),
                evt.getNewValue());
    }

    /**
     * Checks if this editor panel has errored.
     * 
     * @return true iif the content it valid
     */
    public boolean checkError() {
        return target.isEnabled();
    }
}
