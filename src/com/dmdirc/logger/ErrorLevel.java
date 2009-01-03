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

package com.dmdirc.logger;

import com.dmdirc.ui.IconManager;

import javax.swing.Icon;

/** Specific error levels allowed by Logger. */
public enum ErrorLevel {
    /** Fatal error. */
    FATAL("Fatal", IconManager.getIconManager().getIcon("error")),
    /** High priority error. */
    HIGH("High", IconManager.getIconManager().getIcon("error")),
    /** Medium priority error. */
    MEDIUM("Medium", IconManager.getIconManager().getIcon("warning")),
    /** Low priority error. */
    LOW("Low", IconManager.getIconManager().getIcon("info")),
    /** Unknown priority error. */
    UNKNOWN("Unknown", IconManager.getIconManager().getIcon("info"));
    
    /** Error level string. */
    private String value;
    /** Error level icon. */
    private Icon icon;
    
    /** 
     * Instantiates the enum. 
     *
     * @param value toString value
     */
    ErrorLevel(final String value, final Icon icon) {
        this.value = value;
        this.icon = icon;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * Error levels icon.
     * 
     * @return Error levels icon
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Returns if the specified error is more important than this one
     *
     * @param level Error level to compare
     *
     * @return true iif the error is more important
     */
    public boolean moreImportant(final ErrorLevel level) {
        if (level == null) {
            return false;
        }
        
        return ordinal() > level.ordinal();
    }
}
