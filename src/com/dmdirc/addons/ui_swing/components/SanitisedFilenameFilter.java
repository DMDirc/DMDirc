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

package com.dmdirc.addons.ui_swing.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Filters input to create valid filenames.
 */
public class SanitisedFilenameFilter extends DocumentFilter {
    
    /** Invalid filename characters. */
    private static final String INVALID_CHARS = "^[^\\w\\.\\s\\-\\#\\&\\_]";
    
    /** Creates a new instance of SanitisedFilenameFilter. */
    public SanitisedFilenameFilter() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public void insertString(final DocumentFilter.FilterBypass fb,
            final int offset, final String string, final AttributeSet attr)
            throws BadLocationException {
        
        if (string == null || string.isEmpty()) {
            return;
        } else {
            replace(fb, offset, string.length(), string, attr);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void remove(final DocumentFilter.FilterBypass fb, final int offset,
            final int length) throws BadLocationException {
        
        replace(fb, offset, length, "", null);
    }
    
    /** {@inheritDoc} */
    @Override
    public void replace(final DocumentFilter.FilterBypass fb, final int offset,
            final int length, final String text, final AttributeSet attrs)
            throws BadLocationException {
        
        fb.replace(offset, length, sanitise(text), attrs);
    }
    
    /** Sanitises the proposed value. */
    private String sanitise(final String proposedValue) {
        return proposedValue.replaceAll(INVALID_CHARS, "");
    }
    
}
