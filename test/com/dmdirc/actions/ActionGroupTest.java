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
package com.dmdirc.actions;

import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import java.util.HashMap;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ActionGroupTest {

    @Test
    public void testGetAuthor() {
        ActionGroup instance = new ActionGroup("moo");
        instance.setAuthor("foo");
        
        String expResult = "foo";
        String result = instance.getAuthor();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescription() {
        ActionGroup instance = new ActionGroup("bar");
        instance.setDescription("Tra-la-la-la-la");
                
        String expResult = "Tra-la-la-la-la";
        String result = instance.getDescription();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetName() {
        ActionGroup instance = new ActionGroup("foobar");
        
        String expResult = "foobar";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSettings() {
        ActionGroup instance = new ActionGroup("foo");
        
        assertTrue(instance.getSettings().isEmpty());
        
        instance.getSettings().put("", mock(PreferencesSetting.class));
        assertEquals(1, instance.getSettings().size());
    }

    @Test
    public void testGetVersion() {
        ActionGroup instance = new ActionGroup("vtest");
        instance.setVersion(73);
        
        int expResult = 73;
        int result = instance.getVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetComponent() {
        ActionGroup instance = new ActionGroup("zzz");
        instance.setComponent(69);
        
        int expResult = 69;
        int result = instance.getComponent();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testClear() {
        ActionGroup instance = new ActionGroup("zzz");
        instance.add(null);
        assertEquals(1, instance.size());
        instance.clear();
        assertEquals(0, instance.size());
    }
    
    @Test
    public void testRemove() {
        ActionGroup instance = new ActionGroup("zzz");
        instance.add(null);
        assertEquals(1, instance.size());
        instance.remove(null);
        assertEquals(0, instance.size());
    }

    @Test
    public void testContains() {
        ActionGroup instance = new ActionGroup("zzz");
        instance.add(null);
        assertTrue(instance.contains(null));
        instance.remove(null);
        assertFalse(instance.contains(null));
    }    
    
    @Test
    public void testIsDelible() {
        assertTrue(new ActionGroup("foo").isDelible());
    }

}