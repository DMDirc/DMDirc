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

package com.dmdirc.ui.interfaces;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/**
 * Main window interface.
 */
public interface MainWindow {
    
    /**
     * Adds the specified window as a child of the main frame.
     *
     * @param window the window to be added
     */
    void addChild(final Window window);
    
    /**
     * Removes the specified window from our desktop pane.
     *
     * @param window The window to be removed
     */
    void delChild(final Window window);
    
    /**
     * Sets the active internal frame to the one specified.
     *
     * @param frame The frame to be activated
     */
    void setActiveFrame(final Window frame);
    
    /**
     * Retrieves the frame manager that's currently in use.
     *
     * @return The current frame manager
     */
    FrameManager getFrameManager();
    
    /**
     * Retrieves the application icon.
     *
     * @return The application icon
     */
    ImageIcon getIcon();
    
    /**
     * Returns the window that is currently active.
     *
     * @return The active window
     */
    Window getActiveFrame();
    
    /**
     * Adds a JMenuItem to the plugin menu.
     *
     * @param menuItem The menu item to be added.
     */
    void addPluginMenu(final JMenuItem menuItem);
    
    /**
     * Removes a JMenuItem from the plugin menu.
     *
     * @param menuItem The menu item to be removed.
     */
    void removePluginMenu(final JMenuItem menuItem);
    
    /**
     * Quits the client.
     */
    void quit();
    
    /**
     * Sets whether or not the internal frame state is currently maximised.
     * 
     * @param max whether the frame is maxomised
     */
    void setMaximised(final boolean max);
    
    /**
     * Gets whether or not the internal frame state is currently maximised.
     * 
     * @return True iff frames should be maximised, false otherwise
     */
    boolean getMaximised();
    
    /**
     * Returns a prefix for use in the titlebar. Includes the version number
     * if the config option is set.
     * 
     * @return Titlebar prefix
     */
    String getTitlePrefix();
    
    /**
     * Sets the main window's title.
     * 
     * @param newTitle The new title to use for the main window.
     */
    void setTitle(final String newTitle);
    
    /**
     * Shows or hides the main window.
     *
     * @param visible The new visibility of the main window
     */
    void setVisible(final boolean visible);
    
    /**
     * Returns whether the window is visible.
     *
     * @return Whether the main window is visible
     */
    boolean isVisible();
    
}
