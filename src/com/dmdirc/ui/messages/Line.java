/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.ui.core.util.ExtendedAttributedString;
import com.dmdirc.ui.core.util.Utils;

import java.text.AttributedString;
import java.util.Arrays;

/**
 * Represents a line of text in IRC.
 */
public class Line {

    private final String[] lineParts;
    private final Styliser styliser;
    private int fontSize;
    private String fontName;

    /**
     * Creates a new line with a specified height.
     *
     * @param styliser  The styliser to use to style this line
     * @param lineParts Parts of the line
     * @param fontSize  The height for this line
     * @param fontName  The name of the font to use for this line
     */
    public Line(final Styliser styliser, final String[] lineParts,
            final int fontSize, final String fontName) {
        this.styliser = styliser;
        this.lineParts = lineParts;
        this.fontName = fontName;
        this.fontSize = fontSize;
    }

    /**
     * Returns the line parts of this line.
     *
     * @return Lines parts
     */
    public String[] getLineParts() {
        return lineParts;
    }

    /**
     * Returns the length of the specified line.
     *
     * @return Length of the line
     */
    public int getLength() {
        int length = 0;
        for (String linePart : lineParts) {
            length += linePart.length();
        }
        return length;
    }

    /**
     * Returns the height of the specified line.
     *
     * @return Line height
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the default font size for this line.
     *
     * @param fontSize New default font size
     */
    public void setFontSize(final int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Sets the default font name for this line.
     *
     * @param fontName New default font name
     */
    public void setFontName(final String fontName) {
        this.fontName = fontName;
    }

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     */
    public String getText() {
        final StringBuilder lineText = new StringBuilder();
        for (String linePart : lineParts) {
            lineText.append(linePart);
        }
        return Styliser.stipControlCodes(lineText.toString());
    }

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     *
     * @since 0.6.3m1
     */
    public String getStyledText() {
        final StringBuilder lineText = new StringBuilder();
        for (String linePart : lineParts) {
            lineText.append(linePart);
        }
        return lineText.toString();
    }

    /**
     * Converts a StyledDocument into an AttributedString.
     *
     * @return AttributedString representing the specified StyledDocument
     */
    public AttributedString getStyled() {
        final ExtendedAttributedString string = Utils.getAttributedString(
                styliser, lineParts, fontName, fontSize);
        fontSize = string.getMaxLineHeight();
        return string.getAttributedString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Line && Arrays.equals(((Line) obj).getLineParts(), getLineParts());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Arrays.hashCode(getLineParts());
    }

}
