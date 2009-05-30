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

import com.dmdirc.FrameContainer;
import com.dmdirc.util.StringTranscoder;
import com.dmdirc.config.ConfigManager;

/**
 * The Window interface specifies common methods that should be implemented
 * by all windows. It is assumed that all windows have a main text area.
 */
public interface Window {
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area.
     *
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    void addLine(String messageType, Object... args);
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area.
     *
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    void addLine(StringBuffer messageType, Object... args);
    
    /**
     * Adds the specified raw line to the window, without using a formatter.
     * 
     * @param line The line to be added
     * @param timestamp Whether or not to display the timestamp for this line
     */
    void addLine(final String line, final boolean timestamp);
    
    /**
     * Clears the main text area of the command window.
     */
    void clear();
    
    /**
     * Retrieves the config manager for this command window.
     *
     * @return This window's config manager
     */
    ConfigManager getConfigManager();
    
    /**
     * Retrieves the container that owns this command window.
     *
     * @return The container that owns this command window.
     */
    FrameContainer getContainer();
    
    /**
     * Determines if the current window is visible.
     *
     * @return boolean visibility
     */
    boolean isVisible();
    
    /**
     * Sets the visibility of this window.
     * 
     * @param isVisible Whether the window should be visible or not
     */
    void setVisible(boolean isVisible);
    
    /**
     * Retrives the current title of this window.
     * 
     * @return This window's title
     */
    String getTitle();
    
    /**
     * Determines if this frame is currently maximised.
     * 
     * @return true if the frame is maximised, false otherwise
     */
    boolean isMaximum();
    
    /**
     * Sets the title of this window.
     * 
     * @param title The new title to be used.
     */
    void setTitle(String title);
    
    /**
     * Opens this window.
     */
    void open();
    
    /**
     * Restores this window.
     * 
     * @since 0.6.3m1
     */
    void restore();
    
    
    /** 
     * Maximises this window.
     * 
     * @since 0.6.3m1
     */
    void maximise();
    
    /**
     * Toggles Maximise State.
     * 
     * @since 0.6.3m1
     */
    void toggleMaximise();
    
    /**
     * Minimises this window.
     */
    void minimise();
    
    /**
     * Returns the transcoder that is being used by the UI.
     * 
     * @return This window's transcoder
     */
    StringTranscoder getTranscoder();
    
    /** Closes this window. */
    void close();

    /**
     * Requests that this object's frame be activated.
     */
    public void activateFrame();
    
}
