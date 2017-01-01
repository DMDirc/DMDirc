/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.interfaces.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

/**
 * Input field interface.
 */
public interface InputField {

    /**
     * Adds an action listener to this input field.
     *
     * @param listener ActionListener to add
     */
    void addActionListener(final ActionListener listener);

    /**
     * Adds a keylistener to this input field.
     *
     * @param listener KeyListener to add
     */
    void addKeyListener(final KeyListener listener);

    /**
     * Does this input field have focus?
     *
     * @return true iif the input field has focus
     */
    boolean hasFocus();

    /**
     * Removes an action listener to this input field.
     *
     * @param listener ActionListener to remove
     */
    void removeActionListener(final ActionListener listener);

    /**
     * Removes a keylistener to this input field.
     *
     * @param listener KeyListener to remove
     */
    void removeKeyListener(final KeyListener listener);

    /**
     * Returns the selected text in the input field.
     *
     * @return Selected text
     */
    String getSelectedText();

    /**
     * Returns the end of the selection.
     *
     * @return Selection end
     */
    int getSelectionEnd();

    /**
     * Returns the start of the selection.
     *
     * @return Selection start
     */
    int getSelectionStart();

    /**
     * Returns the text in the input field.
     *
     * @return Input field text
     */
    String getText();

    /**
     * Sets the text in the input field to the specified text.
     *
     * @param text New text for the input field
     */
    void setText(final String text);

    /**
     * Returns the caret position.
     *
     * @return Caret position
     */
    int getCaretPosition();

    /**
     * Sets the caret position.
     *
     * @param position Caret position
     */
    void setCaretPosition(final int position);

    /**
     * Shows a colour picker for this input field.
     *
     * @param irc Show irc colours?
     * @param hex Show hex colours?
     */
    void showColourPicker(final boolean irc, final boolean hex);

    /**
     * Hides the colour picker for this input field.
     */
    void hideColourPicker();

}
