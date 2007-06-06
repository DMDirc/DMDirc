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

import com.dmdirc.Main;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.MainFrame;
    
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * The Systray plugin shows DMDirc in the user's system tray, and allows
 * notifications to be disabled.
 * @author chris
 */
public final class SystrayPlugin  extends Plugin implements ActionListener, MouseListener {
    
    /** The command we registered. */
    private PopupCommand command;
    
    /** The tray icon we're currently using. */
    private TrayIcon icon;
    
    /** The menu to use for the tray icon. */
    private final PopupMenu menu;
    
    /** Creates a new system tray plugin. */
    public SystrayPlugin() {
        super();
        final MenuItem show = new MenuItem("Show/hide");
        final MenuItem quit = new MenuItem("Quit");
        
        menu = new PopupMenu();
        menu.add(show);
        menu.add(quit);
        
        show.addActionListener(this);
        quit.addActionListener(this);
    }
    
    /**
     * Sends a notification via the system tray icon.
     * @param title The title of the notification
     * @param message The contents of the notification
     * @param type The type of notification
     */
    public void notify(final String title, final String message, final TrayIcon.MessageType type) {
        if (isActive()) {
            icon.displayMessage(title, message, type);
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
            MainFrame.getMainFrame().setVisible(!MainFrame.getMainFrame().isVisible());
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
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        final URL imageURL = cldr.getResource("com/dmdirc/res/logo.png");
        icon = new TrayIcon(new ImageIcon(imageURL).getImage(), "DMDirc", menu);
        icon.setImageAutoSize(true);
        icon.addMouseListener(this);
        
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
            if (MainFrame.getMainFrame().isVisible()) {
                // TODO: Uncomment the code below, with an appropriate replacement
                //       for requestFocus() that does something more than flash.
                
                //if (MainFrame.getMainFrame().isActive()) {
                MainFrame.getMainFrame().setVisible(false);
                //} else {
                //    MainFrame.getMainFrame().requestFocus();
                //}
            } else {
                MainFrame.getMainFrame().setVisible(true);
            }
        }
    }
    
    /** {@inheritDoc} */
    public void mousePressed(final MouseEvent e) {
    }
    
    /** {@inheritDoc} */
    public void mouseReleased(final MouseEvent e) {
    }
    
    /** {@inheritDoc} */
    public void mouseEntered(final MouseEvent e) {
    }
    
    /** {@inheritDoc} */
    public void mouseExited(final MouseEvent e) {
    }
    
}
