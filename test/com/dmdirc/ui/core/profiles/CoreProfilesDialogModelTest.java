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

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.interfaces.ui.ProfilesDialogModelListener;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Comparator.comparing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreProfilesDialogModelTest {

    @Mock private ProfilesDialogModelListener listener;
    @Mock private ProfileManager profileManager;
    private MutableProfile mutableProfile1;
    private MutableProfile mutableProfile2;
    private MutableProfile mutableProfile3;
    private List<MutableProfile> mutableProfiles;
    private CoreProfilesDialogModel instance;
    private Profile profile1;
    private Profile profile2;
    private Profile profile3;

    @Before
    public void setupMocks() {
        profile1 = getProfile(1);
        profile2 = getProfile(2);
        profile3 = getProfile(3);
        final List<Profile> profiles = Lists.newArrayList(profile3, profile1, profile2);
        Collections.sort(profiles, comparing(Profile::getName));
        mutableProfile1 = new MutableProfile(profile1);
        mutableProfile2 = new MutableProfile(profile2);
        mutableProfile3 = new MutableProfile(profile3);
        mutableProfiles = Lists.newArrayList(mutableProfile1, mutableProfile2, mutableProfile3);
        Collections.sort(mutableProfiles, comparing(MutableProfile::getName));
        when(profileManager.getProfiles()).thenReturn(profiles);
        instance = new CoreProfilesDialogModel(profileManager);
        instance.loadModel();
    }

    private Profile getProfile(final int name) {
        return new Profile("profile" + name, "realname" + name, Optional.of("ident" + name),
                Lists.newArrayList("nickname" + name + '1', "nickname" + name + '2',
                        "nickname" + name + '3'));
    }

    @Test
    public void testGetProfileList() {
        assertEquals(mutableProfiles, instance.getProfileList());
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
        when(profileManager.getProfiles()).thenReturn(Lists.newArrayList());
        instance.loadModel();
        assertFalse("testIsProfileListValidEmptyList", instance.isProfileListValid());
    }

    @Test
    public void testIsProfileListValid() {
        assertTrue("testIsProfileListValid", instance.isProfileListValid());
    }

    @Test
    public void testAddProfile() {
        when(profileManager.getProfiles()).thenReturn(Lists.newArrayList());
        instance.loadModel();
        assertFalse("testAddProfile", instance.getProfile("profile4").isPresent());
        instance.addProfile("profile4", "realname", "ident", Lists.newArrayList("nickname"));
        assertTrue("testAddProfile", instance.getProfile("profile4").isPresent());
    }

    @Test
    public void testEditProfile() {
        final MutableProfile preEdit = instance.getProfile("profile1").get();
        assertEquals("testEditProfile", "profile1", preEdit.getName());
        assertEquals("testEditProfile", "realname1", preEdit.getRealname());
        assertEquals("testEditProfile", "ident1", preEdit.getIdent().get());
        assertEquals("testEditProfile", Lists.newArrayList("nickname11", "nickname12",
                        "nickname13"), preEdit.getNicknames());
        instance.editProfile(preEdit, "profile1", "newRealname", "newIdent",
                Lists.newArrayList("nickname"));
        final MutableProfile postEdit = instance.getProfile("profile1").get();
        assertEquals("testEditProfile", "profile1", postEdit.getName());
        assertEquals("testEditProfile", "newRealname", postEdit.getRealname());
        assertEquals("testEditProfile", "newIdent", postEdit.getIdent().get());
        assertEquals("testEditProfile", Lists.newArrayList("nickname"),
                postEdit.getNicknames());
    }

    @Test
    public void testRenameProfile() {
        assertTrue("testRenameProfile", instance.getProfile("profile1").isPresent());
        assertFalse("testRenameProfile", instance.getProfile("profile4").isPresent());
        final MutableProfile preEdit = instance.getProfile("profile1").get();
        instance.editProfile(preEdit, "profile4", preEdit.getRealname(),
                preEdit.getIdent().get(), preEdit.getNicknames());
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
        assertEquals("testGetSelectedProfileDetails", "ident1",
                instance.getSelectedProfileIdent().get());
        assertEquals("testGetSelectedProfileDetails", "realname1",
                instance.getSelectedProfileRealname().get());
        assertEquals("testGetSelectedProfileDetails",
                Lists.newArrayList("nickname11", "nickname12", "nickname13"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testSetSelectedProfileSelectedNickname() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        assertEquals("testSetSelectedProfileSelectedNickname",
                Optional.<String>empty(), instance.getSelectedProfileSelectedNickname());
        instance.setSelectedProfileSelectedNickname(Optional.ofNullable("nickname12"));
        assertEquals("testSetSelectedProfileSelectedNickname",
                Optional.ofNullable("nickname12"),
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
                Lists.newArrayList("nickname11", "nickname12", "nickname13"),
                instance.getSelectedProfileNicknames().get());
        instance.addSelectedProfileNickname("nickname4");
        assertEquals("testAddSelectedProfileNickname",
                Lists.newArrayList("nickname11", "nickname12", "nickname13", "nickname4"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testRemoveSelectedProfileNickname() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        assertEquals("testRemoveSelectedProfileNickname",
                Lists.newArrayList("nickname11", "nickname12", "nickname13"),
                instance.getSelectedProfileNicknames().get());
        instance.removeSelectedProfileNickname("nickname13");
        assertEquals("testRemoveSelectedProfileNickname",
                Lists.newArrayList("nickname11", "nickname12"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testEditSelectedProfileSelectedNickname() {
        instance.setSelectedProfile(instance.getProfile("profile1"));
        instance.setSelectedProfileSelectedNickname(Optional.ofNullable("nickname12"));
        assertEquals("testSetSelectedProfileSelectedNickname",
                Optional.ofNullable("nickname12"),
                instance.getSelectedProfileSelectedNickname());
        instance.editSelectedProfileNickname("nickname12", "nickname4");
        assertEquals("testAddSelectedProfileNickname",
                Lists.newArrayList("nickname11", "nickname4", "nickname13"),
                instance.getSelectedProfileNicknames().get());
    }

    @Test
    public void testAddListener() {
        instance.addListener(listener);
        instance.addProfile("profile4", "realname", "ident", Lists.newArrayList("nickname"));
        verify(listener).profileAdded(any(MutableProfile.class));
    }

    @Test
    public void testRemoveListener() {
        instance.addListener(listener);
        instance.addProfile("profile4", "realname", "ident", Lists.newArrayList("nickname"));
        assertTrue(instance.getProfile("profile4").isPresent());
        final MutableProfile profile4 = instance.getProfile("profile4").get();
        verify(listener).profileAdded(profile4);
        instance.removeListener(listener);
        instance.setSelectedProfile(Optional.ofNullable(profile4));
        verify(listener, never()).profileRemoved(profile4);
    }

    @Test
    public void testSave() {
        final MutableProfile editedProfile = new MutableProfile("newName", "realName",
                Optional.of("newIdent"), Lists.newArrayList("newNickname"));
        instance.editProfile(mutableProfile1, editedProfile.getName(), editedProfile.getRealname(),
                editedProfile.getIdent().get(), editedProfile.getNicknames());
        final List<MutableProfile> profiles = Lists.newArrayList(editedProfile, mutableProfile2,
                mutableProfile3);
        instance.save();
        assertEquals(profiles, instance.getProfileList());
    }

}
