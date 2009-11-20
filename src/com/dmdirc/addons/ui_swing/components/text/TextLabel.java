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

package com.dmdirc.addons.ui_swing.components.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

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
        setEditorKit(new HTMLEditorKit());
        setUI(new BasicTextPaneUI());

        final StyleSheet styleSheet = ((HTMLDocument) getDocument()).getStyleSheet();
        final Font font = UIManager.getFont("Label.font");
        final Color colour = UIManager.getColor("Label.foreground");
        styleSheet.addRule("body " 
                + "{ font-family: " + font.getFamily() + "; "
                + "font-size: " + font.getSize() + "pt; }");
        styleSheet.addRule("p { margin: 0; }");
        styleSheet.addRule("* { color: rgb(" + colour.getRed()
                + ", " + colour.getGreen() + ", " + colour.getBlue()+"); }");

        setOpaque(false);
        setEditable(false);
        setHighlighter(null);
        setMargin(new Insets(0, 0, 0, 0));

        sas = new SimpleAttributeSet();
        if (justified) {
            StyleConstants.setAlignment(sas, StyleConstants.ALIGN_JUSTIFIED);
        }

        setText(text);
    }

    /** {@inheritDoc} */
    @Override
    public StyledDocument getDocument() {
        return (StyledDocument) super.getDocument();
    }

    /** {@inheritDoc} */
    @Override
    public void setText(final String t) {
        super.setText(t);
        if (t != null && !t.isEmpty()) {
            getDocument().setParagraphAttributes(0, t.length(), sas, false);
        }
    }
}
