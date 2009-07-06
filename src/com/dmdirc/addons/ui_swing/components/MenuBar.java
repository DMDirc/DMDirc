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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.ServerManager;
import com.dmdirc.ServerState;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.FeedbackDialog;
import com.dmdirc.addons.ui_swing.dialogs.NewServerDialog;
import com.dmdirc.addons.ui_swing.dialogs.about.AboutDialog;
import com.dmdirc.addons.ui_swing.dialogs.actionsmanager.ActionsManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.aliases.AliasManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.framemanager.windowmenu.WindowMenuFrameManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dialog.ModalityType;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * DMDirc menu bar.
 */
public class MenuBar extends JMenuBar implements ActionListener, MenuListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** CSD. */
    private JMenuItem csd;
    /** SSD. */
    private JMenuItem ssd;
    /** disconnect. */
    private JMenuItem disconnect;
    /** join. */
    private JMenuItem join;
    /** Swing controller. */
    private SwingController controller;
    /** Main frame. */
    private MainFrame mainFrame;

    /**
     * Instantiates a new menu bar.
     * 
     * @param controller Swing controller
     * @param mainFrame Main frame
     */
    public MenuBar(final SwingController controller, final MainFrame mainFrame) {
        super();
        this.controller = controller;
        this.mainFrame = mainFrame;
        
        setLayout(new MigLayout("ins 0, fillx"));

        initServerMenu();
        initChannelMenu();
        initSettingsMenu();
        add(new WindowMenuFrameManager());
        initHelpMenu();
        add(Box.createHorizontalGlue(), "growx, pushx");
        add(new MDIBar(mainFrame));
        add(Box.createHorizontalStrut(PlatformDefaults.getPanelInsets(1).getUnit()));

        getActionMap().setParent(null);
        getActionMap().clear();
    }

    /**
     * Initialises the server menu.
     */
    private void initServerMenu() {
        JMenuItem menuItem;
        final JMenu menu = new JMenu("Server");
        menu.setMnemonic('s');
        menu.addMenuListener(this);
        add(menu);

        menuItem = new JMenuItem();
        menuItem.setText("New Server...");
        menuItem.setMnemonic('n');
        menuItem.setActionCommand("NewServer");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        disconnect = new JMenuItem();
        disconnect.setText("Disconnect");
        disconnect.setMnemonic('d');
        disconnect.setActionCommand("Disconnect");
        disconnect.addActionListener(this);
        menu.add(disconnect);

        ssd = new JMenuItem();
        ssd.setMnemonic('s');
        ssd.setText("Server settings");
        ssd.setActionCommand("ServerSettings");
        ssd.addActionListener(this);
        menu.add(ssd);

        if (!Apple.isAppleUI()) {
            menuItem = new JMenuItem();
            menuItem.setText("Exit");
            menuItem.setMnemonic('x');
            menuItem.setActionCommand("Exit");
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }
    }

    /**
     * Initialises the channel menu.
     */
    private void initChannelMenu() {
        final JMenu menu = new JMenu("Channel");
        menu.setMnemonic('c');
        menu.addMenuListener(this);
        add(menu);

        join = new JMenuItem();
        join.setText("Join Channel...");
        join.setMnemonic('j');
        join.setActionCommand("JoinChannel");
        join.addActionListener(this);
        menu.add(join);

        csd = new JMenuItem();
        csd.setMnemonic('c');
        csd.setText("Channel Settings");
        csd.setActionCommand("ChannelSettings");
        csd.addActionListener(this);
        menu.add(csd);
    }

    /**
     * Initialises the settings menu.
     */
    private void initSettingsMenu() {
        JMenuItem menuItem;
        final JMenu menu = new JMenu("Settings");
        menu.setMnemonic('e');
        menu.addMenuListener(this);
        add(menu);

        if (!Apple.isAppleUI()) {
            menuItem = new JMenuItem();
            menuItem.setText("Preferences");
            menuItem.setMnemonic('p');
            menuItem.setActionCommand("Preferences");
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }

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

        if (!Apple.isAppleUI()) {
            menuItem = new JMenuItem();
            menuItem.setMnemonic('a');
            menuItem.setText("About");
            menuItem.setActionCommand("About");
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }
    }

    /** 
     * {@inheritDoc}
     *  
     * @param e Action event    
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("NewServer".equals(e.getActionCommand())) {
            NewServerDialog.showNewServerDialog(mainFrame);
        } else if ("Preferences".equals(e.getActionCommand())) {
            SwingPreferencesDialog.showSwingPreferencesDialog(mainFrame);
        } else if (e.getActionCommand().equals("About")) {
            AboutDialog.showAboutDialog(mainFrame);
        } else if (e.getActionCommand().equals("Profile")) {
            ProfileManagerDialog.showProfileManagerDialog(mainFrame);
        } else if (e.getActionCommand().equals("Exit")) {
            mainFrame.quit();
        } else if (e.getActionCommand().equals("Actions")) {
            ActionsManagerDialog.showActionsManagerDialog(mainFrame);
        } else if (e.getActionCommand().equals("Aliases")) {
            AliasManagerDialog.showAliasManagerDialog(mainFrame);
        } else if (e.getActionCommand().equals("JoinDevChat")) {
            ServerManager.getServerManager().joinDevChat();
        } else if (e.getActionCommand().equals("feedback")) {
            FeedbackDialog.showFeedbackDialog(mainFrame);
        } else if (e.getActionCommand().equals("ChannelSettings")) {
            final Window activeWindow = controller.getActiveWindow();
            if (activeWindow instanceof ChannelFrame) {
                controller.showChannelSettingsDialog(((ChannelFrame) activeWindow).
                        getChannel());
            }
        } else if (e.getActionCommand().equals("ServerSettings")) {
            controller.showServerSettingsDialog(controller.getActiveServer());
        } else if (e.getActionCommand().equals("Disconnect")) {
            controller.getActiveServer().disconnect();
        } else if (e.getActionCommand().equals("JoinChannel")) {
            new StandardInputDialog(mainFrame, ModalityType.MODELESS,
                    "Join channel", "Enter the name of the channel to join.") {

                /** Serial version UID. */
                private static final long serialVersionUID = 1;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    controller.getActiveServer().join(getText());
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    //Ignore
                }
            }.display();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void menuSelected(final MenuEvent e) {
        final Window activeWindow = controller.getActiveWindow();

        ssd.setEnabled(activeWindow != null && activeWindow.getContainer().
                getServer() != null && activeWindow.getContainer().
                getServer().getState() == ServerState.CONNECTED);
        csd.setEnabled(activeWindow instanceof ChannelFrame && activeWindow.getContainer().
                getServer() != null && activeWindow.getContainer().
                getServer().getState() == ServerState.CONNECTED);
        disconnect.setEnabled(activeWindow != null && activeWindow.getContainer().
                getServer() != null && activeWindow.getContainer().
                getServer().getState() == ServerState.CONNECTED);
        join.setEnabled(activeWindow != null && activeWindow.getContainer().
                getServer() != null && activeWindow.getContainer().
                getServer().getState() == ServerState.CONNECTED);
    }

    /** {@inheritDoc} */
    @Override
    public void menuDeselected(final MenuEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void menuCanceled(final MenuEvent e) {
        //Ignore
    }
}
