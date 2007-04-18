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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;

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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.*;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;


/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesDialog extends StandardDialog
	implements ActionListener, ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Acceptable input types for the config dialog. */
    private static enum OptionType { TEXTFIELD, CHECKBOX, COMBOBOX, };
    
    /** All text fields in the dialog, used to apply settings. */
    private final Map<String, JTextField> textFields;
    
    /** All checkboxes in the dialog, used to apply settings. */
    private final Map<String, JCheckBox> checkBoxes;
    
    /** All combo boxes in the dialog, used to apply settings. */
    private final Map<String, JComboBox> comboBoxes;
    
    /** Preferences tab list, used to switch option types. */
    private JList tabList;
    
    /** Main card layout. */
    private CardLayout cardLayout;
    
    /** Main panel. */
    private JPanel mainPanel;
    
    /**
     * Creates a new instance of PreferencesDialog.
     * @param parent The frame that owns this dialog
     * @param modal Whether to show modally or not
     */
    public PreferencesDialog(final Frame parent, final boolean modal) {
	super(parent, modal);
	
	textFields = new Hashtable<String, JTextField>();
	checkBoxes = new Hashtable<String, JCheckBox>();
	comboBoxes = new Hashtable<String, JComboBox>();
	
	initComponents();
	setLocationRelativeTo(MainFrame.getMainFrame());
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
	setTitle("Preferences");
	setResizable(false);
	
	mainPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
		SMALL_BORDER, LARGE_BORDER));
	tabList.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createEtchedBorder(),
		BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER)));
	
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
	
	initGeneralTab(mainPanel);
	
	initUITab(mainPanel);
	
	initTreeViewTab(mainPanel);
	
	initNotificationsTab(mainPanel);
	
	initInputTab(mainPanel);
	
	initLoggingTab(mainPanel);
	
	initIdentitiesTab(mainPanel);
	
	initAdvancedTab(mainPanel);
	
	initListeners();
	
	cardLayout.show(mainPanel, "General");
	tabList.setSelectedIndex(0);
	
	pack();
    }
    /**
     * Adds an option of the specified type to the specified panel.
     *
     * @param parent parent to add component to
     * @param optionName Config option name, used to read/write values to
     * @param title user friendly title for the option
     * @param type type of input component required
     */
    private void addComponent(final JPanel parent, final String optionName,
	    final String title, final OptionType type) {
	addComponent(parent, optionName, title, type, null, false);
    }
    
    /**
     * Adds an option of the specified type to the specified panel.
     *
     * @param parent parent to add component to
     * @param optionName Config option name, used to read/write values to
     * @param title user friendly title for the option
     * @param type type of input component required
     * @param comboOptions combobox options
     * @param comboEditable whether the combo box should be editable
     */
    private void addComponent(final JPanel parent, final String optionName,
	    final String title, final OptionType type,
	    final String[] comboOptions, final boolean comboEditable) {
	final String[] configArgs = optionName.split("\\.");
	final String configValue =
		Config.getOption(configArgs[0], configArgs[1]);
	final JLabel label = new JLabel(title, JLabel.TRAILING);
	
	JComponent option;
	
	parent.add(label);
	switch (type) {
	    case TEXTFIELD:
		option = new JTextField();
		((JTextField) option).setText(configValue);
                ((JTextField) option).setPreferredSize(
                        new Dimension(Short.MAX_VALUE, option.getFont().getSize()));
		textFields.put(optionName, (JTextField) option);
		break;
	    case CHECKBOX:
		option = new JCheckBox();
		((JCheckBox) option).
			setSelected(Boolean.parseBoolean(configValue));
                ((JCheckBox) option).setPreferredSize(
                        new Dimension(Short.MAX_VALUE, option.getFont().getSize()));
		checkBoxes.put(optionName, (JCheckBox) option);
		break;
	    case COMBOBOX:
		option = new JComboBox(comboOptions);
		((JComboBox) option).setSelectedItem(configValue);
		comboBoxes.put(optionName, (JComboBox) option);
                ((JComboBox) option).setPreferredSize(
                        new Dimension(Short.MAX_VALUE, option.getFont().getSize()));
		((JComboBox) option).setEditable(comboEditable);
		break;
	    default:
		throw new IllegalArgumentException(type
			+ " is not a valid option");
	}
	label.setLabelFor(option);
	parent.add(option);
    }
    
    /**
     * Initialises the preferences tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initGeneralTab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	
	addComponent(panel, "general.closemessage", "Close message: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "general.partmessage", "Part message: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "general.quitmessage", "Quit message: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "general.cyclemessage", "Cycle message: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "general.kickmessage", "Kick message: ",
		OptionType.TEXTFIELD);
        addComponent(panel, "general.autoSubmitErrors", "Automatically submit errors: ",
		OptionType.CHECKBOX);
	
	layoutGrid(panel, 6, 2, SMALL_BORDER, SMALL_BORDER,
		LARGE_BORDER, LARGE_BORDER);
	
	cardLayoutPanel.add(panel, "General");
	((DefaultListModel) tabList.getModel()).addElement("General");
    }
    
    /**
     * Initialises the UI tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initUITab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	
	addComponent(panel, "ui.maximisewindows", "Auto-Maximise windows: ",
		OptionType.CHECKBOX);
	addComponent(panel, "ui.backgroundcolour", "Window background colour: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "ui.foregroundcolour", "Window foreground colour: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "ui.sortByMode", "Nicklist sort by mode: ",
		OptionType.CHECKBOX);
	addComponent(panel, "ui.sortByCase", "Nicklist sort by case: ",
		OptionType.CHECKBOX);
	addComponent(panel, "channel.splitusermodes", "Split user modes: ",
		OptionType.CHECKBOX);
	addComponent(panel, "ui.quickCopy", "Quick Copy: ",
		OptionType.CHECKBOX);
        addComponent(panel, "ui.pasteProtectionLimit", "Paste protection trigger: ",
		OptionType.TEXTFIELD);
	
	layoutGrid(panel, 8, 2, SMALL_BORDER, SMALL_BORDER,
		LARGE_BORDER, LARGE_BORDER);
	
	cardLayoutPanel.add(panel, "GUI");
	((DefaultListModel) tabList.getModel()).addElement("GUI");
    }
    
    /**
     * Initialises the TreeView tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initTreeViewTab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	
	addComponent(panel, "ui.rolloverEnabled", "Rollover enabled: ",
		OptionType.CHECKBOX);
	addComponent(panel, "ui.rolloverColour", "Rollover colour: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "ui.sortwindows", "Sort windows: ",
		OptionType.CHECKBOX);
	addComponent(panel, "ui.sortservers", "Sort servers: ",
		OptionType.CHECKBOX);
	
	layoutGrid(panel, 4, 2, SMALL_BORDER, SMALL_BORDER,
		LARGE_BORDER, LARGE_BORDER);
	
	cardLayoutPanel.add(panel, "Treeview");
	((DefaultListModel) tabList.getModel()).addElement("Treeview");
	
    }
    
    /**
     * Initialises the Notifications tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initNotificationsTab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	final String[] windowOptions
		= new String[] {"all", "active", "server", };
	
	addComponent(panel, "notifications.socketClosed", "Socket closed: ",
		OptionType.COMBOBOX, windowOptions, false);
	addComponent(panel, "notifications.privateNotice", "Private notice: ",
		OptionType.COMBOBOX, windowOptions, false);
	addComponent(panel, "notifications.privateCTCP", "CTCP request: ",
		OptionType.COMBOBOX, windowOptions, false);
	addComponent(panel, "notifications.privateCTCPreply", "CTCP reply: ",
		OptionType.COMBOBOX, windowOptions, false);
	
	layoutGrid(panel, 4, 2, SMALL_BORDER, SMALL_BORDER,
		LARGE_BORDER, LARGE_BORDER);
	
	cardLayoutPanel.add(panel, "Notifications");
	((DefaultListModel) tabList.getModel()).addElement("Notifications");
    }
    
    /**
     * Initialises the input tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initInputTab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	
	addComponent(panel, "general.commandchar", "Command character: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "tabcompletion.casesensitive",
		"Case-sensitive tab completion: ", OptionType.CHECKBOX);
	
	layoutGrid(panel, 2, 2, SMALL_BORDER, SMALL_BORDER,
		LARGE_BORDER, LARGE_BORDER);
	
	cardLayoutPanel.add(panel, "Input");
	((DefaultListModel) tabList.getModel()).addElement("Input");
    }
    
    /**
     * Initialises the logging tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initLoggingTab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	
	addComponent(panel, "logging.dateFormat", "Date format: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "logging.programLogging", "Program logs: ",
		OptionType.CHECKBOX);
	addComponent(panel, "logging.debugLogging", "Debug logs: ",
		OptionType.CHECKBOX);
	addComponent(panel, "logging.debugLoggingSysOut",
		"Debug console output: ", OptionType.CHECKBOX);
	
	layoutGrid(panel, 4, 2, SMALL_BORDER, SMALL_BORDER,
		LARGE_BORDER, LARGE_BORDER);
	
	cardLayoutPanel.add(panel, "Logging");
	((DefaultListModel) tabList.getModel()).addElement("Logging");
    }
    
    /**
     * Initialises the identities tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initIdentitiesTab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	
	cardLayoutPanel.add(panel, "Identities");
	((DefaultListModel) tabList.getModel()).addElement("Identities");
    }
    
    /**
     * Initialises the advanced tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initAdvancedTab(final JPanel cardLayoutPanel) {
	final JPanel panel = new JPanel(new SpringLayout());
	
	final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
	final String[] lafs = new String[plaf.length];
	int i = 0;
	for (LookAndFeelInfo laf : plaf) {
	    lafs[i++] = laf.getName();
	}
	
	addComponent(panel, "ui.lookandfeel", "Look and feel: ",
		OptionType.COMBOBOX, lafs, false);
	
	addComponent(panel, "ui.showversion", "Show version: ",
		OptionType.CHECKBOX);
	addComponent(panel, "ui.inputbuffersize", "Input bufer size: ",
		OptionType.TEXTFIELD);
	addComponent(panel, "general.browser", "Browser: ",
		OptionType.COMBOBOX, new String[]
	{"firefox", "konqueror", "epiphany", "opera", "mozilla", }, true);
	addComponent(panel, "ui.frameBufferSize", "Frame buffer size: ",
		OptionType.TEXTFIELD);
	
	layoutGrid(panel, 5, 2, SMALL_BORDER, SMALL_BORDER,
		LARGE_BORDER, LARGE_BORDER);
	
	cardLayoutPanel.add(panel, "Advanced");
	((DefaultListModel) tabList.getModel()).addElement("Advanced");
	
    }
    
    /**
     * Initialises listeners for this dialog.
     */
    private void initListeners() {
	getOkButton().addActionListener(this);
	getCancelButton().addActionListener(this);
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
	    /*if (!UIManager.getLookAndFeel().getName()
	    .equals((String) comboBoxes.get("ui.lookandfeel").getSelectedItem())) {
		setLookAndFeel((String) comboBoxes.get("ui.lookandfeel").getSelectedItem());
	    }*/
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
     * Sets the new look and feel for the program.
     * @param lookAndFeel classname of the look and feel to set
     */
    private void setLookAndFeel(final String lookAndFeel) {
	final StringBuilder classNameBuilder = new StringBuilder();
	for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if (laf.getName().equals(lookAndFeel)) {
		classNameBuilder.setLength(0);
		classNameBuilder.append(laf.getClassName());
		break;
	    }
	}
	final String className = classNameBuilder.toString();
	if (className != null) {
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    try {
			UIManager.setLookAndFeel(className);
			SwingUtilities.updateComponentTreeUI(
				MainFrame.getMainFrame());
		    } catch (UnsupportedLookAndFeelException ex) {
			Logger.error(ErrorLevel.WARNING,
				"Unable to select new look and feel.", ex);
		    } catch (IllegalAccessException ex) {
			Logger.error(ErrorLevel.WARNING,
				"Unable to select new look and feel.", ex);
		    } catch (InstantiationException ex) {
			Logger.error(ErrorLevel.WARNING,
				"Unable to select new look and feel.", ex);
		    } catch (ClassNotFoundException ex) {
			Logger.error(ErrorLevel.WARNING,
				"Unable to select new look and feel.", ex);
		    }
		}
	    });
	}
    }
    
    /**
     * Saves the options in the dialog to the config.
     */
    private void saveOptions() {
	String[] optionArgs;
	for (String option : textFields.keySet()) {
	    optionArgs = option.split("\\.");
	    if (textFields.get(option).getText().equals("")) {
		Config.unsetOption(optionArgs[0], optionArgs[1]);
	    } else {
		Config.setOption(optionArgs[0], optionArgs[1],
			textFields.get(option).getText());
	    }
	}
	for (String option : checkBoxes.keySet()) {
	    optionArgs = option.split("\\.");
	    Config.setOption(optionArgs[0], optionArgs[1],
		    "" + checkBoxes.get(option).isSelected());
	}
	for (String option : comboBoxes.keySet()) {
	    optionArgs = option.split("\\.");
	    if (comboBoxes.get(option).getSelectedItem() != null) {
		Config.setOption(optionArgs[0], optionArgs[1],
			(String) comboBoxes.get(option).getSelectedItem());
	    }
	}
	Config.save();
    }
}
