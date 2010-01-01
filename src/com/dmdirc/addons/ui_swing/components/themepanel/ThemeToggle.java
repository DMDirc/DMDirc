/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.ui_swing.components.themepanel;

import com.dmdirc.ui.themes.Theme;

/**
 * Wraps a Theme object with a boolean to indicate whether it should be
 * enabled or not.
 * 
 * @author chris
 */
public class ThemeToggle {
    
    /** The Theme object we're wrapping. */
    private final Theme theme;
    
    /** Whether or not to enable it. */
    private boolean enable;

    /**
     * Creates a new instance of ThemeToggle to wrap the specified
     * Theme.
     * 
     * @param theme The theme to be wrapped
     */
    public ThemeToggle(final Theme theme) {
        this.theme = theme;
        enable = theme.isEnabled();
    }
    
    /**
     * Toggles this theme.
     */
    public void toggle() {
        enable = !enable;
    }
    
    /**
     * Gets the state of this ThemeToggle.
     * 
     * @return True if the theme is or should be loaded, false otherwise.
     */
    public boolean getState() {
        return enable;
    }

    /**
     * Retrieves the Theme object associated with this toggle.
     * 
     * @return This toggle's theme object.
     */
    public Theme getTheme() {
        return theme;
    }

}
