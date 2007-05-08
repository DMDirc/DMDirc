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

package uk.org.ownage.dmdirc.ui.dialogs;

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
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import uk.org.ownage.dmdirc.actions.Action;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * Actions editor dialog, used to edit a particular actions.
 */
public final class ActionsEditorDialog extends StandardDialog implements
        ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Actions manager dialog. */
    private final ActionsManagerDialog owner;
    /** Current settings panel. */
    private JPanel settingsPanel;
    /** Add settings panel. */
    private final JPanel addPanel;
    /** Response panel. */
    private final JPanel responsePanel;
    /** info panel. */
    private final JPanel infoPanel;
    /** name field. */
    private final JTextField name;
    /** event dropdown. */
    private final JComboBox event;
    /** response text area. */
    private final JTextArea responses;
    /** Delete button. */
    private final JButton deleteButton;
    /** Information label. */
    private final JTextArea infoLabel;
    /** No settings warning label. */
    private final JLabel noCurrentSettingsLabel;
    /** new setting value text field. */
    private JTextField newSettingField;
    /** new type combo box. */
    private JComboBox newTypeComboBox;
    /** new comparison combo box. */
    private JComboBox newComparisonComboBox;
    /** channel settings. */
    private Properties settings;
    /** number of current settings. */
    private int numCurrentSettings;
    /** hashmap, config option -> name. */
    private Map<String, String> optionMap;
    /** Valid option types. */
    private enum OPTION_TYPE { TEXTFIELD, CHECKBOX, COMBOBOX, }
    /** text fields. */
    private Map<String, JTextField> textFields;
    /** checkboxes. */
    private Map<String, JCheckBox> checkBoxes;
    /** Action being edited. */
    private Action action;
    
    /**
     * Creates a new instance of ChannelSettingsPane.
     */
    public ActionsEditorDialog(final ActionsManagerDialog parent) {
        this(parent, null);
    }
    
    /**
     * Creates a new instance of ChannelSettingsPane.
     * @param newAction parent action
     */
    public ActionsEditorDialog(final ActionsManagerDialog parent, 
            final Action newAction) {
        super(MainFrame.getMainFrame(), false);
        
        owner = parent;
        action = newAction;
        
        settingsPanel = new JPanel();
        addPanel = new JPanel();
        responsePanel = new JPanel();
        infoPanel = new JPanel();
        
        name = new JTextField();
        event = new JComboBox(new String[]{"Event 1", "Event 2", });
        
        responses = new JTextArea();
        
        infoLabel = new JTextArea();
        noCurrentSettingsLabel = new JLabel();
        
        orderButtons(new JButton(), new JButton());
        deleteButton = new JButton("Delete");
        deleteButton.setActionCommand("delete");
        
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        deleteButton.addActionListener(this);
        
        initSettingsPanel();
        
        this.setVisible(true);
    }
    
    /**
     * Initialises the settings panel.
     */
    private void initSettingsPanel() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        infoLabel.setText("These settings are specific to this channel on " 
                + "this network, any settings specified here will overwrite " 
                + "global settings");
        infoLabel.setBorder(new EmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        infoLabel.setEditable(false);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        infoLabel.setHighlighter(null);
        infoLabel.setBackground(this.getBackground());
        
        this.setLayout(new GridBagLayout());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        this.add(infoLabel, constraints);
        
        constraints.gridy = 1;
        this.add(infoPanel, constraints);
        
        constraints.gridy = 2;
        constraints.weighty = 1.0;
        this.add(settingsPanel, constraints);
        
        constraints.gridy = 3;
        constraints.weighty = 0.0;
        this.add(addPanel, constraints);
        
        constraints.gridy = 4;
        this.add(responsePanel, constraints);
        
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(LARGE_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        this.add(deleteButton, constraints);
        
        constraints.gridx = 1;
        this.add(Box.createHorizontalGlue(), constraints);
        
        constraints.gridx = 2;
        this.add(getLeftButton(), constraints);
        
        constraints.gridx = 3;
        this.add(getRightButton(), constraints);
        
        initInfoPanel();
        initAddPanel();
        initCurrentSettingsPanel();
        initResponsePanel();
        
        pack();
    }
    
    /** Initialises the info panel. */
    private void initInfoPanel() {        
        infoPanel.add(new JLabel("Name: "));
        infoPanel.add(name);
        infoPanel.add(new JLabel("Event: "));
        infoPanel.add(event);
        
        name.setPreferredSize(new Dimension(150,
                name.getFont().getSize()));
        
        event.setPreferredSize(new Dimension(150,
                event.getFont().getSize()));
        
        infoPanel.setLayout(new SpringLayout());
        
        layoutGrid(infoPanel, 2,
                    2, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Initialises the current settings panel.  */
    public void initCurrentSettingsPanel() {
        textFields = new HashMap<String, JTextField>();
        checkBoxes = new HashMap<String, JCheckBox>();
        optionMap = new LinkedHashMap<String, String>();
        
        settingsPanel.setVisible(false);
        settingsPanel.removeAll();
        
        numCurrentSettings = 0;
        
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Conditions"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        
        settingsPanel.setLayout(new SpringLayout());
        
        //add posible things to stuff
        
        if (numCurrentSettings == 0) {           
            noCurrentSettingsLabel.setText("No conditions set.");
            noCurrentSettingsLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            settingsPanel.add(noCurrentSettingsLabel);
            
            layoutGrid(settingsPanel, 1,
                    1, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        } else {
            layoutGrid(settingsPanel, numCurrentSettings,
                    4, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        }
        
        settingsPanel.setVisible(true);
    }
    
    /** Initialises the add settings panel.  */
    public void initAddPanel() {
        final JButton newSettingButton;
        
        newSettingField = new JTextField();
        newSettingButton = new JButton();
        newTypeComboBox = new JComboBox(
                new DefaultComboBoxModel(
                new String[]{"type 1", "type 2", }));
        newComparisonComboBox = new JComboBox(
                new DefaultComboBoxModel(
                new String[]{"Comparison 1", "Comparson 2", }));
        
        addPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Add new condition"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        
        addPanel.setLayout(new SpringLayout());
        
        newTypeComboBox.setPreferredSize(new Dimension(150,
                newTypeComboBox.getFont().getSize()));
        newComparisonComboBox.setPreferredSize(new Dimension(150,
                newComparisonComboBox.getFont().getSize()));
        
        newSettingField.setText("");
        newSettingField.setPreferredSize(new Dimension(150,
                newSettingField.getFont().getSize()));
        
        newSettingButton.setText("Add");
        newSettingButton.setMargin(new Insets(0, 0, 0, 0));
        newSettingButton.setPreferredSize(new Dimension(45, 0));
        newSettingButton.setActionCommand("add");
        newSettingButton.addActionListener(this);
        
        addPanel.add(newTypeComboBox);
        addPanel.add(newComparisonComboBox);
        addPanel.add(newSettingField);
        addPanel.add(newSettingButton);
        
        layoutGrid(addPanel, 1, 4, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
    }
    
    /** initliases the response panel. */
    private void initResponsePanel() {
        final JScrollPane scrollPane = new JScrollPane(responses);
        
        responsePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Response"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        
        scrollPane.setMinimumSize(new Dimension(150,
                responses.getFont().getSize() * 5));
        
        responses.setWrapStyleWord(true);
        responses.setLineWrap(true);
        
        responsePanel.setLayout(new BorderLayout());
        
        responsePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Adds an option to the current options pane.
     * @param configName config option name
     * @param displayName config option display name
     * @param panel parent panel
     * @param type Option type
     * @param optionValue config option value
     */
    private void addCurrentOption(final String configName, 
            final String displayName, final JPanel panel, 
            final OPTION_TYPE type, final String optionValue) {
        final JLabel label = new JLabel();
        final JButton button = new JButton();
        
        JComponent component;
        numCurrentSettings++;
        
        switch (type) {
            case CHECKBOX:
                component = new JCheckBox();
                ((JCheckBox) component).setSelected(
                        Boolean.parseBoolean(optionValue));
                checkBoxes.put(configName, (JCheckBox) component);
                break;
            case TEXTFIELD:
                component = new JTextField();
                ((JTextField) component).setText(optionValue);
                textFields.put(configName, (JTextField) component);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised option type: " 
                        + type);
        }
        
        component.setPreferredSize(new Dimension(150,
                component.getFont().getSize()));
        
        label.setText(displayName + ": ");
        label.setPreferredSize(new Dimension(150,
                label.getFont().getSize()));
        label.setLabelFor(component);
        
        button.setIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource(
                "uk/org/ownage/dmdirc/res/close-inactive.png")));
        button.setRolloverIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource(
                "uk/org/ownage/dmdirc/res/close-active.png")));
        button.setPressedIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource(
                "uk/org/ownage/dmdirc/res/close-active.png")));
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
     * Adds an addable option to the current options.
     * @param name option name
     * @param value option value
     */
    private void addNewCurrentOption(final String type, final String comparison,
            final String value) {
        if (action != null && value != null) {
            //identity.setOption(optionValues[0], optionValues[1], value);
            initCurrentSettingsPanel();
        }
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == getOkButton()) {
            saveSettings();
            this.dispose();
        } else if (event.getSource() == getCancelButton()) {
            this.dispose();
        } else if (event.getSource() == deleteButton) {
            //Delete action
            saveSettings();
            this.dispose();
        } else if ("add".equals(event.getActionCommand())) {
            addNewCurrentOption((String) newTypeComboBox.getSelectedItem(),
                    (String) newComparisonComboBox.getSelectedItem(),
                    newSettingField.getText());
        } else {
            final String[] optionValues = event.getActionCommand().split("\\.");
            //identity.removeOption(optionValues[0], optionValues[1]);
            initCurrentSettingsPanel();
        }
    }
    
    /** Saves the current options. */
    public void saveSettings() {
        owner.loadGroups();
    }
    
}
