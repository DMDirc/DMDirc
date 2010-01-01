/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
     * @param endLine Ending line
     * @param startPos Starting position
     * @param endPos Ending position
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
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    /**
     * Sets the positions end line.
     * 
     * @param endPos new end line
     */
    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    /**
     * Sets the positions start line.
     * 
     * @param startLine new start line
     */
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    /**
     * Sets the positions start position.
     * 
     * @param startPos new start position
     */
    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Position[" + startLine + ", " + startPos + ", " + endLine + ", " + endPos + "]";
    }
}
