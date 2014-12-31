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

import com.google.common.collect.Lists;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProfileManagerLifeCycleManagerTest {

    @Mock private ProfileStore profileStore;
    @Mock private ProfileManager profileManager;
    @Mock private Profile profile1;
    @Mock private Profile profile2;
    private ProfileManagerLifeCycleManager instance;
    private List<Profile> profiles;

    @Before
    public void setup() {
        profiles = Lists.newArrayList(profile1, profile2);
        instance = new ProfileManagerLifeCycleManager(profileManager, profileStore);
        when(profileStore.readProfiles()).thenReturn(profiles);
        when(profileManager.getProfiles()).thenReturn(profiles);
    }

    @Test
    public void testStartUp() throws Exception {
        instance.startUp();
        verify(profileStore).readProfiles();
        verify(profileManager).addProfile(profile1);
        verify(profileManager).addProfile(profile2);
    }

    @Test
    public void testShutDown() throws Exception {
        instance.shutDown();
        verify(profileManager).getProfiles();
        verify(profileStore).writeProfiles(profiles);
    }
}