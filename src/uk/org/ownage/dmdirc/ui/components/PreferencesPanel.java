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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.PreferencesInterface;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesPanel extends StandardDialog
        implements ActionListener, ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    
    /** Acceptable input types for the config dialog. */
    public static enum OptionType { TEXTFIELD, CHECKBOX, COMBOBOX, SPINNER, };
    
    /** All text fields in the dialog, used to apply settings. */
    private final Map<String, JTextField> textFields;
    
    /** All checkboxes in the dialog, used to apply settings. */
    private final Map<String, JCheckBox> checkBoxes;
    
    /** All combo boxes in the dialog, used to apply settings. */
    private final Map<String, JComboBox> comboBoxes;
    
    /** All combo boxes in the dialog, used to apply settings. */
    private final Map<String, JSpinner> spinners;
    
    /** Categories in the dialog. */
    private final Map<String, JPanel> categories;
    
    /** Preferences tab list, used to switch option types. */
    private JList tabList;
    
    /** Main card layout. */
    private CardLayout cardLayout;
    
    /** Main panel. */
    private JPanel mainPanel;
    
    /** Preferences owner. */
    private PreferencesInterface owner;
    
    /** title of window. */
    private String windowTitle;
    
    /**
     * Creates a new instance of PreferencesPanel.
     *
     * @param preferencesOwner Owner of the preferences dialog
     */
    public PreferencesPanel(final PreferencesInterface preferencesOwner) {
        this(preferencesOwner, "Preferences");
    }
    
    /**
     * Creates a new instance of PreferencesPanel.
     *
     * @param preferencesOwner Owner of the preferences dialog
     */
    public PreferencesPanel(final PreferencesInterface preferencesOwner, 
            final String title) {
        super(MainFrame.getMainFrame(), false);
        
        windowTitle = title;
        
        owner = preferencesOwner;
        
        categories = new Hashtable<String, JPanel>();
        
        textFields = new Hashtable<String, JTextField>();
        checkBoxes = new Hashtable<String, JCheckBox>();
        comboBoxes = new Hashtable<String, JComboBox>();
        spinners = new Hashtable<String, JSpinner>();
        
        initComponents();
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        final SpringLayout layout = new SpringLayout();
        final JButton button1 = new JButton();
        final JButton button2 = new JButton();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        tabList = new JList(new DefaultListModel());
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.addListSelectionListener(this);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle(windowTitle);
        setResizable(false);
        
        mainPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER));
        tabList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER)));
        
        getContentPane().setLayout(layout);
        
        tabList.setPreferredSize(new Dimension(100, 450));
        tabList.setMinimumSize(new Dimension(100, 450));
        setMinimumSize(new Dimension(600, 500));
        setPreferredSize(new Dimension(600, 500));
        setMaximumSize(new Dimension(600, 500));
        
        orderButtons(button1, button2);
        
        getContentPane().add(tabList);
        
        getContentPane().add(mainPanel);
        
        getContentPane().add(Box.createHorizontalGlue());
        
        getContentPane().add(button1);
        
        getContentPane().add(button2);
        
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        
        //tab list
        layout.putConstraint(SpringLayout.WEST, tabList, 10,
                SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, tabList, 10,
                SpringLayout.NORTH, getContentPane());
        //main panel
        layout.putConstraint(SpringLayout.WEST, mainPanel, 5,
                SpringLayout.EAST, tabList);
        layout.putConstraint(SpringLayout.NORTH, mainPanel, 5,
                SpringLayout.NORTH, getContentPane());
        //ok button
        layout.putConstraint(SpringLayout.EAST, getRightButton() , -10,
                SpringLayout.EAST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, getRightButton() , 5,
                SpringLayout.SOUTH, mainPanel);
        //cancel button
        layout.putConstraint(SpringLayout.EAST, getLeftButton(), -10,
                SpringLayout.WEST, getRightButton());
        layout.putConstraint(SpringLayout.NORTH, getLeftButton(), 5,
                SpringLayout.SOUTH, mainPanel);
        //panel size
        layout.putConstraint(SpringLayout.EAST, getContentPane(), 10,
                SpringLayout.EAST, mainPanel);
        layout.putConstraint(SpringLayout.SOUTH, getContentPane(), 10,
                SpringLayout.SOUTH, getRightButton());
    }
    
    /**
     * Initialises and adds a component to a panel.
     * @param parent parent panel
     * @param optionName option name to use
     * @param title option display name
     * @param type the type of component to use
     * @param args component specific arguments<p>
     * <ul>
     * <li>TEXTFIELD takes a String as its default value</li>
     * <li>CHECKBOX takes a boolean as its default state</li>
     * <li>COMBOBOX takes a String[] of options, a String selected option
     * and a boolean to specify if its editable</li>
     * <li>SPINNER takes an Integer value to specify the default value
     * or 4 Integers, default, minimum, maximum, step size</li>
     * </ul>
     */
    private void addComponent(final JPanel parent, final String optionName,
            final String title, final OptionType type, final Object... args) {
        final JLabel label = new JLabel(title, JLabel.TRAILING);
        
        JComponent option;
        
        ((JPanel) parent.getComponent(1)).add(label);
        switch (type) {
            case TEXTFIELD:
                option = new JTextField();
                ((JTextField) option).setText((String) args[0]);
                textFields.put(optionName, (JTextField) option);
                break;
            case CHECKBOX:
                option = new JCheckBox();
                ((JCheckBox) option).
                        setSelected((Boolean) args[0]);
                checkBoxes.put(optionName, (JCheckBox) option);
                break;
            case COMBOBOX:
                option = new JComboBox((String[]) args[0]);
                ((JComboBox) option).setSelectedItem(args[1]);
                comboBoxes.put(optionName, (JComboBox) option);
                ((JComboBox) option).setEditable((Boolean) args[2]);
                break;
            case SPINNER:
                if (args.length == 1) {
                    option = new JSpinner(new SpinnerNumberModel());
                    try {
                        ((JSpinner) option).setValue(args[0]);
                    } catch (NumberFormatException ex) {
                        ((JSpinner) option).setEnabled(false);
                        Logger.error(ErrorLevel.TRIVIAL,
                                "Default value incorrect", ex);
                    }
                }else {
                    option = new JSpinner(
                            new SpinnerNumberModel((Integer) args[0],
                            (Integer) args[1], (Integer) args[2],
                            (Integer) args[3]));
                }
                spinners.put(optionName, (JSpinner) option);
                break;
            default:
                throw new IllegalArgumentException(type
                        + " is not a valid option");
        }
        option.setPreferredSize(
                new Dimension(Short.MAX_VALUE, option.getFont().getSize()));
        label.setLabelFor(option);
        ((JPanel) parent.getComponent(1)).add(option);
    }
    
    public void addCategory(final String name) {
        addCategory(name, "");
    }
    
    public void addCategory(final String name, final String blurb) {
        final JPanel panel = new JPanel(new BorderLayout(SMALL_BORDER, LARGE_BORDER));
        categories.put(name, panel);
        mainPanel.add(panel, name);
        ((DefaultListModel) tabList.getModel()).addElement(name);
        
        JTextArea infoLabel = new JTextArea(blurb);
        infoLabel.setEditable(false);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        infoLabel.setHighlighter(null);
        infoLabel.setBackground(panel.getBackground());
        if ("".equals(blurb)) {
            infoLabel.setVisible(false);
        } 
        
        panel.add(infoLabel, BorderLayout.PAGE_START);
        panel.add(new JPanel(new SpringLayout()), BorderLayout.CENTER);
    }
    
    public void addOption(final String category, final String name,
            final String displayName,final OptionType type, final Object... args) {
        addComponent(categories.get(category), name, displayName, type, args);
    }
    
    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            saveOptions();
            setVisible(false);
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            setVisible(false);
        }
    }
    
    /**
     * Called when the selection in the list changes.
     *
     * @param selectionEvent list selection event
     */
    public void valueChanged(final ListSelectionEvent selectionEvent) {
        if (!selectionEvent.getValueIsAdjusting()) {
            cardLayout.show(mainPanel, (String) ((JList) selectionEvent.
                    getSource()).getSelectedValue());
        }
    }
    
    /**
     * Saves the options in the dialog to the config.
     */
    private void saveOptions() {
        final Properties properties = new Properties();
        for (String option : textFields.keySet()) {
            if (textFields.get(option).getText().equals("")) {
                properties.remove(option);
            } else {
                properties.setProperty(option,
                        textFields.get(option).getText());
            }
        }
        for (String option : checkBoxes.keySet()) {
            properties.setProperty(option,
                    "" + checkBoxes.get(option).isSelected());
        }
        for (String option : comboBoxes.keySet()) {
            if (comboBoxes.get(option).getSelectedItem() != null) {
                properties.setProperty(option,
                        (String) comboBoxes.get(option).getSelectedItem());
            }
        }
        for (String option : spinners.keySet()) {
            properties.setProperty(option,
                    "" + spinners.get(option).getValue().toString());
        }
        owner.configClosed(properties);
    }
    
    public void display() {
        for (JPanel panel : categories.values()) {
            layoutGrid((JPanel) panel.getComponent(1),
                    ((JPanel) panel.getComponent(1)).getComponentCount() / 2,
                    2, SMALL_BORDER, SMALL_BORDER, LARGE_BORDER, LARGE_BORDER);
        }
        cardLayout.first(mainPanel);
        tabList.setSelectedIndex(0);
        pack();
        setLocationRelativeTo(MainFrame.getMainFrame());
        this.setVisible(true);
    }
    
}
