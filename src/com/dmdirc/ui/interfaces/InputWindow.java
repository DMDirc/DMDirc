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

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.input.InputHandler;

/**
 * The Input Window interface specifies additional methods that windows should
 * implement if they have an input field.
 * 
 * @author chris
 */
public interface InputWindow extends Window {
    
    /**
     * Retrieves the command Parser for this input window.
     * 
     * @return This window's command parser
     */
    CommandParser getCommandParser();
    
    /**
     * Retrieves the input handler for this input window.
     * 
     * @return This window's input handler
     */
    InputHandler getInputHandler();
    
    /**
     * Toggles the away-status indicator for this input window, if the UI
     * supports it.
     * 
     * @param isAway Whether the away indicator should be displayed or not
     */
    void setAwayIndicator(boolean isAway);
    
    /**
     * Retrieves the container that owns this command window.
     *
     * @return The container that owns this command window.
     */
    @Override
    WritableFrameContainer getContainer();    
    
}
