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

import com.dmdirc.events.ProfileAddedEvent;
import com.dmdirc.events.ProfileDeletedEvent;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.util.SystemInfo;

import com.google.common.collect.Lists;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProfileManagerTest {

    @Mock private EventBus eventBus;
    @Mock private Profile profile1;
    @Mock private Profile profile2;
    @Mock private SystemInfo systemInfo;

    private ProfileManager instance;

    @Before
    public void setUp() throws Exception {
        when(systemInfo.getProperty("user.name")).thenReturn("UserName");
        instance = new ProfileManager(eventBus, systemInfo);
    }

    @Test
    public void testAddProfile() {
        assertTrue(instance.getProfiles().isEmpty());
        instance.addProfile(profile1);
        assertEquals(1, instance.getProfiles().size());
        assertTrue(instance.getProfiles().contains(profile1));
    }

    @Test
    public void testAddProfile_Event() {
        instance.addProfile(profile1);
        verify(eventBus).publishAsync(any(ProfileAddedEvent.class));
    }

    @Test
    public void testDeleteProfile() {
        instance.addProfile(profile1);
        instance.addProfile(profile2);
        assertEquals(2, instance.getProfiles().size());
        assertTrue(instance.getProfiles().contains(profile1));
        assertTrue(instance.getProfiles().contains(profile2));
        instance.deleteProfile(profile2);
        assertEquals(1, instance.getProfiles().size());
        assertTrue(instance.getProfiles().contains(profile1));
        assertFalse(instance.getProfiles().contains(profile2));
    }

    @Test
    public void testDeleteProfile_Event() {
        instance.addProfile(profile1);
        //Fairly certain this shouldn't be needed, but the any matcher below seems to match the add
        reset(eventBus);
        instance.deleteProfile(profile1);
        verify(eventBus).publishAsync(any(ProfileDeletedEvent.class));
    }

    @Test
    public void testGetDefaultProfile_EmptyList() {
        final String nick = "UserName";
        final Profile profile = Profile.create(nick, nick, Optional.empty(), Lists.newArrayList(nick));
        assertEquals(profile, instance.getDefault());
    }

    @Test
    public void testGetDefaultProfile_1() {
        instance.addProfile(profile1);
        instance.addProfile(profile2);
        assertEquals(profile1, instance.getDefault());
    }

    @Test
    public void testGetDefaultProfile_2() {
        instance.addProfile(profile2);
        instance.addProfile(profile1);
        assertEquals(profile2, instance.getDefault());
    }
}