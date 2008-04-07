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

import org.junit.Test;
import static org.junit.Assert.*;

public class PreferencesCategoryTest {

    @Test
    public void testInline() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        assertFalse(category.isInline());
        assertSame(category, category.setInline());
        assertTrue(category.isInline());
    }

    @Test
    public void testInlineBefore() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        assertTrue(category.isInlineBefore());
        assertSame(category, category.setInlineAfter());
        assertFalse(category.isInlineBefore());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddWithObject() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test",
                new PreferencesInterface() {
            public void save() {
                // Do nothing
            }
        });

        category.addSetting(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInlineAdding() {
        final PreferencesCategory category1 = new PreferencesCategory("unit", "test");
        final PreferencesCategory category2 = new PreferencesCategory("unit", "test");
        
        category1.setInline();
        category1.addSubCategory(category2);
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(PreferencesCategoryTest.class);
    }

}
