/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.parser.irc;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParserErrorTest {

    private final ParserError fatal = new ParserError(ParserError.ERROR_FATAL, "moo", "");
    private final ParserError error = new ParserError(ParserError.ERROR_ERROR, "moo", "last line");
    private final ParserError except = new ParserError(ParserError.ERROR_EXCEPTION, "moo", null);
    private final ParserError warning = new ParserError(ParserError.ERROR_WARNING, "moo", "");
    private final ParserError uwarning = new ParserError(ParserError.ERROR_WARNING
            + ParserError.ERROR_USER, "moo", "");

    @Test
    public void testIsFatal() {
        assertTrue(fatal.isFatal());
        assertFalse(error.isFatal());
        assertFalse(except.isFatal());
        assertFalse(warning.isFatal());
    }

    @Test
    public void testIsError() {
        assertTrue(error.isError());
        assertFalse(fatal.isError());
        assertFalse(except.isError());
        assertFalse(warning.isError());
    }

    @Test
    public void testIsWarning() {
        assertFalse(error.isWarning());
        assertFalse(fatal.isWarning());
        assertFalse(except.isWarning());
        assertTrue(warning.isWarning());
    }

    @Test
    public void testIsException() {
        assertFalse(error.isException());
        assertFalse(fatal.isException());
        assertTrue(except.isException());
        assertFalse(warning.isException());        
    }

    @Test
    public void testHasLastLine() {
        assertTrue(error.hasLastLine());
        assertFalse(fatal.hasLastLine());
        assertFalse(except.hasLastLine());
        assertFalse(warning.hasLastLine());
    }
    
    @Test
    public void testGetLevel() {
        assertEquals(ParserError.ERROR_ERROR, error.getLevel());
        assertEquals(ParserError.ERROR_EXCEPTION, except.getLevel());
        assertEquals(ParserError.ERROR_FATAL, fatal.getLevel());
        assertEquals(ParserError.ERROR_WARNING, warning.getLevel());
    }    
    
    @Test
    public void testIsUser() {
        assertTrue(uwarning.isUserError());
        assertFalse(error.isUserError());
        assertFalse(fatal.isUserError());
        assertFalse(except.isUserError());
        assertFalse(warning.isUserError());
    }

    @Test
    public void testException() {
        fatal.setException(new Exception("foo"));
        
        assertTrue(fatal.isException());
        assertTrue(fatal.isFatal());
        assertNotNull(fatal.getException());
        assertEquals("foo", fatal.getException().getMessage());
        
        except.setException(new IllegalAccessException());
        
        assertTrue(except.isException());
        assertFalse(except.isFatal());
        assertFalse(except.isError());
        assertFalse(except.isWarning());
    }

    @Test
    public void testGetData() {
        assertEquals("moo", except.getData());
    }

    @Test
    public void testAppendData() {
        final String origin = warning.getData();
        warning.appendData("new data!");
        
        assertTrue(warning.getData().startsWith(origin));
        assertTrue(warning.getData().indexOf("new data!") > -1);
    }

    @Test
    public void testGetLastLine() {
        assertEquals("last line", error.getLastLine());
    }

}
