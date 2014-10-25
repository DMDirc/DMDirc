/*
 * Copyright (c) 2006-2014 DMDirc Developers
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dmdirc.actions.wrappers;

import com.dmdirc.actions.wrappers.Profile;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test profile class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileTest {

    @Mock private IdentityFactory identityFactory;

    private Profile createProfile() {
        final List<String> nicknames = new ArrayList<>();
        nicknames.add("nickname1");
        nicknames.add("nickname2");
        final ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.getName()).thenReturn("profile");
        when(configProvider.getOption("identity", "name")).thenReturn("profile");
        when(configProvider.getOptionList("profile", "nicknames")).thenReturn(nicknames);
        when(configProvider.getOption("profile", "realname")).thenReturn("realname");
        when(configProvider.getOption("profile", "ident")).thenReturn("ident");

        return new Profile(identityFactory, configProvider);
    }

    /**
     * Test null profile constructor.
     */
    @Test
    public void testEmptyConstructor() {
        final Profile instance = new Profile("New Profile", identityFactory);
        assertEquals("", instance.getIdent());
        assertEquals("New Profile", instance.getName());
        assertEquals(new ArrayList<>(Arrays.asList("NewProfile")), instance.getNicknames());
        assertEquals("New Profile", instance.getRealname());
    }

    /**
     * Test null profile constructor.
     */
    @Test
    public void testIdentityConstructor() {
        final Profile instance = createProfile();
        assertEquals("ident", instance.getIdent());
        assertEquals("profile", instance.getName());
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2", })
                .equals(instance.getNicknames()));
        assertEquals("realname", instance.getRealname());
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNicknameString() {
        final Profile instance = createProfile();
        instance.addNickname("nickname3");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2",
            "nickname3"}).equals(instance.getNicknames()));
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNicknameStringContains() {
        final Profile instance = createProfile();
        instance.addNickname("nickname2");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNicknameStringInt() {
        final Profile instance = createProfile();
        instance.addNickname("nickname3", 0);
        assertTrue(Arrays.asList(new String[]{"nickname3", "nickname1",
            "nickname2"}).equals(instance.getNicknames()));
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNicknameStringIntContains() {
        final Profile instance = createProfile();
        instance.addNickname("nickname2", 0);
        assertTrue(Arrays.asList(new String[]{"nickname1","nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of delNickname method, of class Profile.
     */
    @Test
    public void testDelNickname() {
        final Profile instance = createProfile();
        instance.delNickname("nickname2");
        assertTrue(Arrays.asList(new String[]{"nickname1"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of save method, of class Profile.
     */
    @Test
    public void testSave() {
        final List<String> nicknames = new ArrayList<>();
        nicknames.add("nickname1");
        nicknames.add("nickname2");
        final ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.getName()).thenReturn("profile");
        when(configProvider.getOption("identity", "name")).thenReturn("profile");
        when(configProvider.getOptionList("profile", "nicknames")).thenReturn(nicknames);
        when(configProvider.getOption("profile", "realname")).thenReturn("realname");
        when(configProvider.getOption("profile", "ident")).thenReturn("ident");

        final Profile instance = new Profile(identityFactory, configProvider);
        instance.save();
        verify(configProvider).setOption("identity", "name", "profile");
        verify(configProvider).setOption("profile", "nicknames", nicknames);
        verify(configProvider).setOption("profile", "realname", "realname");
        verify(configProvider).setOption("profile", "ident", "ident");
    }

    @Test
    public void testSaveNoConfig() {
        final ConfigProvider configProvider = mock(ConfigProvider.class);

        when(identityFactory.createProfileConfig("New Profile")).thenReturn(configProvider);

        final Profile instance = new Profile("New Profile", identityFactory);
        instance.save();
        verify(identityFactory).createProfileConfig("New Profile");
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNicknameOldEmpty() {
        final Profile instance = createProfile();
        instance.editNickname("", "nickname3");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNicknameNewEmpty() {
        final Profile instance = createProfile();
        instance.editNickname("nickname2", "");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNicknameSame() {
        final Profile instance = createProfile();
        instance.editNickname("nickname2", "nickname2");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNickname() {
        final Profile instance = createProfile();
        instance.editNickname("nickname2", "nickname3");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname3"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of delete method, of class Profile.
     */
    @Test
    public void testDelete() throws IOException {
        final List<String> nicknames = new ArrayList<>();
        nicknames.add("nickname1");
        nicknames.add("nickname2");
        final ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.getName()).thenReturn("profile");
        when(configProvider.getOption("identity", "name")).thenReturn("profile");
        when(configProvider.getOptionList("profile", "nicknames")).thenReturn(nicknames);
        when(configProvider.getOption("profile", "realname")).thenReturn("realname");
        when(configProvider.getOption("profile", "ident")).thenReturn("ident");

        final Profile instance = new Profile(identityFactory, configProvider);
        instance.delete();
        verify(configProvider).delete();
    }

    /**
     * Test of delete method, of class Profile.
     */
    @Test
    public void testDeleteNullIdentity() throws IOException {
        final Profile instance = new Profile("New Profile", null);
        instance.delete();
    }
}
