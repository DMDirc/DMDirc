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

package com.dmdirc.addons.logging;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import java.util.Stack;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReverseFileReaderTest extends junit.framework.TestCase {

    @Test
    public void testIndividual() {
        try {
            final ReverseFileReader reader =
                    new ReverseFileReader(new File(getClass().getClassLoader().
                    getResource("com/dmdirc/addons/logging/test1.txt").
                    toURI()));
            assertEquals("Line 7", reader.getNextLine());
            assertEquals("Line 6", reader.getNextLine());
            assertEquals("Line 5", reader.getNextLine());
            assertEquals("Line 4", reader.getNextLine());
            assertEquals("Line 3", reader.getNextLine());
            assertEquals("Line 2", reader.getNextLine());
            assertEquals("Line 1", reader.getNextLine());
            reader.close();
        } catch (URISyntaxException ex) {
            assertFalse(true);
        } catch (IOException ex) {
            assertFalse(true);
        }
    }
    
    @Test
    public void testStack() {
        try {
            final ReverseFileReader reader =
                    new ReverseFileReader(new File(getClass().getClassLoader().
                    getResource("com/dmdirc/addons/logging/test1.txt").
                    toURI()));
            final Stack<String> lines = reader.getLines(10);
            
            assertEquals(7, lines.size());
            assertEquals("Line 1", lines.pop());
            assertEquals("Line 2", lines.pop());
            assertEquals("Line 3", lines.pop());
            assertEquals("Line 4", lines.pop());
            assertEquals("Line 5", lines.pop());
            assertEquals("Line 6", lines.pop());
            assertEquals("Line 7", lines.pop());
            reader.close();
        } catch (URISyntaxException ex) {
            assertFalse(true);
        } catch (IOException ex) {
            assertFalse(true);
        }
    }
    
    @Test
    public void testSmallStack() {
        try {
            final ReverseFileReader reader =
                    new ReverseFileReader(new File(getClass().getClassLoader().
                    getResource("com/dmdirc/addons/logging/test1.txt").
                    toURI()));
            final Stack<String> lines = reader.getLines(3);
            
            assertEquals(3, lines.size());
            assertEquals("Line 5", lines.pop());
            assertEquals("Line 6", lines.pop());
            assertEquals("Line 7", lines.pop());
            reader.close();
        } catch (URISyntaxException ex) {
            assertFalse(true);
        } catch (IOException ex) {
            assertFalse(true);
        }
    }
    
    @Test
    public void testReset() {
        try {
            final ReverseFileReader reader =
                    new ReverseFileReader(new File(getClass().getClassLoader().
                    getResource("com/dmdirc/addons/logging/test1.txt").
                    toURI()));
            assertEquals("Line 7", reader.getNextLine());
            assertEquals("Line 6", reader.getNextLine());
            reader.reset();
            assertEquals("Line 7", reader.getNextLine());
            assertEquals("Line 6", reader.getNextLine());
            assertEquals("Line 5", reader.getNextLine());
            reader.close();
        } catch (URISyntaxException ex) {
            assertFalse(true);
        } catch (IOException ex) {
            assertFalse(true);
        }
    }    

}
