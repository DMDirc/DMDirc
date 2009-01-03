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

import java.util.Locale;

/**
 * Frame manager position enum.
 */
public enum FramemanagerPosition {
    /** Top of the window. */
    TOP,
    /** Left side of the window. */
    LEFT,
    /** Bottom of the window. */
    BOTTOM,
    /** Right side of the window. */
    RIGHT,
    /** Unknown position. */
    UNKNOWN;
    
    /**
     * Returns the frame position for s specified string.
     *
     * @param name Name of the string
     *
     * @return FramemanagerPosition for the string
     */
    public static FramemanagerPosition getPosition(final String name) {
        if (name == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(name.toUpperCase(Locale.getDefault()));
        } catch (IllegalArgumentException ex) {
            return valueOf("UNKNOWN");
        }
    }
    
    /**
     * Determines if this position is one of the two horizontal positions.
     * 
     * @return True if this is a horizontal position, false otherwise
     */
    public boolean isHorizontal() {
        return this == TOP || this == BOTTOM;
    }
}
