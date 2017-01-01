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

/**
 * Holds information about a range of text.
 */
public class LinePosition {

    /** Starting line. */
    private int startLine;
    /** Ending line. */
    private int endLine;
    /** Starting position. */
    private int startPos;
    /** Ending position. */
    private int endPos;

    /**
     * Constructs a new line position.
     *
     * @param startLine Starting line
     * @param endLine   Ending line
     * @param startPos  Starting position
     * @param endPos    Ending position
     */
    public LinePosition(final int startLine, final int startPos,
            final int endLine, final int endPos) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    /**
     * Constructs a new line position.
     *
     * @param position Position to create position from
     */
    public LinePosition(final LinePosition position) {
        this.startLine = position.getStartLine();
        this.endLine = position.getEndLine();
        this.startPos = position.getStartPos();
        this.endPos = position.getEndPos();
    }

    /**
     * Returns the end line for this position.
     *
     * @return End line
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Returns the end position for this position.
     *
     * @return End position
     */
    public int getEndPos() {
        return endPos;
    }

    /**
     * Returns the start line for this position.
     *
     * @return Start line
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the start position for this position.
     *
     * @return Start position
     */
    public int getStartPos() {
        return startPos;
    }

    /**
     * Sets the positions end line.
     *
     * @param endLine new end line
     */
    public void setEndLine(final int endLine) {
        this.endLine = endLine;
    }

    /**
     * Sets the positions end line.
     *
     * @param endPos new end line
     */
    public void setEndPos(final int endPos) {
        this.endPos = endPos;
    }

    /**
     * Sets the positions start line.
     *
     * @param startLine new start line
     */
    public void setStartLine(final int startLine) {
        this.startLine = startLine;
    }

    /**
     * Sets the positions start position.
     *
     * @param startPos new start position
     */
    public void setStartPos(final int startPos) {
        this.startPos = startPos;
    }

    /**
     * Gets a new, normalised version of this position. Start and end lines and positions are
     * normalised such that the same range of lines and positions are included, but the start
     * line and position comes before the end line and position.
     *
     * @return A normalised copy of this position.
     */
    public LinePosition getNormalised() {
        if (startLine > endLine) {
            // Multi-line "backwards" selection; swap both lines and positions.
            return new LinePosition(endLine, endPos, startLine, startPos);
        } else if (startLine == endLine && startPos > endPos) {
            // Single-line "backwards" selection; just swap the positions.
            return new LinePosition(startLine, endPos, endLine, startPos);
        } else {
            // Single- or multi-line "forward" selection; swap nothing.
            return new LinePosition(startLine, startPos, endLine, endPos);
        }
    }

    @Override
    public String toString() {
        return "Position[" + startLine + ", " + startPos + ", " + endLine
                + ", " + endPos + ']';
    }

}
