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

package uk.org.ownage.dmdirc.ui.dialogs.actionseditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import uk.org.ownage.dmdirc.actions.Action;
import uk.org.ownage.dmdirc.actions.ActionComparison;
import uk.org.ownage.dmdirc.actions.ActionComponent;
import uk.org.ownage.dmdirc.actions.ActionCondition;
import uk.org.ownage.dmdirc.actions.ActionManager;

import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.ColourChooser;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * Action conditions editing dialog, used in the actions editor dialog.
 */
public final class ConditionEditorDialog extends StandardDialog implements
        ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent dialog, informed of changes on close. */
    private ConditionsTabPanel parent;
    /** Parent action. */
    private Action action;
    /** Parent condition. */
    private ActionCondition condition;
    
    /** Buttons panel. */
    private JPanel buttonsPanel;
    /** Parent conditions panel. */
    private JPanel conditionsPanel;
    /** Argument combobox. */
    private JComboBox arguments;
    /** Component combobox. */
    private JComboBox components;
    /** Comparison combobox. */
    private JComboBox comparisons;
    /** Target textfield. */
    private JTextField targetText;
    
    /**
     * Creates a new instance of ConditionEditorDialog.
     *
     * @param parent parent conditions panel.
     * @param action parent action
     * @param condition condition to be edited (or null)
     */
    public ConditionEditorDialog(final ConditionsTabPanel parent,
            final Action action, final ActionCondition condition) {
        super(MainFrame.getMainFrame(), false);
        
        this.parent = parent;
        this.action = action;
        if (condition == null) {
            this.condition = null;
        } else {
            this.condition = new ActionCondition(condition.getArg(),
                    condition.getComponent(), condition.getComparison(),
                    condition.getTarget());
        }
        
        this.setTitle("Condition Editor");
        
        initComponents();
        addListeners();
        layoutComponents();
        
        this.setLocationRelativeTo(MainFrame.getMainFrame());
        
        this.setVisible(true);
    }
    
    /** Initialises the components. */
    private void initComponents() {
        initButtonsPanel();
        conditionsPanel = new JPanel();
        arguments = new JComboBox(new DefaultComboBoxModel());
        components = new JComboBox(new DefaultComboBoxModel());
        comparisons = new JComboBox(new DefaultComboBoxModel());
        targetText = new JTextField();
        
        arguments.setPreferredSize(new Dimension(300, arguments.getFont().getSize()));
        components.setPreferredSize(new Dimension(300, components.getFont().getSize()));
        comparisons.setPreferredSize(new Dimension(300, comparisons.getFont().getSize()));
        targetText.setPreferredSize(new Dimension(300, targetText.getFont().getSize()));
        
        components.setEnabled(false);
        comparisons.setEnabled(false);
        targetText.setEnabled(false);
        
        if (condition == null || action == null) {
            return;
        }
        
        populateArguments();
    }
    
    private void populateArguments() {
        conditionsPanel.setVisible(false);
        
        for (String argument : action.getTriggers()[0].getType().getArgNames()) {
            ((DefaultComboBoxModel) arguments.getModel()).addElement(argument);
        }
        arguments.setSelectedItem(action.getTriggers()[0].getType().getArgNames()[condition.getArg()]);
        components.setEnabled(true);
        
        populateComponents();
    }
    
    private void populateComponents() {
        for (ActionComponent component : ActionManager.getCompatibleComponents(arguments.getSelectedItem().getClass())) {
            ((DefaultComboBoxModel) components.getModel()).addElement(component);
        }
        components.setSelectedItem(condition.getComponent());
        comparisons.setEnabled(true);
        
        populateComparisons();
    }
    
    private void populateComparisons() {
        for (ActionComparison comparison : ActionManager.getCompatibleComparisons(arguments.getSelectedItem().getClass())) {
            ((DefaultComboBoxModel) comparisons.getModel()).addElement(comparison);
        }
        comparisons.setSelectedItem(condition.getComparison());
        targetText.setEnabled(true);
        
        populateTarget();
    }
    
    private void populateTarget() {
        targetText.setText(condition.getTarget());
        
        conditionsPanel.setVisible(true);
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
        layoutConditionsPanel();
        layoutButtonPanel();
        
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
        
        layoutGrid(conditionsPanel, 4, 2, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Lays out the button panel. */
    private void layoutButtonPanel() {
        this.setLayout(new BorderLayout());
        
        this.add(conditionsPanel, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.PAGE_END);
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == arguments) {
            populateArguments();
        } else if (event.getSource() == components) {
            populateArguments();
        } else if (event.getSource() == comparisons) {
            populateArguments();
        }
        if (event.getSource() == getOkButton()) {
            //notify the parent.
            this.dispose();
        } else if (event.getSource() == getCancelButton()) {
            this.dispose();
        }
    }
    
}
