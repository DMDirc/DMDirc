/*
 *  Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.dialogs;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Extension of JOptionPane to allow Word Wrapping.
 *
 * @author shane
 */
public class DMDircJOptionPane extends JOptionPane {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Maximum characters per line. */
    private int maxCharactersPerLineCount = Integer.MAX_VALUE;
    /** Title of Dialog. */
    private String title = "";

    /**
     * Create a DMDircJOptionPane.
     *
     * @param title Title to use
     * @param message Message to show
     * @param messageType Message Type.
     * @param optionType Options.
     */
    public DMDircJOptionPane(final String title, final String message,
            final int messageType, final int optionType) {
        super(message, messageType, optionType);
        this.title = title;
    }

    /**
     * Set the maximum number of characters per line.
     *
     * @param maxCharactersPerLineCount
     */
    public void setMaxCharactersPerLineCount(final int maxCharactersPerLineCount) {
        this.maxCharactersPerLineCount = maxCharactersPerLineCount;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCharactersPerLineCount() {
        return this.maxCharactersPerLineCount;
    }

    /** Show this dialog. */
    public void showDialog() {
        // Recalculate width, because JOptionPane calculates it before the
        // constructor gets a chance to finish, and so gets a max chars of 0.
        final Object oldMessage = this.getMessage();
        this.setMessage("");
        this.setMessage(oldMessage);

        final JDialog dialog = createDialog(title);
        dialog.setVisible(true);
        dialog.dispose();
    }
}
