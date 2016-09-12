/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.events.DisplayProperty;
import java.util.Optional;

/**
 * Represents a line of text in IRC.
 */
public interface Line {

    /**
     * Returns the length of the specified line.
     *
     * @return Length of the line
     */
    int getLength();

    /**
     * Returns the height of the specified line.
     *
     * @return Line height
     */
    int getFontSize();

    /**
     * Sets the default font size for this line.
     *
     * @param fontSize New default font size
     */
    void setFontSize(final int fontSize);

    /**
     * Sets the default font name for this line.
     *
     * @param fontName New default font name
     */
    void setFontName(final String fontName);

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     */
    String getText();

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     *
     * @since 0.6.3m1
     */
    String getStyledText();

    /**
     * Converts a StyledDocument into an AttributedString.
     *
     * @return AttributedString representing the specified StyledDocument
     */
    <T> T getStyled(final StyledMessageMaker<T> maker);

    <T> Optional<T> getDisplayableProperty(DisplayProperty<T> property);
}
