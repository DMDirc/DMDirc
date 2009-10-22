/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.ListScroller;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify global client preferences.
 */
public final class SwingPreferencesDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    /**
     * The maximum height clients may use if they don't want to scroll.
     *
     * @since 0.6.3m1
     */
    public static final int CLIENT_HEIGHT = 375;
    /** Previously instantiated instance of SwingPreferencesDialog. */
    private static volatile SwingPreferencesDialog me;
    /** Preferences tab list, used to switch option types. */
    private JList tabList;
    /** Main panel. */
    private CategoryPanel mainPanel;
    /** Previously selected category. */
    private PreferencesCategory selected;
    /** Preferences Manager. */
    private PreferencesManager manager;
    /** Manager loading swing worker. */
    private LoggingSwingWorker worker;
    /** Parent window. */
    private MainFrame parentWindow;

    /**
     * Creates a new instance of SwingPreferencesDialog.
     * 
     * @param parentWindow Parent window
     */
    private SwingPreferencesDialog(final MainFrame parentWindow) {
        super(parentWindow, ModalityType.MODELESS);

        this.parentWindow = parentWindow;

        initComponents();

        worker = new LoggingSwingWorker<PreferencesManager, Void>() {

            /** {@inheritDoc} */
            @Override
            protected PreferencesManager doInBackground() throws Exception {
                mainPanel.setWaiting(true);
                return new PreferencesManager();
            }

            /** {@inheritDoc} */
            @Override
            protected void done() {
                if (!isCancelled()) {
                    try {
                        setPrefsManager(get());
                    } catch (InterruptedException ex) {
                        //Ignore
                    } catch (ExecutionException ex) {
                        Logger.appError(ErrorLevel.MEDIUM, ex.getMessage(), ex);
                    }
                }
            }
        };
        worker.execute();
    }

    private void setPrefsManager(final PreferencesManager manager) {
        this.manager = manager;

        ((DefaultListModel) tabList.getModel()).clear();
        mainPanel.setCategory(null);

        final int count = countCategories(manager.getCategories());
        tabList.setCellRenderer(new PreferencesListCellRenderer(count));

        addCategories(manager.getCategories());
    }

    /** 
     * Returns the instance of SwingPreferencesDialog.
     * 
     * @param parentWindow Parent window
     */
    public static void showSwingPreferencesDialog(final MainFrame parentWindow) {
        me = getSwingPreferencesDialog(parentWindow);

        me.display();
    }

    /**
     * Returns the current instance of the ErrorListDialog.
     *
     * @param parentWindow Parent window
     * 
     * @return The current PluginDErrorListDialogialog instance
     */
    public static SwingPreferencesDialog getSwingPreferencesDialog(final MainFrame parentWindow) {
        synchronized (SwingPreferencesDialog.class) {
            if (me == null) {
                me = new SwingPreferencesDialog(parentWindow);
            }
        }

        return me;
    }

    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        mainPanel = new CategoryPanel(this);

        tabList = new JList(new DefaultListModel());
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.addListSelectionListener(this);
        new ListScroller(tabList);
        final JScrollPane tabListScrollPane = new JScrollPane(tabList);
        tabListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            /** {@inheritDoc} */
            @Override
            public void windowClosing(final WindowEvent e) {
                super.windowClosing(e);
                actionPerformed(null);
            }
        });
        setTitle("Preferences");
        setResizable(false);

        tabList.setBorder(BorderFactory.createEtchedBorder());

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        setLayout(new MigLayout("pack"));
        add(tabListScrollPane, "w 150!, hmax 545, growy, pushy");
        add(mainPanel, "wrap, w 480!, pushy, growy, pushy");
        add(getLeftButton(), "span, split, right");
        add(getRightButton(), "right");
    }

    /**
     * Adds the categories from the preferences manager, clearing existing
     * categories first.
     */
    private void addCategories(final List<PreferencesCategory> categories) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                tabList.removeListSelectionListener(SwingPreferencesDialog.this);
                for (PreferencesCategory category : categories) {
                    if (!category.isInline()) {
                        ((DefaultListModel) tabList.getModel()).addElement(
                                category);
                    }
                    addCategories(category.getSubcats());
                }
                tabList.addListSelectionListener(SwingPreferencesDialog.this);
                restoreActiveCategory();
            }
        });
        mainPanel.setWaiting(false);
    }

    /**
     * Counts the number of categories that will be displayed in the list panel.
     *
     * @param categories The collection of categories to inspect
     * @return The number of those categories (including children) that will be displayed
     * @since 0.6.3m1rc3
     */
    protected int countCategories(final Collection<PreferencesCategory> categories) {
        int count = 0;

        for (PreferencesCategory cat : categories) {
            if (!cat.isInline()) {
                count += 1 + countCategories(cat.getSubcats());
            }
        }

        return count;
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
            selected = null;
        }
        mainPanel.setCategory(null);
        
        if (actionEvent != null && getOkButton().equals(actionEvent.getSource())) {
            if (tabList.getSelectedIndex() > -1) {
                final PreferencesCategory node = (PreferencesCategory) tabList.getSelectedValue();
                IdentityManager.getConfigIdentity().setOption("dialogstate",
                        "preferences", node.getPath());
            }
            saveOptions();
        }
        
        if (manager != null) {
            manager.dismiss();
        }
        dispose();
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.6.3m1
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            PreferencesCategory node = null;
            try {
                node = (PreferencesCategory) tabList.getSelectedValue();
            } catch (ArrayIndexOutOfBoundsException ex) {
                //I hate the JVM
            }
            if (node == null) {
                tabList.setSelectedValue(selected, true);
                return;
            }

            if (node == selected) {
                return;
            }

            if (selected != null) {
                selected.fireCategoryDeselected();
            }
            final int index = tabList.getSelectedIndex();
            tabList.scrollRectToVisible(tabList.getCellBounds(index, index));
            selected = node;
            if (selected != null) {
                selected.fireCategorySelected();
            }
            mainPanel.setCategory(selected);
        }
    }

    /**
     * Returns the selected category.
     *
     * @return Selected category
     */
    protected PreferencesCategory getSelectedCategory() {
        return selected;
    }

    /** {@inheritDoc} */
    public void saveOptions() {
        if (manager != null) {
            if (manager.save()) {
                dispose();
                new SwingRestartDialog(parentWindow, 
                        ModalityType.APPLICATION_MODAL,
                        "apply settings").setVisible(true);
            }
        }
    }

    private void restoreActiveCategory() {
        final String oldCategoryPath = IdentityManager.getGlobalConfig().
                getOption("dialogstate", "preferences");
        final DefaultListModel model = (DefaultListModel) tabList.getModel();
        int indexToSelect = 0;
        for (int i = 0; i < model.getSize(); i++) {
            final PreferencesCategory category =
                    (PreferencesCategory) model.get(i);
            if (oldCategoryPath.equals(category.getPath())) {
                indexToSelect = i;
                break;
            }
        }
        tabList.setSelectedIndex(indexToSelect);
    }

    /** {@inheritDoc} */
    @Override
    public void display() {
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (SwingPreferencesDialog.this) {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
            if (manager != null) {
                manager.close();
            }
            if (me == null) {
                return;
            }
            super.dispose();
            me = null;
            worker = null;
            manager = null;
        }
    }
}
