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

package com.dmdirc.ui.swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.interfaces.ActionComparison;
import com.dmdirc.actions.interfaces.ActionComponent;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.ui.swing.components.renderers.ActionCellRenderer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        DocumentListener {

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
        addListeners();
        layoutComponents();

        if (trigger == null) {
            setEnabled(false);
        } else {
            populateArguments();
        }
    }

    /** Initialises the components. */
    private void initComponents() {
        arguments = new JComboBox(new DefaultComboBoxModel());
        arguments.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        components = new JComboBox(new DefaultComboBoxModel());
        components.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        comparisons = new JComboBox(new DefaultComboBoxModel());
        comparisons.putClientProperty("JComboBox.isTableCellEditor",
                Boolean.TRUE);
        target = new JTextField();

        arguments.setRenderer(new ActionCellRenderer());
        components.setRenderer(new ActionCellRenderer());
        comparisons.setRenderer(new ActionCellRenderer());

        components.setEnabled(false);
        comparisons.setEnabled(false);
        target.setEnabled(false);
    }

    /** Populates the arguments combo box. */
    private void populateArguments() {
    }

    /** Populates the components combo box. */
    private void populateComponents() {
    }

    /** Populates the comparisons combo box. */
    private void populateComparisons() {
    }

    /** Populates the target textfield. */
    private void populateTarget() {
    }

    /** Adds the listeners. */
    private void addListeners() {
        arguments.addActionListener(this);
        components.addActionListener(this);
        comparisons.addActionListener(this);
        target.getDocument().addDocumentListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 2"));

        add(new JLabel("Argument:"), "align right");
        add(arguments, "growx");
        add(new JLabel("Component:"), "align right");
        add(components, "growx");
        add(new JLabel("Comparison:"), "align right");
        add(comparisons, "growx");
        add(new JLabel("Target:"), "align right");
        add(target, "growx");
    }

    /** 
     * {@inheritDoc} 
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == arguments && isVisible()) {
            if (arguments.getSelectedItem() == null) {
                condition.setArg(-1);
            } else {
                condition.setArg(arguments.getSelectedIndex());
            }
            populateArguments();
        } else if (e.getSource() == components && isVisible()) {
            if (components.getSelectedItem() == null) {
                condition.setComponent(null);
            } else {
                condition.setComponent((ActionComponent) components.getSelectedItem());
            }
            populateComponents();
        } else if (e.getSource() == comparisons && isVisible()) {
            if (comparisons.getSelectedItem() == null) {
                condition.setComparison(null);
            } else {
                condition.setComparison((ActionComparison) comparisons.getSelectedItem());
            }
            populateComparisons();
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
        components.setEnabled(enabled);
        comparisons.setEnabled(enabled);
        target.setEnabled(enabled);
    }

    /**
     * Sets the action trigger.
     * 
     * @param trigger new trigger
     */
    void setTrigger(final ActionType trigger) {
        this.trigger = trigger;

        setEnabled(trigger != null);
        if (trigger != null) {
            populateArguments();
        }
    }
}
