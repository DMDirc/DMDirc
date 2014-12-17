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

package com.dmdirc.ui.core.feedback;

import com.dmdirc.ServerState;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.FeedbackDialogModelListener;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.util.ClientInfo;

import com.google.common.collect.Lists;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreFeedbackDialogModelTest {

    @Mock private AggregateConfigProvider config;
    @Mock private ConnectionManager connectionManager;
    @Mock private FeedbackSenderFactory feedbackSenderFactory;
    @Mock private FeedbackSender feedbackSender;
    @Mock private FeedbackDialogModelListener listener;
    @Mock private Path path;
    @Mock private ClientInfo clientInfo;
    @Mock private Connection connection;
    @Mock private Parser parser;
    private static final String NAME = "Bob Dole";
    private static final String EMAIL = "bob@dole.com";
    private static final String FEEDBACK = "DMDirc Rocks.";
    private static final String VERSION = "0.1";
    private static final String PATH = "/path/to/config";
    private static final String JAVA = "javaInfo";
    private static final String OS = "osInfo";
    private static final String SERVER_NAME = "serverName";
    private static final String NETWORK_NAME = "networkName";
    private static final String SERVER_SOFTWARE = "serverSoftware";
    private static final String SERVER_SOFTWARE_TYPE = "serverSoftwareType";
    private static final String BOOLEAN_MODES = "booleanModes";
    private static final String LIST_MODES = "listChannelModes";
    private static final String PARAM_MODES = "paramModes";
    private static final String DOUBLE_PARAM_MODES = "doubleParamModes";
    private CoreFeedbackDialogModel instance;

    private String getServerInfoOutput() {
        return "Actual name: " + SERVER_NAME + '\n'
                + "Network: " + NETWORK_NAME + '\n'
                + "IRCd: " + SERVER_SOFTWARE + " - " + SERVER_SOFTWARE_TYPE + '\n'
                + "Modes: " + BOOLEAN_MODES + ' ' + LIST_MODES + ' ' + PARAM_MODES + ' '
                + DOUBLE_PARAM_MODES;
    }

    private String getDMDircInfoOutput() {
        return "DMDirc version: " + VERSION + '\n'
                + "Profile directory: " + PATH + '\n'
                + "Java version: " + JAVA + '\n'
                + "OS Version: " + OS;
    }

    @Before
    public void setup() {
        instance = new CoreFeedbackDialogModel(config,
                connectionManager, feedbackSenderFactory, path, clientInfo);
        when(connectionManager.getConnections()).thenReturn(Lists.newArrayList());
        when(feedbackSenderFactory.getFeedbackSender(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString())).thenReturn(feedbackSender);
        when(config.getOption("version", "version")).thenReturn(VERSION);
        when(clientInfo.getVersionInformation()).thenReturn(VERSION);
        when(path.toString()).thenReturn(PATH);
        when(clientInfo.getJavaInformation()).thenReturn(JAVA);
        when(clientInfo.getOperatingSystemInformation()).thenReturn(OS);
        when(connectionManager.getConnections()).thenReturn(Lists.newArrayList(connection));
        when(connection.getParser()).thenReturn(parser);
        when(parser.getServerName()).thenReturn(SERVER_NAME);
        when(connection.getState()).thenReturn(ServerState.CONNECTED);
        when(connection.getNetwork()).thenReturn(NETWORK_NAME);
        when(parser.getServerSoftware()).thenReturn(SERVER_SOFTWARE);
        when(parser.getServerSoftwareType()).thenReturn(SERVER_SOFTWARE_TYPE);
        when(parser.getBooleanChannelModes()).thenReturn(BOOLEAN_MODES);
        when(parser.getListChannelModes()).thenReturn(LIST_MODES);
        when(parser.getParameterChannelModes()).thenReturn(PARAM_MODES);
        when(parser.getDoubleParameterChannelModes()).thenReturn(DOUBLE_PARAM_MODES);
    }

    @Test
    public void testName() {
        assertEquals("testName", Optional.<String>empty(), instance.getName());
        instance.setName(Optional.ofNullable(NAME));
        assertEquals("testName", Optional.ofNullable(NAME), instance.getName());
    }

    @Test
    public void testEmail() {
        assertEquals("testEmail", Optional.<String>empty(), instance.getEmail());
        instance.setEmail(Optional.ofNullable(EMAIL));
        assertEquals("testEmail", Optional.ofNullable(EMAIL), instance.getEmail());
    }

    @Test
    public void testFeedback() {
        assertEquals("testFeedback", Optional.<String>empty(), instance.getFeedback());
        instance.setFeedback(Optional.ofNullable(FEEDBACK));
        assertEquals("testFeedback", Optional.ofNullable(FEEDBACK), instance.getFeedback());
    }

    @Test
    public void testServerInfo() {
        assertEquals("testServerInfo", false, instance.getIncludeServerInfo());
        instance.setIncludeServerInfo(true);
        assertEquals("testServerInfo", true, instance.getIncludeServerInfo());
    }

    @Test
    public void testDMDircInfo() {
        assertEquals("testDMDircInfo", false, instance.getIncludeDMDircInfo());
        instance.setIncludeDMDircInfo(true);
        assertEquals("testDMDircInfo", true, instance.getIncludeDMDircInfo());
    }

    @Test
    public void testGetDMDircInfo() {
        assertEquals(getDMDircInfoOutput(), instance.getDMDircInfo());
    }

    @Test
    public void testGetServerInfo_Single() {
        assertEquals(getServerInfoOutput() + '\n', instance.getServerInfo());
    }

    @Test
    public void testGetServerInfo_Multiple() {
        when(connectionManager.getConnections()).thenReturn(Lists.newArrayList(connection,
                connection));
        assertEquals(getServerInfoOutput() + '\n' + getServerInfoOutput() + '\n',
                instance.getServerInfo());
    }

    @Test
    public void testGetServerInfoConnection() {
        assertEquals(getServerInfoOutput(), instance.getServerInfo(connection));
    }

    @Test
    public void testGetServerInfoConnection_Disconnected() {
        when(connection.getState()).thenReturn(ServerState.DISCONNECTED);
        assertEquals("", instance.getServerInfo(connection));
    }

    @Test
    public void testSaveWithoutServerWithoutDMDirc() {
        instance.setIncludeDMDircInfo(false);
        instance.setIncludeServerInfo(false);
        instance.setName(Optional.ofNullable(NAME));
        instance.setEmail(Optional.ofNullable(EMAIL));
        instance.setFeedback(Optional.ofNullable(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION, "", "");
    }

    @Test
    public void testSaveWithoutServerWithDMDirc() {
        instance.setIncludeDMDircInfo(true);
        instance.setIncludeServerInfo(false);
        instance.setName(Optional.ofNullable(NAME));
        instance.setEmail(Optional.ofNullable(EMAIL));
        instance.setFeedback(Optional.ofNullable(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                "", getDMDircInfoOutput());
    }

    @Test
    public void testSaveWithoutDMDircWithServer() {
        instance.setIncludeDMDircInfo(false);
        instance.setIncludeServerInfo(true);
        instance.setName(Optional.ofNullable(NAME));
        instance.setEmail(Optional.ofNullable(EMAIL));
        instance.setFeedback(Optional.ofNullable(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                getServerInfoOutput() + '\n', "");
    }

    @Test
    public void testSaveWithServerWithDMDirc() {
        instance.setIncludeDMDircInfo(true);
        instance.setIncludeServerInfo(true);
        instance.setName(Optional.ofNullable(NAME));
        instance.setEmail(Optional.ofNullable(EMAIL));
        instance.setFeedback(Optional.ofNullable(FEEDBACK));
        instance.save();
        verify(feedbackSenderFactory).getFeedbackSender(NAME, EMAIL, FEEDBACK, VERSION,
                getServerInfoOutput() + '\n', getDMDircInfoOutput());
    }

    @Test
    public void testNameListener() {
        instance.addListener(listener);
        instance.setName(Optional.ofNullable(NAME));
        verify(listener).nameChanged(Optional.ofNullable(NAME));
    }

    @Test
    public void testEmailListener() {
        instance.addListener(listener);
        instance.setEmail(Optional.ofNullable(EMAIL));
        verify(listener).emailChanged(Optional.ofNullable(EMAIL));
    }

    @Test
    public void testFeedbackListener() {
        instance.addListener(listener);
        instance.setFeedback(Optional.ofNullable(FEEDBACK));
        verify(listener).feedbackChanged(Optional.ofNullable(FEEDBACK));
    }

    @Test
    public void testServerInfoListener() {
        instance.addListener(listener);
        instance.setIncludeServerInfo(true);
        verify(listener).includeServerInfoChanged(true);
    }

    @Test
    public void testDMDircInfoListener() {
        instance.addListener(listener);
        instance.setIncludeDMDircInfo(true);
        verify(listener).includeDMDircInfoChanged(true);
    }

}
