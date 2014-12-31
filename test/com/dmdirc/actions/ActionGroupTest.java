/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.updater.Version;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ActionGroupTest {

    @Mock private ActionManager actionManager;

    @Test
    public void testGetAuthor() {
        final ActionGroup instance = new ActionGroup(actionManager, "moo");
        instance.setAuthor("foo");

        final String expResult = "foo";
        final String result = instance.getAuthor();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescription() {
        final ActionGroup instance = new ActionGroup(actionManager, "bar");
        instance.setDescription("Tra-la-la-la-la");

        final String expResult = "Tra-la-la-la-la";
        final String result = instance.getDescription();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetName() {
        final ActionGroup instance = new ActionGroup(actionManager, "foobar");

        final String expResult = "foobar";
        final String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSettings() {
        final ActionGroup instance = new ActionGroup(actionManager, "foo");

        assertTrue(instance.getSettings().isEmpty());

        instance.getSettings().put("", mock(PreferencesSetting.class));
        assertEquals(1, instance.getSettings().size());
    }

    @Test
    public void testGetVersion() {
        final ActionGroup instance = new ActionGroup(actionManager, "vtest");
        instance.setVersion(new Version(73));

        final Version expResult = new Version(73);
        final Version result = instance.getVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetComponent() {
        final ActionGroup instance = new ActionGroup(actionManager, "zzz");
        instance.setComponent(69);

        final int expResult = 69;
        final int result = instance.getComponent();
        assertEquals(expResult, result);
    }

    @Test
    public void testClear() {
        final ActionGroup instance = new ActionGroup(actionManager, "zzz");
        instance.add(null);
        assertTrue(instance.iterator().hasNext());
        instance.clear();
        assertFalse(instance.iterator().hasNext());
    }

    @Test
    public void testRemove() {
        final ActionGroup instance = new ActionGroup(actionManager, "zzz");
        instance.add(null);
        assertTrue(instance.iterator().hasNext());
        instance.remove(null);
        assertFalse(instance.iterator().hasNext());
    }

    @Test
    public void testIsDelible() {
        assertTrue(new ActionGroup(actionManager, "foo").isDelible());
    }

}
