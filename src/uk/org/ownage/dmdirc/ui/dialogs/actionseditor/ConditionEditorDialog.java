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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.border.EmptyBorder;

import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.ColourChooser;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * Action conditions editing dialog, used in the actions editor dialog.
 */
public class ConditionEditorDialog extends StandardDialog implements
        ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent dialog, informed of changes on close. */
    private ConditionsTabPanel parent;
    /** Buttons panel. */
    private JPanel buttonsPanel;
    /** conditions panel. */
    private JPanel conditionsPanel;
    /** Argument combobox. */
    private JComboBox argument;
    /** Component combobox. */
    private JComboBox component;
    /** Comparison combobox. */
    private JComboBox comparison;
    /** Target textfield. */
    private JTextField targetText;
    /** Target colour chooser. */
    private ColourChooser targetColour;
    /** Target spinner. */
    private JSpinner targetSpinner;
    /** Current target. */
    private JComponent currentTarget;
    
    /** Creates a new instance of ConditionEditorDialog. */
    public ConditionEditorDialog(final ConditionsTabPanel parent) {
        super(MainFrame.getMainFrame(), false);
        
        this.parent = parent;

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
        argument = new JComboBox(new DefaultComboBoxModel());
        component = new JComboBox(new DefaultComboBoxModel());
        comparison = new JComboBox(new DefaultComboBoxModel());
        targetText = new JTextField();
        targetColour = new ColourChooser("", true, true);
        targetSpinner = new JSpinner(new SpinnerNumberModel());
        
        argument.setPreferredSize(new Dimension(300, argument.getFont().getSize()));
        component.setPreferredSize(new Dimension(300, component.getFont().getSize()));
        comparison.setPreferredSize(new Dimension(300, comparison.getFont().getSize()));
        targetText.setPreferredSize(new Dimension(300, targetText.getFont().getSize()));
        
        currentTarget = targetText;
        
        component.setEnabled(false);
        comparison.setEnabled(false);
        currentTarget.setEnabled(false);
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {
        orderButtons(new JButton(), new JButton());
        
        buttonsPanel = new JPanel();
        
        buttonsPanel.setBorder(new EmptyBorder(0, SMALL_BORDER,
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
    
    private void layoutConditionsPanel() {
        conditionsPanel.setLayout(new SpringLayout());
        
        conditionsPanel.add(new JLabel("Argument: "));
        conditionsPanel.add(argument);
        conditionsPanel.add(new JLabel("Component: "));
        conditionsPanel.add(component);
        conditionsPanel.add(new JLabel("Comparison: "));
        conditionsPanel.add(comparison);
        conditionsPanel.add(new JLabel("Target: "));
        conditionsPanel.add(currentTarget);
        
        layoutGrid(conditionsPanel, 4, 2, SMALL_BORDER, SMALL_BORDER, 
                SMALL_BORDER, SMALL_BORDER);
    }
    
    private void layoutButtonPanel() {
        this.setLayout(new BorderLayout());
        
        this.add(conditionsPanel, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.PAGE_END);        
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == argument) {
            component.setEnabled(false);
        } else if (e.getSource() == component) {
            comparison.setEnabled(true);
        } else if (e.getSource() == comparison) {
            //switch current target and relay out
            layoutConditionsPanel();            
            currentTarget.setEnabled(true);
        }
        if (e.getSource() == getOkButton()) {
            //notify the parent.
            this.dispose();
        } else if (e.getSource() == getCancelButton()) {
            this.dispose();
        }
    }
    
}
