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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.ui.core.util.ExtendedAttributedString;
import com.dmdirc.ui.core.util.Utils;
import com.dmdirc.ui.messages.Styliser;

import java.text.AttributedString;
import java.util.Arrays;

import javax.swing.UIManager;

/**
 * Represents a line of text in IRC.
 */
class Line {

    private final String[] lineParts;
    private final ConfigManager config;
    private int lineHeight;

    /**
     * Creates a new line.
     *
     * @param lineParts Parts of the line
     * @param config Configuration manager for this line
     */
    public Line(final String[] lineParts, final ConfigManager config) {
        this.lineParts = lineParts;
        this.config = config;
        this.lineHeight = -1;
        if (config.hasOptionString("ui", "textPaneFontSize")) {
            this.lineHeight = config.getOptionInt("ui", "textPaneFontSize");
        } else {
            this.lineHeight = UIManager.getFont("TextPane.font").getSize();
        }
    }

    /**
     * Creates a new line with a specified height.
     *
     * @param lineParts Parts of the line
     * @param config Configuration manager for this line
     * @param lineHeight The height for this line
     */
    public Line(final String[] lineParts, final ConfigManager config,
            final int lineHeight) {
        this.lineParts = lineParts;
        this.config = config;
        this.lineHeight = lineHeight;
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
     * Returns the length of the specified line
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
    public int getHeight() {
        return lineHeight;
    }

    /**
     * Returns the Line text at the specified number.
     *
     * @return Line at the specified number or null
     */
    public String getText() {
        StringBuilder lineText = new StringBuilder();
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
        StringBuilder lineText = new StringBuilder();
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
        final ExtendedAttributedString string = Utils.getAttributedString(lineParts,
                config);
        lineHeight = string.getMaxLineHeight();
        return string.getAttributedString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            return Arrays.equals(((Line) obj).getLineParts(), getLineParts());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getLineParts().hashCode();
    }
}
