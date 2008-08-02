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

package com.dmdirc.addons.addonbrowser;

import com.dmdirc.Main;
import com.dmdirc.addons.addonbrowser.AddonInfo.AddonType;
import com.dmdirc.ui.swing.components.TextLabel;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

/**
 * The main window that allows users to browse addons.
 * 
 * @author chris
 */
public class BrowserWindow extends JFrame implements ActionListener,
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
    private final JPanel list = new JPanel(new MigLayout("flowy, ins 0, gap 0"));
    
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
        setTitle("DMDirc Addon Browser");
        setIconImage(Main.getUI().getMainWindow().getIcon().getImage());
        setMaximumSize(new Dimension(550, 550));
        setMinimumSize(new Dimension(550, 550));
        setSize(new Dimension(550, 550));
        setLocationRelativeTo((Component) Main.getUI().getMainWindow());
        
        setLayout(new MigLayout("fill"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        JPanel panel = new JPanel(new MigLayout("fill"));
        panel.setBorder(BorderFactory.createTitledBorder("Search"));
        panel.add(searchBox, "growx");
        add(panel, "width 150!");
        
        panel = new JPanel(new MigLayout("fill"));
        panel.setBorder(BorderFactory.createTitledBorder("Results"));
        panel.add(scrollPane, "grow");
        add(panel, "wrap, spany 4, grow");
        
        panel = new JPanel(new MigLayout("wrap"));
        panel.setBorder(BorderFactory.createTitledBorder("Types"));
        panel.add(pluginsBox);
        panel.add(themesBox);
        panel.add(actionsBox);
        add(panel, "wrap, pushy, growy, width 150!");
        
        panel = new JPanel(new MigLayout("wrap"));
        panel.setBorder(BorderFactory.createTitledBorder("Status"));
        panel.add(verifiedBox);
        panel.add(unverifiedBox);
        panel.add(installedBox);
        panel.add(notinstalledBox);
        add(panel, "wrap, pushy, growy, width 150!");
        
        panel = new JPanel(new MigLayout("wrap"));
        panel.setBorder(BorderFactory.createTitledBorder("Sort by"));
        panel.add(nameButton);
        panel.add(ratingButton);
        panel.add(dateButton);
        panel.add(statusButton);
        add(panel, "wrap, pushy, growy, width 150!");
        
        initListeners();
        
        try {
            loadData();
        } catch (IOException ex) {
            
        } catch (InvalidConfigFileException ex) {
            
        }
        
        setVisible(true);
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
        list.setVisible(false);
        list.removeAll();
        list.add(new JLabel("Sorting list."));
        list.setVisible(true);

        new Thread(new SwingWorker() {

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

                int i = 0;
                list.setVisible(false);
                list.removeAll();
                for (AddonInfo info : newInfos) {
                    list.add(getPanel(info, i++), "wmax 400");
                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        scrollPane.getVerticalScrollBar().setValue(0);
                    }
                });
                list.setVisible(true);
            }
        }).start();
    }
    
    /**
     * Retrieves the panel to use for the specified addon.
     * 
     * @param info The addon to generate a panel for
     * @param index The index of the panel (for alternating background)
     * @return A JPanel representing the addon
     */
    private JPanel getPanel(final AddonInfo info, final int index) {
        final JPanel panel = new JPanel(new MigLayout("fillx, ins 0"));
        panel.setBackground(index % 2 == 1 ? new Color(0xEE, 0xEE, 0xFF) : Color.WHITE);
        
        JLabel title = new JLabel(info.getTitle());
        title.setFont(title.getFont().deriveFont(16f).deriveFont(Font.BOLD));
        panel.add(title, "wmax 165, gaptop 5, gapleft 5");
        
        title = new JLabel(info.getScreenshot());
        title.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(title, "width 150!, height 150::, wrap, spany 4, gaptop 5, gapright 5");

        title = new JLabel(info.getType().toString() + ", rated "
                + info.getRating() + "/10");
        panel.add(title, "gapleft 5, wrap");
        
        TextLabel label = new TextLabel(info.getDescription());
        panel.add(label, "wmax 165, growy, wrap, gapleft 5, gapbottom 5, pushy");
        
        final JButton button = new JButton("Install");
        final boolean installed = info.isInstalled();
        
        if (installed || !info.isDownloadable()) {
            button.setEnabled(false);
        }
        
        panel.add(button, "gapleft 5, split");
        
        if (installed || !info.isDownloadable()) {
            title = new JLabel(installed ? "Already installed" : "No download available");
            title.setForeground(Color.GRAY);
            panel.add(title);
        }
        
        panel.add(new JSeparator(), "newline, span, growx, pushx, gaptop 5");
        
        return panel;
    }

    /** {@inheritDoc} */
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
