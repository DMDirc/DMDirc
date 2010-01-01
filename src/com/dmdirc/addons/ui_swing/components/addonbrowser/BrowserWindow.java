/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.Main;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

/**
 * The main window that allows users to browse addons.
 * 
 * @author chris
 */
public class BrowserWindow extends JDialog implements ActionListener {

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
    private final JCheckBox notinstalledBox = new JCheckBox("Not installed",
            true);
    /** The panel used to list addons. */
    private final AddonTable list = new AddonTable();
    /** The scrollpane for the list panel. */
    private final JScrollPane scrollPane = new JScrollPane(list,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    /** The sort by name button. */
    private final JRadioButton nameButton = new JRadioButton("Name", true);
    /** The sort by rating button. */
    private final JRadioButton ratingButton = new JRadioButton("Rating", false);
    /** The sort by date button. */
    private final JRadioButton dateButton = new JRadioButton("Date", false);
    /** The sort by status button. */
    private final JRadioButton statusButton = new JRadioButton("Status", false);
    /** Row sorter. */
    private final AddonSorter sorter;
    /** Addon filter. */
    private final AddonFilter filter;

    /**
     * Creates and displays a new browser window.
     *
     * @param parentWindow Parent window
     */
    public BrowserWindow(final Window parentWindow) {
        super(parentWindow, "DMDirc Addon Browser", ModalityType.MODELESS);
        setIconImages(parentWindow.getIconImages());
        setResizable(false);
        setLayout(new MigLayout("fill, wmin 650, hmin 600"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

        JPanel panel = new JPanel(new MigLayout("fill"));
        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Search"));
        panel.add(searchBox, "growx");
        add(panel, "width 150!");

        panel = new JPanel(new MigLayout("fill"));
        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Results"));
        panel.add(scrollPane, "grow");
        add(panel, "wrap, spany 4, grow");

        panel = new JPanel(new MigLayout("fill, wrap"));
        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Types"));
        panel.add(pluginsBox, "grow");
        panel.add(themesBox, "grow");
        panel.add(actionsBox, "grow");
        add(panel, "wrap, pushy, growy, width 150!");

        panel = new JPanel(new MigLayout("fill, wrap"));
        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Status"));
        panel.add(verifiedBox, "grow");
        panel.add(unverifiedBox, "grow");
        panel.add(installedBox, "grow");
        panel.add(notinstalledBox, "grow");
        add(panel, "wrap, pushy, growy, width 150!");

        panel = new JPanel(new MigLayout("fill, wrap"));
        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Sort by"));
        panel.add(nameButton, "grow");
        panel.add(ratingButton, "grow");
        panel.add(dateButton, "grow");
        panel.add(statusButton, "grow");
        add(panel, "wrap, pushy, growy, width 150!");

        initListeners();

        filter = new AddonFilter(verifiedBox.getModel(),
                unverifiedBox.getModel(), installedBox.getModel(),
                notinstalledBox.getModel(), pluginsBox.getModel(), themesBox.
                getModel(), actionsBox.getModel(),
                searchBox);
        sorter = new AddonSorter(list.getModel(), dateButton.getModel(),
                nameButton.getModel(), ratingButton.getModel(),
                statusButton.getModel(), filter);
        list.setRowSorter(sorter);

        try {
            loadData();
        } catch (IOException ex) {
        } catch (InvalidConfigFileException ex) {
        }

        pack();
        setLocationRelativeTo(parentWindow);
        setVisible(true);
        setLocationRelativeTo(parentWindow);
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

        verifiedBox.addActionListener(this);
        unverifiedBox.addActionListener(this);
        installedBox.addActionListener(this);
        notinstalledBox.addActionListener(this);

        searchBox.addActionListener(this);

        nameButton.addActionListener(this);
        ratingButton.addActionListener(this);
        dateButton.addActionListener(this);
        statusButton.addActionListener(this);
    }

    /**
     * Loads addon data from the locally cached feed file.
     * 
     * @throws IOException If the file can't be read
     * @throws InvalidConfigFileException If the file is corrupt somehow
     */
    private void loadData() throws IOException, InvalidConfigFileException {
        ConfigFile data = new ConfigFile(Main.getConfigDir() + File.separator +
                "addons.feed");
        data.read();

        for (Map<String, String> entry : data.getKeyDomains().values()) {
            final AddonInfo info = new AddonInfo(entry);
            list.getModel().addRow(new Object[]{new AddonInfoLabel(info),});
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                sorter.sort();
            }
        });
    }
}
