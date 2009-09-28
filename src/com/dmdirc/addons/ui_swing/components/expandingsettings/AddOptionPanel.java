/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.expandingsettings;

import com.dmdirc.addons.ui_swing.components.renderers.AddOptionCellRenderer;
import com.dmdirc.addons.ui_swing.components.ColourChooser;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel.OptionType;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.FontPicker;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.nio.charset.Charset;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

/**
 * Add option panel.
 */
public final class AddOptionPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
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
    /** Add option font picker. */
    private FontPicker addInputFontPicker;
    /** Add option combobox. */
    private JComboBox addInputComboBox;
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
        
        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
        initListeners();
        setLayout(new MigLayout("ins 0"));
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
        addInputCheckbox.setOpaque(UIUtilities.getTabbedPaneOpaque());
        addInputSpinner = new JSpinner(new SpinnerNumberModel());
        addInputNone = new JLabel("");
        addInputFontPicker = new FontPicker("Dialog");
        addInputComboBox = new JComboBox(new DefaultComboBoxModel());
        
        addInputCurrent = addInputNone;
        
        addOptionComboBox.setEnabled(false);
        addOptionButton.setEnabled(false);
    }
    
    /** Initialises listeners. */
    private void initListeners() {
        //Only fire events on selection not on highlight
        addOptionComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        addOptionComboBox.addActionListener(this);
        addOptionButton.addActionListener(this);

        addInputText.getActionMap().put("enter", new AbstractAction() {
            
            private static final long serialVersionUID = 2;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                addOptionButton.doClick();
            }
        });
        addInputText.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enter");
    }
    
    /** Lays out the components. */
    private void layoutComponents() {
        setVisible(false);
        
        removeAll();
        
        add(addOptionComboBox, "left, aligny top");
        add(addInputCurrent, "growx, pushx, aligny top");
        add(addOptionButton, "right, aligny top");
        
        setVisible(true);
    }
    
    /**
     * Adds an addable option.
     *
     * @param optionName Option name
     */
    protected void addOption(final String optionName) {
        ((DefaultComboBoxModel) addOptionComboBox.getModel()).addElement(
                optionName);
        if ("channel.encoding".equals(optionName)) {
            ((DefaultComboBoxModel) addInputComboBox.getModel()).removeAllElements();
            for (Object argument: Charset.availableCharsets().keySet()) {
                ((DefaultComboBoxModel) addInputComboBox.getModel()).addElement(argument);
            }
        }
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
    
    /** Clears the options. */
    protected void clearOptions() {
        addOptionComboBox.removeAllItems();
        addInputCurrent = addInputNone;
        layoutComponents();
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
                    addInputColourChooser.clearColour();
                    addInputCurrent = addInputColourChooser;
                    break;
                case SPINNER:
                    addInputSpinner.setValue(0);
                    addInputCurrent = addInputSpinner;
                    break;
                case FONT:
                    addInputCurrent = addInputFontPicker;
                    break;
                case COMBOBOX:
                    addInputCurrent = addInputComboBox;
                    break;
                default:
                    addInputCurrent = addInputNone;
                    break;
            }
        }
        
        layoutComponents();
        
        addOptionComboBox.requestFocusInWindow();
    }
    
    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
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
                case FONT:
                    parent.addCurrentOption(
                            (String) addOptionComboBox.getSelectedItem(),
                            type,
                            ((Font) addInputFontPicker.getSelectedItem()).getFamily());
                    break;
                case COMBOBOX:
                    parent.addCurrentOption((String) addOptionComboBox.getSelectedItem(),
                            type,
                            (String) addInputComboBox.getSelectedItem());
                    break;
                default:
                    break;
            }
            
            delOption((String) addOptionComboBox.getSelectedItem());
        }
    }
    
}
