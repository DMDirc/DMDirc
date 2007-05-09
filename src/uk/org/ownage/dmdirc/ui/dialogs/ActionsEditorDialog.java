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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Spring;
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
    /** buttons panel. */
    private final JPanel buttonsPanel;
    /** name field. */
    private final JTextField name;
    /** event dropdown. */
    private final JComboBox event;
    /** other event lists. */
    private final JList otherEvents;
    /** response text area. */
    private final JTextArea responses;
    /** Delete button. */
    private final JButton deleteButton;
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
    /** Valid option types. */
    private enum OPTION_TYPE { TEXTFIELD, CHECKBOX, COMBOBOX, }
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
        
        this.setTitle("Action editor");
        
        settingsPanel = new JPanel();
        addPanel = new JPanel();
        responsePanel = new JPanel();
        infoPanel = new JPanel();
        buttonsPanel = new JPanel();
        
        name = new JTextField();
        event = new JComboBox(new String[]{});
        otherEvents = new JList(new DefaultListModel());
        otherEvents.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        responses = new JTextArea();
        
        noCurrentSettingsLabel = new JLabel();
        
        orderButtons(new JButton(), new JButton());
        deleteButton = new JButton("Delete");
        
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        deleteButton.addActionListener(this);
        
        initSettingsPanel();
        
        this.setMinimumSize(new Dimension(600, 625));
        
        this.setVisible(true);
    }
    
    /**
     * Initialises the settings panel.
     */
    private void initSettingsPanel() {
        final SpringLayout layout = new SpringLayout();
        
        initInfoPanel();
        initAddPanel();
        initCurrentSettingsPanel();
        initResponsePanel();
        initButtonsPanel();
        
        //this.add(infoLabel);
        this.add(infoPanel);
        this.add(settingsPanel);
        this.add(addPanel);
        this.add(responsePanel);
        this.add(buttonsPanel);
        
        this.getContentPane().setLayout(layout);
        
        //info label SMALL_BORDER from top, left and right
        layout.putConstraint(SpringLayout.NORTH, this.getContentPane(), -SMALL_BORDER,
                SpringLayout.NORTH, infoPanel);
        layout.putConstraint(SpringLayout.WEST, infoPanel, SMALL_BORDER,
                SpringLayout.WEST, this.getContentPane());
        layout.putConstraint(SpringLayout.EAST, infoPanel, -SMALL_BORDER,
                SpringLayout.EAST, this.getContentPane());
        
        //settings panel SMALL_BORDER from the infoPanel and left
        layout.putConstraint(SpringLayout.NORTH, settingsPanel, SMALL_BORDER,
                SpringLayout.SOUTH, infoPanel);
        layout.putConstraint(SpringLayout.SOUTH, settingsPanel, -SMALL_BORDER,
                SpringLayout.NORTH, addPanel);
        layout.putConstraint(SpringLayout.WEST, settingsPanel, SMALL_BORDER,
                SpringLayout.WEST, this.getContentPane());
        layout.putConstraint(SpringLayout.EAST, settingsPanel, -SMALL_BORDER,
                SpringLayout.EAST, this.getContentPane());
        
        //add panel SMALL_BORDER from the settingsPanel and left
        layout.putConstraint(SpringLayout.SOUTH, addPanel, -SMALL_BORDER,
                SpringLayout.NORTH, responsePanel);
        layout.putConstraint(SpringLayout.WEST, addPanel, SMALL_BORDER,
                SpringLayout.WEST, this.getContentPane());
        layout.putConstraint(SpringLayout.EAST, addPanel, -SMALL_BORDER,
                SpringLayout.EAST, this.getContentPane());
        
        //response panel SMALL_BORDER from the addPanel and left
        layout.putConstraint(SpringLayout.SOUTH, responsePanel, -SMALL_BORDER,
                SpringLayout.NORTH, buttonsPanel);
        layout.putConstraint(SpringLayout.WEST, responsePanel, SMALL_BORDER,
                SpringLayout.WEST, this.getContentPane());
        layout.putConstraint(SpringLayout.EAST, responsePanel, -SMALL_BORDER,
                SpringLayout.EAST, this.getContentPane());
        
        //buttons panel SMALL_BORDER from the left and bottom
        layout.putConstraint(SpringLayout.SOUTH, buttonsPanel, -SMALL_BORDER,
                SpringLayout.SOUTH, this.getContentPane());
        layout.putConstraint(SpringLayout.WEST, buttonsPanel, SMALL_BORDER,
                SpringLayout.WEST, this.getContentPane());
        layout.putConstraint(SpringLayout.EAST, buttonsPanel, -SMALL_BORDER,
                SpringLayout.EAST, this.getContentPane());
        
        pack();
    }
    
    /** Initialises the info panel. */
    private void initInfoPanel() {
        infoPanel.add(new JLabel("Name: "));
        infoPanel.add(name);
        infoPanel.add(new JLabel("Event: "));
        infoPanel.add(event);
        infoPanel.add(new JLabel("Other Events: "));
        infoPanel.add(new JScrollPane(otherEvents));
        
        name.setPreferredSize(new Dimension(150,
                name.getFont().getSize()));
        
        event.setPreferredSize(new Dimension(150,
                event.getFont().getSize()));
        
        infoPanel.setLayout(new SpringLayout());
        
        ((SpringLayout) infoPanel.getLayout()).getConstraints(otherEvents)
        .setHeight(Spring.constant(100));
        
        layoutGrid(infoPanel, 3,
                2, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Initialises the current settings panel.  */
    public void initCurrentSettingsPanel() {
        numCurrentSettings = 0;
        
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Conditions"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        
        settingsPanel.setLayout(new SpringLayout());
        
        noCurrentSettingsLabel.setText("No conditions set.");
        noCurrentSettingsLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        settingsPanel.add(noCurrentSettingsLabel);
        
        layoutGrid(settingsPanel, numCurrentSettings,
                4, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Initialises the add settings panel.  */
    public void initAddPanel() {
        final JButton newSettingButton;
        
        newSettingField = new JTextField();
        newSettingButton = new JButton();
        newTypeComboBox = new JComboBox(
                new DefaultComboBoxModel(
                new String[]{}));
        newComparisonComboBox = new JComboBox(
                new DefaultComboBoxModel(
                new String[]{}));
        
        addPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                new EtchedBorder(), "Add new condition"),
                new EmptyBorder(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER)));
        
        addPanel.setLayout(new SpringLayout());
        
        newTypeComboBox.setPreferredSize(new Dimension(150,
                0));
        newComparisonComboBox.setPreferredSize(new Dimension(150,
                newComparisonComboBox.getFont().getSize() + LARGE_BORDER
                + SMALL_BORDER));
        
        newSettingField.setText("");
        newSettingField.setPreferredSize(new Dimension(150,
                0));
        
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
        
        responses.setRows(5);
        
        responses.setWrapStyleWord(true);
        responses.setLineWrap(true);
        
        responsePanel.setLayout(new BorderLayout());
        
        responsePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    /** Initialises the buttons panel. */
    private void initButtonsPanel() {
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(getLeftButton());
        buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonsPanel.add(getRightButton());
    }
    
    /**
     * Adds an option to the current options pane.
     * @param configName config option name
     * @param displayName config option display name
     * @param panel parent panel
     * @param type Option type
     * @param optionValue config option value
     */
    private void addCurrentOption(final OPTION_TYPE type1,
            final OPTION_TYPE type2, final String optionValue) {
        final JButton button = new JButton();
        
        JComboBox type;
        JComboBox comparison;
        JTextField value;
        
        settingsPanel.setVisible(false);
        
        if (numCurrentSettings == 0) {
            settingsPanel.remove(0);
        }
        
        numCurrentSettings++;
        
        type = new JComboBox(new OPTION_TYPE[]{});
        type.setSelectedItem(type1);
        comparison = new JComboBox(new OPTION_TYPE[]{});
        comparison.setSelectedItem(type2);
        value = new JTextField();
        value.setText(optionValue);
        
        type.setPreferredSize(new Dimension(150,
                type.getFont().getSize()));
        comparison.setPreferredSize(new Dimension(150,
                comparison.getFont().getSize()));
        value.setPreferredSize(new Dimension(150,
                value.getFont().getSize()));
        
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
        button.setActionCommand(String.valueOf(numCurrentSettings));
        button.addActionListener(this);
        
        settingsPanel.add(type);
        settingsPanel.add(comparison);
        settingsPanel.add(value);
        settingsPanel.add(button);
        
        settingsPanel.setLayout(new SpringLayout());
        
        layoutGrid(settingsPanel, numCurrentSettings,
                4, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        
        settingsPanel.setVisible(true);
        
        pack();
    }
    
    public void removeCurrentOption(int optionNumber) {
        int removeItem = (optionNumber - 1) * 4;
        
        settingsPanel.setVisible(false);
        
        settingsPanel.remove(removeItem);
        settingsPanel.remove(removeItem);
        settingsPanel.remove(removeItem);
        settingsPanel.remove(removeItem);
        
        numCurrentSettings--;
        
        settingsPanel.setLayout(new SpringLayout());
        
        layoutGrid(settingsPanel, numCurrentSettings,
                4, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
        
        if (numCurrentSettings == 0) {
            settingsPanel.add(noCurrentSettingsLabel);
        }
        
        settingsPanel.setVisible(true);
        
        pack();
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
            addCurrentOption((OPTION_TYPE) newTypeComboBox.getSelectedItem(),
                    (OPTION_TYPE) newComparisonComboBox.getSelectedItem(),
                    newSettingField.getText());
        } else {
            try {
                removeCurrentOption(Integer.parseInt(event.getActionCommand()));
            } catch (NumberFormatException ex) {
                //Ignore
            }
        }
    }
    
    /** Saves the current options. */
    public void saveSettings() {
        owner.loadGroups();
    }
    
}
