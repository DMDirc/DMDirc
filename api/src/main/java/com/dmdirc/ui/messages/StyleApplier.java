/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.ui.messages;

/**
 * The styliser applies IRC styles to text. Styles are indicated by various control codes which are
 * a de-facto IRC standard.
 */
public interface StyleApplier {

    /** Character used to indicate hyperlinks. */
    char CODE_HYPERLINK = 5;
    /** Character used to indicate channel links. */
    char CODE_CHANNEL = 6;
    /** Character used to indicate smilies. */
    char CODE_SMILIE = 7;
    /** Character used to indicate nickname links. */
    char CODE_NICKNAME = 16;
    /** The character used for tooltips. */
    char CODE_TOOLTIP = 19;

    /**
     * Stylises the specified strings and adds them to the specified maker.
     *
     * @param maker   The message maker to add styling to.
     * @param strings The lines to be stylised
     */
    void addStyledString(StyledMessageMaker<?> maker, String... strings);

    /**
     * Applies the hyperlink styles and intelligent linking regexps to the target.
     *
     * @param string The string to be linked
     *
     * @return A copy of the string with hyperlinks marked up
     */
    String doLinks(String string);

}
