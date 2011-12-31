/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.StringLengthValidator;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PreferencesSettingTest {

    @BeforeClass
    public static void setUp() throws Exception {
        IdentityManager.getIdentityManager().initialise();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMultichoice1() {
        new PreferencesSetting(PreferencesType.MULTICHOICE, "domain",
                "option", "title", "helptext", null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMultichoice2() {
        new PreferencesSetting(PreferencesType.MULTICHOICE,
                new PermissiveValidator<String>(), "domain",
                "option", "title", "helptext", null, null);
    }

    @Test
    public void testNormalConstructor() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);

        assertEquals(PreferencesType.TEXT, ps.getType());
        assertEquals("fallback", ps.getValue());
        assertEquals("title", ps.getTitle());
        assertEquals("helptext", ps.getHelptext());
        assertFalse(ps.isRestartNeeded());
        assertTrue(ps.getValidator() instanceof PermissiveValidator);
    }

    @Test
    public void testValidatorConstructor() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                new NotEmptyValidator(), "domain",
                "option", "title", "helptext", cm, null);

        assertEquals(PreferencesType.TEXT, ps.getType());
        assertEquals("fallback", ps.getValue());
        assertEquals("title", ps.getTitle());
        assertEquals("helptext", ps.getHelptext());
        assertFalse(ps.isRestartNeeded());
        assertTrue(ps.getValidator() instanceof NotEmptyValidator);
    }

    @Test
    public void testRestartNeeded() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);

        assertFalse(ps.isRestartNeeded());
        assertSame(ps, ps.setRestartNeeded());
        assertTrue(ps.isRestartNeeded());
    }

    @Test
    public void testMultichoiceAdding() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("new");
        final Map<String, String> map = new HashMap<String, String>();
        map.put("a", "b");
        map.put("c", "d");

        final PreferencesSetting ps = new PreferencesSetting("domain",
                "option", "title", "helptext", map, cm, null);
        assertEquals(3, ps.getComboOptions().size());
        assertNotNull(ps.getComboOptions().get("new"));
        assertTrue(ps.getComboOptions().get("new").startsWith("Current"));
    }

    @Test
    public void testSetValue() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);
        ps.setValue("newvalue");
        assertEquals("newvalue", ps.getValue());
    }

    @Test
    public void testDismiss() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);
        ps.setValue("newvalue");
        ps.dismiss();
        assertEquals("fallback", ps.getValue());

        final PreferencesSetting ps2 = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);
        ps2.setValue(null);
        ps2.dismiss();
        assertEquals("fallback", ps2.getValue());
    }

    @Test
    public void testListener() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);
        final SettingChangeListener tl = mock(SettingChangeListener.class);
        ps.registerChangeListener(tl);
        ps.setValue("newvalue");
        ps.dismiss();
        ps.dismiss();

        verify(tl, times(2)).settingChanged(ps);
    }

    @Test
    public void testNeedsSaving() {
        ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        when(cm.getOption("domain", "option2")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                new StringLengthValidator(5, 100), "domain",
                "option", "title", "helptext", cm, null);
        assertFalse(ps.needsSaving());
        ps.setValue("abc");
        assertFalse(ps.needsSaving());
        ps.setValue("abcdefg");
        assertTrue(ps.needsSaving());

        final PreferencesSetting ps2 = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option2", "title", "helptext", cm, null);

        ps2.setValue(null);
        assertTrue(ps2.needsSaving());
    }

    @Test
    public void testSaveUnset() {
        IdentityManager.getConfigIdentity().setOption("unit-test", "ps", "abc");

        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                "unit-test", "ps", "title", "helptext",
                IdentityManager.getIdentityManager().getGlobalConfiguration(),
                IdentityManager.getConfigIdentity());

        assertFalse(ps.save());
        ps.setValue(null);
        assertTrue(ps.save());

        assertFalse(IdentityManager.getConfigIdentity().hasOptionString("unit-test", "ps"));
    }

    @Test
    public void testSaveNormal() {
        IdentityManager.getConfigIdentity().setOption("unit-test", "ps", "abc");

        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                "unit-test", "ps", "title", "helptext",
                IdentityManager.getIdentityManager().getGlobalConfiguration(),
                IdentityManager.getConfigIdentity());

        assertFalse(ps.save());
        ps.setValue("def");
        assertTrue(ps.save());

        assertEquals("def", IdentityManager.getConfigIdentity().getOption("unit-test", "ps"));
    }

}