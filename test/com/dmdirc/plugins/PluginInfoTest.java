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

package com.dmdirc.plugins;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import org.junit.Test;
import static org.junit.Assert.*;

public class PluginInfoTest {
    
    private PluginInfo pi;

    @Test
    public void testCheckMinimum() throws PluginException {
        try {
            pi = new PluginInfo(new URL("file:///dev/null"), false);
            
            assertTrue(pi.checkMinimumVersion("5", 6));
            assertTrue(pi.checkMinimumVersion("5", 5));
            assertTrue(pi.checkMinimumVersion("0", 17));
            assertTrue(pi.checkMinimumVersion("100", 0));
            assertTrue(pi.checkMinimumVersion("0", 0));
            assertFalse(pi.checkMinimumVersion("abc", 6));
            assertFalse(pi.checkMinimumVersion("7", 6));
        } catch (MalformedURLException mue) { }
    }
    
    @Test
    public void testCheckMaximim() throws PluginException {
        try {
            pi = new PluginInfo(new URL("file:///dev/null"), false);
            
            assertTrue(pi.checkMaximumVersion("6", 6));
            assertTrue(pi.checkMaximumVersion("7", 6));
            assertTrue(pi.checkMaximumVersion("0", 6));
            assertTrue(pi.checkMaximumVersion("6", 0));
            assertTrue(pi.checkMaximumVersion("0", 0));
            assertTrue(pi.checkMaximumVersion("", 17));
            assertFalse(pi.checkMaximumVersion("abc", 6));
            assertFalse(pi.checkMaximumVersion("7", 10));
        } catch (MalformedURLException mue) { }
    }
    
    @Test
    public void testOS() throws PluginException {
        try {
            pi = new PluginInfo(new URL("file:///dev/null"), false);
            
            assertTrue(pi.checkOS("windows", "windows", "xp", "x86"));
            assertFalse(pi.checkOS("windows", "linux", "2.6.2.11", "x86"));
            assertTrue(pi.checkOS("windows:xp|98|3\\.1", "windows", "xp", "x86"));
            assertFalse(pi.checkOS("windows:xp|98|3\\.1", "windows", "vista", "x86"));
            assertFalse(pi.checkOS("windows:xp|98|3\\.1", "linux", "2.6.2.11", "x86"));
            assertTrue(pi.checkOS("windows:xp|98|3\\.1:.86", "windows", "xp", "x86"));
            assertFalse(pi.checkOS("windows:xp|98|3\\.1:.86", "windows", "xp", "mips"));
            assertFalse(pi.checkOS("windows:xp|98|3\\.1:.86", "windows", "vista", "x86"));
            assertFalse(pi.checkOS("windows:xp|98|3\\.1:.86", "linux", "2.6.2.11", "x86"));        
        } catch (MalformedURLException mue) { }
    }
    
    @Test
    public void testLoad() throws PluginException {
        PluginInfo pi = new PluginInfo(getClass().getResource("testplugin.jar"));
        assertEquals("Author <em@il>", pi.getAuthor());
        assertEquals("Friendly", pi.getFriendlyVersion());
        assertEquals("Description goes here", pi.getDescription());
        assertEquals("randomname", pi.getName());
        assertEquals("Friendly name", pi.getNiceName());
        assertEquals("3", pi.getVersion().toString());
    }
    
    @Test
    public void testUpdate() throws PluginException, IOException {
        final File dir = new File(File.createTempFile("dmdirc-plugin-test", null).getParentFile(),
                "dmdirc-plugin-test-folder");
        final File pluginDir = new File(dir, "plugins");
        
        dir.deleteOnExit();
        pluginDir.mkdirs();
        
        final File target = new File(pluginDir, "test.jar");
        
        target.createNewFile();
        new File(pluginDir, "test.jar.update").createNewFile();
        
        new PluginInfo(target.toURI().toURL(), false);
        
        assertTrue(new File(pluginDir, "test.jar").exists());
        assertFalse(new File(pluginDir, "test.jar.update").exists());
    }

}