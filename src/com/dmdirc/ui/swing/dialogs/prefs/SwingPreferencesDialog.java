/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.dialogs.prefs;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.PrefsComponentFactory;
import com.dmdirc.ui.swing.components.ColourChooser;
import com.dmdirc.ui.swing.components.OptionalColourChooser;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.TextLabel;
import com.dmdirc.ui.swing.components.TreeScroller;

import com.dmdirc.ui.swing.components.durationeditor.DurationDisplay;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify global client preferences.
 */
public final class SwingPreferencesDialog extends StandardDialog implements
        ActionListener, TreeSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;
    /** Previously instantiated instance of SwingPreferencesDialog. */
    private static volatile SwingPreferencesDialog me;
    /** A map of settings to the components used to represent them. */
    private final Map<PreferencesSetting, JComponent> components;
    /** Categories in the dialog. */
    private final Map<PreferencesCategory, JPanel> categories;
    /** Nodes in the treeview. */
    private final Map<TreeNode, String> nodes;
    /** Paths to categories. */
    private final Map<String, PreferencesCategory> paths;
    /** Custom panels, not to be laid out automatically. */
    private final List<JPanel> panels;
    /** Preferences tab list, used to switch option types. */
    private JTree tabList;
    /** Main card layout. */
    private CardLayout cardLayout;
    /** Main panel. */
    private JPanel mainPanel;
    /** root node. */
    private DefaultMutableTreeNode rootNode;
    /** Previously selected category. */
    private PreferencesCategory selected;
    /** Preferences Manager. */
    private final PreferencesManager manager;
    /** Panel gap. */
    private final int padding = (int) PlatformDefaults.getUnitValueX("related").
            getValue();

    /**
     * Creates a new instance of SwingPreferencesDialog.
     */
    private SwingPreferencesDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        manager = new PreferencesManager();

        categories = new HashMap<PreferencesCategory, JPanel>();
        components = new HashMap<PreferencesSetting, JComponent>();
        nodes = new HashMap<TreeNode, String>();
        paths = new HashMap<String, PreferencesCategory>();

        panels = new ArrayList<JPanel>();

        initComponents();

        new TreeScroller(tabList);

        addCategories(manager.getCategories());
    }
    
    /** Returns the instance of SwingPreferencesDialog. */
    public static void showSwingPreferencesDialog() {
        me = getSwingPreferencesDialog();

        me.display();
    }

    /**
     * Returns the current instance of the ErrorListDialog.
     *
     * @return The current PluginDErrorListDialogialog instance
     */
    public static SwingPreferencesDialog getSwingPreferencesDialog() {
        synchronized (SwingPreferencesDialog.class) {
            if (me == null) {
                me = new SwingPreferencesDialog();
            }
        }

        return me;
    }

    /**
     * Initialises GUI components.
     */
    private void initComponents() {
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
        setTitle("Preferences");
        setResizable(false);

        tabList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(padding, padding, padding,
                padding)));

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        setLayout(new MigLayout("fillx, wmin 650, wmax 650, hmax 600"));
        add(tabList, "width 150, growy, spany");
        add(mainPanel, "wrap, width 480, grow, hmax 550");
        add(getLeftButton(), "split, right");
        add(getRightButton(), "right");
    }

    /**
     * Initialises and adds a component to a panel.
     *
     * @param category The category the setting is being added to
     * @param setting The setting to be used
     */
    private void addComponent(final PreferencesCategory category,
            final PreferencesSetting setting) {

        final JLabel label = getLabel(setting);
        JComponent option = PrefsComponentFactory.getComponent(setting);
        if (option instanceof DurationDisplay) {
            ((DurationDisplay) option).setWindow(this);
        } else if (option instanceof ColourChooser) {
            ((ColourChooser) option).setWindow(this);
        } else if (option instanceof OptionalColourChooser) {
            ((OptionalColourChooser) option).setWindow(this);
        }
        components.put(setting, option);

        categories.get(category).add(label, "align label");

        label.setLabelFor(option);
        categories.get(category).add(option, "growx, w 70%");
    }

    /**
     * Retrieves the title label for the specified setting.
     *
     * @param setting The setting whose label is being requested
     * @return A JLabel with the appropriate text and tooltip
     */
    private JLabel getLabel(final PreferencesSetting setting) {
        final JLabel label = new JLabel(setting.getTitle() + ": ");

        if (setting.getHelptext().isEmpty()) {
            label.setToolTipText("No help available.");
        } else {
            label.setToolTipText(setting.getHelptext());
        }

        return label;
    }

    /**
     * Adds a new inline category.
     * 
     * @param category The category to be added
     * @param parent The panel to add the category to
     */
    private void addInlineCategory(final PreferencesCategory category,
            final JPanel parent) {
        final JPanel panel =
                new JPanel(new MigLayout("fillx, gap unrel, wrap 2, hidemode 3"));
        panel.setBorder(BorderFactory.createTitledBorder(category.getTitle()));

        categories.put(category, panel);

        parent.add(panel, "span, growx, wrap");

        initCategory(category, panel, null, "");
    }

    /**
     * Adds the specified category to the preferences dialog.
     *
     * @param category The category to be added
     * @param parentNode the parent node of the category
     * @param namePrefix Category name prefix
     */
    private void addCategory(final PreferencesCategory category,
            final DefaultMutableTreeNode parentNode, final String namePrefix) {
        final JPanel panel =
                new JPanel(new MigLayout("fillx, gap unrel, wrap 2, hidemode 3, pack"));
        final String path = namePrefix + "/" + category.getTitle();
        DefaultMutableTreeNode newNode;

        newNode = new DefaultMutableTreeNode(category.getTitle());

        categories.put(category, panel);
        nodes.put(newNode, path);
        paths.put(path, category);

        mainPanel.add(panel, path);
        ((DefaultTreeModel) tabList.getModel()).insertNodeInto(newNode,
                parentNode, parentNode.getChildCount());
        tabList.scrollPathToVisible(new TreePath(newNode.getPath()));

        initCategory(category, panel, newNode, path);
    }

    /**
     * Initialises the specified category.
     * 
     * @param category The category that is being initialised
     * @param panel The panel to which we're adding its contents
     * @param newNode The treenode that represents this category
     * @param path The textual path of this category
     */
    private void initCategory(final PreferencesCategory category,
            final JPanel panel, final DefaultMutableTreeNode newNode,
            final String path) {

        if (!category.getDescription().isEmpty()) {
            panel.add(new TextLabel(category.getDescription()), "span, growx");
        }

        for (PreferencesCategory child : category.getSubcats()) {
            if (child.isInline() && category.isInlineBefore()) {
                addInlineCategory(child, panel);
            } else if (!child.isInline()) {
                addCategory(child, newNode, path);
            }
        }

        if (category.hasObject()) {
            if (!(category.getObject() instanceof JPanel)) {
                throw new IllegalArgumentException("Custom preferences objects" +
                        " for this UI must extend JPanel.");
            }

            panels.add((JPanel) category.getObject());
            categories.get(category).add((JPanel) category.getObject(), "growx");

            return;
        }

        for (PreferencesSetting setting : category.getSettings()) {
            addComponent(category, setting);
        }

        if (!category.isInlineBefore()) {
            for (PreferencesCategory child : category.getSubcats()) {
                if (child.isInline()) {
                    addInlineCategory(child, panel);
                }
            }
        }
    }

    /**
     * Adds the specified categories to the preferences dialog.
     * 
     * @param categories The categories to be added
     */
    public void addCategories(final Collection<? extends PreferencesCategory> categories) {
        for (PreferencesCategory category : categories) {
            addCategory(category, rootNode, "");
        }
    }

    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (selected != null) {
            selected.fireCategoryDeselected();
        }

        if (getOkButton().equals(actionEvent.getSource())) {
            if (tabList.getSelectionPath() != null) {
                final String node = tabList.getSelectionPath().toString();
                IdentityManager.getConfigIdentity().setOption("dialogstate",
                        "preferences", node.substring(7, node.length() - 1).
                        replaceAll(", ", "->"));
            }
            saveOptions();
        }
        dispose();
    }

    /**
     * Called when the selection in the tree changes.
     *
     * @param selectionEvent list selection event
     */
    @Override
    public void valueChanged(final TreeSelectionEvent selectionEvent) {
        final String path = nodes.get(((JTree) selectionEvent.getSource()).getSelectionPath().
                getLastPathComponent());

        cardLayout.show(mainPanel, path);

        if (selected != null) {
            selected.fireCategoryDeselected();
        }

        selected = paths.get(path);
        selected.fireCategorySelected();
    }

    /** {@inheritDoc} */
    public void saveOptions() {
        manager.fireSaveListeners();

        boolean restart = false;

        for (PreferencesSetting setting : components.keySet()) {
            if (setting.save() && setting.isRestartNeeded()) {
                restart = true;
            }
        }

        if (restart) {
            JOptionPane.showMessageDialog((MainFrame) Main.getUI().
                    getMainWindow(), "One or more of the changes you made " +
                    "won't take effect until you restart the client.",
                    "Restart needed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** {@inheritDoc} */
    private void display() {
        final String[] tabName = IdentityManager.getGlobalConfig().
                getOption("dialogstate", "preferences", "").split("->");
        TreePath path = new TreePath(tabList.getModel().getRoot());

        for (String string : tabName) {
            final TreePath treePath = tabList.getNextMatch(string, 0,
                    Position.Bias.Forward);
            if (treePath != null) {
                final TreeNode node =
                        (TreeNode) treePath.getLastPathComponent();
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
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            manager.close();
            super.dispose();
            me = null;
        }
    }
}
