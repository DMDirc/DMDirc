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

package com.dmdirc.addons.ui_swing.components.themepanel;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.ui.themes.Theme;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.renderers.AddonCellRenderer;
import com.dmdirc.util.URLHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Theme panel. Shows users available themes and allows them to enable/disbale
 * them.
 */
public final class ThemePanel extends JPanel implements
        ActionListener, ListSelectionListener, PreferencesInterface {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** List of themes. */
    private JList themeList;
    /** plugin list scroll pane. */
    private JScrollPane scrollPane;
    /** Button to enable/disable theme. */
    private JButton toggleButton;
    /** Currently selected theme. */
    private int selectedTheme;
    /** Blurb label. */
    private TextLabel blurbLabel;

    /** Creates a new instance of PluginDialog. */
    public ThemePanel() {
        super();

        initComponents();
        addListeners();
        layoutComponents();

        themeList.setSelectedIndex(0);
        selectedTheme = 0;
    }

    /** Initialises the components. */
    private void initComponents() {
        themeList = new JList(new DefaultListModel());
        themeList.setCellRenderer(new AddonCellRenderer());

        scrollPane = new JScrollPane(new JLabel("Loading plugins..."));
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        toggleButton = new JButton("Enable");
        toggleButton.setEnabled(false);

        blurbLabel = new TextLabel("Themes alter the appearance of DMDirc");

        /** {@inheritDoc}. */
        new LoggingSwingWorker() {

            /** {@inheritDoc}. */
            @Override
            protected Object doInBackground() {
                return populateList();
            }

            /** {@inheritDoc}. */
            @Override
            protected void done() {
                super.done();
                scrollPane.setViewportView(themeList);
            }
        }.execute();
    }

    /** Lays out the dialog. */
    private void layoutComponents() {
        setLayout(new MigLayout("ins 0, fill"));

        add(blurbLabel, "wrap 10, growx, pushx");

        add(scrollPane, "wrap 5, grow, push");

        add(toggleButton, "split 2, growx, pushx, sg button");

        final JButton button = new JButton("Get more themes");
        button.addActionListener(this);
        add(button, "growx, pushx, sg button");
    }

    /** 
     * Populates the plugins list with plugins from the plugin manager. 
     * 
     * @return Populated list
     */
    private JList populateList() {
        final List<Theme> list = new ArrayList<Theme>(ThemeManager.
                getAvailableThemes().values());
        Collections.sort(list);

        ((DefaultListModel) themeList.getModel()).clear();

        for (Theme plugin : list) {
            ((DefaultListModel) themeList.getModel()).addElement(new ThemeToggle(
                    plugin));
        }

        if (((DefaultListModel) themeList.getModel()).size() > 0) {
            toggleButton.setEnabled(true);
        }

        themeList.repaint();
        return themeList;
    }

    /** Adds listeners to components. */
    private void addListeners() {
        toggleButton.addActionListener(this);
        themeList.addListSelectionListener(this);
    }

    /**
     * Invoked when an action occurs.
     * 
     * @param e The event related to this action.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == toggleButton && selectedTheme >= 0) {
            final ThemeToggle theme = (ThemeToggle) themeList.getSelectedValue();

            theme.toggle();

            if (theme.getState()) {
                toggleButton.setText("Disable");
            } else {
                toggleButton.setText("Enable");
            }

            themeList.repaint();
        } else if (e.getSource() != toggleButton) {
            URLHandler.getURLHander().launchApp("http://addons.dmdirc.com/");
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            final int selected = ((JList) e.getSource()).getSelectedIndex();
            if (selected >= 0) {
                final ThemeToggle theme = (ThemeToggle) ((JList) e.getSource()).
                        getSelectedValue();
                toggleButton.setEnabled(true);

                if (theme.getState()) {
                    toggleButton.setText("Disable");
                } else {
                    toggleButton.setText("Enable");
                }
            }
            selectedTheme = selected;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        final List<String> enabled = new ArrayList<String>();

        for (Object pit : ((DefaultListModel) themeList.getModel()).toArray()) {
            if (((ThemeToggle) pit).getState()) {
                enabled.add(((ThemeToggle) pit).getTheme().getFileName());
            }
        }

        IdentityManager.getConfigIdentity().setOption("themes", "enabled",
                enabled);
    }
}
