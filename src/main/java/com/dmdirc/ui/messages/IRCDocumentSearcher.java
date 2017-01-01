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

package com.dmdirc.ui.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Searches the textpane for specified phrases.
 */
public class IRCDocumentSearcher {

    /** Document to search. */
    private final Document document;
    /** Phrase to search for. */
    private final String phrase;
    /** Textpane position. */
    private LinePosition position;
    /** Case sensitive? */
    private final boolean caseSensitive;

    /**
     * Constructs a new IRC Document searcher.
     *
     * @param phrase        Phrase to search for
     * @param document      Document to search
     * @param caseSensitive Whether or not this searcher is case sensitive
     */
    public IRCDocumentSearcher(final String phrase, final Document document,
            final boolean caseSensitive) {
        this.phrase = phrase;
        this.document = document;
        this.position = getEndPosition();
        this.caseSensitive = caseSensitive;
    }

    /**
     * Returns the end position in the document.
     *
     * @return End position
     */
    private LinePosition getEndPosition() {
        final int documentSize = document.getNumLines() - 1;
        final int lineLength;
        if (documentSize >= 0) {
            lineLength = document.getLine(documentSize).getLength();
        } else {
            lineLength = 0;
        }

        return new LinePosition(documentSize, lineLength, documentSize,
                lineLength);
    }

    /**
     * Sets the position of the current match
     *
     * @param position New match position
     */
    public void setPosition(final LinePosition position) {
        this.position = position;
    }

    /**
     * Searches up in the document.
     *
     * @return Line position of the next match
     */
    public LinePosition searchUp() {
        if (position == null) {
            position = getEndPosition();
        }

        int line = position.getEndLine();
        for (int remaining = document.getNumLines(); remaining > 0; remaining--) {
            if (line < 0) {
                line = 0;
            }
            final String lineText = document.getLine(line).getText();

            final List<LinePosition> matches = searchLine(line, lineText);

            for (int i = matches.size() - 1; i >= 0; i--) {
                if (position.getEndLine() != line
                        || matches.get(i).getEndPos() < position.getEndPos()) {
                    return matches.get(i);
                }
            }

            line--;

            if (line < 0) {
                line += document.getNumLines();
            }

        }

        return null;
    }

    /**
     * Searches down in the document.
     *
     * @return Line position of the next match
     */
    public LinePosition searchDown() {
        if (position == null) {
            position = getEndPosition();
        }

        int line = position.getStartLine();
        for (int remaining = document.getNumLines(); remaining > 0; remaining--) {
            if (line < 0) {
                line = 0;
            }
            final String lineText = document.getLine(line).getText();

            final List<LinePosition> matches = searchLine(line, lineText);

            for (LinePosition match : matches) {
                if (position.getStartLine() != line
                        || match.getStartPos() > position.getStartPos()) {
                    return match;
                }
            }

            line++;

            if (line >= document.getNumLines()) {
                line -= document.getNumLines();
            }
        }

        return null;
    }

    /**
     * Searches a line and returns all matches on a line.
     *
     * @param lineNum the line number of the line we're searching
     * @param line    Line to search
     *
     * @return List of matches
     */
    private List<LinePosition> searchLine(final int lineNum, final String line) {
        final List<LinePosition> matches = new ArrayList<>();
        final Matcher matcher = Pattern.compile((caseSensitive ? "" : "(?i)")
                + "\\Q" + phrase + "\\E").matcher(line);

        while (matcher.find()) {
            matches.add(new LinePosition(lineNum, matcher.start(), lineNum, matcher.end()));
        }

        return matches;
    }

}
