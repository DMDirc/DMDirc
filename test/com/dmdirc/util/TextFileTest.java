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

package com.dmdirc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class TextFileTest {
    
    private static File tfile;

    @Test
    public void testGetLines() throws IOException {
        final TextFile file =
                new TextFile(getClass().getClassLoader().
                getResource("com/dmdirc/util/test1.txt").openStream());
        final List<String> lines = file.getLines();

        assertEquals(7, lines.size());
        assertEquals("Line 1", lines.get(0));
    }
    
    @Test
    public void testGetLines2() throws IOException {
        final TextFile file =
                new TextFile(getClass().getClassLoader().
                getResource("com/dmdirc/util/test1.txt").openStream());
        final List<String> lines = file.getLines();

        assertEquals(7, lines.size());
        assertEquals("Line 1", lines.get(0));
    }
    
    @Test
    public void testWrite() throws IOException {
        tfile = File.createTempFile("dmdirc_unit_test", null);
        TextFile file = new TextFile(tfile);
        
        final List<String> lines = Arrays.asList(new String[]{
            "hello", "this is a test", "meep"
        });
        
        file.writeLines(lines);
        
        file = new TextFile(tfile);
        final List<String> newLines = file.getLines();
        
        assertEquals(lines, newLines);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testIllegalWrite() throws IOException {
        final TextFile file =
                new TextFile(getClass().getClassLoader().
                getResource("com/dmdirc/util/test1.txt").openStream());
        file.writeLines(Arrays.asList(new String[]{
            "hello", "this is a test", "meep"
        }));
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testIllegalDelete() throws IOException {
        final TextFile file =
                new TextFile(getClass().getClassLoader().
                getResource("com/dmdirc/util/test1.txt").openStream());
        file.delete();
    }
    
    @Test
    public void testDelete() {
        assertTrue(tfile.exists());
        TextFile file = new TextFile(tfile);
        file.delete();
        assertFalse(tfile.exists());
    }
    
}