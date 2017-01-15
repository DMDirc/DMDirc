/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.ui.core.newserver;

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.events.ProfileAddedEvent;
import com.dmdirc.events.ProfileDeletedEvent;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.interfaces.ui.NewServerDialogModelListener;

import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreNewServerDialogModelTest {

    @Mock private AggregateConfigProvider globalConfig;
    @Mock private ConfigProvider userConfig;
    @Mock private ProfileManager profileManager;
    @Mock private ConnectionManager connectionManager;
    @Mock private EventBus eventBus;
    @Mock private Profile profile1;
    @Mock private Profile profile2;
    @Mock private NewServerDialogModelListener listener;

    private CoreNewServerDialogModel instance;

    @Before
    public void setupMocks() {
        when(profileManager.getProfiles()).thenReturn(
                Lists.newArrayList(profile1, profile2));
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("hostname");
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(1111);
        when(globalConfig.getOption("newserver", "password")).thenReturn("password");
        when(globalConfig.getOptionBool("newserver", "ssl")).thenReturn(true);

        instance = new CoreNewServerDialogModel(globalConfig, userConfig, profileManager,
                connectionManager, eventBus);
    }

    @Test
    public void testGetProfiles() {
        instance.loadModel();
        assertEquals("loadModel: ", Lists.newArrayList(profile1, profile2), instance.
                getProfileList());
    }

    @Test
    public void testSelectedProfile() {
        instance.loadModel();
        assertEquals("testSelectedProfile: ",
                Optional.<Profile>empty(), instance.getSelectedProfile());
        instance.setSelectedProfile(Optional.ofNullable(profile1));
        assertEquals("testSelectedProfile", Optional.ofNullable(profile1),
                instance.getSelectedProfile());
    }

    @Test
    public void testProfileValidatorEmpty() {
        when(profileManager.getProfiles()).thenReturn(Lists.<Profile>newArrayList());
        instance.loadModel();
        assertFalse("testProfileValidatorEmpty: ", instance.isProfileListValid());
    }

    @Test
    public void testProfileValidatorNotEmpty() {
        when(profileManager.getProfiles()).thenReturn(Lists.newArrayList(profile1, profile2));
        instance.loadModel();
        assertTrue("testProfileValidatorNotEmpty: ", instance.isProfileListValid());
    }

    @Test
    public void testHostname() {
        instance.loadModel();
        assertEquals("testHostname: ", Optional.ofNullable("hostname"), instance.getHostname());
        instance.setHostname(Optional.ofNullable("test"));
        assertEquals("testHostname: ", Optional.ofNullable("test"), instance.getHostname());
    }

    @Test
    public void testHostnameValidatorValid() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("hostname");
        instance.loadModel();
        assertTrue("testHostnameValidatorValid: ", instance.isHostnameValid());
    }

    @Test
    public void testHostnameValidatorInvalid() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("~!£$^£$%^&");
        instance.loadModel();
        assertFalse("testHostnameValidatorInvalid: ", instance.isHostnameValid());
    }

    @Test
    public void testPort() {
        instance.loadModel();
        assertEquals("testPort: ", Optional.ofNullable(1111), instance.getPort());
        instance.setPort(Optional.ofNullable(5678));
        assertEquals("testPort: ", Optional.ofNullable(5678), instance.getPort());
    }

    @Test
    public void testPortValidatorValid() {
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(1111);
        instance.loadModel();
        assertTrue("testPortValidatorValid: ", instance.isPortValid());
    }

    @Test
    public void testPortValidatorTooLow() {
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(-1);
        instance.loadModel();
        assertFalse("testPortValidatorTooLow: ", instance.isPortValid());
    }

    @Test
    public void testPortValidatorTooHigh() {
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(65536);
        instance.loadModel();
        assertFalse("testPortValidatorTooHigh: ", instance.isPortValid());
    }

    @Test
    public void testPassword() {
        instance.loadModel();
        assertEquals("testPassword: ", Optional.ofNullable("password"), instance.getPassword());
        instance.setPassword(Optional.ofNullable("test"));
        assertEquals("testPassword: ", Optional.ofNullable("test"), instance.getPassword());
    }

    @Test
    public void testPasswordValidatorValid() {
        when(globalConfig.getOption("newserver", "password")).thenReturn("password");
        instance.loadModel();
        assertTrue("testPasswordValidatorValid: ", instance.isPortValid());
    }

    @Test
    public void testSSL() {
        instance.loadModel();
        assertTrue("testSSL: ", instance.getSSL());
        instance.setSSL(false);
        assertFalse("testSSL: ", instance.getSSL());
    }

    @Test
    public void testSaveAsDefault() {
        instance.loadModel();
        assertFalse("testSaveAsDefault:", instance.getSaveAsDefault());
        instance.setSaveAsDefault(true);
        assertTrue("testSaveAsDefault:", instance.getSaveAsDefault());
    }

    @Test
    public void testIsSaveAllowedValid() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("hostname");
        instance.loadModel();
        assertTrue("testIsSaveAllowedValid: ", instance.isSaveAllowed());
    }

    @Test
    public void testIsSaveAllowedInvalidHostname() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("~!£$^£$%^&");
        instance.loadModel();
        assertFalse("testIsSaveAllowedInvalidHostname: ", instance.isSaveAllowed());
    }

    @Test
    public void testIsSaveAllowedInvalidPort() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("-1");
        instance.loadModel();
        assertFalse("testIsSaveAllowedInvalidPort: ", instance.isSaveAllowed());
    }

    @Test
    public void testSaveSaveDefaults() {
        instance.loadModel();
        instance.setSaveAsDefault(true);
        instance.save();
        verify(userConfig).setOption("newserver", "hostname", "hostname");
        verify(userConfig).setOption("newserver", "port", 1111);
        verify(userConfig).setOption("newserver", "password", "password");
        verify(userConfig).setOption("newserver", "ssl", true);
    }

    @Test
    public void testSaveNotDefaults() {
        instance.loadModel();
        instance.setSaveAsDefault(false);
        instance.save();
        verify(userConfig, never()).setOption("newserver", "hostname", "hostname");
        verify(userConfig, never()).setOption("newserver", "port", 1111);
        verify(userConfig, never()).setOption("newserver", "password", "password");
        verify(userConfig, never()).setOption("newserver", "ssl", true);
    }

    @Test
    public void testSaveConnectWithoutProfile() throws URISyntaxException {
        instance.loadModel();
        instance.save();
        verify(connectionManager).connectToAddress(new URI("ircs://password@hostname:1111"));
        verify(connectionManager, never()).connectToAddress(any(URI.class), any(Profile.class));
    }

    @Test
    public void testSaveConnectWithProfile() throws URISyntaxException {
        instance.loadModel();
        instance.setSelectedProfile(Optional.ofNullable(profile1));
        instance.save();
        verify(connectionManager, never()).connectToAddress(any(URI.class));
        verify(connectionManager).connectToAddress(new URI("ircs://password@hostname:1111"),
                profile1);
    }

    @Test
    public void testAddConfigProvider() {
        when(profileManager.getProfiles()).thenReturn(Lists.newArrayList(profile1));
        instance.loadModel();
        assertEquals("testAddConfigProvider:", Lists.newArrayList(profile1),
                instance.getProfileList());
        instance.profileAdded(new ProfileAddedEvent(profile2));
        assertEquals("testAddConfigProvider:", Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
    }

    @Test
    public void testRemoveConfigProvider() {
        when(profileManager.getProfiles()).thenReturn(Lists.newArrayList(profile1, profile2));
        instance.loadModel();
        assertEquals("testRemoveConfigProvider:", Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
        instance.profileDeleted(new ProfileDeletedEvent(profile2));
        assertEquals("testRemoveConfigProvider:", Lists.newArrayList(profile1),
                instance.getProfileList());
    }

    @Test
    public void testRemoveConfigProviderSelectedProfile() {
        when(profileManager.getProfiles()).thenReturn(Lists.newArrayList(profile1, profile2));
        instance.loadModel();
        instance.setSelectedProfile(Optional.ofNullable(profile2));
        assertEquals("testRemoveConfigProviderSelectedProfile:",
                Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
        assertEquals("testRemoveConfigProviderSelectedProfile:", Optional.ofNullable(profile2),
                instance.getSelectedProfile());
        instance.profileDeleted(new ProfileDeletedEvent(profile2));
        assertEquals("testRemoveConfigProviderSelectedProfile:", Lists.newArrayList(profile1),
                instance.getProfileList());
        assertEquals("testRemoveConfigProviderSelectedProfile:", Optional.<Profile>empty(),
                instance.getSelectedProfile());
    }

    @Test
    public void testListenerSelectedProfileChanged() {
        instance.loadModel();
        assertEquals("testListenerSelectedProfileChanged: ", Optional.<Profile>empty(),
                instance.getSelectedProfile());
        instance.addListener(listener);
        instance.setSelectedProfile(Optional.ofNullable(profile1));
        assertEquals("testListenerSelectedProfileChanged: ", Optional.ofNullable(profile1),
                instance.getSelectedProfile());
        verify(listener).selectedProfileChanged(Optional.<Profile>empty(),
                Optional.ofNullable(profile1));
    }

    @Test
    public void testListenerProfileListChangedRemoved() {
        instance.loadModel();
        instance.addListener(listener);
        assertEquals("testListenerProfileListChangedRemoved: ",
                Lists.newArrayList(profile1, profile2), instance.getProfileList());
        instance.profileDeleted(new ProfileDeletedEvent(profile2));
        assertEquals("testListenerProfileListChangedRemoved:", Lists.newArrayList(profile1),
                instance.getProfileList());
        verify(listener).profileListChanged(Lists.newArrayList(profile1));
    }

    @Test
    public void testListenerProfileListChangedAdded() {
        when(profileManager.getProfiles()).thenReturn(Lists.newArrayList(profile1));
        instance.loadModel();
        instance.addListener(listener);
        assertEquals("testListenerProfileListChangedAdded: ", Lists.newArrayList(profile1),
                instance.getProfileList());
        instance.profileAdded(new ProfileAddedEvent(profile2));
        assertEquals("testListenerProfileListChangedAdded:", Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
        verify(listener).profileListChanged(Lists.newArrayList(profile1, profile2));
    }

    @Test
    public void testListenerServerDetailsChangedPort() {
        instance.loadModel();
        instance.addListener(listener);
        instance.setPort(Optional.ofNullable(9999));
        verify(listener).serverDetailsChanged(Optional.ofNullable("hostname"),
                Optional.ofNullable(9999), Optional.ofNullable("password"), true, false);
    }

    @Test
    public void testListenerServerDetailsChangedPassword() {
        instance.loadModel();
        instance.addListener(listener);
        instance.setPassword(Optional.ofNullable("password-test"));
        verify(listener).serverDetailsChanged(Optional.ofNullable("hostname"),
                Optional.ofNullable(1111), Optional.ofNullable("password-test"), true, false);
    }

    @Test
    public void testListenerServerDetailsChangedSSL() {
        instance.loadModel();
        instance.addListener(listener);
        instance.setSSL(false);
        verify(listener).serverDetailsChanged(Optional.ofNullable("hostname"),
                Optional.ofNullable(1111), Optional.ofNullable("password"), false, false);
    }

    @Test
    public void testListenerServerDetailsChangedSaveAsDefault() {
        instance.loadModel();
        instance.addListener(listener);
        instance.setSaveAsDefault(true);
        verify(listener).serverDetailsChanged(Optional.ofNullable("hostname"),
                Optional.ofNullable(1111), Optional.ofNullable("password"), true, true);
    }

    @Test
    public void testRemoveListener() {
        instance.loadModel();
        instance.addListener(listener);
        instance.setSaveAsDefault(true);
        verify(listener).serverDetailsChanged(Optional.ofNullable("hostname"),
                Optional.ofNullable(1111), Optional.ofNullable("password"), true, true);
        instance.removeListener(listener);
        instance.setHostname(Optional.ofNullable("test"));
        verify(listener, never()).serverDetailsChanged(Optional.ofNullable("test"),
                Optional.ofNullable(1111), Optional.ofNullable("password"), true, false);
    }

    @Test
    public void testSaveDefaults() throws URISyntaxException {
        instance.loadModel();
        instance.setSelectedProfile(Optional.ofNullable(profile1));
        instance.setSaveAsDefault(true);
        instance.save();
        verify(userConfig).setOption("newserver", "hostname", "hostname");
        verify(userConfig).setOption("newserver", "port", 1111);
        verify(userConfig).setOption("newserver", "password", "password");
        verify(userConfig).setOption("newserver", "ssl", true);
    }

    @Test
    public void testNoSaveDefaults() throws URISyntaxException {
        instance.loadModel();
        instance.setSelectedProfile(Optional.ofNullable(profile1));
        instance.setSaveAsDefault(false);
        instance.save();
        verify(userConfig, never()).setOption("newserver", "hostname", "hostname");
        verify(userConfig, never()).setOption("newserver", "port", 1111);
        verify(userConfig, never()).setOption("newserver", "password", "password");
        verify(userConfig, never()).setOption("newserver", "ssl", true);
    }

}
