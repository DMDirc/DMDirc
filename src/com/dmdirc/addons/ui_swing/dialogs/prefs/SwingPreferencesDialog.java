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

import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.ListScroller;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.StandardDialog;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
     * @since 0.6.3
     */
    public static int CLIENT_HEIGHT = 375;
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

    /**
     * Creates a new instance of SwingPreferencesDialog.
     */
    private SwingPreferencesDialog() {
        super(SwingController.getMainFrame(), false);

        initComponents();

        worker = new LoggingSwingWorker<PreferencesManager, Void>() {

            /** {@inheritDoc} */
            @Override
            protected PreferencesManager doInBackground() throws Exception {
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
        mainPanel = new CategoryPanel(this);

        tabList = new JList(new DefaultListModel());
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.setCellRenderer(new PreferencesListCellRenderer());
        tabList.addListSelectionListener(this);
        new ListScroller(tabList);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");
        setResizable(false);

        tabList.setBorder(BorderFactory.createEtchedBorder());

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        setLayout(new MigLayout("pack"));
        add(tabList, "w 150!, hmax 545, growy");
        add(mainPanel, "wrap, w 480!, pushy, growy");
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
    }

    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            if (tabList.getSelectedIndex() > -1) {
                final PreferencesCategory node = (PreferencesCategory) tabList.getSelectedValue();
                IdentityManager.getConfigIdentity().setOption("dialogstate",
                        "preferences", node.getPath());
            }
            saveOptions();
        }

        if (selected != null) {
            selected.fireCategoryDeselected();
            selected = null;
        }

        if (manager != null) {
            manager.dismiss();
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

            if (selected != null) {
                selected.fireCategoryDeselected();
            }
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
                JOptionPane.showMessageDialog(SwingController.getMainFrame(),
                        "One or more of the changes you made " +
                        "won't take effect until you restart the client.",
                        "Restart needed",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void restoreActiveCategory() {
        final String oldCategoryPath = IdentityManager.getConfigIdentity().
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
    private void display() {
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
