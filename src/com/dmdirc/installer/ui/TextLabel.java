/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.installer.ui;

import java.awt.Insets;

import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

/**
 * Dyamnic text label.
 */
public class TextLabel extends JTextPane {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new instance of TextLabel.
     */
    public TextLabel() {
        this(null, true);
    }

    /**
     * Creates a new instance of TextLabel.
     *
     * @param text Text to display
     */
    public TextLabel(final String text) {
        this(text, true);
    }

    /**
     * Creates a new instance of TextLabel.
     *
     * @param text Text to display
     * @param justified Justify the text?
     */
    public TextLabel(final String text, final boolean justified) {
        super(new DefaultStyledDocument());

        setOpaque(false);
        setEditable(false);
        setHighlighter(null);
        setMargin(new Insets(0, 0, 0, 0));

        setText(text);
    }
}
