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
import com.dmdirc.util.collections.RollingList;

/**
 * Wraps an {@link IRCDocument} and caches recent lines.
 */
public class CachingDocument<T> {

    /** The document to wrap and cache data from. */
    private final Document document;
    /** The maker to use to produce styled lines. */
    private final StyledMessageMaker<T> maker;
    /** Cached lines. */
    private final RollingList<Line> cachedLines;
    /** Cached attributed strings. */
    private final RollingList<T> cachedStrings;

    public CachingDocument(final Document document, final StyledMessageMaker<T> maker) {
        this.document = document;
        this.maker = maker;

        cachedLines = new RollingList<>(50);
        cachedStrings = new RollingList<>(50);
    }

    /**
     * Returns an attributed character iterator for a particular line, utilising the document cache
     * where possible.
     *
     * @param line Line to be styled
     *
     * @return Styled line
     */
    protected T getStyledLine(final Line line) {
        T styledLine = null;

        if (cachedLines.contains(line)) {
            final int index = cachedLines.getList().indexOf(line);
            styledLine = cachedStrings.get(index);
        }

        if (styledLine == null) {
            line.getDisplayableProperty(DisplayProperty.FOREGROUND_COLOUR)
                    .ifPresent(maker::setDefaultForeground);
            line.getDisplayableProperty(DisplayProperty.BACKGROUND_COLOUR)
                    .ifPresent(maker::setDefaultBackground);

            styledLine = line.getStyled(maker);
            cachedLines.add(line);
            cachedStrings.add(styledLine);
        }

        return styledLine;
    }

    /**
     * Returns an attributed string for a particular line, utilising the document cache where
     * possible.
     *
     * @param line Line number to be styled
     *
     * @return Styled line
     */
    public T getStyledLine(final int line) {
        return getStyledLine(document.getLine(line));
    }

    public int getNumLines() {
        return document.getNumLines();
    }

    public Line getLine(final int line) {
        return document.getLine(line);
    }

}
