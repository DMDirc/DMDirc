/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
package com.dmdirc.logger;

import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorReportStatusTest {

    @Test
    public void testToString() {
        assertTrue(ErrorReportStatus.ERROR.toString().toLowerCase().indexOf("error") > -1);
        assertTrue(ErrorReportStatus.WAITING.toString().toLowerCase().indexOf("wait") > -1);
        assertTrue(ErrorReportStatus.SENDING.toString().toLowerCase().indexOf("send") > -1);
        assertTrue(ErrorReportStatus.FINISHED.toString().toLowerCase().indexOf("finish") > -1);
        assertTrue(ErrorReportStatus.NOT_APPLICABLE.toString().toLowerCase().indexOf("not") > -1);
    }

}