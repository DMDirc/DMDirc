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

package com.dmdirc.ui.core.feedback;

import com.dmdirc.ServerState;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.config.provider.AggregateConfigProvider;
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

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackHelperTest {

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

    @Mock private Path path;
    @Mock private ClientInfo clientInfo;
    @Mock private Connection connection;
    @Mock private Parser parser;
    @Mock private AggregateConfigProvider config;
    @Mock private ConnectionManager connectionManager;

    private FeedbackHelper instance;

    @Before
    public void setUp() throws Exception {
        when(config.getOption("version", "version")).thenReturn(VERSION);
        when(clientInfo.getVersionInformation()).thenReturn(VERSION);
        when(path.toString()).thenReturn(PATH);
        when(clientInfo.getJavaInformation()).thenReturn(JAVA);
        when(clientInfo.getOperatingSystemInformation()).thenReturn(OS);
        when(connectionManager.getConnections()).thenReturn(Lists.newArrayList(connection));
        when(connection.getParser()).thenReturn(Optional.of(parser));
        when(parser.getServerName()).thenReturn(SERVER_NAME);
        when(connection.getState()).thenReturn(ServerState.CONNECTED);
        when(connection.getNetwork()).thenReturn(NETWORK_NAME);
        when(parser.getServerSoftware()).thenReturn(SERVER_SOFTWARE);
        when(parser.getServerSoftwareType()).thenReturn(SERVER_SOFTWARE_TYPE);
        when(parser.getBooleanChannelModes()).thenReturn(BOOLEAN_MODES);
        when(parser.getListChannelModes()).thenReturn(LIST_MODES);
        when(parser.getParameterChannelModes()).thenReturn(PARAM_MODES);
        when(parser.getDoubleParameterChannelModes()).thenReturn(DOUBLE_PARAM_MODES);

        instance = new FeedbackHelper(path, clientInfo, connectionManager, config);
    }

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
        when(connectionManager.getConnections()).thenReturn(
                Lists.newArrayList(connection, connection));
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
    public void testGetVersion() {
        assertEquals(VERSION, instance.getVersion());
    }
}