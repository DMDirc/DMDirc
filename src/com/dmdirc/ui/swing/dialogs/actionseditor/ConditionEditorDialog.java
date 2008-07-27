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

import com.dmdirc.ui.swing.components.renderers.ActionCellRenderer;
import com.dmdirc.Main;
import com.dmdirc.actions.interfaces.ActionComparison;
import com.dmdirc.actions.interfaces.ActionComponent;
import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditions editing dialog, used in the actions editor dialog.
 */
public final class ConditionEditorDialog extends StandardDialog implements
        ActionListener, SubstitutionsPanelListener, FocusListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Previously created instance of ConditionEditorDialog. */
    private static volatile ConditionEditorDialog me;
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
    /** Focused main component. */
    private Component focusedComponent;

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
        populateArguments();
    }

    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parent parent conditions panel.
     * @param trigger Conditions trigger
     * @param condition condition to be edited (or null)
     */
    public static void showConditionEditorDialog(
            final ConditionsTabPanel parent, final ActionType trigger,
            final ActionCondition condition) {
        me = getConditionEditorDialog(parent, trigger, condition);

        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
        me.arguments.requestFocus();
    }

    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parent parent conditions panel.
     * @param trigger Conditions trigger
     * @param condition condition to be edited (or null)
     *
     * @return Currently instatiated ConditionEditorDialog (or null if none)
     */
    public static ConditionEditorDialog getConditionEditorDialog(
            final ConditionsTabPanel parent, final ActionType trigger,
            final ActionCondition condition) {
        synchronized (ConditionEditorDialog.class) {
            if (me == null) {
                me = new ConditionEditorDialog(parent, trigger, condition);
            } else if (JOptionPane.showConfirmDialog(parent,
                    "This will discard any changed you have made to existing " +
                    "condition, are you sure you want to edit a new action?",
                    "Discard changes", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                me.dispose();
                me = new ConditionEditorDialog(parent, trigger, condition);
            }
        }

        return me;
    }

    /**
     * Checks if there is an existing condition editor dialog open.
     * 
     * @return true iff the conditions editor dialog is open
     */
    public static boolean isConditionEditorDialogOpen() {
        return me == null;
    }

    /** Closes the existing condition editor dialog. */
    public static void disposeDialog() {
        if (me != null) {
            me.dispose();
        }
    }

    /** Initialises the components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        getOkButton().setEnabled(false);
        glassPane = new JPanel(new BorderLayout());

        substitutionsPanel =
                new SubstitutionsPanel(parent.getOwner().getTrigger());

        arguments = new JComboBox(new DefaultComboBoxModel());
        components = new JComboBox(new DefaultComboBoxModel());
        comparisons = new JComboBox(new DefaultComboBoxModel());
        targetText = new JTextField();

        arguments.setRenderer(new ActionCellRenderer());
        components.setRenderer(new ActionCellRenderer());
        comparisons.setRenderer(new ActionCellRenderer());

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
                    trigger.getType().getArgTypes()[arguments.getSelectedIndex()])) {
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

        if (components.getSelectedItem() != null) {
            for (ActionComparison comp : ActionManager.getCompatibleComparisons(
                    ((ActionComponent) components.getSelectedItem()).getType())) {
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

        pack();
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
        subsButton.addFocusListener(this);
    }

    /** Lays out the components in the dialog. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 2, fill"));

        add(new JLabel("Argument: "));
        add(arguments, "growx");
        add(new JLabel("Component: "));
        add(components, "growx");
        add(new JLabel("Comparison: "));
        add(comparisons, "growx");
        add(new JLabel("Target: "));
        add(targetText, "growx");
        add(subsButton, "span 2, growx");

        add(getLeftButton(), "right");
        add(getRightButton(), "right");

        layoutGlassPane();

        pack();
    }

    /** Lays out the glass pane. */
    private void layoutGlassPane() {
        substitutionsPanel.setPreferredSize(new Dimension(250, 150));

        glassPane.add(substitutionsPanel, BorderLayout.CENTER);
        glassPane.add(cancelSubsButton, BorderLayout.PAGE_END);

        glassPane.setOpaque(true);
        setGlassPane(glassPane);
        glassPane.setVisible(false);
    }

    /** 
     * {@inheritDoc}.
     * 
     * @param event Action event
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == subsButton) {
            if (getFocusOwner() != subsButton) {
                focusedComponent = getFocusOwner();
            }
            glassPane.setVisible(true);
        } else if (event.getSource() == cancelSubsButton) {
            glassPane.setVisible(false);
            focusedComponent.requestFocus();
        } else if (event.getSource() == arguments && isVisible()) {
            if (arguments.getSelectedItem() == null) {
                argument = -1;
            } else {
                argument = arguments.getSelectedIndex();
            }
            populateArguments();
        } else if (event.getSource() == components && isVisible()) {
            if (components.getSelectedItem() == null) {
                component = null;
            } else {
                component = (ActionComponent) components.getSelectedItem();
            }
            populateComponents();
        } else if (event.getSource() == comparisons && isVisible()) {
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
    @Override
    public void substitutionInsert(final ActionSubstitution substitution) {
        glassPane.setVisible(false);
        targetText.replaceSelection(substitution.toString());
        focusedComponent.requestFocus();
    }

    /**
     * Sets the Actiontype of the substitutions panel.
     *
     * @param type ActionType for the panel
     */
    public void setTrigger(final ActionType type) {
        substitutionsPanel.setType(type);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Focus event
     */
    @Override
    public void focusGained(final FocusEvent e) {
        focusedComponent = e.getOppositeComponent();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Focus event
     */
    @Override
    public void focusLost(final FocusEvent e) {
    //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
