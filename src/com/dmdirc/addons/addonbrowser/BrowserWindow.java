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
package com.dmdirc.addons.addonbrowser;

import com.dmdirc.Main;
import com.dmdirc.addons.addonbrowser.AddonInfo.AddonType;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * The main window that allows users to browse addons.
 * 
 * @author chris
 */
public class BrowserWindow extends JDialog implements ActionListener,
        Comparator<AddonInfo> {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** The search box. */
    private final JTextField searchBox = new JTextField();
    /** The plugins check box. */
    private final JCheckBox pluginsBox = new JCheckBox("Plugins", true);
    /** The themes check box. */
    private final JCheckBox themesBox = new JCheckBox("Themes", true);
    /** The actions check box. */
    private final JCheckBox actionsBox = new JCheckBox("Action Packs", true);
    /** The verified check box. */
    private final JCheckBox verifiedBox = new JCheckBox("Verified", true);
    /** The unverified check box. */
    private final JCheckBox unverifiedBox = new JCheckBox("Unverified", false);
    /** The installed checkbox. */
    private final JCheckBox installedBox = new JCheckBox("Installed", true);
    /** The not installed checkbox. */
    private final JCheckBox notinstalledBox = new JCheckBox("Not installed", true);
    /** The panel used to list addons. */
    private final JList list = new JList(new DefaultListModel());
    /** The scrollpane for the list panel. */
    private final JScrollPane scrollPane = new JScrollPane(list,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    /** The sort by name button. */
    private final JRadioButton nameButton = new JRadioButton("Name", true);
    /** The sort by rating button. */
    private final JRadioButton ratingButton = new JRadioButton("Rating", false);
    /** The sort by date button. */
    private final JRadioButton dateButton = new JRadioButton("Date", false);
    /** The sort by status button. */
    private final JRadioButton statusButton = new JRadioButton("Status", false);
    /** All known addon infos. */
    private final List<AddonInfo> infos = new ArrayList<AddonInfo>();

    /**
     * Creates and displays a new browser window.
     */
    public BrowserWindow() {
        super((MainFrame) Main.getUI().getMainWindow(), "DMDirc Addon Browser", false);
        setIconImage(((MainFrame) Main.getUI().getMainWindow()).getIcon().getImage());
        setResizable(false);
        setLayout(new MigLayout("fill, wmin 650, hmin 600"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        list.setCellRenderer(new AddonInfoListCellRenderer());

        JPanel panel = new JPanel(new MigLayout("fill"));
        panel.setBorder(BorderFactory.createTitledBorder("Search"));
        panel.add(searchBox, "growx");
        add(panel, "width 150!");

        panel = new JPanel(new MigLayout("fill"));
        panel.setBorder(BorderFactory.createTitledBorder("Results"));
        panel.add(scrollPane, "grow");
        add(panel, "wrap, spany 4, grow");

        panel = new JPanel(new MigLayout("fill, wrap"));
        panel.setBorder(BorderFactory.createTitledBorder("Types"));
        panel.add(pluginsBox, "grow");
        panel.add(themesBox, "grow");
        panel.add(actionsBox, "grow");
        add(panel, "wrap, pushy, growy, width 150!");

        panel = new JPanel(new MigLayout("fill, wrap"));
        panel.setBorder(BorderFactory.createTitledBorder("Status"));
        panel.add(verifiedBox, "grow");
        panel.add(unverifiedBox, "grow");
        panel.add(installedBox, "grow");
        panel.add(notinstalledBox, "grow");
        add(panel, "wrap, pushy, growy, width 150!");

        panel = new JPanel(new MigLayout("fill, wrap"));
        panel.setBorder(BorderFactory.createTitledBorder("Sort by"));
        panel.add(nameButton, "grow");
        panel.add(ratingButton, "grow");
        panel.add(dateButton, "grow");
        panel.add(statusButton, "grow");
        add(panel, "wrap, pushy, growy, width 150!");

        initListeners();

        try {
            loadData();
        } catch (IOException ex) {
        } catch (InvalidConfigFileException ex) {
        }

        pack();
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        setVisible(true);
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
    }

    /**
     * Registers listeners and sets up button groups.
     */
    private void initListeners() {
        final ButtonGroup group = new ButtonGroup();
        group.add(nameButton);
        group.add(ratingButton);
        group.add(dateButton);
        group.add(statusButton);

        pluginsBox.addActionListener(this);
        themesBox.addActionListener(this);
        actionsBox.addActionListener(this);

        nameButton.addActionListener(this);
        ratingButton.addActionListener(this);
        dateButton.addActionListener(this);
        statusButton.addActionListener(this);

        verifiedBox.addActionListener(this);
        unverifiedBox.addActionListener(this);
        installedBox.addActionListener(this);
        notinstalledBox.addActionListener(this);

        searchBox.addActionListener(this);
    }

    /**
     * Loads addon data from the locally cached feed file.
     * 
     * @throws IOException If the file can't be read
     * @throws InvalidConfigFileException If the file is corrupt somehow
     */
    private void loadData() throws IOException, InvalidConfigFileException {
        ConfigFile data = new ConfigFile(Main.getConfigDir() + File.separator + "addons.feed");
        data.read();
        int i = 0;

        for (Map<String, String> entry : data.getKeyDomains().values()) {
            final AddonInfo info = new AddonInfo(entry);
            infos.add(info);
        }

        sortAndFilter();
    }

    /**
     * Sorts and filters the list of addons according to the currently selected
     * options.
     */
    private void sortAndFilter() {
        ((DefaultListModel) list.getModel()).clear();
        list.add(new JLabel("Sorting list.", JLabel.CENTER), "grow, pushy");

        new LoggingSwingWorker() {

            final List<AddonInfo> newInfos = new ArrayList<AddonInfo>();

            /* {@inheritDoc} */
            @Override
            protected Object doInBackground() {
                for (AddonInfo info : infos) {
                    if ((!verifiedBox.isSelected() && info.isVerified()) ||
                            (!unverifiedBox.isSelected() && !info.isVerified()) ||
                            (!installedBox.isSelected() && info.isInstalled()) ||
                            (!notinstalledBox.isSelected() &&
                            !info.isInstalled()) || (!pluginsBox.isSelected() &&
                            info.getType() == AddonType.TYPE_PLUGIN) ||
                            (!themesBox.isSelected() && info.getType() ==
                            AddonType.TYPE_THEME) ||
                            (!actionsBox.isSelected() && info.getType() ==
                            AddonType.TYPE_ACTION_PACK) || (!searchBox.getText().
                            isEmpty() && !info.matches(searchBox.getText()))) {
                        continue;
                    }

                    newInfos.add(info);
                }

                Collections.sort(newInfos, BrowserWindow.this);
                return newInfos;
            }

            /* {@inheritDoc} */
            @Override
            protected void done() {
                super.done();

                ((DefaultListModel) list.getModel()).clear();
                for (AddonInfo info : newInfos) {
                    ((DefaultListModel) list.getModel()).addElement(info);
                }
                UIUtilities.resetScrollPane(scrollPane);
            }
        }.execute();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        sortAndFilter();
    }

    /** {@inheritDoc} */
    @Override
    public int compare(final AddonInfo o1, final AddonInfo o2) {
        if (dateButton.isSelected()) {
            return o1.getId() - o2.getId();
        } else if (nameButton.isSelected()) {
            return o1.getTitle().compareTo(o2.getTitle());
        } else if (ratingButton.isSelected()) {
            return o1.getRating() - o2.getRating();
        } else if (statusButton.isSelected()) {
            return (o1.isVerified() ? 1 : 0) - (o2.isVerified() ? 1 : 0);
        } else {
            return 0;
        }
    }
}
