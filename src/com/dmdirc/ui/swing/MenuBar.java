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

package com.dmdirc.ui.swing;

import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.ui.swing.components.TextFrame;
import com.dmdirc.ui.swing.dialogs.FeedbackDialog;
import com.dmdirc.ui.swing.dialogs.NewServerDialog;
import com.dmdirc.ui.swing.dialogs.PluginDialog;
import com.dmdirc.ui.swing.dialogs.about.AboutDialog;
import com.dmdirc.ui.swing.dialogs.aliases.AliasManagerDialog;
import com.dmdirc.ui.swing.dialogs.prefs.PreferencesDialog;
import com.dmdirc.ui.swing.dialogs.profiles.ProfileManagerDialog;
import com.dmdirc.ui.swing.framemanager.windowmenu.WindowMenuFrameManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * DMDirc menu bar.
 */
public class MenuBar extends JMenuBar implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Plugins menu. */
    private PluginsMenu plugins;

    /**
     * Instantiates a new menu bar.
     */
    public MenuBar() {
        initServerMenu();
        initSettingsMenu();
        add(new WindowMenuFrameManager());
        initHelpMenu();
    }

    /**
     * Initialises the server menu.
     */
    private void initServerMenu() {
        JMenuItem menuItem;
        final JMenu menu = new JMenu("Server");
        menu.setMnemonic('s');
        add(menu);

        menuItem = new JMenuItem();
        menuItem.setText("New Server...");
        menuItem.setMnemonic('n');
        menuItem.setActionCommand("NewServer");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setText("Exit");
        menuItem.setMnemonic('x');
        menuItem.setActionCommand("Exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
    }

    /**
     * Initialises the settings menu.
     */
    private void initSettingsMenu() {
        JMenuItem menuItem;
        final JMenu menu = new JMenu("Settings");
        menu.setMnemonic('e');
        add(menu);

        menuItem = new JMenuItem();
        menuItem.setText("Preferences");
        menuItem.setMnemonic('p');
        menuItem.setActionCommand("Preferences");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('m');
        menuItem.setText("Profile Manager");
        menuItem.setActionCommand("Profile");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('a');
        menuItem.setText("Actions Manager");
        menuItem.setActionCommand("Actions");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('l');
        menuItem.setText("Alias Manager");
        menuItem.setActionCommand("Aliases");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        plugins = new PluginsMenu();
        menu.add(plugins);
    }

    /**
     * Initialises the help menu.
     */
    private void initHelpMenu() {
        JMenuItem menuItem;
        final JMenu menu = new JMenu("Help");
        menu.setMnemonic('h');
        add(menu);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('a');
        menuItem.setText("About");
        menuItem.setActionCommand("About");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('j');
        menuItem.setText("Join Dev channel");
        menuItem.setActionCommand("JoinDevChat");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('f');
        menuItem.setText("Send Feedback");
        menuItem.setActionCommand("feedback");
        menuItem.addActionListener(this);
        menu.add(menuItem);
    }

    /**
     * Adds a JMenuItem to the plugin menu.
     *
     * @param menuItem The menu item to be added.
     */
    public void addPluginMenu(final JMenuItem menuItem) {
        plugins.addPluginMenu(menuItem);
    }

    /**
     * Removes a JMenuItem from the plugin menu.
     *
     * @param menuItem The menu item to be removed.
     */
    public void removePluginMenu(final JMenuItem menuItem) {
        plugins.removePluginMenu(menuItem);
    }

    /** 
     * {@inheritDoc}
     *  
     * @param e Action event    
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("NewServer".equals(e.getActionCommand())) {
            NewServerDialog.showNewServerDialog();
        } else if ("Preferences".equals(e.getActionCommand())) {
            PreferencesDialog.showPreferencesDialog();
        } else if (e.getActionCommand().equals("About")) {
            AboutDialog.showAboutDialog();
        } else if (e.getActionCommand().equals("Profile")) {
            ProfileManagerDialog.showProfileManagerDialog();
        } else if (e.getActionCommand().equals("Exit")) {
            ((MainFrame) Main.getUI().getMainWindow()).quit();
        } else if (e.getActionCommand().equals("ManagePlugins")) {
            PluginDialog.showPluginDialog();
        } else if (e.getActionCommand().equals("Actions")) {
            //ActionsManagerDialog.showActionsManagerDialog();
            com.dmdirc.ui.swing.dialogs.actionsmanager.ActionsManagerDialog.showActionsManagerDialog();
        } else if (e.getActionCommand().equals("Aliases")) {
            AliasManagerDialog.showAliasManagerDialog();
        } else if (e.getActionCommand().equals("Minimise")) {
            ((TextFrame) Main.getUI().getMainWindow().getActiveFrame()).minimise();
        } else if (e.getActionCommand().equals("Close")) {
            ((TextFrame) Main.getUI().getMainWindow().getActiveFrame()).close();
        } else if (e.getActionCommand().equals("JoinDevChat")) {
            ServerManager.getServerManager().joinDevChat();
        } else if (e.getActionCommand().equals("feedback")) {
            FeedbackDialog.showFeedbackDialog();
        }
    }
}
