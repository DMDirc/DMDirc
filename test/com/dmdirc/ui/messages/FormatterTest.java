/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormatterTest {
    
    @Before
    public void setUp() throws Exception {
        IdentityManager.load();
    }

    @Test
    public void testFormatMessage() {
        Formatter.registerDefault("unitTest", "abc %2$s %1$s def");
        
        // Standard format test
        assertEquals(Formatter.formatMessage("unitTest", "123", "456"), "abc 456 123 def");
        
        // Check unknown formats
        assertTrue(Formatter.formatMessage("unitTest123", "m").indexOf("No format string") > -1);
        
        Formatter.registerDefault("unitTest2", "abc %2$$$ZAS %1$s def");
        
        // And invalid formats
        assertTrue(Formatter.formatMessage("unitTest2", "m").indexOf("Invalid format string") > -1);
    }

    @Test
    public void testGetFormats() {
        final Set<String> s1 = Formatter.getFormats();
        Formatter.registerDefault("unitTest3", "abc");
        final Set<String> s2 = Formatter.getFormats();
        final Set<String> s3 = Formatter.getFormats();
        
        assertEquals(s2, s3);
        assertTrue(s1.size() + 1 == s2.size());
    }

    @Test
    public void testHasFormat() {
        final String[] targets = new String[]{"unknown", "abc", "def", "unittestfail"};
        
        for (String target : targets) {
            assertFalse(Formatter.hasFormat(target));
        }
        
        for (String target : Formatter.getFormats()) {
            assertTrue(Formatter.hasFormat(target));
        }
    }

    @Test
    public void testSaveAndLoad() {
        Formatter.registerDefault("unitTest_saveLoad", "");
        
        final String fileName = "unittest_formatter";
        final File file = new File(Main.getConfigDir() + fileName);
        
        if (file.exists()) {
            file.delete();
        }
        
        Formatter.saveAs(fileName);
        
        assertTrue(file.exists());
        
        Formatter.reload();
        
        Formatter.loadFile(fileName);
        
        assertTrue(Formatter.hasFormat("unitTest_saveLoad"));
        
        file.delete();
    }

    @Test
    public void testReload() {
        Formatter.reload();
        
        final Set<String> s1 = Formatter.getFormats();
        
        Formatter.registerDefault("UnitTestABCDEF", "");
        
        Formatter.reload();
        
        final Set<String> s2 = Formatter.getFormats();
        
        assertEquals(s1.size(), s2.size());
    }
    
}
