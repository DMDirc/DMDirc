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

import com.dmdirc.ui.IconManager;
import com.dmdirc.addons.ui_swing.components.ColourChooser;
import com.dmdirc.addons.ui_swing.components.ImageButton;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel.OptionType;
import com.dmdirc.addons.ui_swing.UIUtilities;

import com.dmdirc.addons.ui_swing.components.FontPicker;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

/**
 * Current options panel.
 */
public final class CurrentOptionsPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Parent settings panel. */
    private final SettingsPanel parent;
    
    /** config option -> text fields. */
    private Map<String, JTextField> textFields;
    /** config option -> checkboxes. */
    private Map<String, JCheckBox> checkBoxes;
    /** config option -> colours. */
    private Map<String, ColourChooser> colours;
    /** config option -> spinners. */
    private Map<String, JSpinner> spinners;
    /** config option -> spinners. */
    private Map<String, FontPicker> fonts;
    /** config option -> comboboxes. */
    private Map<String, JComboBox> comboboxes;
    
    /**
     * Creates a new instance of CurrentOptionsPanel.
     *
     * @param parent Parent settings panel.
     */
    protected CurrentOptionsPanel(final SettingsPanel parent) {
        super();
        
        this.parent = parent;
        
        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        textFields = new HashMap<String, JTextField>();
        checkBoxes = new HashMap<String, JCheckBox>();
        colours = new HashMap<String, ColourChooser>();
        spinners = new HashMap<String, JSpinner>();
        fonts = new HashMap<String, FontPicker>();
        comboboxes = new HashMap<String, JComboBox>();
    }
    
    /** Clears all the current options. */
    protected void clearOptions() {
        textFields.clear();
        checkBoxes.clear();
        colours.clear();
        spinners.clear();
        fonts.clear();
        comboboxes.clear();
        populateCurrentSettings();
    }
    
    /**
     * Adds a current option.
     *
     * @param optionName option to add
     * @param type Option type
     * @param value Option value
     */
    protected void addOption(final String optionName,
            final OptionType type, final String value) {
        switch (type) {
            case TEXTFIELD:
                textFields.put(optionName, new JTextField(value));
                break;
            case CHECKBOX:
                checkBoxes.put(optionName, new JCheckBox("", Boolean.parseBoolean(value)));
                break;
            case COLOUR:
                colours.put(optionName, new ColourChooser(value, true, true));
                break;
            case SPINNER:
                spinners.put(optionName, new JSpinner(new SpinnerNumberModel()));
                spinners.get(optionName).setValue(Integer.parseInt(value));
                break;
            case FONT:
                fonts.put(optionName, new FontPicker(value));
                break;
            case COMBOBOX:
                if ("channel.encoding".equals(optionName)) {
                    comboboxes.put(optionName, new JComboBox(new DefaultComboBoxModel(Charset.availableCharsets().keySet().toArray())));
                    comboboxes.get(optionName).setSelectedItem(value);
                } else {
                    //Ignore
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal Type: " + type);
        }
        
        populateCurrentSettings();
    }
    
    /**
     * Deletes a current option.
     *
     * @param optionName Option to delete
     * @param type Option type
     */
    protected void delOption(final String optionName,
            final OptionType type) {
        switch (type) {
            case TEXTFIELD:
                textFields.remove(optionName);
                break;
            case CHECKBOX:
                checkBoxes.remove(optionName);
                break;
            case COLOUR:
                colours.remove(optionName);
                break;
            case SPINNER:
                spinners.remove(optionName);
                break;
            case FONT:
                fonts.remove(optionName);
                break;
            case COMBOBOX:
                comboboxes.remove(optionName);
                break;
            default:
                throw new IllegalArgumentException("Illegal Type: " + type);
        }
        
        populateCurrentSettings();
    }
    
    /**
     * Retrives an options value.
     *
     * @param optionName Option to delete
     * @param type Option type
     *
     * @return Option value or a blank string
     */
    public String getOption(final String optionName, final OptionType type) {
        String returnValue = null;
        switch (type) {
            case TEXTFIELD:
                if (textFields.containsKey(optionName)) {
                    returnValue =  textFields.get(optionName).getText();
                }
                break;
            case CHECKBOX:
                if (checkBoxes.containsKey(optionName)) {
                    if (checkBoxes.get(optionName).isSelected()) {
                        returnValue = "true";
                    } else {
                        returnValue = "false";
                    }
                }
                break;
            case COLOUR:
                if (colours.containsKey(optionName)) {
                    returnValue = colours.get(optionName).getColour();
                }
                break;
            case SPINNER:
                if (spinners.containsKey(optionName)) {
                    returnValue = spinners.get(optionName).getValue().toString();
                }
                break;
            case FONT:
                if (fonts.containsKey(optionName)) {
                    returnValue = ((Font) fonts.get(optionName).getSelectedItem()).getFamily();
                }
                break;
            case COMBOBOX:
                if (comboboxes.containsKey(optionName)) {
                    returnValue = (String) ((DefaultComboBoxModel) comboboxes.get(optionName).getModel()).getSelectedItem();
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal Type: " + type);
        }
        return returnValue;
    }
    
    /**
     * Adds an option to the current options pane.
     * @param configName config option name
     * @param displayName config option display name
     * @param panel parent panel
     * @param component Option component to add
     */
    private void addCurrentOption(final String configName, final String displayName,
            final JPanel panel, final JComponent component) {
        final JLabel label = new JLabel();
        final ImageButton button = new ImageButton(configName,
                IconManager.getIconManager().getIcon("close-inactive"),
                IconManager.getIconManager().getIcon("close-active"));
        
        label.setText(displayName + ": ");
        label.setLabelFor(component);
        
        button.addActionListener(this);
        
        panel.add(label);
        panel.add(component, "growx, pushx");
        panel.add(button, "wrap");
    }
    
    
    /** Populates the current settings. */
    protected void populateCurrentSettings() {
        setVisible(false);
        
        setLayout(new MigLayout("fillx, aligny top"));
        
        removeAll();
        
        for (Entry<String, JTextField> entry : textFields.entrySet()) {
            addCurrentOption(entry.getKey(),
                    parent.getOptionName(entry.getKey()),
                    this, entry.getValue());
        }
        
        for (Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            addCurrentOption(entry.getKey(),
                    parent.getOptionName(entry.getKey()),
                    this, entry.getValue());
        }
        
        for (Entry<String, ColourChooser> entry : colours.entrySet()) {
            addCurrentOption(entry.getKey(),
                    parent.getOptionName(entry.getKey()),
                    this, entry.getValue());
        }
        
        for (Entry<String, JSpinner> entry : spinners.entrySet()) {
            addCurrentOption(entry.getKey(),
                    parent.getOptionName(entry.getKey()),
                    this, entry.getValue());
        }

        for (Entry<String, FontPicker> entry : fonts.entrySet()) {
            addCurrentOption(entry.getKey(),
                    parent.getOptionName(entry.getKey()),
                    this, entry.getValue());
        }

        for (Entry<String, JComboBox> entry : comboboxes.entrySet()) {
            addCurrentOption(entry.getKey(),
                    parent.getOptionName(entry.getKey()),
                    this, entry.getValue());
        }
        
        setVisible(true);
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Action performed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        delOption(e.getActionCommand(), parent.getOptionType(e.getActionCommand()));
        parent.addAddableOption(e.getActionCommand());
    }
    
}
