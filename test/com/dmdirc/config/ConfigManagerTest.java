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
package com.dmdirc.config;

import com.dmdirc.interfaces.ConfigChangeListener;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConfigManagerTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void testNonExistantOption() {
        new ConfigManager("", "", "").getOption("unit-test123", "foobar");
    }
    
    @Test
    public void testStats() {
        final ConfigManager cm = new ConfigManager("", "", "");
        assertNull(ConfigManager.getStats().get("unit-test123.baz"));
        cm.hasOption("unit-test123", "baz");
        assertNotNull(ConfigManager.getStats().get("unit-test123.baz"));
        assertEquals(1, (int) ConfigManager.getStats().get("unit-test123.baz"));
    }
    
    @Test
    public void testDomainListener() {
        final ConfigChangeListener listener = mock(ConfigChangeListener.class);
        final ConfigManager cm = new ConfigManager("", "", "");
        cm.addChangeListener("unit-test", listener);
        
        cm.configChanged("foo", "bar");
        verify(listener, never()).configChanged(anyString(), anyString());
        
        cm.configChanged("unit-test", "bar");
        verify(listener).configChanged("unit-test", "bar");
    }
    
    @Test
    public void testDomainKeyListener() {
        final ConfigChangeListener listener = mock(ConfigChangeListener.class);
        final ConfigManager cm = new ConfigManager("", "", "");
        cm.addChangeListener("unit-test", "foo", listener);
        
        cm.configChanged("foo", "bar");
        verify(listener, never()).configChanged(anyString(), anyString());
        
        cm.configChanged("unit-test", "bar");
        verify(listener, never()).configChanged(anyString(), anyString());
        
        cm.configChanged("unit-test", "foo");
        verify(listener).configChanged("unit-test", "foo");
    }    
    
}