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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinePositionTest {

    @Test
    public void testNormaliseWithForwardMultiLineSelection() {
        final LinePosition input = new LinePosition(2, 5, 4, 1);
        assertEquals(input.toString(), input.getNormalised().toString());
    }

    @Test
    public void testNormaliseWithForwardSingleLineSelection() {
        final LinePosition input = new LinePosition(2, 5, 2, 8);
        assertEquals(input.toString(), input.getNormalised().toString());
    }

    @Test
    public void testNormaliseWithBackwardMultiLineSelection() {
        final LinePosition input = new LinePosition(5, 8, 2, 5);
        final LinePosition expected = new LinePosition(2, 5, 5, 8);
        assertEquals(expected.toString(), input.getNormalised().toString());
    }

    @Test
    public void testNormaliseWithBackwardSingleLineSelection() {
        final LinePosition input = new LinePosition(2, 8, 2, 5);
        final LinePosition expected = new LinePosition(2, 5, 2, 8);
        assertEquals(expected.toString(), input.getNormalised().toString());
    }

    @Test
    public void testCloneConstructor() {
        final LinePosition input = new LinePosition(2, 4, 6, 8);
        assertEquals(input.toString(), new LinePosition(input).toString());
    }

    @Test
    public void testGetters() {
        final LinePosition input = new LinePosition(2, 4, 6, 8);
        assertEquals(2, input.getStartLine());
        assertEquals(4, input.getStartPos());
        assertEquals(6, input.getEndLine());
        assertEquals(8, input.getEndPos());
    }

    @Test
    public void testSetters() {
        final LinePosition input = new LinePosition(2, 4, 6, 8);

        input.setStartLine(17);
        assertEquals(17, input.getStartLine());
        input.setStartPos(27);
        assertEquals(27, input.getStartPos());
        input.setEndLine(13);
        assertEquals(13, input.getEndLine());
        input.setEndPos(786);
        assertEquals(786, input.getEndPos());

        final LinePosition expected = new LinePosition(17, 27, 13, 786);
        assertEquals(expected.toString(), input.toString());
    }

}