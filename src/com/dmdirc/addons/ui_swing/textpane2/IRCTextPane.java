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

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

/** Text pane designed for IRC. */
public class IRCTextPane extends JTextPane {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** Instantiates a new IRCTextPane. */
    public IRCTextPane() {
        this(new IRCDocument());
    }

    /**
     * Instantiates a new IRCTextPane with the specified document.
     *
     * @param doc
     */
    public IRCTextPane(final IRCDocument doc) {
        super(doc);
        super.setEditable(false);
    }

    /**
     * Adds an array of unparsed styled strings to the textpane.
     *
     * @param strings Unstyled lines to add
     */
    public void addStyledString(final String[] strings) {
        //Styliser.addStyledString((StyledDocument) getDocument(), string);
        for (String str : strings) {
            try {
                getDocument().insertString(getDocument().getLength(), str, null);
            } catch (BadLocationException ex) {
                Logger.appError(ErrorLevel.HIGH, "Adding a line to the document failed.", ex);
            }
        }
    }

    /**
     * Adds an unparsed styled string to the textpane.
     *
     * @param string Unstyled line to add
     */
    public void addStyledString(final String string) {
        addStyledString(new String[]{string});
    }

    /** Clears the textpane. */
    public void clear() {
        setDocument(new IRCDocument());
    }

    /** Clears the selection in the textpane. */
    public void clearSelection() {
        setSelectionStart(0);
        setSelectionEnd(0);
    }

    /**
     * Trims the textpane to the specified size.
     *
     * @param frameBufferSize Size to trim the textpane to
     */
    public void trim(final int frameBufferSize) {
        //trim
    }

    /** {@inheritDoc} */
    @Override
    public void setEditable(final boolean b) {
        //Ignore
    }
}