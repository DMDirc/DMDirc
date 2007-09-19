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
import com.dmdirc.actions.ActionComparison;
import com.dmdirc.actions.ActionComponent;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionType;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * Action conditions editing dialog, used in the actions editor dialog.
 */
public final class ConditionEditorDialog extends StandardDialog implements
        ActionListener, SubstitutionsPanelListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Previously created instance of ConditionEditorDialog. */
    private static ConditionEditorDialog me;
    
    /** Parent dialog, informed of changes on close. */
    private final ConditionsTabPanel parent;
    /** Parent action type trigger. */
    private final ActionType trigger;
    /** conditions to be edited, or null if new. */
    private final transient ActionCondition condition;
    /** Condition argument. */
    private int argument;
    /** Condition component. */
    private ActionComponent component;
    /** Condition comparison. */
    private ActionComparison comparison;
    /** Condition target. */
    private String target;
    
    /** Buttons panel. */
    private JPanel buttonsPanel;
    /** Parent conditions panel. */
    private JPanel conditionsPanel;
    /** Glass pane. */
    private JPanel glassPane;
    /** Substitutions panel. */
    private SubstitutionsPanel substitutionsPanel;
    /** Argument combobox. */
    private JComboBox arguments;
    /** Component combobox. */
    private JComboBox components;
    /** Comparison combobox. */
    private JComboBox comparisons;
    /** Target textfield. */
    private JTextField targetText;
    /** show substitutions button. */
    private JButton subsButton;
    /** cancel substitutions button. */
    private JButton cancelSubsButton;
    
    /**
     * Creates a new instance of ConditionEditorDialog.
     *
     * @param parent parent conditions panel.
     * @param trigger Conditions trigger
     * @param condition condition to be edited (or null)
     */
    private ConditionEditorDialog(final ConditionsTabPanel parent,
            final ActionType trigger, final ActionCondition condition) {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        
        this.trigger = trigger;
        this.parent = parent;
        this.condition = condition;
        if (condition == null) {
            this.argument = -1;
            this.component = null;
            this.comparison = null;
            this.target = null;
        } else {
            this.argument = condition.getArg();
            this.component = condition.getComponent();
            this.comparison = condition.getComparison();
            this.target = condition.getTarget();
        }
        
        setTitle("Condition Editor");
        
        setResizable(false);
        
        initComponents();
        addListeners();
        layoutComponents();
        
        //setSize(new Dimension(770, 300));
        
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        
        setVisible(true);
    }
    
    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parent parent conditions panel.
     * @param trigger Conditions trigger
     * @param condition condition to be edited (or null)
     */
    public static synchronized void showConditionEditorDialog(
            final ConditionsTabPanel parent, final ActionType trigger,
            final ActionCondition condition) {
        me = new ConditionEditorDialog(parent, trigger, condition);
        me.populateArguments();
        me.setVisible(true);
        me.requestFocus();
    }
    
    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @return Currently instatiated ConditionEditorDialog (or null if none)
     */
    public static synchronized ConditionEditorDialog getConditionEditorDialog() {
        return me;
    }
    
    /** Initialises the components. */
    private void initComponents() {
        initButtonsPanel();
        glassPane = new JPanel(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        substitutionsPanel = new SubstitutionsPanel(parent.getOwner().getTrigger());
        
        conditionsPanel = new JPanel();
        arguments = new JComboBox(new DefaultComboBoxModel());
        components = new JComboBox(new DefaultComboBoxModel());
        comparisons = new JComboBox(new DefaultComboBoxModel());
        targetText = new JTextField();
        
        arguments.setRenderer(new ActionCellRenderer());
        components.setRenderer(new ActionCellRenderer());
        comparisons.setRenderer(new ActionCellRenderer());
        
        arguments.setPreferredSize(new Dimension(300, arguments.getFont().getSize()));
        components.setPreferredSize(new Dimension(300, components.getFont().getSize()));
        comparisons.setPreferredSize(new Dimension(300, comparisons.getFont().getSize()));
        targetText.setPreferredSize(new Dimension(300, targetText.getFont().getSize()));
        
        subsButton = new JButton("Show substitutions");
        cancelSubsButton = new JButton("Back");
        
        components.setEnabled(false);
        comparisons.setEnabled(false);
        targetText.setEnabled(false);
        subsButton.setEnabled(false);
        
        populateArguments();
    }
    
    /** Populates the arguments combo box. */
    private void populateArguments() {
        conditionsPanel.setVisible(false);
        
        ((DefaultComboBoxModel) arguments.getModel()).removeAllElements();
        
        for (String arg : trigger.getType().getArgNames()) {
            ((DefaultComboBoxModel) arguments.getModel()).addElement(arg);
        }
        
        if (argument == -1) {
            arguments.setSelectedIndex(-1);
            components.setEnabled(false);
            component = null;
            comparison = null;
            target = null;
            getOkButton().setEnabled(false);
        } else {
            arguments.setSelectedIndex(argument);
            components.setEnabled(true);
            components.setSelectedIndex(-1);
        }
        
        populateComponents();
    }
    
    /** Populates the components combo box. */
    private void populateComponents() {
        ((DefaultComboBoxModel) components.getModel()).removeAllElements();
        
        if (arguments.getSelectedItem() != null) {
            for (ActionComponent comp : ActionManager.getCompatibleComponents(
                    trigger.getType().getArgTypes()[arguments.getSelectedIndex()]
                    )) {
                ((DefaultComboBoxModel) components.getModel()).addElement(comp);
            }
        }
        
        if (component == null) {
            components.setSelectedIndex(-1);
            comparisons.setEnabled(false);
            comparison = null;
            target = null;
            getOkButton().setEnabled(false);
        } else {
            components.setSelectedItem(component);
            comparisons.setEnabled(true);
        }
        
        populateComparisons();
    }
    
    /** Populates the comparisons combo box. */
    private void populateComparisons() {
        ((DefaultComboBoxModel) comparisons.getModel()).removeAllElements();
        
        if (arguments.getSelectedItem() != null) {
            for (ActionComparison comp : ActionManager.getCompatibleComparisons(
                    arguments.getSelectedItem().getClass())) {
                ((DefaultComboBoxModel) comparisons.getModel()).addElement(comp);
            }
        }
        
        if (comparison == null) {
            comparisons.setSelectedIndex(-1);
            targetText.setEnabled(false);
            subsButton.setEnabled(false);
            target = null;
            getOkButton().setEnabled(false);
        } else {
            comparisons.setSelectedItem(comparison);
            targetText.setEnabled(true);
            subsButton.setEnabled(true);
            getOkButton().setEnabled(true);
        }
        
        populateTarget();
    }
    
    /** Populates the target textfield. */
    private void populateTarget() {
        targetText.setText(target);
        
        conditionsPanel.setVisible(true);
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {
        orderButtons(new JButton(), new JButton());
        
        getOkButton().setEnabled(false);
        
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
        
        arguments.addActionListener(this);
        components.addActionListener(this);
        comparisons.addActionListener(this);
        substitutionsPanel.addSubstitutionsPanelListener(this);
        subsButton.addActionListener(this);
        cancelSubsButton.addActionListener(this);
    }
    
    /** Lays out the components in the dialog. */
    private void layoutComponents() {
        layoutConditionsPanel();
        layoutButtonPanel();
        layoutGlassPane();
        
        pack();
    }
    
    /** Lays out the conditions panel. */
    private void layoutConditionsPanel() {
        conditionsPanel.setLayout(new SpringLayout());
        
        conditionsPanel.add(new JLabel("Argument: "));
        conditionsPanel.add(arguments);
        conditionsPanel.add(new JLabel("Component: "));
        conditionsPanel.add(components);
        conditionsPanel.add(new JLabel("Comparison: "));
        conditionsPanel.add(comparisons);
        conditionsPanel.add(new JLabel("Target: "));
        conditionsPanel.add(targetText);
        conditionsPanel.add(Box.createGlue());
        conditionsPanel.add(subsButton);
        
        layoutGrid(conditionsPanel, 5, 2, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Lays out the button panel. */
    private void layoutButtonPanel() {
        setLayout(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        
        add(conditionsPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.PAGE_END);
    }
    
    /** Lays out the glass pane. */
    private void layoutGlassPane() {
        glassPane.setBorder(BorderFactory.createEmptyBorder(
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        substitutionsPanel.setPreferredSize(new Dimension(250, 150));
        
        glassPane.add(substitutionsPanel, BorderLayout.CENTER);
        glassPane.add(cancelSubsButton, BorderLayout.PAGE_END);
        
        glassPane.setOpaque(true);
        setGlassPane(glassPane);
        glassPane.setVisible(false);
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == subsButton) {
            glassPane.setVisible(true);
        } else if (event.getSource() == cancelSubsButton) {
            glassPane.setVisible(false);
        } else if (event.getSource() == arguments && conditionsPanel.isVisible()) {
            if (arguments.getSelectedItem() == null) {
                argument = -1;
            } else {
                argument = arguments.getSelectedIndex();
            }
            populateArguments();
        } else if (event.getSource() == components && conditionsPanel.isVisible()) {
            if (components.getSelectedItem() == null) {
                component = null;
            } else {
                component = (ActionComponent) components.getSelectedItem();
            }
            populateComponents();
        } else if (event.getSource() == comparisons && conditionsPanel.isVisible()) {
            if (comparisons.getSelectedItem() == null) {
                comparison = null;
            } else {
                comparison = (ActionComparison) comparisons.getSelectedItem();
            }
            populateComparisons();
        }
        if (event.getSource() == getOkButton()) {
            if (condition == null) {
                parent.addCondition(new ActionCondition(argument, component,
                        comparison, targetText.getText()));
            } else {
                condition.setArg(argument);
                condition.setComponent(component);
                condition.setComparison(comparison);
                condition.setTarget(targetText.getText());
                parent.doConditions();
            }
            dispose();
        } else if (event.getSource() == getCancelButton()) {
            dispose();
        }
    }
    
    /** {@inheritDoc} */
    public void substitutionInsert(final ActionSubstitution substitution) {
        glassPane.setVisible(false);
        targetText.replaceSelection(substitution.toString());
    }
    
    /**
     * Sets the Actiontype of the substitutions panel.
     *
     * @param type ActionType for the panel
     */
    public void setTrigger(final ActionType type) {
        substitutionsPanel.setType(type);
    }
    
}
