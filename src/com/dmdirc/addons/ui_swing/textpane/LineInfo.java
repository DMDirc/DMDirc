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
 * Information about a position in a line.
 */
public final class LineInfo {

    /** Line number. */
    private int line;
    /** What part of a line. */
    private int part;
    /** Character index? */
    private int index;

    /** 
     * Creates a new instance of LineInfo. 
     *
     * @param line Line number
     * @param part line wrap number
     */
    public LineInfo(final int line, final int part) {
        this(line, part, -1);
    }

    /** 
     * Creates a new instance of LineInfo. 
     *
     * @param line Line number
     * @param part line wrap number
     * @param index Position index
     */
    public LineInfo(final int line, final int part, final int index) {
        this.line = line;
        this.part = part;
        this.index = index;
    }

    /**
     * Returns the line number of this object.
     *
     * @return Line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the part for this line.
     *
     * @return Part number
     */
    public int getPart() {
        return part;
    }

    /**
     * Returns the index for this line.
     * 
     * @return Index for the line of -1
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Sets the index for this line.
     * 
     * @param index New index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Sets the line for this line.
     * 
     * @param line New line
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Sets the line part for this line
     * 
     * @param part New part
     */
    public void setPart(int part) {
        this.part = part;
    }
    
    /* {@inheritDoc} */
    @Override
    public String toString() {
        return "LineInfo[line=" + line + ", part=" + part + ", index=" + index + "]";
    }
}
