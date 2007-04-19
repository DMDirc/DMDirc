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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.identities.Identity;
import uk.org.ownage.dmdirc.identities.IdentityManager;

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
    
    /** Parent channel. */
    private final Channel channel;
    /** Parent container panel. */
    private final JPanel parent;
    /** Current settings panel. */
    private JPanel settingsPanel;
    /** Information label. */
    private final JLabel infoLabel;
    /** No settings warning label. */
    private final JLabel noCurrentSettingsLabel;
    /** splut user modes checkbox. */
    private final JCheckBox splitUserModes;
    /** cycle text text field. */
    private final JTextField cycleText;
    /** kick text text field. */
    private final JTextField kickText;
    /** part text text field. */
    private final JTextField partText;
    /** background colour text field. */
    private final JTextField backColour;
    /** fore ground colour text field. */
    private final JTextField foreColour;
    /** frame buffer text field. */
    private final JTextField frameBuffer;
    /** input buffer text field. */
    private final JTextField inputBuffer;
    /** new setting value text field. */
    private final JTextField newSettingField;
    /** new setting combo box. */
    private final JComboBox newSettingComboBox;
    /** add new settinb button. */
    private final JButton newSettingButton;
    /** channel settings . */
    private Properties settings;
    /** number of current settings. */
    private int numCurrentSettings;
    /** number of addable settings. */
    private int numAddableSettings;
    /** hashmap, config option -> name */
    private HashMap<String, String> optionMap;
    /** Channel identity file. */
    private Identity identity;
    /** Valid option types */
    private enum OPTION_TYPE { TEXTFIELD, CHECKBOX, }
    /** text fields */
    private HashMap<String, JTextField> textFields;
    /** checkboxes */
    private HashMap<String, JCheckBox> checkBoxes;
    
    /**
     * Creates a new instance of ChannelSettingsPane.
     * @param newParent parent panel
     * @param newChannel parent Channel
     */
    public ChannelSettingsPane(final JPanel newParent, final Channel newChannel) {
        parent = newParent;
        channel = newChannel;
        
        identity = IdentityManager.getChannelConfig(channel.getServer().getNetwork(),
                channel.getChannelInfo().getName());
        
        infoLabel = new JLabel();
        noCurrentSettingsLabel = new JLabel();
        splitUserModes = new JCheckBox();
        cycleText = new JTextField();
        kickText = new JTextField();
        partText = new JTextField();
        backColour = new JTextField();
        foreColour = new JTextField();
        frameBuffer = new JTextField();
        inputBuffer = new JTextField();
        newSettingField = new JTextField();
        newSettingComboBox = new JComboBox(new DefaultComboBoxModel());
        newSettingButton = new JButton();
        
        textFields = new HashMap<String, JTextField>();
        checkBoxes = new HashMap<String, JCheckBox>();
        optionMap = new LinkedHashMap<String, String>();
        
        initSettingsPanel();
    }
    
    /**
     * Initialises the settings panel.
     */
    private void initSettingsPanel() {
        this.setVisible(false);
        this.removeAll();
        final GridBagConstraints constraints = new GridBagConstraints();
        final JPanel addPanel = new JPanel();
        settingsPanel = new JPanel();
        JLabel label;
        JButton button;
        numCurrentSettings = 0;
        numAddableSettings = 0;
        optionMap.clear();
        textFields.clear();
        checkBoxes.clear();
        
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Current settings"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        settingsPanel.setLayout(new SpringLayout());
        addPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Add new setting"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        addPanel.setLayout(new SpringLayout());
        
        infoLabel.setText("<html>These settings are specific to this channel on this network,<br>"
                + "any settings specified here will overwrite global settings</html>");
        infoLabel.setBorder(new EmptyBorder(0, 0, LARGE_BORDER, 0));
        
        if (identity == null) {
            settings = new Properties();
        } else {
            settings = identity.getProperties();
        }
        
        addOption("channel.splitusermodes", "Split user modes", OPTION_TYPE.CHECKBOX);
        addOption("general.cyclemessage", "Cycle message", OPTION_TYPE.TEXTFIELD);
        addOption("general.kickmessage", "Kick message", OPTION_TYPE.TEXTFIELD);
        addOption("general.partmessage", "Part message", OPTION_TYPE.TEXTFIELD);
        addOption("ui.backgroundcolour", "Background colour", OPTION_TYPE.TEXTFIELD);
        addOption("ui.foregroundcolour", "Foreground colour", OPTION_TYPE.TEXTFIELD);
        addOption("ui.frameBufferSize", "Frame buffer size", OPTION_TYPE.TEXTFIELD);
        addOption("ui.inputbuffersize", "Input buffer size", OPTION_TYPE.TEXTFIELD);
        
        if (numCurrentSettings == 0) {
            noCurrentSettingsLabel.setText("No channel specific settings.");
            noCurrentSettingsLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            settingsPanel.add(noCurrentSettingsLabel);
            
            layoutGrid(settingsPanel, 1,
                    1, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        } else {
            layoutGrid(settingsPanel, numCurrentSettings,
                    3, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        }
        
        newSettingComboBox.setPreferredSize(new Dimension(150,
                newSettingComboBox.getFont().getSize()));
        newSettingField.setText("");
        newSettingField.setPreferredSize(new Dimension(150,
                newSettingField.getFont().getSize()));
        newSettingButton.setText("Add");
        newSettingButton.setMargin(new Insets(0, 0, 0, 0));
        newSettingButton.setPreferredSize(new Dimension(45, 0));
        newSettingButton.addActionListener(this);
        addPanel.add(newSettingComboBox);
        addPanel.add(newSettingField);
        addPanel.add(newSettingButton);
        
        layoutGrid(addPanel, 1, 3, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        
        this.setLayout(new GridBagLayout());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        this.add(infoLabel, constraints);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = 1;
        this.add(settingsPanel, constraints);
        constraints.weighty = 0.0;
        constraints.gridy = 2;
        this.add(addPanel, constraints);
        this.setVisible(true);
        parent.add(this, BorderLayout.CENTER);
    }
    
    /**
     * Adds an option to the panel
     * @param configName config option name
     * @param type config option type
     */
    private void addOption(final String configName, final String displayName,
            final OPTION_TYPE type) {
        String optionValue;
        
        optionMap.put(displayName, configName);
        if ((optionValue = settings.getProperty(configName)) != null) {
            addCurrentOption(configName, displayName, settingsPanel, type, optionValue);
        } else {
            addAddableOption(displayName);
        }
    }
    
    /**
     * Adds an option to the current options pane.
     */
    private void addCurrentOption(final String configName, final String displayName,
            final JPanel panel, 
            final OPTION_TYPE type, final String optionValue) {
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
                break;
            default:
                throw new IllegalArgumentException("Unrecognised option type");
        }
        component.setPreferredSize(new Dimension(150,
                splitUserModes.getFont().getSize()));
        JLabel label = new JLabel();
        label.setText(displayName + ": ");
        label.setPreferredSize(new Dimension(150,
                label.getFont().getSize()));
        label.setLabelFor(component);
        JButton button = new JButton();
        button.setIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-inactive.png")));
        button.setRolloverIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-active.png")));
        button.setPressedIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-active.png")));
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
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
     */
    public void addAddableOption(final String displayName) {
        numAddableSettings++;
        ((DefaultComboBoxModel) newSettingComboBox.getModel()).addElement(displayName);
    }
    
    /**
     * Adds an addable option to the current options.
     * @param name option name
     * @param value option value
     */
    private void addNewCurrentOption(final String name, final String value) {
        if (identity != null && value != null) {
            String[] optionValues = optionMap.get(name).split("\\.");
            identity.setOption(optionValues[0], optionValues[1], value);
            initSettingsPanel();
        }
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        String option;
        if (event.getSource() == newSettingButton) {
            addNewCurrentOption((String) newSettingComboBox.getSelectedItem(),
                    newSettingField.getText());
        } else {
            String[] optionValues = event.getActionCommand().split("\\.");
            identity.removeOption(optionValues[0], optionValues[1]);
            initSettingsPanel();
        }
    }
    
    /** Saves teh current options */
    public void saveSettings() {
        Config.save();
    }
}
