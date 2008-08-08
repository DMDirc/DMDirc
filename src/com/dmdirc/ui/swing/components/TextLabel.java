/*
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

package com.dmdirc.ui.swing.components;

import java.awt.Insets;

import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
    /** Simple attribute set. */
    private SimpleAttributeSet sas;

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

        sas = new SimpleAttributeSet();
        if (justified) {
            StyleConstants.setAlignment(sas, StyleConstants.ALIGN_JUSTIFIED);
        }
        StyleConstants.setFontFamily(sas, getFont().getFamily());
        StyleConstants.setFontSize(sas, getFont().getSize());
        StyleConstants.setBold(sas, getFont().isBold());
        StyleConstants.setItalic(sas, getFont().isItalic());

        setText(text);
    }

    /** {@inheritDoc} */
    @Override
    public void setText(final String t) {
        super.setText(t);
        if (t != null && !t.isEmpty()) {
            ((StyledDocument) getDocument()).setParagraphAttributes(0,
                    t.length(), sas, true);
        }
    }
}
