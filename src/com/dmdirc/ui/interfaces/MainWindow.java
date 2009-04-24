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

package com.dmdirc.ui.interfaces;

import javax.swing.ImageIcon;

/**
 * Main window interface.
 */
public interface MainWindow {
        
    /**
     * Retrieves the application icon.
     *
     * @return The application icon
     */
    ImageIcon getIcon();
    
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
