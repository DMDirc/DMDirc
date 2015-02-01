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

package com.dmdirc.config;

import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.util.io.InvalidConfigFileException;
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.PermissiveValidator;

import com.google.common.collect.Lists;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConfigFileBackedConfigProviderTest {

    private static final String[] FILES = {
            "simple-ircd", "simple-server", "profile-new", "profile-old", "profile-old-no-alts",
            "no-name", "no-target", "invalid-config-file", "simple-ircd-extra"
    };

    public static final PermissiveValidator<String> PERMISSIVE_VALIDATOR =
            new PermissiveValidator<>();

    @Mock private IdentityManager identityManager;
    @Mock private ConfigChangeListener changeListener;

    private FileSystem fs;

    @Before
    public void setUp() throws Exception {
        fs = Jimfs.newFileSystem(Configuration.unix());

        for (String file: FILES) {
            Files.copy(getClass().getResourceAsStream(file), fs.getPath(file));
        }
    }

    @Test(expected = InvalidIdentityFileException.class)
    public void testNoName() throws IOException, InvalidIdentityFileException {
        new ConfigFileBackedConfigProvider(identityManager, fs.getPath("no-name"), false);
    }

    @Test(expected = InvalidIdentityFileException.class)
    public void testNoTarget() throws IOException, InvalidIdentityFileException {
        new ConfigFileBackedConfigProvider(identityManager, fs.getPath("no-target"), false);
    }

    @Test(expected = InvalidIdentityFileException.class)
    public void testInvalidConfigFile() throws IOException, InvalidIdentityFileException {
        new ConfigFileBackedConfigProvider(identityManager, fs.getPath("invalid-config-file"), false);
    }

    @Test
    public void testProfileSimple() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("profile-new");
        assertTrue(provider.isProfile());
        assertEquals("nick1\nnick2\nnick3", provider.getOption("profile", "nicknames"));
        assertEquals("ident", provider.getOption("profile", "ident"));
        assertEquals("Guy Incognito", provider.getOption("profile", "realname"));
    }

    @Test
    public void testProfileNonProfileConfig() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        assertFalse(provider.isProfile());
    }

    @Test
    public void testReadsIrcdTarget() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        assertEquals(ConfigTarget.TYPE.IRCD, provider.getTarget().getType());
        assertEquals("DMDircircd!", provider.getTarget().getData());
    }

    @Test
    public void testReadsServerTarget() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-server");
        assertEquals(ConfigTarget.TYPE.SERVER, provider.getTarget().getType());
        assertEquals("test123", provider.getTarget().getData());
    }

    @Test
    public void testSetsOrder() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        assertEquals(5000, provider.getTarget().getOrder());
    }

    @Test
    public void testHasOptionPermissiveValidator()
            throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        assertTrue(provider.hasOption("unit", "test", PERMISSIVE_VALIDATOR));
        assertFalse(provider.hasOption("unit", "untest", PERMISSIVE_VALIDATOR));
        assertFalse(provider.hasOption("foo", "test", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testHasOptionFailingValidator() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        assertFalse(provider.hasOption("unit", "test", new NumericalValidator(0, 100)));
    }

    @Test
    public void testGetOptionPermissioveValidator()
            throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        assertEquals("true", provider.getOption("unit", "test", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testGetOptionFailingValidator() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        assertNull(provider.getOption("unit", "test", new NumericalValidator(0, 100)));
    }

    @Test
    public void testUnsetOption() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.unsetOption("unit", "test");
        assertFalse(provider.hasOption("unit", "test", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testSetOptionBoolean() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.setOption("new", "option", true);
        assertEquals("true", provider.getOption("new", "option", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testSetOptionInt() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.setOption("new", "option", 1234);
        assertEquals("1234", provider.getOption("new", "option", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testSetOptionList() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.setOption("new", "option", Lists.newArrayList("first", "second"));
        assertEquals("first\nsecond", provider.getOption("new", "option", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testSetOptionListWithSingleEntry()
            throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.setOption("new", "option", Lists.newArrayList("first"));
        assertEquals("first", provider.getOption("new", "option", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testSaveSimple() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.setOption("newdomain", "test123", 47);
        provider.save();

        final ConfigFileBackedConfigProvider loaded = getProvider("simple-ircd");
        assertTrue(loaded.hasOptionInt("newdomain", "test123"));
        assertEquals(Integer.valueOf(47), loaded.getOptionInt("newdomain", "test123"));

        // Check old settings are preserved
        assertTrue(loaded.hasOptionString("unit", "test"));
        assertEquals(ConfigTarget.TYPE.IRCD, loaded.getTarget().getType());
        assertEquals("DMDircircd!", loaded.getTarget().getData());
    }

    @Test
    public void testFiresSettingChanged() throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.addListener(changeListener);
        provider.setOption("new", "option", "boo");
        verify(changeListener).configChanged("new", "option");
    }

    @Test
    public void testDoesNotFireSettingChangedIfValueIsSame()
            throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.setOption("new", "option", "boo");
        provider.addListener(changeListener);
        provider.setOption("new", "option", "boo");
        verify(changeListener, never()).configChanged(anyString(), anyString());
    }

    @Test
    public void testFiresSettingChangedWhenUnset()
            throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.setOption("new", "option", "boo");
        provider.addListener(changeListener);
        provider.unsetOption("new", "option");
        verify(changeListener).configChanged("new", "option");
    }

    @Test
    public void testDoesNotFireSettingChangedIfNonExistantOptionUnset()
            throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.addListener(changeListener);
        provider.unsetOption("new", "option");
        verify(changeListener, never()).configChanged(anyString(), anyString());
    }

    @Test
    public void testDoesNotFireSettingChangedIfRemoved()
            throws IOException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.addListener(changeListener);
        provider.removeListener(changeListener);
        provider.setOption("new", "option", "boo");
        verify(changeListener, never()).configChanged(anyString(), anyString());
    }

    @Test
    public void testReloadLoadsExtraSettings()
            throws IOException, InvalidConfigFileException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        copyFileAndReload(provider);
        assertTrue(provider.hasOption("more", "settings", PERMISSIVE_VALIDATOR));
        assertEquals("here", provider.getOption("more", "settings", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testReloadUnsetsMissingSettings()
            throws IOException, InvalidConfigFileException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        copyFileAndReload(provider);
        assertFalse(provider.hasOption("unit", "test", PERMISSIVE_VALIDATOR));
    }

    @Test
    public void testReloadFiresSettingChangedForChangedOptions()
            throws IOException, InvalidConfigFileException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.addListener(changeListener);
        copyFileAndReload(provider);
        verify(changeListener).configChanged("meep", "moop");
    }

    @Test
    public void testReloadFiresSettingChangedForNewOptions()
            throws IOException, InvalidConfigFileException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.addListener(changeListener);
        copyFileAndReload(provider);
        verify(changeListener).configChanged("more", "settings");
        verify(changeListener).configChanged("more", "because");
        verify(changeListener).configChanged("more", "are");
        verify(changeListener).configChanged("more", "fun");
    }

    @Test
    public void testReloadFiresSettingChangedForRemovedOptions()
            throws IOException, InvalidConfigFileException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.addListener(changeListener);
        copyFileAndReload(provider);
        verify(changeListener).configChanged("unit", "test");
    }

    @Test
    public void testReloadDoesNotFireSettingChangedForUnchangedOptions()
            throws IOException, InvalidConfigFileException, InvalidIdentityFileException {
        final ConfigFileBackedConfigProvider provider = getProvider("simple-ircd");
        provider.addListener(changeListener);
        copyFileAndReload(provider);
        verify(changeListener, never()).configChanged("meep", "mop");
    }

    private void copyFileAndReload(final ConfigProvider provider)
            throws IOException, InvalidConfigFileException {
        Files.copy(fs.getPath("simple-ircd-extra"), fs.getPath("simple-ircd"),
                StandardCopyOption.REPLACE_EXISTING);
        provider.reload();
    }

    private ConfigFileBackedConfigProvider getProvider(final String file)
            throws IOException, InvalidIdentityFileException {
        return new ConfigFileBackedConfigProvider(identityManager, fs.getPath(file), false);
    }

}
