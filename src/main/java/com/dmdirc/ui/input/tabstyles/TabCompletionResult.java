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

package com.dmdirc.ui.input.tabstyles;

/**
 * Details the proposed result of tab completion. Results consist of a new string (to replace the
 * current one), and a new caret position.
 */
public class TabCompletionResult {

    /** The new string to use in the result. */
    private final String text;
    /** The new caret position to be used. */
    private final int position;

    /**
     * Creates a new instance of TabCompletionResult.
     *
     * @param text     The text for this result
     * @param position The caret position for this result
     */
    public TabCompletionResult(final String text, final int position) {
        this.text = text;
        this.position = position;
    }

    /**
     * Retrieves the replacement string for this result.
     *
     * @return This result's text
     */
    public String getText() {
        return text;
    }

    /**
     * Retrieves the new caret position for this result.
     *
     * @return This result's caret position
     */
    public int getPosition() {
        return position;
    }

}
