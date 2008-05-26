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

import com.dmdirc.ui.swing.components.TextLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Action conditions tree panel.
 */
public class ActionConditionsTreePanel extends JPanel implements ActionListener {

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
    private JTextField rule;
    /** Selected rule type identifier. */
    public enum RuleType {
        /** All triggers. */
        ALL,
        /** One trigger. */
        ONE,
        /** Custom rule. */
        CUSTOM;
    };

    /** Instantiates the panel. */
    public ActionConditionsTreePanel() {
        super();
        
        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {        
        group = new ButtonGroup();
        allButton = new JRadioButton("All of the conditions are true");
        oneButton = new JRadioButton("At least one of the conditions is true");
        customButton = new JRadioButton("The conditions match a custom rule");
        
        rule = new JTextField();
        
        allButton.setSelected(true);
        rule.setVisible(false);
        
        group.add(allButton);
        group.add(oneButton);
        group.add(customButton);
    }

    /** Adds the listeners. */
    private void addListeners() {
        allButton.addActionListener(this);
        oneButton.addActionListener(this);
        customButton.addActionListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, pack, hidemode 3, wrap 1, debug"));
        add(new TextLabel("Only execute this action if..."), "growx");
        add(allButton, "growx");
        add(oneButton, "growx");
        add(customButton, "growx");
        add(rule, "growx");
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        rule.setVisible(e.getSource().equals(customButton));
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        allButton.setEnabled(enabled);
        oneButton.setEnabled(enabled);
        customButton.setEnabled(enabled);
        rule.setEnabled(enabled);
    }
    
    /**
     * Returns the selected rule type.
     * 
     * @return Selected rule type
     */
    public RuleType getRuleType() {
        if (allButton.isSelected()) {
            return RuleType.ALL;
        } else if (oneButton.isSelected()) {
            return RuleType.ONE;
        } else {
            return RuleType.CUSTOM;
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
}
