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

package com.dmdirc.addons.ui_swing.components.text;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

/**
 * Dyamnic text label with hyperlink support.
 */
public class HTMLLabel extends JEditorPane {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new instance of TextLabel.
     */
    public HTMLLabel() {
        this(null);
    }

    /**
     * Creates a new instance of TextLabel.
     *
     * @param text Text to display
     */
    public HTMLLabel(final String text) {
        super("text/html", text);

        final StyleSheet styleSheet = ((HTMLDocument) getDocument()).getStyleSheet();
        final Font font = UIManager.getFont("Label.font");
        final Color colour = UIManager.getColor("Label.foreground");
        styleSheet.addRule("body { font-family: " + font.getFamily()
                + "; " + "font-size: " + font.getSize() + "pt; }");
        styleSheet.addRule("* { color: rgb(" + colour.getRed()
                + ", " + colour.getGreen() + ", " + colour.getBlue()+"); }");

        init();
    }

    /** Initialiases the component. */
    private void init() {
        setUI(new BasicTextPaneUI());
        setOpaque(false);
        setEditable(false);
        setHighlighter(null);
        setFont(UIManager.getFont("TextField.font"));
    }
}
