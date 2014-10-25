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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.ui.core.profiles;

import com.dmdirc.profiles.Profile;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.interfaces.ui.ProfilesDialogModelListener;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreProfilesDialogModelTest {

    @Mock private IdentityController identityController;
    @Mock private ProfilesDialogModelListener listener;
    @Mock private IdentityFactory identityFactory;
    @Mock private ConfigProvider profileProvider1;
    @Mock private ConfigProvider profileProvider2;
    @Mock private ConfigProvider profileProvider3;
    private List<ConfigProvider> profiles;
    private CoreProfilesDialogModel instance;

    @Before
    public void setupMocks() {
        mockProfile("profile1", profileProvider1);
        mockProfile("profile2", profileProvider2);
        mockProfile("profile3", profileProvider3);
        profiles = Lists.newArrayList(profileProvider1, profileProvider2, profileProvider3);
        when(identityController.getProvidersByType(anyString())).thenReturn(profiles);
        instance = new CoreProfilesDialogModel(identityController, identityFactory);
        instance.loadModel();
    }

    private void mockProfile(final String name, final ConfigProvider mock) {
        when(mock.getName()).thenReturn(name);
        when(mock.getOption("identity", "name")).thenReturn(name);
        when(mock.getOptionList("profile", "nicknames"))
                .thenReturn(Lists.newArrayList("nickname1", "nickname2", "nickname3"));
        when(mock.getOption("profile", "realname")).thenReturn("realname");
        when(mock.getOption("profile", "ident")).thenReturn("ident");
        when(mock.toString()).thenReturn("Identity: " + name);
    }

    @Test
    public void testGetProfileList() {
        assertEquals("testGetProfileList", profiles.size(), instance.getProfileList().size());
    }

    @Test
    public void testGetProfileNotExist() {
        assertFalse("testGetProfileNotExist", instance.getProfile("").isPresent());
    }

    @Test
    public void testGetProfileExist() {
        assertTrue("testGetProfileExist", instance.getProfile("profile1").isPresent());
    }

    @Test
    public void testIsProfileListValidEmptyList() {
        when(identityController.getProvidersByType(anyString()))
                .thenReturn(Lists.<ConfigProvider>newArrayList());
        instance.loadModel();
        assertFalse("testIsProfileListValidEmptyList", instance.isProfileListValid());
    }

    @Test
    public void testIsProfileListValid() {
        assertTrue("testIsProfileListValid", instance.isProfileListValid());
    }

    @Test
    public void testAddProfile() {
        when(identityController.getProvidersByType(anyString()))
                .thenReturn(Lists.<ConfigProvider>newArrayList());
        instance.loadModel();
        assertFalse("testAddProfile", instance.getProfile("profile4").isPresent());
        instance.addProfile("profile4", "realname", "ident", Lists.newArrayList("nickname"));
        assertTrue("testAddProfile", instance.getProfile("profile4").isPresent());
    }

    @Test
    public void testEditProfile() {
        final Profile preEdit = instance.getProfile("profile1").get();
        assertEquals("testEditProfile", "profile1", preEdit.getName());
        assertEquals("testEditProfile", "realname", preEdit.getRealname());
        assertEquals("testEditProfile", "ident", preEdit.getIdent());
        assertEquals("testEditProfile", Lists.newArrayList("nickname1", "nickname2", "nickname3"),
                preEdit.getNicknames());
        instance.editProfile("profile1", "newRealname", "newIdent", Lists.newArrayList("nickname"));
        final Profile postEdit = instance.getProfile("profile1").get();
        assertEquals("testEditProfile", "profile1", postEdit.getName());
        assertEquals("testEditProfile", "newRealname", postEdit.getRealname());
        assertEquals("testEditProfile", "newIdent", postEdit.getIdent());
        assertEquals("testEditProfile", Lists.newArrayList("nickname"),
                postEdit.getNicknames());
    }

    @Test
    public void testRenameProfile() {
        assertTrue("testRenameProfile", instance.getProfile("profile1").isPresent());
        assertFalse("testRenameProfile", instance.getProfile("profile4").isPresent());
        instance.renameProfile("profile1", "profile4");
        assertFalse("testRenameProfile", instance.getProfile("profile1").isPresent());
        assertTrue("testRenameProfile", instance.getProfile("profile4").isPresent());
    }

    @Test
    public void testRemoveProfile() {
        assertEquals("testRemoveProfile", 3, instance.getProfileList().size());
        assertTrue("testRemoveProfile", instance.getProfile("profile3").isPresent());
        instance.removeProfile("profile3");
        assertEquals("testRemoveProfile", 2, instance.getProfileList().size());
        assertFalse("testRemoveProfile", instance.getProfile("profile3").isPresent());
    }

    @Test
    public void testSave() {

    }

    @Test
    public void testSetSelectedProfile() {
    }

    @Test
    public void testGetSelectedProfile() {
        assertTrue("testGetSelectedProfile", instance.getSelectedProfile().isPresent());
        instance.setSelectedProfile(instance.getProfile("profile2"));
        assertEquals("testGetSelectedProfile", "profile2",
                instance.getSelectedProfile().get().getName());
    }

    @Test
    public void testGetSelectedProfileDetails() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        assertEquals("testGetSelectedProfileDetails", "profile1",
                instance.getSelectedProfileName().get());
        assertEquals("testGetSelectedProfileDetails", "ident",
                instance.getSelectedProfileIdent().get());
        assertEquals("testGetSelectedProfileDetails", "realname",
                instance.getSelectedProfileRealname().get());
        assertEquals("testGetSelectedProfileDetails",
                Lists.newArrayList("nickname1", "nickname2", "nickname3"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testSetSelectedProfileSelectedNickname() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        assertEquals("testSetSelectedProfileSelectedNickname",
                Optional.empty(), instance.getSelectedProfileSelectedNickname());
        instance.setSelectedProfileSelectedNickname(Optional.ofNullable("nickname2"));
        assertEquals("testSetSelectedProfileSelectedNickname",
                Optional.ofNullable("nickname2"),
                instance.getSelectedProfileSelectedNickname());
    }

    @Test
    public void testSetSelectedProfileDetails() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        instance.setSelectedProfileName(Optional.ofNullable("testName"));
        instance.setSelectedProfileIdent(Optional.ofNullable("testIdent"));
        instance.setSelectedProfileRealname(Optional.ofNullable("testRealname"));
        instance.setSelectedProfileNicknames(Optional.ofNullable((List<String>) Lists.
                newArrayList("testNickname")));
        assertEquals("testGetSelectedProfileDetails", "testName",
                instance.getSelectedProfileName().get());
        assertEquals("testGetSelectedProfileDetails", "testIdent",
                instance.getSelectedProfileIdent().get());
        assertEquals("testGetSelectedProfileDetails", "testRealname",
                instance.getSelectedProfileRealname().get());
        assertEquals("testGetSelectedProfileDetails",
                Lists.newArrayList("testNickname"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testAddSelectedProfileNickname() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        assertEquals("testAddSelectedProfileNickname",
                Lists.newArrayList("nickname1", "nickname2", "nickname3"),
                instance.getSelectedProfileNicknames().get());
        instance.addSelectedProfileNickname("nickname4");
        assertEquals("testAddSelectedProfileNickname",
                Lists.newArrayList("nickname1", "nickname2", "nickname3", "nickname4"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testRemoveSelectedProfileNickname() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        assertEquals("testRemoveSelectedProfileNickname",
                Lists.newArrayList("nickname1", "nickname2", "nickname3"),
                instance.getSelectedProfileNicknames().get());
        instance.removeSelectedProfileNickname("nickname3");
        assertEquals("testRemoveSelectedProfileNickname",
                Lists.newArrayList("nickname1", "nickname2"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testEditSelectedProfileSelectedNickname() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        instance.setSelectedProfileSelectedNickname(Optional.ofNullable("nickname2"));
        assertEquals("testSetSelectedProfileSelectedNickname",
                Optional.ofNullable("nickname2"),
                instance.getSelectedProfileSelectedNickname());
        instance.editSelectedProfileNickname("nickname2", "nickname4");
        assertEquals("testAddSelectedProfileNickname",
                Lists.newArrayList("nickname1", "nickname4", "nickname3"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testAddListener() {
        instance.addListener(listener);
        instance.addProfile("profile4", "realname", "ident", Lists.newArrayList("nickname"));
        verify(listener).profileAdded(any(Profile.class));
    }

    @Test
    public void testRemoveListener() {
        instance.addListener(listener);
        instance.addProfile("profile4", "realname", "ident", Lists.newArrayList("nickname"));
        assertTrue(instance.getProfile("profile4").isPresent());
        final Profile profile4 = instance.getProfile("profile4").get();
        verify(listener).profileAdded(profile4);
        instance.removeListener(listener);
        instance.setSelectedProfile(Optional.ofNullable(profile4));
        verify(listener, never()).profileRemoved(profile4);
    }

}
