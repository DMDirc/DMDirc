/*
 * 
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.core.util;

import java.text.AttributedString;

/**
 * Wraps attributed string adding other accessible information.
 */
public class ExtendedAttributedString {

    private int lineHeight = 0;
    private AttributedString string;

    /**
     * Creates a new extended attributed string with the specified information.
     *
     * @param string Attributed string
     * @param lineHeight Line height
     */
    public ExtendedAttributedString(final AttributedString string, final int lineHeight) {
        this.lineHeight = lineHeight;
        this.string = string;
    }

    /**
     * Sets the maximum height of this line.
     *
     * @param lineHeight Line height
     */
    public void setMaxHeight(final int lineHeight) {
        this.lineHeight = lineHeight;
    }

    /**
     * Returns the maximum height of this string.
     *
     * @return Maximum height
     */
    public int getMaxLineHeight() {
        return lineHeight;
    }

    /**
     * Sets the attributed string.
     *
     * @param string Attributed string
     */
    public void setAttributedString(final AttributedString string) {
        this.string = string;
    }

    /**
     * Gets the attributed string.
     *
     * @return Attributed String
     */
    public AttributedString getAttributedString() {
        return string;
    }
}
