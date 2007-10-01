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

package com.dmdirc.ui.input.tabstyles;

import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Defines the methods that should be implemented by tab completion styles.
 * Styles control the result of tab completing.
 * 
 * @author Chris
 */
public abstract class TabCompletionStyle {
        
    /** The tab completer that we use. */
    protected TabCompleter tabCompleter;
    
    /** The input window that we use. */
    protected InputWindow window;
    
    /**
     * Sets this style's tab completer and window to the ones specified.
     * 
     * @param completer The new tab completer to use
     * @param window The window in which the tab completer is used
     */
    public void setContext(final TabCompleter completer, final InputWindow window) {
        this.tabCompleter = completer;
        this.window = window;
    }
    
    /**
     * Retrieves a name for this completion style.
     * 
     * @return The name for this completion style
     */
    public abstract String getName();
    
    /**
     * Retrieves this style's result for the specified parameters.
     * 
     * @param original The original string which the user inputted
     * @param start The start offset of the word that's being tab-completed
     * @param end The end offset of the word that's being tab-completed
     * @param additional A list of additional targets which may match
     * @return This style's proposed result
     */
    public abstract TabCompletionResult getResult(final String original,
            final int start, final int end, final AdditionalTabTargets additional);
    
}