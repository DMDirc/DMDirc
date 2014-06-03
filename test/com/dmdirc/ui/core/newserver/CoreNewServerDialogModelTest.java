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

package com.dmdirc.ui.core.newserver;

import com.dmdirc.ServerManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.ui.NewServerDialogModelListener;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreNewServerDialogModelTest {

    @Mock private AggregateConfigProvider globalConfig;
    @Mock private ConfigProvider userConfig;
    @Mock private IdentityController controller;
    @Mock private ServerManager serverManager;
    @Mock private ConfigProvider profile1;
    @Mock private ConfigProvider profile2;
    @Mock private NewServerDialogModelListener listener;

    @Before
    public void setupMocks() {
        when(controller.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(profile1, profile2));
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("hostname");
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(1111);
        when(globalConfig.getOption("newserver", "password")).thenReturn("password");
        when(globalConfig.getOptionBool("newserver", "ssl")).thenReturn(true);
    }

    @Test
    public void testGetProfiles() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("loadModel: ", Lists.newArrayList(profile1, profile2), instance.
                getProfileList());
    }

    @Test
    public void testSelectedProfile() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("testSelectedProfile: ", Optional.absent(), instance.getSelectedProfile());
        instance.setSelectedProfile(Optional.fromNullable(profile1));
        assertEquals("testSelectedProfile", Optional.fromNullable(profile1),
                instance.getSelectedProfile());
    }

    @Test
    public void testProfileValidatorEmpty() {
        when(controller.getProvidersByType("profile")).thenReturn(
                Lists.<ConfigProvider>newArrayList());
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertFalse("testProfileValidatorEmpty: ", instance.isProfileListValid());
    }

    @Test
    public void testProfileValidatorNotEmpty() {
        when(controller.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(profile1, profile2));
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertTrue("testProfileValidatorNotEmpty: ", instance.isProfileListValid());
    }

    @Test
    public void testHostname() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("testHostname: ", Optional.fromNullable("hostname"), instance.getHostname());
        instance.setHostname(Optional.fromNullable("test"));
        assertEquals("testHostname: ", Optional.fromNullable("test"), instance.getHostname());
    }

    @Test
    public void testHostnameValidatorValid() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("hostname");
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertTrue("testHostnameValidatorValid: ", instance.isHostnameValid());
    }

    @Test
    public void testHostnameValidatorInvalid() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("~!£$^£$%^&");
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertFalse("testHostnameValidatorInvalid: ", instance.isHostnameValid());
    }

    @Test
    public void testPort() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("testPort: ", Optional.fromNullable(1111), instance.getPort());
        instance.setPort(Optional.fromNullable(5678));
        assertEquals("testPort: ", Optional.fromNullable(5678), instance.getPort());
    }

    @Test
    public void testPortValidatorValid() {
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(1111);
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertTrue("testPortValidatorValid: ", instance.isPortValid());
    }

    @Test
    public void testPortValidatorTooLow() {
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(-1);
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertFalse("testPortValidatorTooLow: ", instance.isPortValid());
    }

    @Test
    public void testPortValidatorTooHigh() {
        when(globalConfig.getOptionInt("newserver", "port")).thenReturn(65536);
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertFalse("testPortValidatorTooHigh: ", instance.isPortValid());
    }

    @Test
    public void testPassword() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("testPassword: ", Optional.fromNullable("password"), instance.getPassword());
        instance.setPassword(Optional.fromNullable("test"));
        assertEquals("testPassword: ", Optional.fromNullable("test"), instance.getPassword());
    }

    @Test
    public void testPasswordValidatorValid() {
        when(globalConfig.getOption("newserver", "password")).thenReturn("password");
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertTrue("testPasswordValidatorValid: ", instance.isPortValid());
    }

    @Test
    public void testSSL() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertTrue("testSSL: ", instance.getSSL());
        instance.setSSL(false);
        assertFalse("testSSL: ", instance.getSSL());
    }

    @Test
    public void testSaveAsDefault() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertFalse("testSaveAsDefault:", instance.getSaveAsDefault());
        instance.setSaveAsDefault(true);
        assertTrue("testSaveAsDefault:", instance.getSaveAsDefault());
    }

    @Test
    public void testIsSaveAllowedValid() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("hostname");
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertTrue("testIsSaveAllowedValid: ", instance.isSaveAllowed());
    }

    @Test
    public void testIsSaveAllowedInvalidHostname() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("~!£$^£$%^&");
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertFalse("testIsSaveAllowedInvalidHostname: ", instance.isSaveAllowed());
    }

    @Test
    public void testIsSaveAllowedInvalidPort() {
        when(globalConfig.getOption("newserver", "hostname")).thenReturn("-1");
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertFalse("testIsSaveAllowedInvalidPort: ", instance.isSaveAllowed());
    }

    @Test
    public void testSaveSaveDefaults() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
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
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
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
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.save();
        verify(serverManager).connectToAddress(new URI("ircs://password@hostname:1111"));
        verify(serverManager, never()).connectToAddress(any(URI.class), any(ConfigProvider.class));
    }

    @Test
    public void testSaveConnectWithProfile() throws URISyntaxException {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.setSelectedProfile(Optional.fromNullable(profile1));
        instance.save();
        verify(serverManager, never()).connectToAddress(any(URI.class));
        verify(serverManager).connectToAddress(new URI("ircs://password@hostname:1111"), profile1);
    }

    @Test
    public void testAddConfigProvider() {
        when(controller.getProvidersByType("profile")).thenReturn(Lists.newArrayList(profile1));
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("testAddConfigProvider:", Lists.newArrayList(profile1),
                instance.getProfileList());
        instance.configProviderAdded(profile2);
        assertEquals("testAddConfigProvider:", Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
    }

    @Test
    public void testRemoveConfigProvider() {
        when(controller.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(profile1, profile2));
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("testRemoveConfigProvider:", Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
        instance.configProviderRemoved(profile2);
        assertEquals("testRemoveConfigProvider:", Lists.newArrayList(profile1),
                instance.getProfileList());
    }

    @Test
    public void testRemoveConfigProviderSelectedProfile() {
        when(controller.getProvidersByType("profile")).thenReturn(
                Lists.newArrayList(profile1, profile2));
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.setSelectedProfile(Optional.fromNullable(profile2));
        assertEquals("testRemoveConfigProviderSelectedProfile:",
                Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
        assertEquals("testRemoveConfigProviderSelectedProfile:", Optional.fromNullable(profile2),
                instance.getSelectedProfile());
        instance.configProviderRemoved(profile2);
        assertEquals("testRemoveConfigProviderSelectedProfile:", Lists.newArrayList(profile1),
                instance.getProfileList());
        assertEquals("testRemoveConfigProviderSelectedProfile:", Optional.absent(),
                instance.getSelectedProfile());
    }

    @Test
    public void testListenerSelectedProfileChanged() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        assertEquals("testListenerSelectedProfileChanged: ", Optional.<ConfigProvider>absent(),
                instance.getSelectedProfile());
        instance.addListener(listener);
        instance.setSelectedProfile(Optional.fromNullable(profile1));
        assertEquals("testListenerSelectedProfileChanged: ", Optional.fromNullable(profile1),
                instance.getSelectedProfile());
        verify(listener).selectedProfileChanged(Optional.<ConfigProvider>absent(),
                Optional.fromNullable(profile1));
    }

    @Test
    public void testListenerProfileListChangedRemoved() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.addListener(listener);
        assertEquals("testListenerProfileListChangedRemoved: ",
                Lists.newArrayList(profile1, profile2), instance.getProfileList());
        instance.configProviderRemoved(profile2);
        assertEquals("testListenerProfileListChangedRemoved:", Lists.newArrayList(profile1),
                instance.getProfileList());
        verify(listener).profileListChanged(Lists.newArrayList(profile1));
    }

    @Test
    public void testListenerProfileListChangedAdded() {
        when(controller.getProvidersByType("profile"))
                .thenReturn(Lists.newArrayList(profile1));
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.addListener(listener);
        assertEquals("testListenerProfileListChangedAdded: ", Lists.newArrayList(profile1),
                instance.getProfileList());
        instance.configProviderAdded(profile2);
        assertEquals("testListenerProfileListChangedAdded:", Lists.newArrayList(profile1, profile2),
                instance.getProfileList());
        verify(listener).profileListChanged(Lists.newArrayList(profile1, profile2));
    }

    @Test
    public void testListenerServerDetailsChangedPort() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.addListener(listener);
        instance.setPort(Optional.fromNullable(9999));
        verify(listener).serverDetailsChanged(Optional.fromNullable("hostname"),
                Optional.fromNullable(9999), Optional.fromNullable("password"), true, false);
    }

    @Test
    public void testListenerServerDetailsChangedPassword() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.addListener(listener);
        instance.setPassword(Optional.fromNullable("password-test"));
        verify(listener).serverDetailsChanged(Optional.fromNullable("hostname"),
                Optional.fromNullable(1111), Optional.fromNullable("password-test"), true, false);
    }

    @Test
    public void testListenerServerDetailsChangedSSL() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.addListener(listener);
        instance.setSSL(false);
        verify(listener).serverDetailsChanged(Optional.fromNullable("hostname"),
                Optional.fromNullable(1111), Optional.fromNullable("password"), false, false);
    }

    @Test
    public void testListenerServerDetailsChangedSaveAsDefault() {
        CoreNewServerDialogModel instance = new CoreNewServerDialogModel(globalConfig, userConfig,
                controller, serverManager);
        instance.loadModel();
        instance.addListener(listener);
        instance.setSaveAsDefault(true);
        verify(listener).serverDetailsChanged(Optional.fromNullable("hostname"),
                Optional.fromNullable(1111), Optional.fromNullable("password"), true, true);
    }

}
