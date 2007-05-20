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

package com.dmdirc.ui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dmdirc.Config;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.MainFrame;

import static com.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesPanel extends StandardDialog implements
        ActionListener, TreeSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 7;
    
    /** Acceptable input types for the config dialog. */
    public static enum OptionType {
        /** JTextfield. */
        TEXTFIELD,
        /** JCheckBox. */
        CHECKBOX,
        /** JComboBox. */
        COMBOBOX,
        /** JSpinner. */
        SPINNER,
        /** ColourChooser. */
        COLOUR,
        /** OptionalColourChooser. */
        OPTIONALCOLOUR,
        /** JPanel. */
        PANEL,
    };
    
    /** All text fields in the dialog, used to apply settings. */
    private final Map<String, JTextField> textFields;
    
    /** All checkboxes in the dialog, used to apply settings. */
    private final Map<String, JCheckBox> checkBoxes;
    
    /** All combo boxes in the dialog, used to apply settings. */
    private final Map<String, JComboBox> comboBoxes;
    
    /** All spinners in the dialog, used to apply settings. */
    private final Map<String, JSpinner> spinners;
    
    /** All colours in the dialog, used to apply settings. */
    private final Map<String, ColourChooser> colours;
    
    /** All optional colours in the dialog, used to apply settings. */
    private final Map<String, OptionalColourChooser> optionalColours;
    
    /** Categories in the dialog. */
    private final Map<String, JPanel> categories;
    
    /** Custom panels, not to be laid out automatically. */
    private final List<JPanel> panels;
    
    /** Preferences tab list, used to switch option types. */
    private JTree tabList;
    
    /** Main card layout. */
    private CardLayout cardLayout;
    
    /** Main panel. */
    private JPanel mainPanel;
    
    /** Preferences owner. */
    private PreferencesInterface owner;
    
    /** title of window. */
    private String windowTitle;
    
    /** root node. */
    private DefaultMutableTreeNode rootNode;
    
    /** Node icons. */
    private Map<DefaultMutableTreeNode, Icon> nodeIcons;
    
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
     * @param title preferences dialog title
     */
    public PreferencesPanel(final PreferencesInterface preferencesOwner,
            final String title) {
        super(MainFrame.getMainFrame(), false);
        
        windowTitle = title;
        
        owner = preferencesOwner;
        
        categories = new HashMap<String, JPanel>();
        
        textFields = new HashMap<String, JTextField>();
        checkBoxes = new HashMap<String, JCheckBox>();
        comboBoxes = new HashMap<String, JComboBox>();
        spinners = new HashMap<String, JSpinner>();
        colours = new HashMap<String, ColourChooser>();
        optionalColours = new HashMap<String, OptionalColourChooser>();
        
        panels = new ArrayList<JPanel>();
        
        nodeIcons = new HashMap<DefaultMutableTreeNode, Icon>();
        
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
        
        rootNode = new DefaultMutableTreeNode("root");
        
        tabList = new JTree(new DefaultTreeModel(rootNode));
        tabList.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        tabList.putClientProperty("JTree.lineStyle", "Angled");
        tabList.setUI(new javax.swing.plaf.metal.MetalTreeUI());
        tabList.setRootVisible(false);
        tabList.setShowsRootHandles(false);
        tabList.setCellRenderer(new PreferencesTreeCellRenderer());
        tabList.addTreeSelectionListener(this);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle(windowTitle);
        setResizable(false);
        
        mainPanel.setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER,
                LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
        tabList.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEtchedBorder(), BorderFactory.createEmptyBorder(
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER)));
        
        getContentPane().setLayout(layout);
        
        tabList.setPreferredSize(new Dimension(150, 450));
        tabList.setMinimumSize(new Dimension(150, 450));
        setMinimumSize(new Dimension(650, 500));
        setPreferredSize(new Dimension(650, 500));
        setMaximumSize(new Dimension(650, 500));
        
        orderButtons(button1, button2);
        
        getContentPane().add(tabList);
        
        getContentPane().add(mainPanel);
        
        getContentPane().add(Box.createHorizontalGlue());
        
        getContentPane().add(button1);
        
        getContentPane().add(button2);
        
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        
        // tab list
        layout.putConstraint(SpringLayout.WEST, tabList, LARGE_BORDER,
                SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, tabList, LARGE_BORDER,
                SpringLayout.NORTH, getContentPane());
        // main panel
        layout.putConstraint(SpringLayout.WEST, mainPanel, SMALL_BORDER,
                SpringLayout.EAST, tabList);
        layout.putConstraint(SpringLayout.NORTH, mainPanel, SMALL_BORDER,
                SpringLayout.NORTH, getContentPane());
        // ok button
        layout.putConstraint(SpringLayout.EAST, getRightButton(), -LARGE_BORDER,
                SpringLayout.EAST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, getRightButton(), SMALL_BORDER,
                SpringLayout.SOUTH, mainPanel);
        // cancel button
        layout.putConstraint(SpringLayout.EAST, getLeftButton(), -LARGE_BORDER,
                SpringLayout.WEST, getRightButton());
        layout.putConstraint(SpringLayout.NORTH, getLeftButton(), SMALL_BORDER,
                SpringLayout.SOUTH, mainPanel);
        // panel size
        layout.putConstraint(SpringLayout.EAST, getContentPane(), LARGE_BORDER,
                SpringLayout.EAST, mainPanel);
        layout.putConstraint(SpringLayout.SOUTH, getContentPane(), LARGE_BORDER,
                SpringLayout.SOUTH, getRightButton());
    }
    
    /**
     * Initialises and adds a component to a panel.
     *
     * @param parent parent panel
     * @param optionName option name to use
     * @param title option display name
     * @param helpText help text for the option
     * @param type the type of component to use
     * @param args component specific arguments<p>
     * <ul>
     * <li>TEXTFIELD takes a String as its default value</li>
     * <li>CHECKBOX takes a boolean as its default state</li>
     * <li>COMBOBOX takes a String[] of options, a String
     *   selected option and a boolean to specify if its editable</li>
     * <li>SPINNER takes an Integer value to specify the default value or
     *   4 Integers, default, minimum maximum, step size</li>
     * <li>COLOUR takes a String as the initial value, and
     *   two booleans, one to show irc colours, one to show hex colours</li>
     * <li>OPTIONALCOLOUR takes a String as the initial value, and
     *   three booleans, one to set the initial state (enabled or disabled),
     *   one to show irc colours, one to show hex colours</li>
     * <li>TABLE takes a table model param</li>
     * </ul>
     */
    private void addComponent(final JPanel parent, final String optionName,
            final String title, final String helpText, final OptionType type,
            final Object... args) {
        final JLabel label = new JLabel(title, JLabel.TRAILING);
        if ("".equals(helpText)) {
            label.setToolTipText("No help available.");
        } else {
            label.setToolTipText(helpText);
        }
        
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
                ((JCheckBox) option).setSelected((Boolean) args[0]);
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
                        Logger.error(ErrorLevel.TRIVIAL, "Default value incorrect",
                                ex);
                    }
                } else {
                    option = new JSpinner(
                            new SpinnerNumberModel((Integer) args[0],
                            (Integer) args[1], (Integer) args[2],
                            (Integer) args[3]));
                }
                spinners.put(optionName, (JSpinner) option);
                break;
            case COLOUR:
                option = new ColourChooser((String) args[0], (Boolean) args[1],
                        (Boolean) args[2]);
                colours.put(optionName, (ColourChooser) option);
                break;
            case OPTIONALCOLOUR:
                option = new OptionalColourChooser((String) args[0], (Boolean) args[1],
                        (Boolean) args[2], (Boolean) args[3]);
                optionalColours.put(optionName, (OptionalColourChooser) option);
                break;
            case PANEL:
                option = (JComponent) args[0];
                break;
            default:
                throw new IllegalArgumentException(type + " is not a valid option");
        }
        option.setPreferredSize(new Dimension(Short.MAX_VALUE, option.getFont()
        .getSize()));
        label.setLabelFor(option);
        ((JPanel) parent.getComponent(1)).add(option);
    }
    
    /**
     * Adds a named category to the preferences panel.
     *
     * @param name Category name
     * @param blurb category blurb
     */
    public void addCategory(final String name, final String blurb) {
        addCategory("", name, blurb, null);
    }
    
    /**
     * Adds a named category to the preferences panel.
     *
     * @param parentCategory parent category
     * @param name Category name
     * @param blurb category blurb
     */
    public void addCategory(final String parentCategory, final String name,
            final String blurb) {
        addCategory(parentCategory, name, blurb, null);
    }
    
    /**
     * Adds a named category to the preferences panel.
     *
     * @param parentCategory parent category
     * @param name Category name
     * @param blurb category blurb
     * @param nodeIcon icon for the node
     */
    public void addCategory(final String parentCategory, final String name,
            final String blurb, final Icon nodeIcon) {
        final JPanel panel = new JPanel(new BorderLayout(SMALL_BORDER,
                LARGE_BORDER));
        
        DefaultMutableTreeNode parent;
        DefaultMutableTreeNode newNode;
        
        newNode = new DefaultMutableTreeNode(name);
        
        if ("".equals(parentCategory)) {
            parent = rootNode;
        } else {
            parent = (DefaultMutableTreeNode) tabList.getNextMatch(
                    parentCategory, 0, Position.Bias.Forward)
                    .getLastPathComponent();
        }
        
        nodeIcons.put(newNode, nodeIcon);
        
        categories.put(name, panel);
        mainPanel.add(panel, name);
        ((DefaultTreeModel) tabList.getModel()).insertNodeInto(newNode, parent,
                parent.getChildCount());
        tabList.scrollPathToVisible(new TreePath(newNode.getPath()));
        
        final JTextArea infoLabel = new JTextArea(blurb);
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
    
    /**
     * Replaces the option panel in a category with the specified panel, this
     * panel will be be automatically laid out.
     *
     * @param category category to replace the options panel in
     * @param panel panel to replace options panel with
     */
    public void replaceOptionPanel(final String category, final JPanel panel) {
        panels.add(panel);
        categories.get(category).add(panel, 1);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     */
    public void addTextfieldOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.TEXTFIELD, defaultValue);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     */
    public void addCheckboxOption(final String category, final String name,
            final String displayName, final String helpText,
            final boolean defaultValue) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.CHECKBOX, defaultValue);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param options Default combo box options
     * @param defaultValue default value
     * @param editable editable combo box
     */
    public void addComboboxOption(final String category, final String name,
            final String displayName, final String helpText,
            final String[] options, final String defaultValue,
            final boolean editable) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.COMBOBOX, options, defaultValue, editable);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     */
    public void addSpinnerOption(final String category, final String name,
            final String displayName, final String helpText,
            final int defaultValue) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.SPINNER, defaultValue);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @param minimum minimum value
     * @param maximum maximum value
     * @param stepSize step size interval
     */
    public void addSpinnerOption(final String category, final String name,
            final String displayName, final String helpText,
            final int defaultValue, final int minimum, final int maximum,
            final int stepSize) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.SPINNER, defaultValue, minimum, maximum, stepSize);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @param showIrcColours show irc colours in the colour picker
     * @param showHexColours show hex colours in the colour picker
     */
    public void addColourOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue, final boolean showIrcColours,
            final boolean showHexColours) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.COLOUR, defaultValue, showIrcColours, showHexColours);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param defaultValue default value
     * @param initialState initial state
     * @param showIrcColours show irc colours in the colour picker
     * @param showHexColours show hex colours in the colour picker
     */
    public void addOptionalColourOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue, final boolean initialState,
            final boolean showIrcColours, final boolean showHexColours) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.OPTIONALCOLOUR, defaultValue, initialState, 
                showIrcColours, showHexColours);
    }
    
    /**
     * Adds an option to the specified category.
     *
     * @param category category option is to be added to
     * @param name config name for the option
     * @param displayName displayable name for the option
     * @param helpText Help text to be displayed for the option
     * @param panel panel to add
     */
    public void addPanelOption(final String category, final String name,
            final String displayName, final String helpText,
            final JPanel panel) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.PANEL, panel);
    }
    
    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            Config.setOption("dialogstate", owner.getClass().getName(),
                    tabList.getSelectionPath().getLastPathComponent().toString());
            saveOptions();
            dispose();
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            dispose();
        }
    }
    
    /**
     * Called when the selection in the tree changes.
     *
     * @param selectionEvent list selection event
     */
    public void valueChanged(final TreeSelectionEvent selectionEvent) {
        cardLayout.show(mainPanel, ((JTree) selectionEvent.getSource())
        .getSelectionPath().getLastPathComponent().toString());
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
                properties
                        .setProperty(option, textFields.get(option).getText());
            }
        }
        for (String option : checkBoxes.keySet()) {
            properties.setProperty(option, ""
                    + checkBoxes.get(option).isSelected());
        }
        for (String option : comboBoxes.keySet()) {
            if (comboBoxes.get(option).getSelectedItem() != null) {
                properties.setProperty(option, (String) comboBoxes.get(option)
                .getSelectedItem());
            }
        }
        for (String option : spinners.keySet()) {
            properties.setProperty(option, ""
                    + spinners.get(option).getValue().toString());
        }
        for (String option : colours.keySet()) {
            properties
                    .setProperty(option, "" + colours.get(option).getColour());
        }
        owner.configClosed(properties);
    }
    
    /**
     * Lays out the preferences panel and displays it.
     */
    public void display() {
        for (JPanel panel : categories.values()) {
            if (!panels.contains((JPanel) panel.getComponent(1))) {
                layoutGrid((JPanel) panel.getComponent(1), ((JPanel) panel
                        .getComponent(1)).getComponentCount() / 2, 2, SMALL_BORDER,
                        SMALL_BORDER, LARGE_BORDER, LARGE_BORDER);
            }
        }
        final String tabName = Config.getOption("dialogstate", owner.getClass().getName());
        
        if (tabName == null || "".equals(tabName)) {
            cardLayout.first(mainPanel);
            tabList.setSelectionPath(tabList.getPathForRow(0));
        } else {
            tabList.setSelectionPath(tabList.getNextMatch(
                    tabName, 0, Position.Bias.Forward));
        }
        pack();
        setLocationRelativeTo(MainFrame.getMainFrame());
        this.setVisible(true);
    }
    
    /**
     * Preferences tree cell renderer.
     */
    private class PreferencesTreeCellRenderer extends DefaultTreeCellRenderer {
        
        /**
         * A version number for this class. It should be changed whenever the class
         * structure is changed (or anything else that would prevent serialized
         * objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;
        
        /**
         * Creates a new instance of PreferencesTreeCellRenderer.
         */
        public PreferencesTreeCellRenderer() {
            super();
        }
        /**
         * Configures the renderer based on the passed parameters.
         * @param tree JTree for this renderer.
         * @param value node to be renderered.
         * @param sel whether the node is selected.
         * @param expanded whether the node is expanded.
         * @param leaf whether the node is a leaf.
         * @param row the node's row.
         * @param focused whether the node has focus.
         * @return RendererComponent for this node.
         */
        @Override
        public final Component getTreeCellRendererComponent(final JTree tree,
                final Object value, final boolean sel, final boolean expanded,
                final boolean leaf, final int row, final boolean focused) {
            
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focused);
            
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            
            setBackground(tree.getBackground());
            setForeground(tree.getForeground());
            setOpaque(true);
            setToolTipText(null);
            setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                    SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
            
            setPreferredSize(new Dimension((int) tabList.getPreferredSize().getWidth() - 20,
                    getFont().getSize() + SMALL_BORDER));
            
            if (selected) {
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            setIcon(nodeIcons.get(node));
            
            return this;
        }
    }
    
}
