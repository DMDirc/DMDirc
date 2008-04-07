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
package com.dmdirc.config.prefs;

import com.dmdirc.config.IdentityManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class PreferencesManagerTest extends junit.framework.TestCase {

    @Test
    public void testDefaults() {
        final PreferencesManager pm = new PreferencesManager();
        assertNotNull(pm.getCategory("General"));
        assertNotNull(pm.getCategory("Connection"));
        assertNotNull(pm.getCategory("Messages"));
        assertNotNull(pm.getCategory("Advanced"));
        assertNotNull(pm.getCategory("GUI"));
        assertNotNull(pm.getCategory("Plugins"));
        assertNotNull(pm.getCategory("Updates"));
        assertNotNull(pm.getCategory("URL Handlers"));
    }
    
    @Test
    public void testGetCategory() {
        final PreferencesManager pm = new PreferencesManager();
        assertNull(pm.getCategory("unittest123"));
    }
    
    @Test
    public void testGetCategories() {
        final PreferencesManager pm = new PreferencesManager();
        assertNotNull(pm.getCategories());
        assertFalse(pm.getCategories().isEmpty());
        
        for (PreferencesCategory cat : pm.getCategories()) {
            assertNotNull(pm.getCategory(cat.getTitle()));
        }
    }
    
    @Test
    public void testSaveListener() {
        IdentityManager.load();
        
        final PreferencesManager pm = new PreferencesManager();
        final TestPreferencesInterface tpi = new TestPreferencesInterface();
        
        pm.registerSaveListener(tpi);
        pm.fireSaveListeners();
        assertTrue(tpi.saved);
    }

    private class TestPreferencesInterface implements PreferencesInterface {
        
        public boolean saved;

        public void save() {
            saved = true;
        }
        
    }

}