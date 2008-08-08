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

import com.dmdirc.config.prefs.validator.ConditionRuleValidator;
import com.dmdirc.actions.ConditionTree;
import com.dmdirc.actions.ConditionTreeFactory;
import com.dmdirc.actions.ConditionTreeFactory.ConditionTreeFactoryType;
import com.dmdirc.ui.swing.components.TextLabel;
import com.dmdirc.ui.swing.components.validating.ValidatingJTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditions tree panel.
 */
public class ActionConditionsTreePanel extends JPanel implements ActionListener, PropertyChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Button group. */
    private ButtonGroup group;
    /** All triggers button. */
    private JRadioButton allButton;
    /** One trigger button. */
    private JRadioButton oneButton;
    /** Custom rule button. */
    private JRadioButton customButton;
    /** Custom rule field. */
    private ValidatingJTextField rule;
    /** Condition tree factory. */
    private ConditionTreeFactory treeFactory;
    /** Condition count. */
    private int conditionCount;

    /** Instantiates the panel. */
    public ActionConditionsTreePanel() {
        super();

        initComponents();
        addListeners();
        layoutComponents();

        selectTreeButton();
    }

    /** Initialises the components. */
    private void initComponents() {
        group = new ButtonGroup();
        allButton = new JRadioButton("All of the conditions are true");
        oneButton = new JRadioButton("At least one of the conditions is true");
        customButton = new JRadioButton("The conditions match a custom rule");

        rule = new ValidatingJTextField(new ConditionRuleValidator());

        group.add(allButton);
        group.add(oneButton);
        group.add(customButton);
    }

    /** Adds the listeners. */
    private void addListeners() {
        allButton.addActionListener(this);
        oneButton.addActionListener(this);
        customButton.addActionListener(this);
        rule.addPropertyChangeListener("validationResult", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, pack, hidemode 3"));
        add(new TextLabel("Only execute this action if..."), "growx");
        add(allButton, "growx");
        add(oneButton, "growx");
        add(customButton, "growx");
        add(rule, "growx");
    }

    /**
     * Selects the appropriate radio button for the tree.
     */
    private void selectTreeButton() {
        group.clearSelection();
        final ConditionTreeFactoryType type;
        if (treeFactory == null) {
            type = ConditionTreeFactoryType.CONJUNCTION;
        } else {
            type = treeFactory.getType();
        }
        
        switch (type) {
            case DISJUNCTION:
                oneButton.setSelected(true);
                rule.setText("");
                rule.setEnabled(false);
                firePropertyChange("validationResult", true, true);
                break;
            case CUSTOM:
                customButton.setSelected(true);
                rule.setText(treeFactory.getConditionTree(conditionCount).toString());
                rule.setEnabled(true);
                rule.checkError();
                break;
            default:
                allButton.setSelected(true);
                rule.setText("");
                rule.setEnabled(false);
                firePropertyChange("validationResult", true, true);
                break;
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        rule.setEnabled(e.getSource().equals(customButton));
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        allButton.setEnabled(enabled);
        oneButton.setEnabled(enabled);
        customButton.setEnabled(enabled);
    }

    /**
     * Returns the selected rule type.
     * 
     * @param conditionCount Condition count
     * 
     * @return Selected rule type
     */
    public ConditionTreeFactoryType getRuleType(final int conditionCount) {
        if (conditionCount >= 2) {
            final ConditionTree tree =
                    ConditionTree.parseString(rule.getText());
            treeFactory = ConditionTreeFactory.getFactory(tree, conditionCount);
            return treeFactory.getType();
        } else {
            return ConditionTreeFactoryType.CONJUNCTION;
        }
    }

    /**
     * Returns the current custom rule.
     * 
     * @return Custom rule
     */
    public String getRule() {
        return rule.getText();
    }

    /**
     * Sets the tree rule.
     * 
     * @param conditionCount condition count
     * @param tree new condition tree
     */
    public void setRule(final int conditionCount, final ConditionTree tree) {
        if (tree != null && conditionCount >= 2) {
            this.conditionCount = conditionCount;
            treeFactory = ConditionTreeFactory.getFactory(tree, conditionCount);
            selectTreeButton();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        firePropertyChange("validationResult", evt.getOldValue(), evt.getNewValue());
    }
    
    /** Validates the conditions. */
    public void validateConditions() {
        selectTreeButton();
    }
}
