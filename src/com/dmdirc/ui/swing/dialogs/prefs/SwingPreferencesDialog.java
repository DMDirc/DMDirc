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
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.PrefsComponentFactory;
import com.dmdirc.ui.swing.components.ColourChooser;
import com.dmdirc.ui.swing.components.ListScroller;
import com.dmdirc.ui.swing.components.OptionalColourChooser;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.TextLabel;
import com.dmdirc.ui.swing.components.durationeditor.DurationDisplay;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify global client preferences.
 */
public final class SwingPreferencesDialog extends StandardDialog implements
        ActionListener, ListSelectionListener, MouseListener {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    /** Normal help string text. */
    private static final String text = "Hover over a setting to see a " +
            "description, if available.";

    /**
     * The maximum height clients may use if they don't want to scroll.
     *
     * @since 0.6.3
     */
    public static int CLIENT_HEIGHT = 430;

    /** Previously instantiated instance of SwingPreferencesDialog. */
    private static volatile SwingPreferencesDialog me;
    /** A map of settings to the components used to represent them. */
    private final Map<PreferencesSetting, JComponent> components;
    /** Categories in the dialog. */
    private final Map<PreferencesCategory, JPanel> categories;
    /** Paths to categories. */
    private final Map<String, PreferencesCategory> paths;
    /** Custom panels, not to be laid out automatically. */
    private final List<JPanel> panels;
    /** Preferences tab list, used to switch option types. */
    private JList tabList;
    /** Main card layout. */
    private CardLayout cardLayout;
    /** Main panel. */
    private JPanel mainPanel;
    /** Title label. */
    private JLabel title;
    /** Tooltip display area. */
    private TextLabel tooltip;
    /** Cached tooltips. */
    private final Map<Component, String> tooltips = new HashMap<Component, String>();
    /** Previously selected category. */
    private PreferencesCategory selected;
    /** Preferences Manager. */
    private final PreferencesManager manager;
    /** Panel gap. */
    private final int padding = (int) PlatformDefaults.getUnitValueX("related").
            getValue();
    /** Panel left padding. */
    private final int leftPadding = (int) PlatformDefaults.getPanelInsets(1).getValue();
    /** Panel right padding. */
    private final int rightPadding = (int) PlatformDefaults.getPanelInsets(3).getValue();

    /**
     * Creates a new instance of SwingPreferencesDialog.
     */
    private SwingPreferencesDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        manager = new PreferencesManager();

        categories = new HashMap<PreferencesCategory, JPanel>();
        components = new HashMap<PreferencesSetting, JComponent>();
        paths = new HashMap<String, PreferencesCategory>();

        panels = new ArrayList<JPanel>();

        initComponents();

        new ListScroller(tabList);

        addCategories(manager.getCategories());
        addMouseListeners(mainPanel.getComponents());
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

        final JPanel titlePanel = new JPanel(new MigLayout());

        title = new JLabel("Preferences");
        title.setFont(title.getFont().deriveFont((float) 18));

        titlePanel.add(title, "growx, growy");
        titlePanel.setOpaque(true);
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEtchedBorder());

        tooltip = new TextLabel("");
        resetTooltip();

        final JPanel tooltipPanel = new JPanel(new MigLayout());
        tooltipPanel.add(tooltip, "growx, growy");
        tooltipPanel.setOpaque(true);
        tooltipPanel.setBackground(Color.WHITE);
        tooltipPanel.setBorder(BorderFactory.createEtchedBorder());

        tabList = new JList(new DefaultListModel());
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.setCellRenderer(new PreferencesListCellRenderer());
        tabList.addListSelectionListener(this);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");
        setResizable(false);

        tabList.setBorder(BorderFactory.createEtchedBorder());

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        
        setLayout(new MigLayout("fillx, wmax 650, hmax 600"));
        add(tabList, "w 150!, growy, spany 3");
        add(titlePanel, "wrap, w 480!");
        add(mainPanel, "wrap, w 480!, pushy, h " + (CLIENT_HEIGHT + 20) + "!");
        add(tooltipPanel, "wrap, w 480!, h 65!");
        add(getLeftButton(), "span, split, right");
        add(getRightButton(), "right");
    }

    /**
     * Resets the content of the tooltip.
     *
     * @since 0.6.3
     */
    protected void resetTooltip() {
        tooltip.setText(text);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setItalic(sas, true);
        tooltip.getDocument().setParagraphAttributes(0, text.length(), sas, true);
    }

    /**
     * Sets the content of the tooltip area to the specified text.
     *
     * @param text The text to be displayed
     * @since 0.6.3
     */
    protected void setTooltip(final String text) {
        tooltip.setText(text);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setItalic(sas, false);
        tooltip.getDocument().setParagraphAttributes(0, text.length(), sas, true);
    }

    /**
     * Iterates and recurses over the specified components, adding mouse
     * listeners to any {@link JComponent} with a tooltip set.
     *
     * @param components The components to iterate over
     * @since 0.6.3
     */
    protected void addMouseListeners(final Component[] components) {
        for (Component component : components) {
            if (component instanceof JComponent && ((JComponent) component).getToolTipText() != null) {
                tooltips.put(component, ((JComponent) component).getToolTipText());
                ((JComponent) component).setToolTipText(null);

                component.addMouseListener(this);
            }

            if (component instanceof Container) {
                addMouseListeners(((Container) component).getComponents());
            }
        }
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
        option.setToolTipText(setting.getHelptext());

        if (option instanceof DurationDisplay) {
            ((DurationDisplay) option).setWindow(this);
        } else if (option instanceof ColourChooser) {
            ((ColourChooser) option).setWindow(this);
        } else if (option instanceof OptionalColourChooser) {
            ((OptionalColourChooser) option).setWindow(this);
        }
        components.put(setting, option);

        categories.get(category).add(label, "align label, growx");

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
                new JPanel(new MigLayout("fillx, gap unrel, wrap 2, hidemode 3, pack, " +
                "wmax 480-" + leftPadding + "-" + rightPadding + "-2*" + padding));
        panel.setBorder(BorderFactory.createTitledBorder(category.getTitle()));

        categories.put(category, panel);

        parent.add(panel, "span, growx, wrap");

        initCategory(category, panel, "");
    }

    /**
     * Adds the specified category to the preferences dialog.
     *
     * @since 0.6.3
     * @param category The category to be added
     * @param namePrefix Category name prefix
     */
    private void addCategory(final PreferencesCategory category, final String namePrefix) {
        final JPanel panel =
                new JPanel(new MigLayout("fillx, gap unrel, wrap 2, hidemode 3, " +
                "wmax 480-" + leftPadding + "-" + rightPadding + "-2*" + padding));
        final String path = category.getPath();

        categories.put(category, panel);
        paths.put(path, category);

        final JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane, path);
        ((DefaultListModel) tabList.getModel()).addElement(category);
        tabList.setSelectedValue(category, true);

        initCategory(category, panel, path);
    }

    /**
     * Initialises the specified category.
     *
     * @since 0.6.3
     * @param category The category that is being initialised
     * @param panel The panel to which we're adding its contents
     * @param path The textual path of this category
     */
    private void initCategory(final PreferencesCategory category,
            final JPanel panel, final String path) {

        if (!category.getDescription().isEmpty()) {
            panel.add(new TextLabel(category.getDescription()), "span, growx");
        }

        for (PreferencesCategory child : category.getSubcats()) {
            if (child.isInline() && category.isInlineBefore()) {
                addInlineCategory(child, panel);
            } else if (!child.isInline()) {
                addCategory(child, path);
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
            addCategory(category, "");
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
            if (tabList.getSelectedIndex() > -1) {
                final PreferencesCategory node = (PreferencesCategory) tabList.getSelectedValue();
                //IdentityManager.getConfigIdentity().setOption("dialogstate",
                  //      "preferences", node.substring(7, node.length() - 1).
                    //    replaceAll(", ", "->"));
            }
            saveOptions();
        }
        dispose();
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.6.3
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        final PreferencesCategory node = (PreferencesCategory) tabList.getSelectedValue();
        cardLayout.show(mainPanel, node.getPath());

        if (selected != null) {
            selected.fireCategoryDeselected();
        }

        selected = node;
        selected.fireCategorySelected();
        title.setText(selected.getPath());
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
        /*final String[] tabName = IdentityManager.getGlobalConfig().
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
        }*/
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            manager.close();
            super.dispose();
            me = null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @param e Mouse event
     *
     * @since 0.6.3
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        // Not used
    }

    /**
     * {@inheritDoc}
     * 
     * @param e Mouse event
     *
     * @since 0.6.3
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        // Not used
    }

    /**
     * {@inheritDoc}
     * 
     * @param e Mouse event
     *
     * @since 0.6.3
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // Not used
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     * 
     * @since 0.6.3
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        setTooltip(tooltips.get(e.getComponent()));
    }

    /**
     * {@inheritDoc}
     * 
     * @param e Mouse event
     *
     * @since 0.6.3
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        resetTooltip();
    }

}
