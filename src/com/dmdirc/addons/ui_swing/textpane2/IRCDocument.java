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

package com.dmdirc.addons.ui_swing.textpane2;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Segment;
import javax.swing.text.StyleContext;

/** Stylised document. */
public class IRCDocument extends DefaultStyledDocument {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** Instantiates a new IRCDocument. */
    public IRCDocument() {
        this(new StyleContext());
    }

    /**
     * Instantiates a new IRCDocument using the specified StyleContext.
     *
     * @param styles StyleContext to use
     */
    public IRCDocument(final StyleContext styles) {
        super(new DocumentContent(), styles);
    }

    public IRCDocument(Content c, StyleContext styles) {
        super(c, styles);
    }
    
    public String getText(int offset, int length) throws BadLocationException {
	if (length < 0) {
	    throw new BadLocationException("Length must be positive. Length = "+length, 0);
	}
	
	return super.getText(offset, length);
    }    
    
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
	if (length < 0) {
	    throw new BadLocationException("Length must be positive. Length = "+length, 0);
	}
	
        super.getText(offset, length, txt);
    }    
}