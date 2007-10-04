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

package com.dmdirc.addons.systray;

import com.dmdirc.IconManager;
import com.dmdirc.Main;
import com.dmdirc.actions.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.EventPlugin;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.swing.MainFrame;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The Systray plugin shows DMDirc in the user's system tray, and allows
 * notifications to be disabled.
 * @author chris
 */
public final class SystrayPlugin extends Plugin implements ActionListener,
        MouseListener, EventPlugin {
    
    /** The command we registered. */
    private PopupCommand command;
    
    /** The tray icon we're currently using. */
    private final TrayIcon icon;
    
    /** Creates a new system tray plugin. */
    public SystrayPlugin() {
        super();
        final MenuItem show = new MenuItem("Show/hide");
        final MenuItem quit = new MenuItem("Quit");
        
        final PopupMenu menu = new PopupMenu();
        menu.add(show);
        menu.add(quit);
        
        show.addActionListener(this);
        quit.addActionListener(this);
        
        icon = new TrayIcon(IconManager.getIconManager().getImage("logo"), 
                "DMDirc", menu);
        icon.setImageAutoSize(true);
        icon.addMouseListener(this);
    }
    
    /**
     * Sends a notification via the system tray icon.
     * @param title The title of the notification
     * @param message The contents of the notification
     * @param type The type of notification
     */
    public void notify(final String title, final String message, final TrayIcon.MessageType type) {
        if (isActive()) {
            icon.displayMessage(title, Styliser.stipControlCodes(message), type);
        }
    }
    
    /**
     * Sends a notification via the system tray icon.
     * @param title The title of the notification
     * @param message The contents of the notification
     */
    public void notify(final String title, final String message) {
        notify(title, message, TrayIcon.MessageType.NONE);
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("Show/hide")) {
            Main.getUI().getMainWindow().setVisible(!Main.getUI().getMainWindow().isVisible());
        } else if (e.getActionCommand().equals("Quit")) {
            Main.quit();
        }
    }
    
    /** {@inheritDoc} */
    public boolean onLoad() {
        return SystemTray.isSupported();
    }
    
    /** {@inheritDoc} */
    public void onActivate() {
        try {
            SystemTray.getSystemTray().add(icon);
            command = new PopupCommand(this);
        } catch (AWTException ex) {
            // Should probably unload ourself here?
        }
    }
    
    /** {@inheritDoc}. */
    public void onDeactivate() {
        SystemTray.getSystemTray().remove(icon);
        command.unregister();
    }
    
    /** {@inheritDoc} */
    public String getVersion() {
        return "0.1";
    }
    
    /** {@inheritDoc} */
    public String getAuthor() {
        return "Chris <chris@dmdirc.com>";
    }
    
    /** {@inheritDoc} */
    public String getDescription() {
        return "Adds a system tray icon";
    }
    
    /** {@inheritDoc}. */
    public String toString() {
        return "Systray Plugin";
    }
    
    /** {@inheritDoc} */
    public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (Main.getUI().getMainWindow().isVisible()) {               
                Main.getUI().getMainWindow().setVisible(false);
            } else {
                Main.getUI().getMainWindow().setVisible(true);
                ((MainFrame) Main.getUI().getMainWindow()).setState(Frame.NORMAL);
                ((MainFrame) Main.getUI().getMainWindow()).toFront();
            }
        }
    }
    
    /** {@inheritDoc} */
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.CLIENT_MINIMISED
                && IdentityManager.getGlobalConfig().getOptionBool("plugin-systray", "autominimise", false)) {
            Main.getUI().getMainWindow().setVisible(false);
        }
    }
    
}
