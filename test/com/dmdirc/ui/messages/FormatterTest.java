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
package com.dmdirc.ui.messages;

import com.dmdirc.config.ConfigManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormatterTest extends junit.framework.TestCase {
    
    private final MyConfigManager mcm = new MyConfigManager();
    
    @Test
    public void testBasicFormats() {
        assertEquals("Hello!", Formatter.formatMessage(mcm, "1%1$s", "Hello!"));
        assertEquals("Hello!", Formatter.formatMessage(mcm, "1%1$s", "Hello!", "Moo!", "Bar!"));
        assertTrue(Formatter.formatMessage(mcm, "0%1$s", "Hello!")
                .toLowerCase().indexOf("no format string") > -1);
        assertTrue(Formatter.formatMessage(mcm, "1%5$s", "Hello!")
                .toLowerCase().indexOf("invalid format string") > -1);
        assertTrue(Formatter.formatMessage(mcm, "1%1$Z", "Hello!")
                .toLowerCase().indexOf("invalid format string") > -1);
    }
    
    @Test
    public void testCasting() {
        assertEquals("H", Formatter.formatMessage(mcm, "1%1$c", "Hello!"));
        assertEquals("10", Formatter.formatMessage(mcm, "1%1$d", "10"));
        assertEquals("111999", Formatter.formatMessage(mcm, "1%1$s", "111999"));
    }


    private class MyConfigManager extends ConfigManager {
        
        public MyConfigManager() {
            super(null, null, null);
        }

        @Override
        public String getOption(String domain, String option) {
            return option.substring(1);
        }

        @Override
        public boolean hasOption(String domain, String option) {
            return option.charAt(0) == '1';
        }
        
    }

}