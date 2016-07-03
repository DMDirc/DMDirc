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

package com.dmdirc.config.profiles;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.config.ConfigProvider;

import com.google.common.collect.Lists;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdentitiesProfileMigratorTest {

    @Mock private IdentityManager identityManager;
    @Mock private ProfileManager profileManager;
    @Mock private ConfigProvider configProvider1;
    @Mock private ConfigProvider configProvider2;
    @Mock private ConfigProvider configProvider3;

    private IdentitiesProfileMigrator instance;

    @Before
    public void setup() {
        instance = new IdentitiesProfileMigrator(identityManager, profileManager);
        when(configProvider1.getName()).thenReturn("name1");
        when(configProvider1.getOption("profile", "realname")).thenReturn("realname1");
        when(configProvider1.hasOptionString("profile", "ident1")).thenReturn(false);
        when(configProvider1.getOptionList("profile", "nicknames")).thenReturn(
                Lists.newArrayList("nickname1")
        );
        when(configProvider2.getName()).thenReturn("name2");
        when(configProvider2.getOption("profile", "realname")).thenReturn("realname2");
        when(configProvider2.hasOptionString("profile", "ident")).thenReturn(true);
        when(configProvider2.getOption("profile", "ident")).thenReturn("ident2");
        when(configProvider2.getOptionList("profile", "nicknames")).thenReturn(
                Lists.newArrayList("nickname2")
        );
        when(configProvider3.getName()).thenReturn("name3");
        when(configProvider3.getOption("profile", "realname")).thenReturn("realname3");
        when(configProvider3.hasOptionString("profile", "ident")).thenReturn(true);
        when(configProvider3.getOption("profile", "ident")).thenReturn("ident3");
        when(configProvider3.getOptionList("profile", "nicknames")).thenReturn(
                Lists.newArrayList("nickname31", "nickname32", "nickname33")
        );
    }

    @Test
    public void testNeedsMigration_NoProfiles() {
        when(identityManager.getProvidersByType("profile")).thenReturn(Lists.newArrayList());
        assertFalse(instance.needsMigration());
    }

    @Test
    public void testNeedsMigration_Profiles() {
        when(identityManager.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(configProvider1));
        assertTrue(instance.needsMigration());
    }

    @Test
    public void testMigrate_NoIdent() {
        when(identityManager.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(configProvider1));
        instance.migrate();
        verify(profileManager).addProfile(Profile.create("name1", "realname1", Optional.empty(),
                Lists.newArrayList("nickname1")));
    }

    @Test
    public void testMigrate_Ident() {
        when(identityManager.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(configProvider2));
        instance.migrate();
        verify(profileManager).addProfile(Profile.create("name2", "realname2", Optional.of("ident2"),
                Lists.newArrayList("nickname2")));
    }

    @Test
    public void testMigrate_MultipleNicknames() {
        when(identityManager.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(configProvider3));
        instance.migrate();
        verify(profileManager).addProfile(Profile.create("name3", "realname3", Optional.of("ident3"),
                Lists.newArrayList("nickname31", "nickname32", "nickname33")));
    }
}