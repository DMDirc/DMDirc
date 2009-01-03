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

import javax.swing.JComponent;

/**
 * A frame manager is a widget that allows the user to navigate between the
 * various frames that will be open at any one time.
 * 
 * @author chris
 */
public interface FrameManager {
    
    /**
     * Sets the parent component of this frame manager. The frame manager
     * should render itself within the parent.
     * 
     * @param parent The parent control
     */
    void setParent(JComponent parent);
    
    /**
     * Indicates whether this frame manager can be positioned vertically
     * (i.e., at the side of the screen).
     * 
     * @return True iff the frame manager can be positioned vertically
     */
    boolean canPositionVertically();
    
    /**
     * Indicates whether this frame manager can be positioned horizontally
     * (i.e., at the top or bottom of the screen).
     * 
     * @return True iff the frame manager can be positioned horizontally
     */
    boolean canPositionHorizontally();
    
    /**
     * Adds a window to this frame manager.
     * 
     * @param window The server to be added
     */
    void addWindow(FrameContainer window);
    
    /**
     * Removes a window from this frame manager.
     * 
     * @param window The server to be removed
     */
    void delWindow(FrameContainer window);   
    
    /**
     * Adds a new window to this frame manager.
     * 
     * @param parent The parent to which the window belongs, or null
     * @param window The custom window to be added
     */
    void addWindow(FrameContainer parent, FrameContainer window);
    
    /**
     * Removes a window from this frame manager.
     * 
     * @param parent The parent to which the window belongs, or null
     * @param window The custom window to be removed
     */
    void delWindow(FrameContainer parent, FrameContainer window);

}
