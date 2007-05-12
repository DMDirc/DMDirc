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

package uk.org.ownage.dmdirc.ui.dialogs.channelsetting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.identities.Identity;
import uk.org.ownage.dmdirc.ui.components.ColourChooser;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * Channel settings panel.
 */
public final class ChannelSettingsPane extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent container panel. */
    private final JPanel parent;
    /** Current settings panel. */
    private JPanel settingsPanel;
    /** Information label. */
    private final JLabel infoLabel;
    /** No settings warning label. */
    private final JLabel noCurrentSettingsLabel;
    /** new setting value text field. */
    private JTextField newSettingField;
    /** new setting combo box. */
    private JComboBox newSettingComboBox;
    /** channel settings. */
    private Properties settings;
    /** number of current settings. */
    private int numCurrentSettings;
    /** hashmap, config option -> name. */
    private Map<String, String> optionMap;
    /** Channel identity file. */
    private final Identity identity;
    /** Valid option types. */
    private enum OPTION_TYPE { TEXTFIELD, CHECKBOX, COLOUR, }
    /** text fields. */
    private Map<String, JTextField> textFields;
    /** checkboxes. */
    private Map<String, JCheckBox> checkBoxes;
    /** colours. */
    private Map<String, ColourChooser> colours;
    
    /**
     * Creates a new instance of ChannelSettingsPane.
     * @param newParent parent panel
     * @param newIdentity parent identity
     */
    public ChannelSettingsPane(final JPanel newParent, final Identity newIdentity) {
        super();
        parent = newParent;
        identity = newIdentity;
        
        infoLabel = new JLabel();
        noCurrentSettingsLabel = new JLabel();
        
        initSettingsPanel();
    }
    
    /**
     * Initialises the settings panel.
     */
    private void initSettingsPanel() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        infoLabel.setText("<html>These settings are specific to this channel on this network,<br>"
                + "any settings specified here will overwrite global settings</html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, LARGE_BORDER, 0));
        
        if (identity == null) {
            settings = new Properties();
        } else {
            settings = identity.getProperties();
        }
        
        this.setLayout(new GridBagLayout());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        this.add(infoLabel, constraints);
        
        initAddPanel();
        initCurrentSettingsPanel();
        
        parent.add(this, BorderLayout.CENTER);
    }
    
    /** Initialises the current settings panel.  */
    public void initCurrentSettingsPanel() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        textFields = new HashMap<String, JTextField>();
        checkBoxes = new HashMap<String, JCheckBox>();
        colours = new HashMap<String, ColourChooser>();
        
        optionMap = new LinkedHashMap<String, String>();
        
        if (settingsPanel == null) {
            settingsPanel = new JPanel();
            settingsPanel.setVisible(false);
        } else {
            settingsPanel.setVisible(false);
            settingsPanel.removeAll();
        }
        
        numCurrentSettings = 0;
        
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Current settings"),
                BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        
        settingsPanel.setLayout(new SpringLayout());
        
        addOption("channel.splitusermodes", "Split user modes", OPTION_TYPE.CHECKBOX);
        addOption("general.cyclemessage", "Cycle message", OPTION_TYPE.TEXTFIELD);
        addOption("general.kickmessage", "Kick message", OPTION_TYPE.TEXTFIELD);
        addOption("general.partmessage", "Part message", OPTION_TYPE.TEXTFIELD);
        addOption("ui.backgroundcolour", "Background colour", OPTION_TYPE.COLOUR);
        addOption("ui.foregroundcolour", "Foreground colour", OPTION_TYPE.COLOUR);
        addOption("ui.frameBufferSize", "Frame buffer size", OPTION_TYPE.TEXTFIELD);
        addOption("ui.inputbuffersize", "Input buffer size", OPTION_TYPE.TEXTFIELD);
        
        if (numCurrentSettings == 0) {
            noCurrentSettingsLabel.setText("No channel specific settings.");
            noCurrentSettingsLabel.setBorder(
                    BorderFactory.createEmptyBorder(0, 0, 0, 0));
            settingsPanel.add(noCurrentSettingsLabel);
            
            layoutGrid(settingsPanel, 1,
                    1, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        } else {
            layoutGrid(settingsPanel, numCurrentSettings,
                    3, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        }
        
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        
        this.add(settingsPanel, constraints);
        
        settingsPanel.setVisible(true);
    }
    
    /** Initialises the add settings panel.  */
    public void initAddPanel() {
        final GridBagConstraints constraints = new GridBagConstraints();
        final JButton newSettingButton;
        
        final JPanel addPanel;
        
        addPanel = new JPanel();
        
        newSettingField = new JTextField();
        newSettingButton = new JButton();
        newSettingComboBox = new JComboBox(new DefaultComboBoxModel());
        
        addPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Add new setting"),
                BorderFactory.createEmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        
        addPanel.setLayout(new SpringLayout());
        
        newSettingComboBox.setPreferredSize(new Dimension(150,
                newSettingComboBox.getFont().getSize()));
        
        
        
        newSettingField.setText("");
        newSettingField.setPreferredSize(new Dimension(150,
                newSettingField.getFont().getSize()));
        newSettingButton.setText("Add");
        newSettingButton.setMargin(new Insets(0, 0, 0, 0));
        newSettingButton.setPreferredSize(new Dimension(45, 0));
        newSettingButton.setActionCommand("Add setting");
        newSettingButton.addActionListener(this);
        addPanel.add(newSettingComboBox);
        addPanel.add(newSettingField);
        addPanel.add(newSettingButton);
        
        layoutGrid(addPanel, 1, 3, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        
        this.add(addPanel, constraints);
    }
    
    /**
     * Adds an option to the panel.
     * @param configName config option name
     * @param displayName config option display name
     * @param type config option type
     */
    private void addOption(final String configName, final String displayName,
            final OPTION_TYPE type) {
        String optionValue;
        
        optionMap.put(displayName, configName);
        optionValue = settings.getProperty(configName);
        
        if (optionValue == null) {
            addAddableOption(displayName);
        } else {
            addCurrentOption(configName, displayName, settingsPanel, type, optionValue);
        }
    }
    
    /**
     * Adds an option to the current options pane.
     * @param configName config option name
     * @param displayName config option display name
     * @param panel parent panel
     * @param type Option type
     * @param optionValue config option value
     */
    private void addCurrentOption(final String configName, final String displayName,
            final JPanel panel, final OPTION_TYPE type, final String optionValue) {
        final JLabel label = new JLabel();
        final JButton button = new JButton();
        
        JComponent component;
        numCurrentSettings++;
        
        switch (type) {
            case CHECKBOX:
                component = new JCheckBox();
                ((JCheckBox) component).setSelected(Boolean.parseBoolean(optionValue));
                checkBoxes.put(configName, (JCheckBox) component);
                break;
            case TEXTFIELD:
                component = new JTextField();
                ((JTextField) component).setText(optionValue);
                textFields.put(configName, (JTextField) component);
                break;
            case COLOUR:
                component = new ColourChooser(optionValue, true, true);
                colours.put(configName, (ColourChooser) component);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised option type: " + type);
        }
        
        component.setPreferredSize(new Dimension(150,
                component.getFont().getSize()));
        
        label.setText(displayName + ": ");
        label.setPreferredSize(new Dimension(150,
                label.getFont().getSize()));
        label.setLabelFor(component);
        
        button.setIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-inactive.png")));
        button.setRolloverIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-active.png")));
        button.setPressedIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-active.png")));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setPreferredSize(new Dimension(16, 0));
        button.setActionCommand(configName);
        button.addActionListener(this);
        
        panel.add(label);
        panel.add(component);
        panel.add(button);
    }
    
    /**
     * Adds an option to the addable options combo box.
     * @param displayName option display name
     */
    public void addAddableOption(final String displayName) {
        if (((DefaultComboBoxModel) newSettingComboBox.getModel()).getIndexOf(displayName) == -1) {
            ((DefaultComboBoxModel) newSettingComboBox.getModel()).addElement(displayName);
        }
    }
    
    /**
     * Adds an addable option to the current options.
     * @param name option name
     * @param value option value
     */
    private void addNewCurrentOption(final String name, final String value) {
        if (identity != null && value != null && optionMap.get(name) != null) {
            final String[] optionValues = optionMap.get(name).split("\\.");
            identity.setOption(optionValues[0], optionValues[1], value);
            ((DefaultComboBoxModel) newSettingComboBox.getModel()).removeElement(name);
            initCurrentSettingsPanel();
        }
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if ("Add setting".equals(event.getActionCommand())) {
            addNewCurrentOption((String) newSettingComboBox.getSelectedItem(),
                    newSettingField.getText());
        } else {
            final String[] optionValues = event.getActionCommand().split("\\.");
            identity.removeOption(optionValues[0], optionValues[1]);
            initCurrentSettingsPanel();
        }
    }
    
    /** Saves the current options. */
    public void saveSettings() {
        for (String configName : textFields.keySet()) {
            final String[] optionValues = configName.split("\\.");
            final JTextField textField = textFields.get(configName);
            identity.setOption(optionValues[0], optionValues[1],
                    textField.getText());
        }
        for (String configName : checkBoxes.keySet()) {
            final String[] optionValues = configName.split("\\.");
            final JCheckBox checkBox = checkBoxes.get(configName);
            identity.setOption(optionValues[0], optionValues[1],
                    Boolean.toString(checkBox.isSelected()));
        }
        for (String configName : colours.keySet()) {
            final String[] optionValues = configName.split("\\.");
            final ColourChooser colour = colours.get(configName);
            identity.setOption(optionValues[0], optionValues[1],
                    colour.getColour());
        }
        Config.save();
    }
}
