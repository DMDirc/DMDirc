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

package com.dmdirc.ui.swing.components;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.interfaces.PreferencesPanel.OptionType;
import com.dmdirc.ui.swing.MainFrame;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;

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
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Allows the user to modify global client preferences.
 */
public final class SwingPreferencesPanel extends StandardDialog implements
        ActionListener, TreeSelectionListener, PreferencesPanel {
    
    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;
    
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
    private transient PreferencesInterface owner;
    
    /** title of window. */
    private String windowTitle;
    
    /** root node. */
    private DefaultMutableTreeNode rootNode;
    
    /**
     * Creates a new instance of SwingPreferencesPanel.
     *
     * @param preferencesOwner Owner of the preferences dialog
     */
    public SwingPreferencesPanel(final PreferencesInterface preferencesOwner) {
        this(preferencesOwner, "Preferences");
    }
    
    /**
     * Creates a new instance of SwingPreferencesPanel.
     *
     * @param preferencesOwner Owner of the preferences dialog
     * @param title preferences dialog title
     */
    public SwingPreferencesPanel(final PreferencesInterface preferencesOwner,
            final String title) {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        
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
        
        initComponents();
        
        new TreeScroller(tabList);
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
        
        tabList.setPreferredSize(new Dimension(150, 550));
        tabList.setMinimumSize(new Dimension(150, 550));
        setMinimumSize(new Dimension(650, 600));
        setPreferredSize(new Dimension(650, 600));
        setMaximumSize(new Dimension(650, 600));
        
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
        if (helpText.isEmpty()) {
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
                if (args[0] instanceof String[]) {
                    option = new JComboBox((String[]) args[0]);
                    ((JComboBox) option).setSelectedItem(args[1]);
                } else {
                    System.out.println(args[1]);
                    final DefaultComboBoxModel model = (DefaultComboBoxModel) args[0];
                    option = new JComboBox(model);
                    ((JComboBox) option).setRenderer((ListCellRenderer) args[3]);
                    for (int i = 0; i < model.getSize(); i++) {
                        final Object entry = model.getElementAt(i);
                        if (((Entry) entry).getValue().equals(args[1])) {
                            System.out.println("match");
                            ((JComboBox) option).setSelectedItem(entry);
                            break;
                        }
                    }
                }
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
                        Logger.userError(ErrorLevel.LOW, "Default value incorrect");
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
    
    /** {@inheritDoc} */
    public void addCategory(final String name, final String blurb) {
        addCategory("", name, blurb);
    }
    
    /** {@inheritDoc} */
    public void addCategory(final String parentCategory, final String name,
            final String blurb) {
        final JPanel panel = new JPanel(new BorderLayout(SMALL_BORDER,
                LARGE_BORDER));
        
        DefaultMutableTreeNode parent;
        DefaultMutableTreeNode newNode;
        
        newNode = new DefaultMutableTreeNode(name);
        
        if (parentCategory.isEmpty()) {
            parent = rootNode;
        } else {
            parent = (DefaultMutableTreeNode) tabList.getNextMatch(
                    parentCategory, 0, Position.Bias.Forward)
                    .getLastPathComponent();
        }
        
        categories.put(name, panel);
        mainPanel.add(panel, name);
        ((DefaultTreeModel) tabList.getModel()).insertNodeInto(newNode, parent,
                parent.getChildCount());
        tabList.scrollPathToVisible(new TreePath(newNode.getPath()));
        
        final TextLabel infoLabel = new TextLabel(blurb, this);
        if (blurb.isEmpty()) {
            infoLabel.setVisible(false);
        }
        
        panel.add(infoLabel, BorderLayout.PAGE_START);
        panel.add(new JPanel(new SpringLayout()), BorderLayout.CENTER);
    }
    
    /** {@inheritDoc} */
    public void replaceOptionPanel(final String category, final JPanel panel) {
        panels.add(panel);
        categories.get(category).add(panel, 1);
    }
    
    /** {@inheritDoc} */
    public void addTextfieldOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.TEXTFIELD, defaultValue);
    }
    
    /** {@inheritDoc} */
    public void addCheckboxOption(final String category, final String name,
            final String displayName, final String helpText,
            final boolean defaultValue) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.CHECKBOX, defaultValue);
    }
    
    /** {@inheritDoc} */
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
     * @param options Combo box model
     * @param renderer Combo box renderer
     * @param defaultValue default value
     * @param editable editable combo box
     */
    public void addComboboxOption(final String category, final String name,
            final String displayName, final String helpText,
            final DefaultComboBoxModel options, final ListCellRenderer renderer,
            final String defaultValue, final boolean editable) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.COMBOBOX, options, defaultValue, editable, renderer);
    }
    
    /** {@inheritDoc} */
    public void addSpinnerOption(final String category, final String name,
            final String displayName, final String helpText,
            final int defaultValue) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.SPINNER, defaultValue);
    }
    
    /** {@inheritDoc} */
    public void addSpinnerOption(final String category, final String name,
            final String displayName, final String helpText,
            final int defaultValue, final int minimum, final int maximum,
            final int stepSize) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.SPINNER, defaultValue, minimum, maximum, stepSize);
    }
    
    /** {@inheritDoc} */
    public void addColourOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue, final boolean showIrcColours,
            final boolean showHexColours) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.COLOUR, defaultValue, showIrcColours, showHexColours);
    }
    
    /** {@inheritDoc} */
    public void addOptionalColourOption(final String category, final String name,
            final String displayName, final String helpText,
            final String defaultValue, final boolean initialState,
            final boolean showIrcColours, final boolean showHexColours) {
        addComponent(categories.get(category), name, displayName, helpText,
                OptionType.OPTIONALCOLOUR, defaultValue, initialState,
                showIrcColours, showHexColours);
    }
    
    /** {@inheritDoc} */
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
            if (tabList.getSelectionPath() != null) {
                final String node = tabList.getSelectionPath().toString();
                IdentityManager.getConfigIdentity().setOption("dialogstate",
                        owner.getClass().getName().replaceAll("\\.", "-"),
                        node.substring(7, node.length() - 1).
                        replaceAll(", ", "->"));
            }
            saveOptions();
            dispose();
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            dispose();
            owner.configCancelled();
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
    
    /** {@inheritDoc} */
    public void saveOptions() {
        final Properties properties = new Properties();
        for (String option : textFields.keySet()) {
            properties.setProperty(option, textFields.get(option).getText());
        }
        for (String option : checkBoxes.keySet()) {
            properties.setProperty(option, ""
                    + checkBoxes.get(option).isSelected());
        }
        for (String option : comboBoxes.keySet()) {
            if (comboBoxes.get(option).getSelectedItem() != null) {
                if (comboBoxes.get(option).getSelectedItem() instanceof String) {
                    properties.setProperty(option, (String) comboBoxes.get(option)
                    .getSelectedItem());
                } else {
                    properties.setProperty(option, (String) ((Entry) comboBoxes.get(option)
                    .getSelectedItem()).getValue());
                }
            }
        }
        for (String option : spinners.keySet()) {
            properties.setProperty(option, ""
                    + spinners.get(option).getValue().toString());
        }
        for (String option : colours.keySet()) {
            properties
                    .setProperty(option, colours.get(option).getColour());
        }
        for (String option : optionalColours.keySet()) {
            final OptionalColourChooser colour = optionalColours.get(option);
            if (colour.isEnabled()) {
                properties.setProperty(option, colour.getColour());
            } else {
                properties.setProperty(option, "");
            }
        }
        owner.configClosed(properties);
    }
    
    /** {@inheritDoc} */
    public void display() {
        for (JPanel panel : categories.values()) {
            if (!panels.contains((JPanel) panel.getComponent(1))) {
                layoutGrid((JPanel) panel.getComponent(1), ((JPanel) panel
                        .getComponent(1)).getComponentCount() / 2, 2, SMALL_BORDER,
                        SMALL_BORDER, LARGE_BORDER, LARGE_BORDER);
            }
        }
        
        final String[] tabName = IdentityManager.getGlobalConfig().
                getOption("dialogstate", owner.getClass().getName().
                replaceAll("\\.", "-"), "").split("->");
        TreePath path = new TreePath(tabList.getModel().getRoot());
        
        for (String string : tabName) {
            final TreePath treePath = tabList.getNextMatch(string, 0,
                    Position.Bias.Forward);
            if (treePath != null) {
                final TreeNode node = (TreeNode) treePath.getLastPathComponent();
                if (node != null) {
                    path = path.pathByAddingChild(node);
                }
            }
        }
        
        if (path == null || path.getPathCount() <= 1) {
            tabList.setSelectionPath(tabList.getPathForRow(0));
        } else {
            tabList.setSelectionPath(path);
        }
        pack();
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        setVisible(true);
    }
    
    /**
     * Preferences tree cell renderer.
     */
    private class PreferencesTreeCellRenderer extends JLabel implements TreeCellRenderer {
        
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
        public final Component getTreeCellRendererComponent(final JTree tree,
                final Object value, final boolean sel, final boolean expanded,
                final boolean leaf, final int row, final boolean focused) {
            
            setText(value.toString());
            setBackground(tree.getBackground());
            setForeground(tree.getForeground());
            setOpaque(true);
            setToolTipText(null);
            setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                    SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
            
            setPreferredSize(new Dimension((int) tabList.getPreferredSize().getWidth() - 20,
                    getFont().getSize() + SMALL_BORDER));
            
            if (sel) {
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            return this;
        }
    }
    
}
