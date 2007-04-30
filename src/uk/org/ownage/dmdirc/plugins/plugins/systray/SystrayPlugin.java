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

package uk.org.ownage.dmdirc.plugins.plugins.systray;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import javax.swing.ImageIcon;

import uk.org.ownage.dmdirc.plugins.Plugin;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * The Systray plugin shows DMDirc in the user's system tray, and allows
 * notifications to be disabled.
 * @author chris
 */
public class SystrayPlugin implements Plugin {
    
    /** The tray icon we're currently using. */
    private TrayIcon icon;
    
    /**
     * Sends a notification via the system tray icon.
     * @param title The title of the notification
     * @param message The contents of the notification
     * @param type The type of notification
     */
    public void notify(final String title, final String message, final TrayIcon.MessageType type) {
        icon.displayMessage(title, message, type);
    }
    
    /**
     * Sends a notification via the system tray icon.
     * @param title The title of the notification
     * @param message The contents of the notification
     */
    public void notify(final String title, final String message) {
        notify(title, message, TrayIcon.MessageType.NONE);
    }    
    
    /** {@inheritDoc}. */
    public boolean onLoad() {
        if (SystemTray.isSupported()) {
            final SystemTray tray = SystemTray.getSystemTray();
            
            final ImageIcon image = MainFrame.getMainFrame().getIcon();
            
            icon = new TrayIcon(image.getImage());
            
            icon.setImageAutoSize(false);
            
            try {
                tray.add(icon);
            } catch (AWTException ex) {
                return false;
            }
            
            new PopupCommand(this);
        } else {
            return false;
        }
        
        return true;
    }
    
    /** {@inheritDoc}. */
    public void onUnload() {
    }
    
    /** {@inheritDoc}. */
    public void onActivate() {
        
    }
    
    /** {@inheritDoc}. */
    public void onDeactivate() {
        final SystemTray tray = SystemTray.getSystemTray();
        
        tray.remove(icon);
    }
    
    /** {@inheritDoc}. */
    public boolean isConfigurable() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public void showConfig() {
    }
    
    /** {@inheritDoc}. */
    public int getVersion() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getAuthor() {
        return "Chris 'MD87' Smith - chris@dmdirc.com";
    }
    
    /** {@inheritDoc}. */
    public String getDescription() {
        return "Adds a system tray icon";
    }
    
}
