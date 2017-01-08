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
package com.dmdirc.config.prefs;

import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.StringLengthValidator;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PreferencesSettingTest {

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
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
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
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
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
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);

        assertFalse(ps.isRestartNeeded());
        assertSame(ps, ps.setRestartNeeded());
        assertTrue(ps.isRestartNeeded());
    }

    @Test
    public void testMultichoiceAdding() {
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("new");
        final Map<String, String> map = new HashMap<>();
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
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain",
                "option", "title", "helptext", cm, null);
        ps.setValue("newvalue");
        assertEquals("newvalue", ps.getValue());
    }

    @Test
    public void testDismiss() {
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
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
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
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
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
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
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        final ConfigProvider configProvider = mock(ConfigProvider.class);

        when(cm.getOption("unit-test", "ps")).thenReturn("abc");
        when(configProvider.getOption("unit-test", "ps")).thenReturn("abc");

        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                "unit-test", "ps", "title", "helptext", cm, configProvider);

        assertFalse(ps.save());
        ps.setValue(null);
        assertTrue(ps.save());

        verify(configProvider).unsetOption("unit-test", "ps");
    }

    @Test
    public void testSaveNormal() {
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        final ConfigProvider configProvider = mock(ConfigProvider.class);

        when(cm.getOption("unit-test", "ps")).thenReturn("abc");
        when(configProvider.getOption("unit-test", "ps")).thenReturn("abc");

        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                "unit-test", "ps", "title", "helptext", cm, configProvider);

        assertFalse(ps.save());
        ps.setValue("def");
        assertTrue(ps.save());

        verify(configProvider).setOption("unit-test", "ps", "def");
    }

    @Test
    public void testIsSet() {
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        final ConfigProvider configProvider = mock(ConfigProvider.class);

        when(cm.getOption("unit-test", "ps")).thenReturn("abc");
        when(configProvider.getOption("unit-test", "ps")).thenReturn("abc");

        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                "unit-test", "ps", "title", "helptext", cm, configProvider);

        assertFalse(ps.isSet());
    }

    @Test
    public void testIsNotSet() {
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        final ConfigProvider identity = mock(ConfigProvider.class);

        when(cm.getOption("unit-test", "ps")).thenReturn(null);
        when(identity.getOption("unit-test", "ps")).thenReturn(null);

        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                "unit-test", "ps", "title", "helptext", cm, identity);

        assertFalse(ps.isSet());
    }

    @Test
    public void testUnknownComboOption() {
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        final ConfigProvider configProvider = mock(ConfigProvider.class);

        when(cm.getOption("unit-test", "ps")).thenReturn("abc");
        when(configProvider.getOption("unit-test", "ps")).thenReturn("abc");

        final Map<String, String> options = new HashMap<>();

        final PreferencesSetting ps = new PreferencesSetting("unit-test",
                "ps", "title", "helptext", options, cm, configProvider);

        assertEquals("Current (abc)", ps.getComboOptions().get("abc"));
    }

    @Test
    public void testKnownComboOption() {
        final AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        final ConfigProvider configProvider = mock(ConfigProvider.class);

        when(cm.getOption("unit-test", "ps")).thenReturn("abc");
        when(configProvider.getOption("unit-test", "ps")).thenReturn("abc");

        final Map<String, String> options = new HashMap<>();
        options.put("abc", "123");

        final PreferencesSetting ps = new PreferencesSetting("unit-test",
                "ps", "title", "helptext", options, cm, configProvider);

        assertEquals("123", ps.getComboOptions().get("abc"));
    }

}
