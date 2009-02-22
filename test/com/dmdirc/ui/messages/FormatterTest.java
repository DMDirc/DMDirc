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
package com.dmdirc.ui.messages;

import com.dmdirc.harness.TestConfigManagerOptionToggle;
import com.dmdirc.config.ConfigManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormatterTest {
    
    private final TestConfigManagerOptionToggle mcm = new TestConfigManagerOptionToggle();
    
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
    
    @Test
    public void testCaching() {
        try {
            assertEquals("H", Formatter.formatMessage(mcm, "1%1$C", "Hello!"));
            assertEquals("H", Formatter.formatMessage(mcm, "1%1$C", "Hello!", 123, null));
            assertEquals("HELLO!", Formatter.formatMessage(mcm, "1%1$S", "Hello!", 123, null));
            assertEquals("HELLO!", Formatter.formatMessage(mcm, "1%1$S", "Hello!"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }
    
    @Test
    public void testFormatDuration() {
        assertEquals("1 minute, 1 second", Formatter.formatMessage(mcm, "1%1$u", "61"));
    }
    
    @Test
    public void testFormatDurationSeconds() {
        assertEquals("1 second", Formatter.formatDuration(1));
        assertEquals("2 seconds", Formatter.formatDuration(2));
    }
    
    @Test
    public void testFormatDurationMinutes() {
        assertEquals("1 minute", Formatter.formatDuration(60));
        assertEquals("1 minute, 1 second", Formatter.formatDuration(61));
        assertEquals("1 minute, 2 seconds", Formatter.formatDuration(62));
        assertEquals("2 minutes, 2 seconds", Formatter.formatDuration(122));
    }
    
    @Test
    public void testFormatDurationHours() {
        assertEquals("1 hour", Formatter.formatDuration(3600));
        assertEquals("1 hour, 1 second", Formatter.formatDuration(3601));
        assertEquals("2 hours, 1 minute, 5 seconds", Formatter.formatDuration(7265));
    }
    
    @Test
    public void testFormatDurationDays() {
        assertEquals("1 day", Formatter.formatDuration(86400));
        assertEquals("1 day, 10 minutes, 1 second", Formatter.formatDuration(87001));
    }

}