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

package com.dmdirc.ui.components.expandingsettings;

import com.dmdirc.ui.components.ColourChooser;
import com.dmdirc.ui.components.expandingsettings.SettingsPanel.OptionType;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.UIUtilities.layoutGrid;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

/**
 * Add option panel.
 */
public final class AddOptionPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent settings panel. */
    private final SettingsPanel parent;
    
    /** Add options combobox. */
    private JComboBox addOptionComboBox;
    /** Add option button. */
    private JButton addOptionButton;
    /** Current add option input. */
    private Component addInputCurrent;
    /** Add option colour chooser. */
    private ColourChooser addInputColourChooser;
    /** Add option textfield. */
    private JTextField addInputText;
    /** Add option checkbox. */
    private JCheckBox addInputCheckbox;
    /** Add option checkbox. */
    private JSpinner addInputSpinner;
    /** Add option checkbox. */
    private JLabel addInputNone;
    
    /**
     * Creates a new instance of AddOptionPanel.
     *
     * @param parent Parent settings panel.
     */
    protected AddOptionPanel(final SettingsPanel parent) {
        super();
        
        this.parent = parent;
        
        initComponents();
        initListeners();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        addOptionComboBox = new JComboBox(new DefaultComboBoxModel());
        addOptionButton = new JButton("Add");
        
        addOptionComboBox.setRenderer(new AddOptionCellRenderer(parent));
        
        addInputColourChooser = new ColourChooser();
        addInputText = new JTextField();
        addInputCheckbox = new JCheckBox();
        addInputSpinner = new JSpinner(new SpinnerNumberModel());
        addInputNone = new JLabel("");
        
        addOptionComboBox.setPreferredSize(new Dimension(150,
                addOptionButton.getFont().getSize()));
        addOptionButton.setPreferredSize(new Dimension(100,
                addOptionButton.getFont().getSize()));
        
        addInputColourChooser.setPreferredSize(new Dimension(150, 0));
        addInputText.setPreferredSize(new Dimension(150, 0));
        addInputCheckbox.setPreferredSize(new Dimension(150, 0));
        addInputSpinner.setPreferredSize(new Dimension(150, 0));
        addInputNone.setPreferredSize(new Dimension(150, 0));
        
        addInputCurrent = addInputNone;
        
        addOptionComboBox.setEnabled(false);
        addOptionButton.setEnabled(false);
    }
    
    /** Initialises listeners. */
    private void initListeners() {
        addOptionComboBox.addActionListener(this);
        addOptionButton.addActionListener(this);
    }
    
    /** Lays out the components. */
    private void layoutComponents() {
        this.setVisible(false);
        
        setLayout(new SpringLayout());
        
        removeAll();
        
        add(addOptionComboBox);
        add(addInputCurrent);
        add(addOptionButton);
        
        layoutGrid(this, 1, 3, 0, 0, SMALL_BORDER, SMALL_BORDER);
        
        this.setVisible(true);
    }
    
    /**
     * Adds an addable option.
     *
     * @param optionName Option name
     */
    protected void addOption(final String optionName) {
        ((DefaultComboBoxModel) addOptionComboBox.getModel()).addElement(
                optionName);
        addOptionButton.setEnabled(true);
        addOptionComboBox.setEnabled(true);
    }
    
    /**
     * Removes an addable option.
     *
     * @param optionName Option name
     */
    protected void delOption(final String optionName) {
        ((DefaultComboBoxModel) addOptionComboBox.getModel()).removeElement(
                optionName);
        if (addOptionComboBox.getModel().getSize() == 0) {
            addOptionComboBox.setEnabled(false);
            addOptionButton.setEnabled(false);
        }
    }
    
    /**
     * Swaps the input field type to the appropriate type.
     *
     * @param type Option type
     */
    private void switchInputField(final OptionType type) {
        if (type == null) {
            addInputCurrent = addInputNone;
        } else {
            switch (type) {
                case TEXTFIELD:
                    addInputText.setText("");
                    addInputCurrent = addInputText;
                    break;
                case CHECKBOX:
                    addInputCheckbox.setSelected(false);
                    addInputCurrent = addInputCheckbox;
                    break;
                case COLOUR:
                    addInputColourChooser.setColour("");
                    addInputCurrent = addInputColourChooser;
                    break;
                case SPINNER:
                    addInputSpinner.setValue(0);
                    addInputCurrent = addInputSpinner;
                    break;
                default:
                    addInputCurrent = addInputNone;
                    break;
            }
        }
        
        layoutComponents();
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addOptionComboBox) {
            if (addOptionComboBox.getSelectedItem() == null) {
                addOptionComboBox.setEnabled(false);
                addOptionButton.setEnabled(false);
            }
            switchInputField(parent.getOptionType(
                    (String) addOptionComboBox.getSelectedItem()));
        } else if (e.getSource() == addOptionButton) {
            final OptionType type = parent.getOptionType(
                    (String) addOptionComboBox.getSelectedItem());
            
            switch (type) {
                case TEXTFIELD:
                    parent.addCurrentOption(
                            (String) addOptionComboBox.getSelectedItem(),
                            type,
                            addInputText.getText());
                    break;
                case CHECKBOX:
                    parent.addCurrentOption(
                            (String) addOptionComboBox.getSelectedItem(),
                            type,
                            String.valueOf(addInputCheckbox.isSelected()));
                    break;
                case COLOUR:
                    parent.addCurrentOption(
                            (String) addOptionComboBox.getSelectedItem(),
                            type,
                            addInputColourChooser.getColour());
                    break;
                case SPINNER:
                    parent.addCurrentOption(
                            (String) addOptionComboBox.getSelectedItem(),
                            type,
                            addInputSpinner.getValue().toString());
                    break;
                default:
                    break;
            }
            
            delOption((String) addOptionComboBox.getSelectedItem());
        }
    }
    
}
